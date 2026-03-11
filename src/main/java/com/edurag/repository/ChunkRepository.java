package com.edurag.repository;

import com.edurag.model.TextChunk;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChunkRepository extends MongoRepository<TextChunk, String> {

    List<TextChunk> findByDocumentId(String documentId);

    List<TextChunk> findBySubject(String subject);

    List<TextChunk> findBySubjectAndChapter(String subject, String chapter);

    List<TextChunk> findBySubjectAndChapterOrderByRepeatCountDesc(String subject, String chapter);

    long countByDocumentId(String documentId);

    void deleteByDocumentId(String documentId);
}
