package com.example.caseintake.llm;

/** Provider-neutral LLM interface. documentId scopes deterministic fixture recordings. */
public interface LlmClient {
    String generate(String documentId, String prompt);
    String modelName();
}
