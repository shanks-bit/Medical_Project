package com.example.caseintake.document;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Repository
public class DocumentRepository {
    private static final Pattern SAFE_DOCUMENT_ID = Pattern.compile("[A-Za-z0-9_-]+");

    private final Path documentsDirectory;
    private final Path outputDirectory;
    private final ObjectMapper objectMapper;

    public DocumentRepository(
            ObjectMapper objectMapper,
            @Value("${app.documents-path:documents}") String documentsPath,
            @Value("${app.output-path:out}") String outputPath) {
        this.objectMapper = objectMapper;
        this.documentsDirectory = Path.of(documentsPath).toAbsolutePath().normalize();
        this.outputDirectory = Path.of(outputPath).toAbsolutePath().normalize();
    }

    public List<String> findAllIds() {
        if (!Files.isDirectory(documentsDirectory)) {
            throw new IllegalStateException("Documents directory not found: " + documentsDirectory);
        }

        try (Stream<Path> files = Files.list(documentsDirectory)) {
            return files
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().matches("D\\d{2}\\.txt"))
                    .map(path -> path.getFileName().toString().replaceFirst("\\.txt$", ""))
                    .sorted(Comparator.naturalOrder())
                    .toList();
        } catch (IOException exception) {
            throw new IllegalStateException("Could not list documents", exception);
        }
    }

    public String readText(String documentId) {
        Path file = resolveDocumentPath(documentId);
        if (!Files.isRegularFile(file)) {
            throw new DocumentNotFoundException("Document not found: " + documentId);
        }
        try {
            return Files.readString(file);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read document: " + documentId, exception);
        }
    }

    public JsonNode readExtractionOrNull(String documentId) {
        validateDocumentId(documentId);
        Path file = outputDirectory.resolve(documentId + ".json").normalize();
        if (!file.startsWith(outputDirectory) || !Files.isRegularFile(file)) {
            return null;
        }
        try {
            return objectMapper.readTree(file.toFile());
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read extraction for: " + documentId, exception);
        }
    }

    private Path resolveDocumentPath(String documentId) {
        validateDocumentId(documentId);
        Path file = documentsDirectory.resolve(documentId + ".txt").normalize();
        if (!file.startsWith(documentsDirectory)) {
            throw new IllegalArgumentException("Invalid document ID");
        }
        return file;
    }

    private void validateDocumentId(String documentId) {
        if (documentId == null || !SAFE_DOCUMENT_ID.matcher(documentId).matches()) {
            throw new IllegalArgumentException("Invalid document ID: " + documentId);
        }
    }
}
