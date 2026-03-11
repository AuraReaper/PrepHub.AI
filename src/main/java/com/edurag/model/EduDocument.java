package com.edurag.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "documents")
public class EduDocument {

    @Id
    private String id;
    private String fileName;
    private String subject;
    private String chapter;
    private String uploadedBy;
    private LocalDateTime uploadedAt;
    private String filePath;
    private long fileSize;
    private String fileType;
    private List<TextChunk> chunks;
    private int totalChunks;
    private String status;

    public EduDocument() {}

    public String getId() { return id; }
    public String getFileName() { return fileName; }
    public String getSubject() { return subject; }
    public String getChapter() { return chapter; }
    public String getUploadedBy() { return uploadedBy; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public String getFilePath() { return filePath; }
    public long getFileSize() { return fileSize; }
    public String getFileType() { return fileType; }
    public List<TextChunk> getChunks() { return chunks; }
    public int getTotalChunks() { return totalChunks; }
    public String getStatus() { return status; }

    public void setId(String id) { this.id = id; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public void setSubject(String subject) { this.subject = subject; }
    public void setChapter(String chapter) { this.chapter = chapter; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    public void setChunks(List<TextChunk> chunks) { this.chunks = chunks; }
    public void setTotalChunks(int totalChunks) { this.totalChunks = totalChunks; }
    public void setStatus(String status) { this.status = status; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final EduDocument doc = new EduDocument();
        public Builder id(String id) { doc.id = id; return this; }
        public Builder fileName(String fileName) { doc.fileName = fileName; return this; }
        public Builder subject(String subject) { doc.subject = subject; return this; }
        public Builder chapter(String chapter) { doc.chapter = chapter; return this; }
        public Builder uploadedBy(String uploadedBy) { doc.uploadedBy = uploadedBy; return this; }
        public Builder uploadedAt(LocalDateTime uploadedAt) { doc.uploadedAt = uploadedAt; return this; }
        public Builder filePath(String filePath) { doc.filePath = filePath; return this; }
        public Builder fileSize(long fileSize) { doc.fileSize = fileSize; return this; }
        public Builder fileType(String fileType) { doc.fileType = fileType; return this; }
        public Builder chunks(List<TextChunk> chunks) { doc.chunks = chunks; return this; }
        public Builder totalChunks(int totalChunks) { doc.totalChunks = totalChunks; return this; }
        public Builder status(String status) { doc.status = status; return this; }
        public EduDocument build() { return doc; }
    }
}
