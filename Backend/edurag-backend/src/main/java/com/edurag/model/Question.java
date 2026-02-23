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
@Document(collection = "questions")
public class Question {

    @Id
    private String id;

    private String documentId;
    private String chunkId;

    @Indexed
    private String subject;

    @Indexed
    private String chapter;

    private String questionText;
    private String answer;
    private String difficulty;   // EASY, MEDIUM, HARD
    private String type;         // MCQ, SHORT_ANSWER, DESCRIPTIVE

    private List<String> options; // For MCQ
    private String correctOption; // For MCQ

    private List<Double> questionEmbedding;

    private int repeatCount;
    private LocalDateTime generatedAt;
}
