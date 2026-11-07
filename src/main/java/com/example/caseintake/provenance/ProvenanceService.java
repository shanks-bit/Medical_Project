package com.example.caseintake.provenance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

@Service
public class ProvenanceService {
    /** Adds checker-compatible Unicode code-point offsets. Only exact source quotes are accepted. */
    public void attachOffsets(JsonNode extraction, String sourceText) {
        JsonNode cases = extraction.path("cases");
        if (!cases.isArray()) return;

        for (JsonNode caseNode : cases) {
            JsonNode fields = caseNode.path("fields");
            if (!(fields instanceof ObjectNode fieldsObject)) continue;

            fieldsObject.fields().forEachRemaining(entry -> {
                if (!(entry.getValue() instanceof ObjectNode field)) return;
                if (!"extracted".equals(field.path("status").asText())) return;

                JsonNode provenance = field.path("provenance");
                if (!(provenance instanceof ArrayNode provenanceArray) || provenanceArray.isEmpty()) {
                    makeUnknown(field, "No supporting quote was returned by the model.");
                    return;
                }

                for (JsonNode item : provenanceArray) {
                    if (!(item instanceof ObjectNode quoteNode)) continue;
                    String quote = quoteNode.path("quote").asText("");
                    int utf16Start = quote.isBlank() ? -1 : sourceText.indexOf(quote);
                    if (utf16Start < 0) {
                        makeUnknown(field, "The model quote could not be matched exactly to the source text.");
                        return;
                    }
                    int utf16End = utf16Start + quote.length();
                    int codePointStart = sourceText.codePointCount(0, utf16Start);
                    int codePointEnd = codePointStart + quote.codePointCount(0, quote.length());
                    quoteNode.put("start", codePointStart);
                    quoteNode.put("end", codePointEnd);
                }
            });
        }
    }

    private void makeUnknown(ObjectNode field, String reason) {
        field.remove("value");
        field.remove("confidence");
        field.remove("provenance");
        field.put("status", "unknown");
        field.put("note", reason);
    }
}
