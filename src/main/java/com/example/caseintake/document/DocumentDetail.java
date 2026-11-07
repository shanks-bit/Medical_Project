package com.example.caseintake.document;

import com.fasterxml.jackson.databind.JsonNode;

public record DocumentDetail(
        String id,
        String text,
        JsonNode extraction
) { }
