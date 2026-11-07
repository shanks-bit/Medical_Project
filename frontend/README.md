# Case Intake Review UI (React 19)

React 19 + TypeScript + Vite frontend for the Case Intake Spring Boot API.

## Requirements
- Node.js 20.19+ or 22.12+
- npm
- Spring Boot backend running at `http://localhost:8080`

## Run locally

```bash
cd frontend
npm install
npm run dev
```

Open http://localhost:5173. Vite proxies `/api` requests to the Spring Boot backend on port 8080.

## Build and type-check

```bash
npm run build
npx tsc --noEmit
npm test
```

## API used
- `GET /api/catalog`
- `GET /api/documents`
- `GET /api/documents/{id}`

The UI reads sections, field labels, field types and options from the catalog. It does not hardcode catalog field IDs. Provenance positions are interpreted as Unicode code-point offsets and converted to JavaScript UTF-16 offsets for rendering.
