# Case Intake - starter project

## Requirements
- Java 21
- Maven 3.9+ (or add the Maven Wrapper)
- Node.js 20+ (for the React frontend, added later)

## Run the backend
Run commands from this project root (the folder containing `pom.xml`):

```bash
mvn test
mvn spring-boot:run
```

If you use IntelliJ IDEA, open this folder, set Project SDK to Java 21, reload Maven, then run `CaseIntakeApplication`.

## Available endpoints
- `GET http://localhost:8080/api/catalog` - returns the dynamic field catalog.
- `GET http://localhost:8080/api/documents` - lists D01-D08 text documents and any committed extraction summary.
- `GET http://localhost:8080/api/documents/D01` - returns the original text and extraction JSON (null until `out/D01.json` exists).

The catalog, documents and output paths can be configured in `src/main/resources/application.yml`.

## Current milestones
- Spring Boot application on Java 21.
- Dynamic catalog loading; no field IDs hardcoded in Java.
- Document listing and document detail APIs.
- Unit tests for catalog and document repository/service behavior.

Next: extraction models, LLM interface, prompt builder, evidence offsets, validation, fixtures/replay, and React UI.

Never commit a real LLM API key. Use environment variables for secrets.

## Milestone: Gemini extraction

Set the API key in the environment before starting Spring Boot:

PowerShell:
```powershell
$env:OPENAI_API_KEY="your-key-here"
$env:OPENAI_MODEL="gpt-4.1-mini"
$env:LLM_PROVIDER="openai"
```

Then restart the application and call:
```powershell
curl.exe -X POST http://localhost:8080/api/extractions/D01
```

The result is saved to `out/D01.json`. Never put the API key in source code or commit it.
The endpoint is a development trigger; the final deliverable also needs a batch command and
fixture replay mode, which are implemented in later milestones.
