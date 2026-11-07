package com.example.caseintake.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationServiceTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final ValidationService service = new ValidationService();

    @Test void acceptsValidCodedValueAndDate() throws Exception {
        var catalog = mapper.readTree("""
          {"fields":[
            {"id":"sex","type":"coded","options":[{"code":"male"},{"code":"female"}]},
            {"id":"date","type":"date"}]}
          """);
        var extraction = mapper.readTree("""
          {"cases":[{"fields":{
            "sex":{"status":"extracted","value":"female","confidence":"high","provenance":[{"quote":"woman"}]},
            "date":{"status":"extracted","value":"2026-03-04","confidence":"medium","provenance":[{"quote":"March 4"}]}
          }}]}
          """);
        service.validateAndNormalize(extraction, catalog);
        assertEquals("extracted", extraction.at("/cases/0/fields/sex/status").asText());
        assertEquals("extracted", extraction.at("/cases/0/fields/date/status").asText());
    }

    @Test void convertsOutOfRangeValueToUnknown() throws Exception {
        var catalog = mapper.readTree("""
          {"fields":[{"id":"age","type":"integer","validation":{"min":0,"max":120}}]}
          """);
        var extraction = mapper.readTree("""
          {"cases":[{"fields":{"age":{"status":"extracted","value":145,"confidence":"high","provenance":[{"quote":"145 years"}]}}}]}
          """);
        service.validateAndNormalize(extraction, catalog);
        assertEquals("unknown", extraction.at("/cases/0/fields/age/status").asText());
        assertFalse(extraction.at("/cases/0/fields/age").has("value"));
    }

    @Test void rejectsInvalidCodedOption() throws Exception {
        var catalog = mapper.readTree("""
          {"fields":[{"id":"sex","type":"coded","options":[{"code":"male"},{"code":"female"}]}]}
          """);
        var extraction = mapper.readTree("""
          {"cases":[{"fields":{"sex":{"status":"extracted","value":"unknown_gender","confidence":"high"}}}]}
          """);
        service.validateAndNormalize(extraction, catalog);
        assertEquals("unknown", extraction.at("/cases/0/fields/sex/status").asText());
    }
}
