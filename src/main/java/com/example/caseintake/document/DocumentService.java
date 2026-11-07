package com.example.caseintake.document;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentService {
    private final DocumentRepository documentRepository;

    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public List<DocumentSummary> getAllDocuments() {
        List<DocumentSummary> summaries = new ArrayList<>();

        for (String id : documentRepository.findAllIds()) {
            String text = documentRepository.readText(id);
            JsonNode extraction = documentRepository.readExtractionOrNull(id);

            int caseCount = extraction != null && extraction.path("cases").isArray()
                    ? extraction.path("cases").size() : 0;
            List<String> flags = new ArrayList<>();
            if (extraction != null && extraction.path("flags").isArray()) {
                extraction.path("flags").forEach(flag -> flags.add(flag.asText()));
            }

            summaries.add(new DocumentSummary(id, extractTitle(text), caseCount, List.copyOf(flags)));
        }
        return summaries;
    }

    public DocumentDetail getDocument(String id) {
        String text = documentRepository.readText(id);
        JsonNode extraction = documentRepository.readExtractionOrNull(id);
        return new DocumentDetail(id, text, extraction);
    }

    private String extractTitle(String text) {
        return text.lines()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .findFirst()
                .orElse("Untitled document");
    }
}
