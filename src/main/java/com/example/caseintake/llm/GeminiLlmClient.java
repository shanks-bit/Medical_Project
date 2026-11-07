package com.example.caseintake.llm;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Component
public class GeminiLlmClient implements LlmClient {
    private final RestClient restClient;
    private final String apiKey;
    private final String model;
    private final String baseUrl;

    public GeminiLlmClient(RestClient.Builder builder,
                           @Value("${app.llm.gemini-api-key:}") String apiKey,
                           @Value("${app.llm.gemini-model:gemini-2.5-flash}") String model,
                           @Value("${app.llm.gemini-base-url:https://generativelanguage.googleapis.com/v1beta/models}") String baseUrl) {
        this.restClient = builder.build();
        this.apiKey = apiKey;
        this.model = model;
        this.baseUrl = baseUrl;
    }

    @Override
    public String generate(String documentId, String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY environment variable is not configured");
        }

        var uri = UriComponentsBuilder
                .fromUriString(baseUrl + "/" + model + ":generateContent")
                .queryParam("key", apiKey)
                .build()
                .encode()
                .toUri();

        Map<String, Object> request = Map.of(
                "contents", List.of(Map.of(
                        "role", "user",
                        "parts", List.of(Map.of("text", prompt)))),
                "generationConfig", Map.of(
                        "temperature", 0.1,
                        "responseMimeType", "application/json")
        );

        JsonNode response = restClient.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(JsonNode.class);

        JsonNode text = response == null ? null
                : response.path("candidates").path(0).path("content").path("parts").path(0).path("text");
        if (text == null || text.isMissingNode() || text.asText().isBlank()) {
            throw new IllegalStateException("Gemini returned an empty response");
        }
        return text.asText();
    }

    @Override
    public String modelName() {
        return "gemini/" + model;
    }
}
