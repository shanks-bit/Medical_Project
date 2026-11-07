# Case Intake Exercise

## OpenAI setup (default)

Set these environment variables before starting Spring Boot:

```powershell
$env:OPENAI_API_KEY="YOUR_OPENAI_API_KEY"
$env:OPENAI_MODEL="gpt-4.1-mini"
$env:LLM_PROVIDER="openai"
$env:LLM_MODE="live"
```

Create an API key at https://platform.openai.com/api-keys. ChatGPT subscriptions and API billing are separate. The application uses the OpenAI Responses API and requests JSON output. Never commit API keys.

Start the app and extract a document:

```powershell
mvn spring-boot:run
curl.exe -X POST http://localhost:8080/api/extractions/D01
```

Live model exchanges are recorded under `fixtures/<provider>/<DOCUMENT_ID>/`. For example, `fixtures/openai/D01/request.json` and `response.json`. Credentials are not stored.

## Offline replay

Stop the web application, then run `replay.ps1` on Windows or `bash replay.sh` on macOS/Linux. Replay uses the selected provider's fixtures and makes no network calls. For OpenAI fixtures, keep `LLM_PROVIDER=openai` (the default). Replay does not require an API key.

## Optional Gemini provider

Set `LLM_PROVIDER=gemini`, `GEMINI_API_KEY`, and optionally `GEMINI_MODEL=gemini-2.5-flash`. The provider implementation is selected through the `LlmClient` interface. OpenAI is the default.

## Tests

```powershell
mvn test
```

See `ASSIGNMENT.md`, `GLOSSARY.md`, and `STEP5-FIXTURES.md` for the exercise requirements and domain context.

## React 19 review UI

The React 19 + TypeScript application is in `frontend/`.

```bash
cd frontend
npm install
npm run dev
```

Start the Spring Boot API separately on port 8080. The Vite development server runs on port 5173 and proxies `/api` calls to the backend. See `frontend/README.md` for build and type-check commands.
