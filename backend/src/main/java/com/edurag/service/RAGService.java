package com.edurag.service;

import com.edurag.dto.ApiDtos;
import com.edurag.model.TextChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * RAG (Retrieval-Augmented Generation) Service.
 *
 * Workflow:
 * 1. Convert user query to embedding
 * 2. Retrieve top-K similar chunks from MongoDB
 * 3. Increment repeatCount for retrieved chunks
 * 4. Build context + query prompt
 * 5. Call Gemini and return structured answer
 */
@Service
public class RAGService {

    private static final Logger log = LoggerFactory.getLogger(RAGService.class);
    private static final int TOP_K = 5;

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private GeminiApiService geminiApiService;

    @Autowired
    private com.edurag.repository.ChunkRepository chunkRepository;

    /**
     * Answer a student question using RAG.
     */
    public ApiDtos.AskResponse ask(String question, String subject, String chapter) {
        log.info("RAG ask: '{}' | subject={}, chapter={}", question, subject, chapter);

        // 1. Retrieve similar chunks
        List<TextChunk> relevantChunks = embeddingService.findSimilarChunks(question, subject, chapter, TOP_K);

        if (relevantChunks.isEmpty()) {
            return ApiDtos.AskResponse.builder()
                    .question(question)
                    .answer("I couldn't find relevant content for your question. Please ensure materials have been uploaded for this subject and chapter.")
                    .subject(subject)
                    .chapter(chapter)
                    .build();
        }

        // 2. Increment repeat count for retrieved chunks (tracks popular topics)
        relevantChunks.forEach(chunk -> {
            chunk.setRepeatCount(chunk.getRepeatCount() + 1);
            chunkRepository.save(chunk);
        });

        // 3. Build context string
        String context = relevantChunks.stream()
                .map(TextChunk::getContent)
                .collect(Collectors.joining("\n\n---\n\n"));

        List<String> chunkTexts = relevantChunks.stream()
                .map(c -> c.getContent().substring(0, Math.min(150, c.getContent().length())) + "...")
                .collect(Collectors.toList());

        // 4. Build prompt and generate answer
        String prompt = buildRAGPrompt(context, question, subject, chapter);
        String answer = geminiApiService.generateText(prompt);

        log.info("RAG answer generated for question: '{}'", question);

        return ApiDtos.AskResponse.builder()
                .question(question)
                .answer(answer)
                .relevantChunks(chunkTexts)
                .subject(subject)
                .chapter(chapter)
                .build();
    }

    /**
     * Answer a doubt/chatbot question with a more conversational tone.
     */
    public ApiDtos.AskResponse solveDoubt(String doubt, String subject, String chapter) {
        log.info("Doubt solver: '{}' | subject={}, chapter={}", doubt, subject, chapter);

        List<TextChunk> relevantChunks = embeddingService.findSimilarChunks(doubt, subject, chapter, TOP_K);

        String context = relevantChunks.stream()
                .map(TextChunk::getContent)
                .collect(Collectors.joining("\n\n---\n\n"));

        String prompt = buildDoubtPrompt(context, doubt);
        String answer = geminiApiService.generateText(prompt);

        return ApiDtos.AskResponse.builder()
                .question(doubt)
                .answer(answer)
                .relevantChunks(relevantChunks.stream()
                        .map(c -> c.getContent().substring(0, Math.min(100, c.getContent().length())) + "...")
                        .collect(Collectors.toList()))
                .subject(subject)
                .chapter(chapter)
                .build();
    }

    private String buildRAGPrompt(String context, String question, String subject, String chapter) {
        return """
            You are a knowledgeable education assistant helping students understand their study material.
            Subject: %s | Chapter: %s
            
            Use the following content to answer the student's question accurately and clearly.
            If the answer is not in the content, say so honestly.
            
            STUDY CONTENT:
            %s
            
            STUDENT QUESTION: %s
            
            Provide a clear, structured answer. Use bullet points or numbered steps if helpful.
            """.formatted(subject, chapter, context, question);
    }

    private String buildDoubtPrompt(String context, String doubt) {
        return """
            You are a friendly and helpful tutor answering a student's doubt.
            Use the following study material as context to answer the doubt clearly and in simple language.
            
            STUDY CONTENT:
            %s
            
            STUDENT'S DOUBT: %s
            
            Answer conversationally, as a teacher explaining to a student. Keep it clear and easy to understand.
            """.formatted(context, doubt);
    }
}
