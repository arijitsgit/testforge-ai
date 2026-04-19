# TestForge AI

AI-powered API test intelligence platform. Upload an OpenAPI spec → get risk-ranked test cases + one-click JUnit code generation.

## Quick Start

### Backend (Java 17 + Spring Boot 3)

```bash
cd backend
export ANTHROPIC_API_KEY=sk-ant-...
mvn spring-boot:run
# Runs on http://localhost:8080
```

### Frontend (React + TypeScript)

```bash
cd frontend
npm install
npm run dev
# Runs on http://localhost:5173
```

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/analyze/spec` | Upload OpenAPI spec → AI generates test cases |
| POST | `/api/analyze/coverage` | Upload spec + test file → detect coverage gaps |
| POST | `/api/generate/test-code` | Generate JUnit 5 + RestAssured code for a test case |
| GET  | `/api/health` | Health check |

## Demo

Upload `sample-specs/fund-subscription-api.yaml` for the competition demo.

## Tech Stack

- **Backend**: Java 17, Spring Boot 3.3, Jackson, swagger-parser
- **AI**: Anthropic Claude API (claude-sonnet-4-6) via direct REST
- **Frontend**: React, TypeScript, Vite 5, Tailwind CSS 4, Axios
