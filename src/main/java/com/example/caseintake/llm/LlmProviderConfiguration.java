package com.example.caseintake.llm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LlmProviderConfiguration {
    @Bean("providerLlmClient")
    public LlmClient providerLlmClient(
            @Value("${app.llm.provider:openai}") String provider,
            OpenAiLlmClient openAi,
            GeminiLlmClient gemini) {
        return switch (provider.toLowerCase()) {
            case "openai" -> openAi;
            case "gemini" -> gemini;
            default -> throw new IllegalArgumentException(
                    "Unsupported app.llm.provider: " + provider + ". Use openai or gemini.");
        };
    }
}
