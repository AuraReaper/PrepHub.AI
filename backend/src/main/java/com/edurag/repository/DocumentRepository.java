package com.edurag.repository;

import com.edurag.model.EduDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends MongoRepository<EduDocument, String> {
    List<EduDocument> findBySubject(String subject);
    List<EduDocument> findBySubjectAndChapter(String subject, String chapter);
    List<EduDocument> findByUploadedBy(String uploadedBy);
    List<EduDocument> findByStatus(String status);
}
