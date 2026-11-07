package com.example.caseintake.document;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentServiceTest {
    @TempDir Path tempDir;

    @Test
    void listsDocumentsAndReadsText() throws Exception {
        Path documents = Files.createDirectories(tempDir.resolve("documents"));
        Path output = Files.createDirectories(tempDir.resolve("out"));
        Files.writeString(documents.resolve("D01.txt"), "Subject: Test report\nPatient had a headache.");
        Files.writeString(documents.resolve("D02.txt"), "Second report");
        Files.writeString(output.resolve("D01.json"), "{\"cases\":[{},{}],\"flags\":[\"multiple_patients\"]}");

        DocumentRepository repository = new DocumentRepository(
                new ObjectMapper(), documents.toString(), output.toString());
        DocumentService service = new DocumentService(repository);

        var summaries = service.getAllDocuments();
        assertThat(summaries).hasSize(2);
        assertThat(summaries.get(0).id()).isEqualTo("D01");
        assertThat(summaries.get(0).title()).isEqualTo("Subject: Test report");
        assertThat(summaries.get(0).cases()).isEqualTo(2);
        assertThat(summaries.get(0).flags()).containsExactly("multiple_patients");

        var detail = service.getDocument("D01");
        assertThat(detail.text()).contains("Patient had a headache");
        assertThat(detail.extraction().path("cases")).hasSize(2);
    }
}
