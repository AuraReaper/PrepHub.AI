package com.edurag.service;

import com.edurag.dto.ApiDtos;
import com.edurag.model.EduDocument;
import com.edurag.model.TextChunk;
import com.edurag.repository.DocumentRepository;
import com.edurag.repository.ChunkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Orchestrates the full upload pipeline:
 * 1. Save file
 * 2. Extract text
 * 3. Chunk text
 * 4. Generate embeddings
 * 5. Detect duplicates (repeatCount)
 * 6. Save to MongoDB
 */
@Service
public class UploadService {

    private static final Logger log = LoggerFactory.getLogger(UploadService.class);
    private static final double DUPLICATE_THRESHOLD = 0.92;

    @Autowired private FileProcessingService fileProcessingService;
    @Autowired private EmbeddingService embeddingService;
    @Autowired private DocumentRepository documentRepository;
    @Autowired private ChunkRepository chunkRepository;

    /**
     * Handle file upload synchronously (returns after processing starts).
     */
    public ApiDtos.UploadResponse uploadDocument(MultipartFile file,
                                                  String subject,
                                                  String chapter,
                                                  String uploadedBy) throws Exception {
        log.info("Upload started: {} by {}", file.getOriginalFilename(), uploadedBy);

        String fileType = fileProcessingService.detectFileType(file);
        if ("UNKNOWN".equals(fileType)) {
            throw new IllegalArgumentException("Unsupported file type. Please upload PDF, DOCX, PPTX, or TXT.");
        }

        // Save file to disk
        String filePath = fileProcessingService.saveFile(file);

        // Create document metadata record (PROCESSING state)
        EduDocument doc = EduDocument.builder()
                .fileName(file.getOriginalFilename())
                .subject(subject)
                .chapter(chapter)
                .uploadedBy(uploadedBy)
                .uploadedAt(LocalDateTime.now())
                .filePath(filePath)
                .fileSize(file.getSize())
                .fileType(fileType)
                .status("PROCESSING")
                .totalChunks(0)
                .build();

        doc = documentRepository.save(doc);
        final String docId = doc.getId();

        // Process async so we can return a response quickly
        processDocumentAsync(file, doc);

        return ApiDtos.UploadResponse.builder()
                .documentId(docId)
                .fileName(file.getOriginalFilename())
                .status("PROCESSING")
                .message("File received. Processing and embedding in background.")
                .build();
    }

    @Async
    public void processDocumentAsync(MultipartFile file, EduDocument doc) {
        try {
            // Extract text
            String rawText = fileProcessingService.extractText(file, doc.getFileType());
            if (rawText == null || rawText.isBlank()) {
                markFailed(doc, "No text extracted from file.");
                return;
            }

            // Chunk text
            List<String> textChunks = fileProcessingService.chunkText(rawText);
            List<TextChunk> chunksToSave = new ArrayList<>();

            for (int i = 0; i < textChunks.size(); i++) {
                TextChunk chunk = TextChunk.builder()
                        .documentId(doc.getId())
                        .subject(doc.getSubject())
                        .chapter(doc.getChapter())
                        .content(textChunks.get(i))
                        .chunkIndex(i)
                        .repeatCount(0)
                        .createdAt(LocalDateTime.now())
                        .build();

                // Generate embedding for this chunk
                try {
                    List<Double> embedding = geminiApiService().generateEmbedding(chunk.getContent());
                    chunk.setEmbedding(embedding);

                    // Check for near-duplicate chunks
                    if (!embeddingService.checkAndIncrementIfDuplicate(chunk, DUPLICATE_THRESHOLD)) {
                        chunksToSave.add(chunk);
                    }
                } catch (Exception e) {
                    log.warn("Embedding failed for chunk {}, saving without embedding: {}", i, e.getMessage());
                    chunksToSave.add(chunk);
                }
            }

            // Bulk save all unique chunks
            chunkRepository.saveAll(chunksToSave);

            // Update document status
            doc.setStatus("READY");
            doc.setTotalChunks(chunksToSave.size());
            documentRepository.save(doc);

            log.info("Document {} processed: {} chunks saved", doc.getId(), chunksToSave.size());

        } catch (Exception e) {
            log.error("Document processing failed for {}: {}", doc.getId(), e.getMessage());
            markFailed(doc, e.getMessage());
        }
    }

    private void markFailed(EduDocument doc, String reason) {
        doc.setStatus("FAILED");
        documentRepository.save(doc);
        log.error("Document {} marked FAILED: {}", doc.getId(), reason);
    }

    // Can't @Autowired inside @Async method body cleanly, use a helper
    @Autowired
    private GeminiApiService geminiApiServiceBean;

    private GeminiApiService geminiApiService() {
        return geminiApiServiceBean;
    }
}
