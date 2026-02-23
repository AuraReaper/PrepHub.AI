package com.edurag.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "documents")
public class EduDocument {

    @Id
    private String id;

    private String fileName;
    private String subject;
    private String chapter;
    private String uploadedBy; // admin
    private LocalDateTime uploadedAt;
    private String filePath;
    private long fileSize;
    private String fileType; // PDF, DOCX, PPTX

    // Extracted text chunks
    private List<TextChunk> chunks;
    private int totalChunks;
    private String status; // PROCESSING, READY, FAILED
}
