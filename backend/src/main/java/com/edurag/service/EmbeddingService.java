package com.edurag.service;

import com.edurag.model.TextChunk;
import com.edurag.repository.ChunkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages embedding creation and similarity-based retrieval.
 * Uses cosine similarity to find relevant chunks.
 */
@Service
public class EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);

    @Autowired
    private GeminiApiService geminiApiService;

    @Autowired
    private ChunkRepository chunkRepository;

    /**
     * Generate and store embeddings for a list of text chunks.
     */
    public void embedAndSaveChunks(List<TextChunk> chunks) {
        for (TextChunk chunk : chunks) {
            try {
                List<Double> embedding = geminiApiService.generateEmbedding(chunk.getContent());
                chunk.setEmbedding(embedding);
                chunkRepository.save(chunk);
                log.debug("Embedded chunk {} for doc {}", chunk.getChunkIndex(), chunk.getDocumentId());
            } catch (Exception e) {
                log.error("Failed to embed chunk {}: {}", chunk.getChunkIndex(), e.getMessage());
            }
        }
    }

    /**
     * Find the top-k most similar chunks for a given query.
     * Filters by subject and chapter if provided.
     */
    public List<TextChunk> findSimilarChunks(String query, String subject, String chapter, int topK) {
        // Generate query embedding
        List<Double> queryEmbedding = geminiApiService.generateEmbedding(query);

        // Fetch candidate chunks from MongoDB
        List<TextChunk> candidates;
        if (subject != null && !subject.isBlank() && chapter != null && !chapter.isBlank()) {
            candidates = chunkRepository.findBySubjectAndChapter(subject, chapter);
        } else if (subject != null && !subject.isBlank()) {
            candidates = chunkRepository.findBySubject(subject);
        } else {
            candidates = chunkRepository.findAll();
        }

        log.info("Searching {} candidate chunks for query: '{}'", candidates.size(), query);

        // Rank by cosine similarity and return top-k
        return candidates.stream()
                .filter(c -> c.getEmbedding() != null && !c.getEmbedding().isEmpty())
                .sorted((a, b) -> Double.compare(
                        cosineSimilarity(queryEmbedding, b.getEmbedding()),
                        cosineSimilarity(queryEmbedding, a.getEmbedding())))
                .limit(topK)
                .collect(Collectors.toList());
    }

    /**
     * Compute cosine similarity between two vectors.
     */
    public double cosineSimilarity(List<Double> a, List<Double> b) {
        if (a == null || b == null || a.size() != b.size()) return 0.0;

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < a.size(); i++) {
            dotProduct += a.get(i) * b.get(i);
            normA      += a.get(i) * a.get(i);
            normB      += b.get(i) * b.get(i);
        }

        if (normA == 0 || normB == 0) return 0.0;
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * Check if a new chunk is similar to any existing chunk for the same doc.
     * If similar, increment repeatCount instead of saving a duplicate.
     */
    public boolean checkAndIncrementIfDuplicate(TextChunk newChunk, double threshold) {
        List<TextChunk> existing = chunkRepository.findByDocumentId(newChunk.getDocumentId());

        for (TextChunk existing_chunk : existing) {
            if (existing_chunk.getEmbedding() == null) continue;
            double sim = cosineSimilarity(newChunk.getEmbedding(), existing_chunk.getEmbedding());
            if (sim >= threshold) {
                existing_chunk.setRepeatCount(existing_chunk.getRepeatCount() + 1);
                chunkRepository.save(existing_chunk);
                log.info("Chunk similarity {} >= threshold {}. Incrementing repeatCount to {}",
                        sim, threshold, existing_chunk.getRepeatCount());
                return true; // is duplicate
            }
        }
        return false;
    }
}
