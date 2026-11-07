package com.example.caseintake.extraction;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/extractions")
public class ExtractionController {
    private final ExtractionService extractionService;

    public ExtractionController(ExtractionService extractionService) {
        this.extractionService = extractionService;
    }

    /** Development endpoint to run extraction for one document. */
    @PostMapping("/{documentId}")
    public JsonNode extract(@PathVariable String documentId) {
        return extractionService.extractAndSave(documentId);
    }
}
