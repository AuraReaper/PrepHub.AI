package com.edurag.service;

import com.edurag.dto.ApiDtos;
import com.edurag.model.Question;
import com.edurag.model.TextChunk;
import com.edurag.repository.ChunkRepository;
import com.edurag.repository.QuestionRepository;
import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Generates structured Q&A from document chunks using Gemini.
 */
@Service
public class QuestionGenerationService {

    private static final Logger log = LoggerFactory.getLogger(QuestionGenerationService.class);

    @Autowired
    private GeminiApiService geminiApiService;

    @Autowired
    private ChunkRepository chunkRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private EmbeddingService embeddingService;

    private final Gson gson = new GsonBuilder().create();

    /**
     * Generate questions for the given subject/chapter.
     */
    public ApiDtos.GenerateResponse generateQuestions(String subject, String chapter,
                                                      int count, String difficulty, String type) {
        // Get relevant chunks
        List<TextChunk> chunks;
        if (subject != null && chapter != null) {
            chunks = chunkRepository.findBySubjectAndChapter(subject, chapter);
        } else if (subject != null) {
            chunks = chunkRepository.findBySubject(subject);
        } else {
            chunks = chunkRepository.findAll();
        }

        if (chunks.isEmpty()) {
            throw new RuntimeException("No content found for subject=" + subject + ", chapter=" + chapter);
        }

        // Pick random chunks to diversify questions
        Collections.shuffle(chunks);
        List<TextChunk> selectedChunks = chunks.subList(0, Math.min(chunks.size(), 5));
        String context = selectedChunks.stream()
                .map(TextChunk::getContent)
                .collect(Collectors.joining("\n\n"));

        // Build generation prompt
        String prompt = buildGenerationPrompt(context, count, difficulty, type);

        log.info("Generating {} {} questions for subject={}, chapter={}", count, type, subject, chapter);
        String rawResponse = geminiApiService.generateText(prompt);

        // Parse and save questions
        List<Question> questions = parseAndSaveQuestions(rawResponse, subject, chapter,
                selectedChunks.get(0).getDocumentId(), difficulty, type);

        // Convert to DTOs
        List<ApiDtos.QuestionDTO> questionDTOs = questions.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return ApiDtos.GenerateResponse.builder()
                .subject(subject)
                .chapter(chapter)
                .totalGenerated(questionDTOs.size())
                .questions(questionDTOs)
                .build();
    }

    private String buildGenerationPrompt(String context, int count, String difficulty, String type) {
        String difficultyInstruction = "MIXED".equals(difficulty)
                ? "Mix EASY, MEDIUM, and HARD difficulties."
                : "All questions should be " + difficulty + " difficulty.";

        String typeInstruction = switch (type) {
            case "MCQ"          -> "Generate Multiple Choice Questions (MCQ) with 4 options (A, B, C, D) and a correct answer.";
            case "SHORT_ANSWER" -> "Generate short answer questions with concise answers (1-2 sentences).";
            case "DESCRIPTIVE"  -> "Generate descriptive questions with detailed answers.";
            default             -> "Mix MCQ, SHORT_ANSWER, and DESCRIPTIVE questions.";
        };

        return """
            You are an expert educator. Based on the following content, generate exactly %d questions.
            
            %s
            %s
            
            Return ONLY a valid JSON array with this structure:
            [
              {
                "questionText": "...",
                "type": "MCQ | SHORT_ANSWER | DESCRIPTIVE",
                "difficulty": "EASY | MEDIUM | HARD",
                "answer": "...",
                "options": ["A. ...", "B. ...", "C. ...", "D. ..."],
                "correctOption": "A"
              }
            ]
            
            For SHORT_ANSWER and DESCRIPTIVE, set options to [] and correctOption to "".
            Return ONLY the JSON array, no extra text.
            
            CONTENT:
            %s
            """.formatted(count, difficultyInstruction, typeInstruction, context);
    }

    private List<Question> parseAndSaveQuestions(String rawJson, String subject, String chapter,
                                                  String documentId, String difficulty, String type) {
        List<Question> saved = new ArrayList<>();
        try {
            // Clean up markdown code blocks if Gemini wraps in ```json
            String cleaned = rawJson.replaceAll("```json", "").replaceAll("```", "").trim();
            JsonArray arr = gson.fromJson(cleaned, JsonArray.class);

            for (JsonElement el : arr) {
                JsonObject obj = el.getAsJsonObject();

                List<String> options = new ArrayList<>();
                if (obj.has("options")) {
                    for (JsonElement opt : obj.getAsJsonArray("options")) {
                        options.add(opt.getAsString());
                    }
                }

                Question question = Question.builder()
                        .documentId(documentId)
                        .subject(subject)
                        .chapter(chapter)
                        .questionText(obj.get("questionText").getAsString())
                        .type(obj.has("type") ? obj.get("type").getAsString() : type)
                        .difficulty(obj.has("difficulty") ? obj.get("difficulty").getAsString() : difficulty)
                        .answer(obj.has("answer") ? obj.get("answer").getAsString() : "")
                        .options(options)
                        .correctOption(obj.has("correctOption") ? obj.get("correctOption").getAsString() : "")
                        .repeatCount(0)
                        .generatedAt(LocalDateTime.now())
                        .build();

                // Generate embedding for the question
                try {
                    List<Double> embedding = embeddingService.cosineSimilarity(
                            geminiApiService.generateEmbedding(question.getQuestionText()),
                            Collections.emptyList()) == 0
                            ? geminiApiService.generateEmbedding(question.getQuestionText())
                            : null;
                    question.setQuestionEmbedding(
                            geminiApiService.generateEmbedding(question.getQuestionText()));
                } catch (Exception e) {
                    log.warn("Could not embed question: {}", e.getMessage());
                }

                saved.add(questionRepository.save(question));
            }

        } catch (Exception e) {
            log.error("Failed to parse question JSON: {}", e.getMessage());
            log.debug("Raw response: {}", rawJson);
        }

        return saved;
    }

    private ApiDtos.QuestionDTO toDTO(Question q) {
        return ApiDtos.QuestionDTO.builder()
                .id(q.getId())
                .questionText(q.getQuestionText())
                .answer(q.getAnswer())
                .difficulty(q.getDifficulty())
                .type(q.getType())
                .options(q.getOptions())
                .correctOption(q.getCorrectOption())
                .repeatCount(q.getRepeatCount())
                .build();
    }
}
