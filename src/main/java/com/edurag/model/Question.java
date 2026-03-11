package com.edurag.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

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
    private String difficulty;
    private String type;
    private List<String> options;
    private String correctOption;
    private List<Double> questionEmbedding;
    private int repeatCount;
    private LocalDateTime generatedAt;

    public Question() {}

    public String getId() { return id; }
    public String getDocumentId() { return documentId; }
    public String getChunkId() { return chunkId; }
    public String getSubject() { return subject; }
    public String getChapter() { return chapter; }
    public String getQuestionText() { return questionText; }
    public String getAnswer() { return answer; }
    public String getDifficulty() { return difficulty; }
    public String getType() { return type; }
    public List<String> getOptions() { return options; }
    public String getCorrectOption() { return correctOption; }
    public List<Double> getQuestionEmbedding() { return questionEmbedding; }
    public int getRepeatCount() { return repeatCount; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }

    public void setId(String id) { this.id = id; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public void setChunkId(String chunkId) { this.chunkId = chunkId; }
    public void setSubject(String subject) { this.subject = subject; }
    public void setChapter(String chapter) { this.chapter = chapter; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public void setAnswer(String answer) { this.answer = answer; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public void setType(String type) { this.type = type; }
    public void setOptions(List<String> options) { this.options = options; }
    public void setCorrectOption(String correctOption) { this.correctOption = correctOption; }
    public void setQuestionEmbedding(List<Double> questionEmbedding) { this.questionEmbedding = questionEmbedding; }
    public void setRepeatCount(int repeatCount) { this.repeatCount = repeatCount; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final Question q = new Question();
        public Builder id(String id) { q.id = id; return this; }
        public Builder documentId(String documentId) { q.documentId = documentId; return this; }
        public Builder chunkId(String chunkId) { q.chunkId = chunkId; return this; }
        public Builder subject(String subject) { q.subject = subject; return this; }
        public Builder chapter(String chapter) { q.chapter = chapter; return this; }
        public Builder questionText(String questionText) { q.questionText = questionText; return this; }
        public Builder answer(String answer) { q.answer = answer; return this; }
        public Builder difficulty(String difficulty) { q.difficulty = difficulty; return this; }
        public Builder type(String type) { q.type = type; return this; }
        public Builder options(List<String> options) { q.options = options; return this; }
        public Builder correctOption(String correctOption) { q.correctOption = correctOption; return this; }
        public Builder questionEmbedding(List<Double> questionEmbedding) { q.questionEmbedding = questionEmbedding; return this; }
        public Builder repeatCount(int repeatCount) { q.repeatCount = repeatCount; return this; }
        public Builder generatedAt(LocalDateTime generatedAt) { q.generatedAt = generatedAt; return this; }
        public Question build() { return q; }
    }
}
