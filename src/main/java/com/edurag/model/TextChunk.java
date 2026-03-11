package com.edurag.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "chunks")
public class TextChunk {

    @Id
    private String id;
    private String documentId;

    @Indexed
    private String subject;

    @Indexed
    private String chapter;

    private String content;
    private int chunkIndex;
    private List<Double> embedding;
    private int repeatCount;
    private LocalDateTime createdAt;

    public TextChunk() {}

    public String getId() { return id; }
    public String getDocumentId() { return documentId; }
    public String getSubject() { return subject; }
    public String getChapter() { return chapter; }
    public String getContent() { return content; }
    public int getChunkIndex() { return chunkIndex; }
    public List<Double> getEmbedding() { return embedding; }
    public int getRepeatCount() { return repeatCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(String id) { this.id = id; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public void setSubject(String subject) { this.subject = subject; }
    public void setChapter(String chapter) { this.chapter = chapter; }
    public void setContent(String content) { this.content = content; }
    public void setChunkIndex(int chunkIndex) { this.chunkIndex = chunkIndex; }
    public void setEmbedding(List<Double> embedding) { this.embedding = embedding; }
    public void setRepeatCount(int repeatCount) { this.repeatCount = repeatCount; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final TextChunk chunk = new TextChunk();
        public Builder id(String id) { chunk.id = id; return this; }
        public Builder documentId(String documentId) { chunk.documentId = documentId; return this; }
        public Builder subject(String subject) { chunk.subject = subject; return this; }
        public Builder chapter(String chapter) { chunk.chapter = chapter; return this; }
        public Builder content(String content) { chunk.content = content; return this; }
        public Builder chunkIndex(int chunkIndex) { chunk.chunkIndex = chunkIndex; return this; }
        public Builder embedding(List<Double> embedding) { chunk.embedding = embedding; return this; }
        public Builder repeatCount(int repeatCount) { chunk.repeatCount = repeatCount; return this; }
        public Builder createdAt(LocalDateTime createdAt) { chunk.createdAt = createdAt; return this; }
        public TextChunk build() { return chunk; }
    }
}
