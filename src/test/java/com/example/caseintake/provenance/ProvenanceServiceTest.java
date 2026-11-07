package com.example.caseintake.provenance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProvenanceServiceTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final ProvenanceService service = new ProvenanceService();

    @Test
    void attachesUnicodeCodePointOffsets() throws Exception {
        String source = "😀 Patient developed headache.";
        ObjectNode result = (ObjectNode) mapper.readTree("""
                {"cases":[{"fields":{"reaction":{"status":"extracted","value":"headache",
                "confidence":"high","provenance":[{"quote":"Patient developed headache."}]}}}]}
                """);

        service.attachOffsets(result, source);

        var provenance = result.path("cases").path(0).path("fields").path("reaction").path("provenance").path(0);
        assertEquals(2, provenance.path("start").asInt());
        assertEquals(29, provenance.path("end").asInt());
    }

    @Test
    void marksUnverifiableQuoteUnknown() throws Exception {
        ObjectNode result = (ObjectNode) mapper.readTree("""
                {"cases":[{"fields":{"reaction":{"status":"extracted","value":"rash",
                "confidence":"high","provenance":[{"quote":"rash on arm"}]}}}]}
                """);

        service.attachOffsets(result, "Patient reported a rash.");

        var field = result.path("cases").path(0).path("fields").path("reaction");
        assertEquals("unknown", field.path("status").asText());
        assertFalse(field.has("value"));
    }
}
