package com.example.caseintake.document;

import java.util.List;

public record DocumentSummary(
        String id,
        String title,
        int cases,
        List<String> flags
) { }
