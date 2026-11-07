package com.example.caseintake.document;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {
    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping
    public List<DocumentSummary> getDocuments() {
        return documentService.getAllDocuments();
    }

    @GetMapping("/{id}")
    public DocumentDetail getDocument(@PathVariable String id) {
        return documentService.getDocument(id);
    }
}
