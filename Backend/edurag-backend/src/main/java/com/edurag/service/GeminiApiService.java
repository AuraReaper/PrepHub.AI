package com.edurag.service;

import com.google.gson.*;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Low-level Gemini API client.
 * Handles REST calls to Google Gemini for:
 * - Text generation (chat, Q&A, question generation)
 * - Embedding creation
 */
@Service
public class GeminiApiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiApiService.class);

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.base-url}")
    private String baseUrl;

    @Value("${gemini.model.chat}")
    private String chatModel;

    @Value("${gemini.model.embedding}")
    private String embeddingModel;

    private final OkHttpClient httpClient;
    private final Gson gson;
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public GeminiApiService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * Generate a text response from Gemini given a prompt.
     */
    public String generateText(String prompt) {
        String url = baseUrl + "/models/" + chatModel + ":generateContent?key=" + apiKey;

        JsonObject requestBody = new JsonObject();
        JsonArray contents = new JsonArray();
        JsonObject content = new JsonObject();
        JsonArray parts = new JsonArray();
        JsonObject part = new JsonObject();
        part.addProperty("text", prompt);
        parts.add(part);
        content.add("parts", parts);
        contents.add(content);
        requestBody.add("contents", contents);

        // Generation config
        JsonObject genConfig = new JsonObject();
        genConfig.addProperty("temperature", 0.7);
        genConfig.addProperty("maxOutputTokens", 2048);
        requestBody.add("generationConfig", genConfig);

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(gson.toJson(requestBody), JSON))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                log.error("Gemini API error {}: {}", response.code(), errorBody);
                throw new RuntimeException("Gemini API error " + response.code() + ": " + errorBody);
            }

            String responseBody = response.body().string();
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);

            return jsonResponse
                    .getAsJsonArray("candidates")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString();

        } catch (IOException e) {
            log.error("Network error calling Gemini", e);
            throw new RuntimeException("Failed to call Gemini API", e);
        }
    }

    /**
     * Generate embedding vector for a given text using Gemini.
     */
    public List<Double> generateEmbedding(String text) {
        String url = baseUrl + "/models/" + embeddingModel + ":embedContent?key=" + apiKey;

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", "models/" + embeddingModel);
        JsonObject content = new JsonObject();
        JsonArray parts = new JsonArray();
        JsonObject part = new JsonObject();
        part.addProperty("text", text);
        parts.add(part);
        content.add("parts", parts);
        requestBody.add("content", content);

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(gson.toJson(requestBody), JSON))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                log.error("Gemini Embedding API error {}: {}", response.code(), errorBody);
                throw new RuntimeException("Gemini Embedding API error " + response.code());
            }

            String responseBody = response.body().string();
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
            JsonArray values = jsonResponse
                    .getAsJsonObject("embedding")
                    .getAsJsonArray("values");

            List<Double> embedding = new ArrayList<>();
            for (JsonElement val : values) {
                embedding.add(val.getAsDouble());
            }
            return embedding;

        } catch (IOException e) {
            log.error("Network error calling Gemini Embedding API", e);
            throw new RuntimeException("Failed to call Gemini Embedding API", e);
        }
    }
}
