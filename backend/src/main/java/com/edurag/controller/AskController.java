package com.edurag.controller;

import com.edurag.dto.ApiDtos;
import com.edurag.service.RAGService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for student Q&A and doubt solving.
 *
 * POST /ask        - RAG-powered question answering (Student Practice)
 * POST /ask/doubt  - Chatbot doubt solver
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AskController {

    private static final Logger log = LoggerFactory.getLogger(AskController.class);

    @Autowired
    private RAGService ragService;

    /**
     * Answer a student's question using RAG.
     * Body: { "question": "...", "subject": "...", "chapter": "..." }
     */
    @PostMapping("/ask")
    public ResponseEntity<?> ask(@RequestBody ApiDtos.AskRequest request) {
        if (request.getQuestion() == null || request.getQuestion().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Question cannot be empty"));
        }

        try {
            ApiDtos.AskResponse response = ragService.ask(
                    request.getQuestion(),
                    request.getSubject(),
                    request.getChapter()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("RAG ask failed", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to answer: " + e.getMessage()));
        }
    }

    /**
     * Solve a student's doubt using the chatbot (more conversational).
     * Body: { "question": "...", "subject": "...", "chapter": "..." }
     */
    @PostMapping("/ask/doubt")
    public ResponseEntity<?> solveDoubt(@RequestBody ApiDtos.AskRequest request) {
        if (request.getQuestion() == null || request.getQuestion().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Doubt cannot be empty"));
        }

        try {
            ApiDtos.AskResponse response = ragService.solveDoubt(
                    request.getQuestion(),
                    request.getSubject(),
                    request.getChapter()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Doubt solving failed", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to solve doubt: " + e.getMessage()));
        }
    }
}
