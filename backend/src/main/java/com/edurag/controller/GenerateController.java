package com.edurag.controller;

import com.edurag.dto.ApiDtos;
import com.edurag.model.Question;
import com.edurag.repository.QuestionRepository;
import com.edurag.service.QuestionGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for question generation and retrieval.
 *
 * POST /generate                  - Generate questions for a subject/chapter
 * GET  /generate/questions        - Retrieve stored questions
 * GET  /generate/questions/{id}   - Get a specific question
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class GenerateController {

    private static final Logger log = LoggerFactory.getLogger(GenerateController.class);

    @Autowired private QuestionGenerationService questionGenerationService;
    @Autowired private QuestionRepository questionRepository;

    /**
     * Generate questions from stored study material.
     * Body: { "subject": "...", "chapter": "...", "numberOfQuestions": 5, "difficulty": "MIXED", "type": "MIXED" }
     */
    @PostMapping("/generate")
    public ResponseEntity<?> generateQuestions(@RequestBody ApiDtos.GenerateRequest request) {
        if (request.getSubject() == null || request.getSubject().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Subject is required"));
        }

        try {
            ApiDtos.GenerateResponse response = questionGenerationService.generateQuestions(
                    request.getSubject(),
                    request.getChapter(),
                    request.getNumberOfQuestions() > 0 ? request.getNumberOfQuestions() : 5,
                    request.getDifficulty() != null ? request.getDifficulty() : "MIXED",
                    request.getType() != null ? request.getType() : "MIXED"
            );
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Question generation failed", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error in question generation", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Generation failed: " + e.getMessage()));
        }
    }

    /**
     * Retrieve stored questions filtered by subject/chapter/difficulty/type.
     */
    @GetMapping("/generate/questions")
    public ResponseEntity<List<Question>> getQuestions(
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String chapter,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String type) {

        List<Question> questions;

        if (subject != null && chapter != null && difficulty != null) {
            questions = questionRepository.findBySubjectAndChapterAndDifficulty(subject, chapter, difficulty);
        } else if (subject != null && chapter != null && type != null) {
            questions = questionRepository.findBySubjectAndChapterAndType(subject, chapter, type);
        } else if (subject != null && chapter != null) {
            questions = questionRepository.findBySubjectAndChapter(subject, chapter);
        } else if (subject != null) {
            questions = questionRepository.findBySubject(subject);
        } else {
            questions = questionRepository.findAll();
        }

        return ResponseEntity.ok(questions);
    }

    /**
     * Get a specific question by ID.
     */
    @GetMapping("/generate/questions/{id}")
    public ResponseEntity<?> getQuestion(@PathVariable String id) {
        return questionRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get popular questions (highest repeatCount) for a subject/chapter.
     */
    @GetMapping("/generate/popular")
    public ResponseEntity<List<Question>> getPopularQuestions(
            @RequestParam String subject,
            @RequestParam String chapter) {
        List<Question> questions = questionRepository
                .findBySubjectAndChapterOrderByRepeatCountDesc(subject, chapter);
        return ResponseEntity.ok(questions.stream().limit(10).toList());
    }
}
