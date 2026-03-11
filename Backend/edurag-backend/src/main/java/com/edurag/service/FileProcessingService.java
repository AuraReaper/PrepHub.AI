package com.edurag.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Handles file uploads, text extraction from PDFs / DOCX / PPTX,
 * and chunking extracted text into overlapping segments.
 */
@Service
public class FileProcessingService {

    private static final Logger log = LoggerFactory.getLogger(FileProcessingService.class);

    @Value("${chunking.size:500}")
    private int chunkSize;

    @Value("${chunking.overlap:100}")
    private int chunkOverlap;

    @Value("${upload.dir:uploads/}")
    private String uploadDir;

    /**
     * Save uploaded file to disk and return its saved path.
     */
    public String saveFile(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String uniqueName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(uniqueName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        log.info("Saved file: {}", filePath);
        return filePath.toString();
    }

    /**
     * Detect file type from content type or extension.
     */
    public String detectFileType(MultipartFile file) {
        String contentType = file.getContentType();
        String originalName = file.getOriginalFilename() != null
                ? file.getOriginalFilename().toLowerCase() : "";

        if ("application/pdf".equals(contentType) || originalName.endsWith(".pdf")) {
            return "PDF";
        } else if (originalName.endsWith(".docx") ||
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(contentType)) {
            return "DOCX";
        } else if (originalName.endsWith(".pptx") ||
                "application/vnd.openxmlformats-officedocument.presentationml.presentation".equals(contentType)) {
            return "PPTX";
        } else if (originalName.endsWith(".txt") || "text/plain".equals(contentType)) {
            return "TXT";
        }
        return "UNKNOWN";
    }

    /**
     * Extract raw text from the file based on its type.
     */
    public String extractText(MultipartFile file, String fileType) throws IOException {
        return switch (fileType) {
            case "PDF"  -> extractFromPdf(file);
            case "DOCX" -> extractFromDocx(file);
            case "PPTX" -> extractFromPptx(file);
            case "TXT"  -> new String(file.getBytes());
            default     -> throw new IllegalArgumentException("Unsupported file type: " + fileType);
        };
    }

    /**
     * Extract text from a PDF using Apache PDFBox.
     */
    private String extractFromPdf(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(document);
            log.info("Extracted {} characters from PDF ({} pages)", text.length(), document.getNumberOfPages());
            return text;
        }
    }

    /**
     * Extract text from a DOCX using Apache POI.
     */
    private String extractFromDocx(MultipartFile file) throws IOException {
        try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {
            StringBuilder sb = new StringBuilder();
            document.getParagraphs().forEach(p -> sb.append(p.getText()).append("\n"));
            document.getTables().forEach(table ->
                table.getRows().forEach(row ->
                    row.getTableCells().forEach(cell ->
                        sb.append(cell.getText()).append(" ")
                    )
                )
            );
            String text = sb.toString();
            log.info("Extracted {} characters from DOCX", text.length());
            return text;
        }
    }

    /**
     * Extract text from a PPTX using Apache POI.
     */
    private String extractFromPptx(MultipartFile file) throws IOException {
        try (XMLSlideShow pptx = new XMLSlideShow(file.getInputStream())) {
            StringBuilder sb = new StringBuilder();
            for (XSLFSlide slide : pptx.getSlides()) {
                sb.append("--- Slide: ").append(slide.getSlideName()).append(" ---\n");
                for (XSLFShape shape : slide.getShapes()) {
                    if (shape instanceof XSLFTextShape textShape) {
                        sb.append(textShape.getText()).append("\n");
                    }
                }
            }
            String text = sb.toString();
            log.info("Extracted {} characters from PPTX", text.length());
            return text;
        }
    }

    /**
     * Split extracted text into overlapping chunks.
     * Each chunk is ~chunkSize words with chunkOverlap words of overlap.
     */
    public List<String> chunkText(String text) {
        // Split into words first
        String[] words = text.trim().replaceAll("\\s+", " ").split(" ");
        List<String> chunks = new ArrayList<>();

        int step = chunkSize - chunkOverlap;
        if (step <= 0) step = chunkSize;

        for (int i = 0; i < words.length; i += step) {
            int end = Math.min(i + chunkSize, words.length);
            String chunk = String.join(" ", Arrays.copyOfRange(words, i, end)).trim();
            if (!chunk.isBlank()) {
                chunks.add(chunk);
            }
            if (end == words.length) break;
        }

        log.info("Created {} chunks from {} words (chunkSize={}, overlap={})",
                chunks.size(), words.length, chunkSize, chunkOverlap);
        return chunks;
    }
}
