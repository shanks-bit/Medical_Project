# Fixture recording and offline replay

The application supports provider-specific fixtures. With OpenAI selected (default), configure:

```powershell
$env:OPENAI_API_KEY="YOUR_KEY"
$env:OPENAI_MODEL="gpt-4.1-mini"
$env:LLM_PROVIDER="openai"
$env:LLM_MODE="live"
```

Call the extraction endpoint for each core document. Each successful call records the prompt and raw model response under `fixtures/openai/Dxx/`. The API key is never written to fixture files. Failed provider calls are not recorded as successful fixtures.

```powershell
1..8 | ForEach-Object {
  $id = "D{0:D2}" -f $_
  Write-Host "Processing $id"
  curl.exe -f -X POST "http://localhost:8080/api/extractions/$id"
  if ($LASTEXITCODE -ne 0) { Write-Host "Failed: $id. Retry it later." }
  Start-Sleep -Seconds 15
}
```

Stop the Spring Boot server and run `replay.ps1` on Windows or `bash replay.sh` on macOS/Linux. Replay loads `fixtures/<provider>/<document-id>/response.json`, applies the current catalog and current validation/provenance logic, and does not call a model API. It requires no API key or network. Keep `LLM_PROVIDER` set to the provider whose fixtures you want to replay.

The optional Gemini provider can be selected with `LLM_PROVIDER=gemini` and `GEMINI_API_KEY`. Provider and model are stored in fixture metadata so recordings from different providers are not mixed.
