package com.example.caseintake.extraction;

import com.example.caseintake.catalog.CatalogService;
import com.example.caseintake.document.DocumentRepository;
import com.example.caseintake.llm.LlmClient;
import com.example.caseintake.provenance.ProvenanceService;
import com.example.caseintake.validation.ValidationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class ExtractionService {
    private final DocumentRepository documents;
    private final CatalogService catalogService;
    private final PromptBuilder promptBuilder;
    private final LlmClient llmClient;
    private final ProvenanceService provenanceService;
    private final ValidationService validationService;
    private final ObjectMapper objectMapper;
    private final Path outputDirectory;

    public ExtractionService(DocumentRepository documents,
                             CatalogService catalogService,
                             PromptBuilder promptBuilder,
                             LlmClient llmClient,
                             ProvenanceService provenanceService,
                             ValidationService validationService,
                             ObjectMapper objectMapper,
                             @Value("${app.output-path:out}") String outputPath) {
        this.documents = documents;
        this.catalogService = catalogService;
        this.promptBuilder = promptBuilder;
        this.llmClient = llmClient;
        this.provenanceService = provenanceService;
        this.validationService = validationService;
        this.objectMapper = objectMapper;
        this.outputDirectory = Path.of(outputPath).toAbsolutePath().normalize();
    }

    public JsonNode extractAndSave(String documentId) {
        String sourceText = documents.readText(documentId);
        JsonNode catalog = catalogService.getCatalog();
        String prompt = promptBuilder.build(catalog, sourceText);
        String modelResponse = llmClient.generate(documentId, prompt);

        final JsonNode parsed;
        try {
            parsed = objectMapper.readTree(modelResponse);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("LLM response was not valid JSON", exception);
        }
        if (!(parsed instanceof ObjectNode result)) {
            throw new IllegalStateException("LLM response must be a JSON object");
        }

        result.put("document_id", documentId);
        result.put("catalog_version", catalog.path("catalog_version").asText("unknown"));
        result.put("model", llmClient.modelName());
        if (!result.path("flags").isArray()) result.set("flags", objectMapper.createArrayNode());
        if (!result.path("cases").isArray()) result.set("cases", objectMapper.createArrayNode());

        ensureEveryCatalogField(result, catalog);
        validationService.validateAndNormalize(result, catalog);
        provenanceService.attachOffsets(result, sourceText);
        save(documentId, result);
        return result;
    }

    private void ensureEveryCatalogField(ObjectNode result, JsonNode catalog) {
        JsonNode fields = catalog.path("fields");
        ArrayNode cases = (ArrayNode) result.path("cases");
        for (JsonNode caseNode : cases) {
            if (!(caseNode instanceof ObjectNode caseObject)) continue;
            JsonNode existingFields = caseObject.path("fields");
            ObjectNode fieldResults;
            if (existingFields instanceof ObjectNode object) fieldResults = object;
            else {
                fieldResults = objectMapper.createObjectNode();
                caseObject.set("fields", fieldResults);
            }
            for (JsonNode catalogField : fields) {
                String fieldId = catalogField.path("id").asText();
                if (!fieldResults.has(fieldId)) {
                    ObjectNode unknown = objectMapper.createObjectNode();
                    unknown.put("status", "unknown");
                    unknown.put("note", "The model did not return this catalog field.");
                    fieldResults.set(fieldId, unknown);
                }
            }
        }
    }

    private void save(String documentId, JsonNode result) {
        try {
            Files.createDirectories(outputDirectory);
            Path target = outputDirectory.resolve(documentId + ".json").normalize();
            if (!target.startsWith(outputDirectory)) throw new IllegalArgumentException("Invalid document ID");
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(target.toFile(), result);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not save extraction output", exception);
        }
    }
}
