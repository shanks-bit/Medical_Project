#!/usr/bin/env bash
set -euo pipefail
# Run from repository root. Replay reads fixtures and does not call Gemini.
CATALOG_PATH="${1:-catalog/field_catalog.json}"
mvn -q -DskipTests package
mvn -q -DincludeScope=runtime -Dmdep.outputFile=target/classpath.txt dependency:build-classpath
CLASSPATH="target/classes:$(cat target/classpath.txt)"
java -cp "$CLASSPATH" com.example.caseintake.ReplayRunner "--app.catalog-path=$CATALOG_PATH"
