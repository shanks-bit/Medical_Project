package com.example.caseintake.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/** Validates model-produced values against the live field catalog. */
@Service
public class ValidationService {

    public void validateAndNormalize(JsonNode extraction, JsonNode catalog) {
        JsonNode catalogFields = catalog.path("fields");
        JsonNode cases = extraction.path("cases");
        if (!catalogFields.isArray() || !cases.isArray()) return;

        for (JsonNode caseNode : cases) {
            if (!(caseNode.path("fields") instanceof ObjectNode caseFields)) continue;
            for (JsonNode definition : catalogFields) {
                String id = definition.path("id").asText("");
                if (id.isBlank()) continue;
                JsonNode result = caseFields.get(id);
                if (!(result instanceof ObjectNode fieldResult)) {
                    caseFields.set(id, unknown("The model did not return this catalog field."));
                    continue;
                }
                String status = fieldResult.path("status").asText("");
                if ("unknown".equals(status) || "conflict".equals(status)) {
                    fieldResult.remove("value");
                    fieldResult.remove("confidence");
                    fieldResult.remove("provenance");
                    if (!"unknown".equals(status) && !"conflict".equals(status)) {
                        caseFields.set(id, unknown("Invalid status returned by model."));
                    }
                    continue;
                }
                if (!"extracted".equals(status)) {
                    caseFields.set(id, unknown("Invalid or missing extraction status."));
                    continue;
                }
                String problem = validateValue(fieldResult.path("value"), definition);
                if (problem == null && !isConfidenceValid(fieldResult.path("confidence"))) {
                    problem = "Missing or invalid confidence; expected high, medium or low.";
                }
                if (problem != null) caseFields.set(id, unknown(problem));
            }
        }
    }

    private String validateValue(JsonNode value, JsonNode definition) {
        if (value == null || value.isMissingNode() || value.isNull()) return "Extracted field has no value.";
        String type = definition.path("type").asText("");
        boolean validType = switch (type) {
            case "string" -> value.isTextual();
            case "integer" -> value.isIntegralNumber();
            case "number" -> value.isNumber();
            case "date" -> value.isTextual() && isIsoDate(value.asText());
            case "coded" -> value.isTextual();
            case "coded_multi", "string_list" -> value.isArray();
            default -> false;
        };
        if (!validType) return "Value does not match catalog type '" + type + "'.";

        if ("coded".equals(type) && !isAllowedCode(value.asText(), definition))
            return "Value is not one of the catalog's allowed options.";
        if ("coded_multi".equals(type)) {
            if (!allStrings(value)) return "Every coded_multi value must be a string.";
            Set<String> seen = new HashSet<>();
            for (JsonNode item : value) {
                if (!isAllowedCode(item.asText(), definition)) return "Value '" + item.asText() + "' is not an allowed option.";
                if (!seen.add(item.asText())) return "Duplicate values are not allowed.";
            }
            if (seen.contains("none") && seen.size() > 1) return "The 'none' option must be used alone.";
        }
        if ("string_list".equals(type) && !allStrings(value))
            return "Every string_list item must be a string.";

        JsonNode validation = definition.path("validation");
        if (validation.has("pattern") && value.isTextual()) {
            try {
                if (!Pattern.matches(validation.path("pattern").asText(), value.asText()))
                    return "Value does not match the catalog pattern.";
            } catch (PatternSyntaxException ex) {
                return "Catalog contains an invalid validation pattern.";
            }
        }
        if (value.isNumber()) {
            double number = value.asDouble();
            if (validation.has("min") && number < validation.path("min").asDouble())
                return "Value is below the catalog minimum.";
            if (validation.has("max") && number > validation.path("max").asDouble())
                return "Value exceeds the catalog maximum.";
        }
        return null;
    }

    private boolean isAllowedCode(String code, JsonNode definition) {
        JsonNode options = definition.path("options");
        if (!options.isArray()) return false;
        for (JsonNode option : options) if (code.equals(option.path("code").asText())) return true;
        return false;
    }

    private boolean allStrings(JsonNode array) {
        for (JsonNode item : array) if (!item.isTextual()) return false;
        return true;
    }

    private boolean isIsoDate(String value) {
        try { return LocalDate.parse(value).toString().equals(value); }
        catch (DateTimeParseException ex) { return false; }
    }

    private boolean isConfidenceValid(JsonNode confidence) {
        return confidence.isTextual() && Set.of("high", "medium", "low").contains(confidence.asText());
    }

    private ObjectNode unknown(String note) {
        ObjectNode result = com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.objectNode();
        result.put("status", "unknown");
        result.put("note", note);
        return result;
    }
}
