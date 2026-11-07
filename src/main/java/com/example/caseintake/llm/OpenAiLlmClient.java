package com.example.caseintake.llm;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/** OpenAI Responses API implementation of the provider-neutral LlmClient. */
@Component
public class OpenAiLlmClient implements LlmClient {
    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public OpenAiLlmClient(RestClient.Builder builder,
                           @Value("${app.llm.api-key:}") String apiKey,
                           @Value("${app.llm.model:gpt-4.1-mini}") String model,
                           @Value("${app.llm.openai-base-url:https://api.openai.com/v1}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override
    public String generate(String documentId, String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OPENAI_API_KEY environment variable is not configured");
        }

        Map<String, Object> request = Map.of(
                "model", model,
                "input", prompt,
                "text", Map.of("format", Map.of("type", "json_object"))
        );

        JsonNode response = restClient.post()
                .uri("/responses")
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(JsonNode.class);

        if (response == null) {
            throw new IllegalStateException("OpenAI returned an empty response");
        }

        StringBuilder text = new StringBuilder();
        JsonNode output = response.path("output");
        if (output.isArray()) {
            for (JsonNode item : output) {
                JsonNode content = item.path("content");
                if (content.isArray()) {
                    for (JsonNode part : content) {
                        if ("output_text".equals(part.path("type").asText())) {
                            text.append(part.path("text").asText());
                        }
                    }
                }
            }
        }

        if (text.isEmpty()) {
            String status = response.path("status").asText("unknown");
            String detail = response.path("incomplete_details").toString();
            throw new IllegalStateException("OpenAI returned no output_text (status=" + status + ", details=" + detail + ")");
        }
        return text.toString();
    }

    @Override
    public String modelName() {
        return "openai/" + model;
    }
}
