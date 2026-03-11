package com.edurag.dto;

import java.util.List;

public class ApiDtos {

    // ===== Request DTOs =====

    public static class AskRequest {
        private String question;
        private String subject;
        private String chapter;

        public String getQuestion() { return question; }
        public String getSubject() { return subject; }
        public String getChapter() { return chapter; }
        public void setQuestion(String question) { this.question = question; }
        public void setSubject(String subject) { this.subject = subject; }
        public void setChapter(String chapter) { this.chapter = chapter; }
    }

    public static class GenerateRequest {
        private String subject;
        private String chapter;
        private int numberOfQuestions = 5;
        private String difficulty = "MIXED";
        private String type = "MIXED";

        public String getSubject() { return subject; }
        public String getChapter() { return chapter; }
        public int getNumberOfQuestions() { return numberOfQuestions; }
        public String getDifficulty() { return difficulty; }
        public String getType() { return type; }
        public void setSubject(String subject) { this.subject = subject; }
        public void setChapter(String chapter) { this.chapter = chapter; }
        public void setNumberOfQuestions(int numberOfQuestions) { this.numberOfQuestions = numberOfQuestions; }
        public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
        public void setType(String type) { this.type = type; }
    }

    // ===== Response DTOs =====

    public static class UploadResponse {
        private String documentId;
        private String fileName;
        private String status;
        private int chunksCreated;
        private String message;

        public String getDocumentId() { return documentId; }
        public String getFileName() { return fileName; }
        public String getStatus() { return status; }
        public int getChunksCreated() { return chunksCreated; }
        public String getMessage() { return message; }
        public void setDocumentId(String documentId) { this.documentId = documentId; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        public void setStatus(String status) { this.status = status; }
        public void setChunksCreated(int chunksCreated) { this.chunksCreated = chunksCreated; }
        public void setMessage(String message) { this.message = message; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private final UploadResponse r = new UploadResponse();
            public Builder documentId(String v) { r.documentId = v; return this; }
            public Builder fileName(String v) { r.fileName = v; return this; }
            public Builder status(String v) { r.status = v; return this; }
            public Builder chunksCreated(int v) { r.chunksCreated = v; return this; }
            public Builder message(String v) { r.message = v; return this; }
            public UploadResponse build() { return r; }
        }
    }

    public static class AskResponse {
        private String question;
        private String answer;
        private List<String> relevantChunks;
        private String subject;
        private String chapter;

        public String getQuestion() { return question; }
        public String getAnswer() { return answer; }
        public List<String> getRelevantChunks() { return relevantChunks; }
        public String getSubject() { return subject; }
        public String getChapter() { return chapter; }
        public void setQuestion(String question) { this.question = question; }
        public void setAnswer(String answer) { this.answer = answer; }
        public void setRelevantChunks(List<String> relevantChunks) { this.relevantChunks = relevantChunks; }
        public void setSubject(String subject) { this.subject = subject; }
        public void setChapter(String chapter) { this.chapter = chapter; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private final AskResponse r = new AskResponse();
            public Builder question(String v) { r.question = v; return this; }
            public Builder answer(String v) { r.answer = v; return this; }
            public Builder relevantChunks(List<String> v) { r.relevantChunks = v; return this; }
            public Builder subject(String v) { r.subject = v; return this; }
            public Builder chapter(String v) { r.chapter = v; return this; }
            public AskResponse build() { return r; }
        }
    }

    public static class GenerateResponse {
        private String subject;
        private String chapter;
        private int totalGenerated;
        private List<QuestionDTO> questions;

        public String getSubject() { return subject; }
        public String getChapter() { return chapter; }
        public int getTotalGenerated() { return totalGenerated; }
        public List<QuestionDTO> getQuestions() { return questions; }
        public void setSubject(String subject) { this.subject = subject; }
        public void setChapter(String chapter) { this.chapter = chapter; }
        public void setTotalGenerated(int totalGenerated) { this.totalGenerated = totalGenerated; }
        public void setQuestions(List<QuestionDTO> questions) { this.questions = questions; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private final GenerateResponse r = new GenerateResponse();
            public Builder subject(String v) { r.subject = v; return this; }
            public Builder chapter(String v) { r.chapter = v; return this; }
            public Builder totalGenerated(int v) { r.totalGenerated = v; return this; }
            public Builder questions(List<QuestionDTO> v) { r.questions = v; return this; }
            public GenerateResponse build() { return r; }
        }
    }

    public static class QuestionDTO {
        private String id;
        private String questionText;
        private String answer;
        private String difficulty;
        private String type;
        private List<String> options;
        private String correctOption;
        private int repeatCount;

        public String getId() { return id; }
        public String getQuestionText() { return questionText; }
        public String getAnswer() { return answer; }
        public String getDifficulty() { return difficulty; }
        public String getType() { return type; }
        public List<String> getOptions() { return options; }
        public String getCorrectOption() { return correctOption; }
        public int getRepeatCount() { return repeatCount; }
        public void setId(String id) { this.id = id; }
        public void setQuestionText(String questionText) { this.questionText = questionText; }
        public void setAnswer(String answer) { this.answer = answer; }
        public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
        public void setType(String type) { this.type = type; }
        public void setOptions(List<String> options) { this.options = options; }
        public void setCorrectOption(String correctOption) { this.correctOption = correctOption; }
        public void setRepeatCount(int repeatCount) { this.repeatCount = repeatCount; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private final QuestionDTO r = new QuestionDTO();
            public Builder id(String v) { r.id = v; return this; }
            public Builder questionText(String v) { r.questionText = v; return this; }
            public Builder answer(String v) { r.answer = v; return this; }
            public Builder difficulty(String v) { r.difficulty = v; return this; }
            public Builder type(String v) { r.type = v; return this; }
            public Builder options(List<String> v) { r.options = v; return this; }
            public Builder correctOption(String v) { r.correctOption = v; return this; }
            public Builder repeatCount(int v) { r.repeatCount = v; return this; }
            public QuestionDTO build() { return r; }
        }
    }

    public static class ApiError {
        private String error;
        private String message;
        private int status;

        public String getError() { return error; }
        public String getMessage() { return message; }
        public int getStatus() { return status; }
        public void setError(String error) { this.error = error; }
        public void setMessage(String message) { this.message = message; }
        public void setStatus(int status) { this.status = status; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private final ApiError r = new ApiError();
            public Builder error(String v) { r.error = v; return this; }
            public Builder message(String v) { r.message = v; return this; }
            public Builder status(int v) { r.status = v; return this; }
            public ApiError build() { return r; }
        }
    }
}
