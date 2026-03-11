package com.edurag.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

// ===== Request DTOs =====

public class ApiDtos {

    @Data
    public static class AskRequest {
        private String question;
        private String subject;
        private String chapter;
    }

    @Data
    public static class GenerateRequest {
        private String subject;
        private String chapter;
        private int numberOfQuestions = 5;
        private String difficulty = "MIXED";
        private String type = "MIXED";
    }

    // ===== Response DTOs =====

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UploadResponse {
        private String documentId;
        private String fileName;
        private String status;
        private int chunksCreated;
        private String message;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AskResponse {
        private String question;
        private String answer;
        private List<String> relevantChunks;
        private String subject;
        private String chapter;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerateResponse {
        private String subject;
        private String chapter;
        private int totalGenerated;
        private List<QuestionDTO> questions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionDTO {
        private String id;
        private String questionText;
        private String answer;
        private String difficulty;
        private String type;
        private List<String> options;
        private String correctOption;
        private int repeatCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiError {
        private String error;
        private String message;
        private int status;
    }
}
