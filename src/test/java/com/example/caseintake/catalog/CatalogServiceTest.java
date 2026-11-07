package com.example.caseintake.catalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogServiceTest {
    @TempDir Path tempDir;

    @Test
    void readsCatalogFromConfiguredPath() throws Exception {
        Path file = tempDir.resolve("catalog.json");
        Files.writeString(file, "{\"catalog_version\":\"1.0\",\"fields\":[{\"id\":\"example\"}]}");

        CatalogService service = new CatalogService(new ObjectMapper(), file.toString());

        assertThat(service.getCatalog().path("catalog_version").asText()).isEqualTo("1.0");
        assertThat(service.getCatalog().path("fields").get(0).path("id").asText()).isEqualTo("example");
    }
}
