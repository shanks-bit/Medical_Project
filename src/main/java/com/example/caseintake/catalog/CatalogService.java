package com.example.caseintake.catalog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class CatalogService {
    private final ObjectMapper objectMapper;
    private final Path catalogPath;

    public CatalogService(ObjectMapper objectMapper,
                          @Value("${app.catalog-path:catalog/field_catalog.json}") String catalogPath) {
        this.objectMapper = objectMapper;
        this.catalogPath = Path.of(catalogPath).toAbsolutePath().normalize();
    }

    public JsonNode getCatalog() {
        if (!Files.isRegularFile(catalogPath)) {
            throw new IllegalStateException("Catalog file not found: " + catalogPath);
        }
        try (var input = Files.newInputStream(catalogPath)) {
            return objectMapper.readTree(input);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read catalog: " + catalogPath, exception);
        }
    }
}
