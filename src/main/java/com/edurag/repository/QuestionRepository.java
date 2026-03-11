package com.edurag.repository;

import com.edurag.model.Question;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends MongoRepository<Question, String> {

    List<Question> findBySubject(String subject);

    List<Question> findBySubjectAndChapter(String subject, String chapter);

    List<Question> findBySubjectAndChapterAndDifficulty(String subject, String chapter, String difficulty);

    List<Question> findBySubjectAndChapterAndType(String subject, String chapter, String type);

    List<Question> findByDocumentId(String documentId);

    List<Question> findBySubjectAndChapterOrderByRepeatCountDesc(String subject, String chapter);

    long countBySubjectAndChapter(String subject, String chapter);

    void deleteByDocumentId(String documentId);
}
