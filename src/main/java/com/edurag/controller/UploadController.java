package com.edurag.controller;

import com.edurag.dto.ApiDtos;
import com.edurag.model.EduDocument;
import com.edurag.repository.DocumentRepository;
import com.edurag.service.UploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for file upload operations.
 * POST /upload  - Upload a study material (PDF/DOCX/PPTX)
 * GET  /upload/status/{id}  - Check processing status
 * GET  /upload/documents - List all documents
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class UploadController {

    private static final Logger log = LoggerFactory.getLogger(UploadController.class);

    @Autowired private UploadService uploadService;
    @Autowired private DocumentRepository documentRepository;

    /**
     * Upload a study material file.
     * Expects: multipart/form-data with file, subject, chapter, uploadedBy
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file")       MultipartFile file,
            @RequestParam("subject")    String subject,
            @RequestParam("chapter")    String chapter,
            @RequestParam(value = "uploadedBy", defaultValue = "admin") String uploadedBy) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "File is empty"));
        }

        try {
            ApiDtos.UploadResponse response = uploadService.uploadDocument(file, subject, chapter, uploadedBy);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Upload failed", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }

    /**
     * Check document processing status.
     */
    @GetMapping("/upload/status/{documentId}")
    public ResponseEntity<?> getStatus(@PathVariable String documentId) {
        return documentRepository.findById(documentId)
                .map(doc -> ResponseEntity.ok(Map.of(
                        "documentId", doc.getId(),
                        "fileName", doc.getFileName(),
                        "status", doc.getStatus(),
                        "totalChunks", doc.getTotalChunks(),
                        "subject", doc.getSubject(),
                        "chapter", doc.getChapter()
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * List all uploaded documents.
     */
    @GetMapping("/upload/documents")
    public ResponseEntity<List<EduDocument>> listDocuments(
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String chapter) {

        List<EduDocument> docs;
        if (subject != null && chapter != null) {
            docs = documentRepository.findBySubjectAndChapter(subject, chapter);
        } else if (subject != null) {
            docs = documentRepository.findBySubject(subject);
        } else {
            docs = documentRepository.findAll();
        }
        return ResponseEntity.ok(docs);
    }

    /**
     * Delete a document and all its chunks/questions.
     */
    @DeleteMapping("/upload/documents/{documentId}")
    public ResponseEntity<?> deleteDocument(@PathVariable String documentId) {
        if (!documentRepository.existsById(documentId)) {
            return ResponseEntity.notFound().build();
        }
        documentRepository.deleteById(documentId);
        return ResponseEntity.ok(Map.of("message", "Document deleted", "documentId", documentId));
    }
}
