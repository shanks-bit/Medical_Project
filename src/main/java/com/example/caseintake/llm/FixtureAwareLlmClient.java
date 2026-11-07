package com.example.caseintake.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

/** Records sanitized provider exchanges in live mode and replays them without network in replay mode. */
@Component
@Primary
public class FixtureAwareLlmClient implements LlmClient {
    private static final Pattern SAFE_ID = Pattern.compile("[A-Za-z0-9_-]+");
    private final LlmClient providerClient;
    private final ObjectMapper mapper;
    private final Path fixtureDirectory;
    private final String mode;
    private final String provider;

    public FixtureAwareLlmClient(@Qualifier("providerLlmClient") LlmClient providerLlmClient, ObjectMapper mapper,
            @Value("${app.fixture-path:fixtures}") String fixturePath,
            @Value("${app.llm.mode:live}") String mode,
            @Value("${app.llm.provider:openai}") String provider) {
        this.providerClient = providerLlmClient;
        this.mapper = mapper;
        this.fixtureDirectory = Path.of(fixturePath).toAbsolutePath().normalize();
        this.mode = mode;
        this.provider = provider.toLowerCase();
        if (!mode.equalsIgnoreCase("live") && !mode.equalsIgnoreCase("replay")) {
            throw new IllegalArgumentException("app.llm.mode must be 'live' or 'replay'");
        }
    }

    @Override
    public String generate(String documentId, String prompt) {
        validateId(documentId);
        if (mode.equalsIgnoreCase("replay")) return replay(documentId);
        String response = providerClient.generate(documentId, prompt);
        record(documentId, prompt, response);
        return response;
    }

    private String replay(String id) {
        Path responseFile = fixturePath(id).resolve("response.json");
        if (!Files.isRegularFile(responseFile)) {
            throw new IllegalStateException("Missing replay fixture: " + responseFile
                    + ". Run live extraction with provider '" + provider + "' first.");
        }
        try {
            var recorded = mapper.readTree(responseFile.toFile());
            String recordedProvider = recorded.path("provider").asText("");
            if (!provider.equalsIgnoreCase(recordedProvider)) {
                throw new IllegalStateException("Fixture provider mismatch. Selected " + provider
                        + " but fixture contains " + recordedProvider + ".");
            }
            return recorded.path("raw_response").asText();
        } catch (IOException e) {
            throw new IllegalStateException("Could not read fixture for " + id, e);
        }
    }

    private void record(String id, String prompt, String response) {
        try {
            Path directory = fixturePath(id);
            Files.createDirectories(directory);
            ObjectNode request = mapper.createObjectNode();
            request.put("document_id", id);
            request.put("provider", provider);
            request.put("model", providerClient.modelName());
            request.put("prompt", prompt);
            ObjectNode recordedResponse = mapper.createObjectNode();
            recordedResponse.put("document_id", id);
            recordedResponse.put("provider", provider);
            recordedResponse.put("model", providerClient.modelName());
            recordedResponse.put("raw_response", response);
            mapper.writerWithDefaultPrettyPrinter().writeValue(directory.resolve("request.json").toFile(), request);
            mapper.writerWithDefaultPrettyPrinter().writeValue(directory.resolve("response.json").toFile(), recordedResponse);
        } catch (IOException e) {
            throw new IllegalStateException("Could not record fixture for " + id, e);
        }
    }

    private Path fixturePath(String id) {
        return fixtureDirectory.resolve(provider).resolve(id).normalize();
    }

    private void validateId(String id) {
        if (id == null || !SAFE_ID.matcher(id).matches() || !fixturePath(id).startsWith(fixtureDirectory)) {
            throw new IllegalArgumentException("Invalid document ID: " + id);
        }
    }

    @Override
    public String modelName() {
        return providerClient.modelName();
    }
}
