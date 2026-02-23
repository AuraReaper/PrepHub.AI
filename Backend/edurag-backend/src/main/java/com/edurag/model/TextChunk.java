package com.edurag.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chunks")
public class TextChunk {

    @Id
    private String id;

    private String documentId;   // Reference to parent EduDocument

    @Indexed
    private String subject;

    @Indexed
    private String chapter;

    private String content;      // The actual text chunk
    private int chunkIndex;      // Position in document

    private List<Double> embedding; // Vector embedding from Gemini

    private int repeatCount;     // How many times this chunk was retrieved
    private LocalDateTime createdAt;
}
