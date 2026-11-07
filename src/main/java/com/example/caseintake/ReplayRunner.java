package com.example.caseintake;

import com.example.caseintake.document.DocumentRepository;
import com.example.caseintake.extraction.ExtractionService;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

/** Offline command-line entry point. It never starts an HTTP server. */
public class ReplayRunner {
    public static void main(String[] args) {
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(CaseIntakeApplication.class)
                .web(WebApplicationType.NONE)
                .properties("app.llm.mode=replay")
                .run(args)) {
            DocumentRepository documents = context.getBean(DocumentRepository.class);
            ExtractionService extraction = context.getBean(ExtractionService.class);
            for (String id : documents.findAllIds()) {
                extraction.extractAndSave(id);
                System.out.println("Replayed " + id);
            }
            System.out.println("Replay completed successfully.");
        } catch (Exception e) {
            System.err.println("Replay failed: " + e.getMessage());
            System.exit(1);
        }
    }
}
