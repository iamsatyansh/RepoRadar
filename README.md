# RepoRadar

RepoRadar is a GitHub Repository Intelligence Platform built with a React/TypeScript frontend and a Java 21 Spring Boot API. It accepts a canonical public GitHub repository URL, captures a point-in-time snapshot of repository metadata, languages, bounded commit activity, contributors, and deterministic engineering observations, then stores the evidence in PostgreSQL.

## Local development

The frontend runs through Vite on port `3000`; its `/api` requests proxy to Spring Boot on port `8080`. The API reads the application-specific `REPORADAR_DATABASE_*`, `REPORADAR_JWT_SECRET`, and optional `REPORADAR_GITHUB_TOKEN` variables. Use a base64-encoded secret of at least 32 bytes for JWT signing. The GitHub token should be a least-privileged fine-grained token with read-only public repository metadata access; it is optional for development but recommended to avoid unauthenticated API limits.

```bash
# terminal one: PostgreSQL
export REPORADAR_DATABASE_PASSWORD=replace-me
docker compose up postgres

# terminal two: API
cd backend
export REPORADAR_DATABASE_URL=jdbc:postgresql://localhost:5432/reporadar
export REPORADAR_DATABASE_USERNAME=reporadar
export REPORADAR_JWT_SECRET="base64-encoded-32-byte-secret"
export REPORADAR_GITHUB_TOKEN="optional-read-only-token"
mvn spring-boot:run

# terminal three: frontend
pnpm dev
```

## Containerized launch

Set `REPORADAR_DATABASE_PASSWORD` and `REPORADAR_JWT_SECRET`, then run `docker compose up --build`. The browser is served on `http://localhost:8081`, while Nginx routes `/api` to the private Spring Boot service. PostgreSQL is not published to the host.

> This downloadable archive bundles the generated Signal Ledger images under `client/public/assets`, so its Docker image is self-contained. The managed project preview uses its own project-storage paths separately.

## Verification

Run `pnpm check && pnpm build` for the frontend and `mvn -DskipTests package` in `backend` for the API. The repository contains a PostgreSQL Testcontainers integration test. The current sandbox’s Docker Engine 29 API is incompatible with the currently released Testcontainers Java client; run that integration test on a Docker Engine version supported by Testcontainers or update the dependency when an upstream compatible module release is available.

## Production boundary

Use HTTPS at the ingress layer, a non-default 256-bit JWT signing secret stored in a secret manager, a GitHub token that never reaches the browser, managed PostgreSQL backups, and an image registry with vulnerability scanning. The managed WebDev frontend does not host the Java/PostgreSQL Docker stack; deploy the Compose services to an environment with Docker or migrate the API/database to managed infrastructure.
