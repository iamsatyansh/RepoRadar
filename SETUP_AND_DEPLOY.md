# RepoRadar: Complete Setup and Deployment Guide

> **Important:** The `manus.space` URL is a static frontend preview. It cannot run the Java API or PostgreSQL database, so registration, sign-in, and analysis creation require the Docker or local development workflow below.

## 1. What is included

The project contains a React/TypeScript frontend, a Java 21 Spring Boot API, PostgreSQL/Flyway database migrations, GitHub REST integration, JWT authentication, Docker Compose, CI configuration, and backend tests.

| Directory or file | Purpose |
|---|---|
| `client/` | React frontend and Nginx production image. |
| `backend/` | Spring Boot API, JPA entities, Flyway migration, JWT security, GitHub client, and Java tests. |
| `docker-compose.yml` | Runs PostgreSQL, the API, and the frontend as one local or server deployment. |
| `README.md` | Concise architecture and run reference. |
| `BACKEND_HANDOFF.md` | API contract and backend design notes. |
| `.github/workflows/quality.yml` | Frontend and backend quality checks for GitHub Actions. |

## 2. Prerequisites

For the recommended deployment path, install Docker Engine and Docker Compose v2. For local source development, also install Java 21, Maven 3.9+, Node.js 22, and Corepack/pnpm. Docker provides Compose as the standard way to define and run multi-container applications.[1]

Confirm the tools:

```bash
docker --version
docker compose version
java -version
mvn -version
node --version
corepack --version
```

## 3. Get the project and create secrets

Unzip the archive, open a terminal in the `repo-radar` directory, and create the required environment variables. Do **not** commit these values or share them in a repository.

```bash
unzip reporadar-fullstack-project.zip
cd repo-radar

export REPORADAR_DATABASE_PASSWORD="$(openssl rand -base64 24)"
export REPORADAR_JWT_SECRET="$(openssl rand -base64 32)"
export REPORADAR_GITHUB_TOKEN=""
```

`REPORADAR_JWT_SECRET` must decode to at least 32 bytes for HS256 signing. The GitHub token is optional for basic public-repository development, but a fine-grained, least-privilege read token is recommended in a real deployment to avoid low anonymous API limits. Keep it server-side only; never put it in `VITE_*` variables or React source.[2] [3]

## 4. Recommended path: run the complete product with Docker Compose

From the project root, keep the variables from the previous section in the same terminal and run:

```bash
docker compose up --build -d
docker compose ps
docker compose logs -f api
```

When all services report healthy, open **http://localhost:8081**. Create an account from the registration screen, sign in, and analyze a public URL such as:

```text
https://github.com/spring-projects/spring-boot
```

The browser talks to the Nginx frontend at port `8081`; Nginx forwards `/api/v1/*` to the Spring Boot service, while PostgreSQL stays private inside the Compose network. To check the API health from inside its container, run:

```bash
docker compose exec api curl -fsS http://localhost:8080/actuator/health
```

To stop the stack without deleting data, use `docker compose down`. To remove the PostgreSQL volume as well, which permanently deletes local application data, use `docker compose down -v` only when you explicitly want a clean database.

## 5. Local development path

Use this route when you want hot reload and to debug Java or React code.

### Terminal 1: PostgreSQL

```bash
export REPORADAR_DATABASE_PASSWORD="replace-with-a-local-password"
export REPORADAR_JWT_SECRET="$(openssl rand -base64 32)"
docker compose up postgres
```

### Terminal 2: Spring Boot API

```bash
cd backend
export REPORADAR_DATABASE_URL="jdbc:postgresql://localhost:5432/reporadar"
export REPORADAR_DATABASE_USERNAME="reporadar"
export REPORADAR_DATABASE_PASSWORD="replace-with-a-local-password"
export REPORADAR_JWT_SECRET="use-the-same-value-as-terminal-1"
export REPORADAR_GITHUB_TOKEN="optional-read-only-github-token"
mvn spring-boot:run
```

The API starts on **http://localhost:8080**. Flyway automatically applies the versioned schema migration on startup.

### Terminal 3: React frontend

```bash
cd repo-radar
corepack enable
pnpm install --frozen-lockfile
pnpm dev
```

Open **http://localhost:3000**. Vite proxies `/api` requests to `http://localhost:8080`, so registration works without setting a frontend API URL locally.

## 6. Verify the build and tests

Run these commands from the project root:

```bash
pnpm check
pnpm build

cd backend
mvn test
mvn -DskipTests package
```

The portable Java test suite covers authentication service behavior, MVC validation and security boundaries, plus GitHub adapter request headers, caching, payload mapping, not-found, rate-limit, transport, and upstream-error behavior. The PostgreSQL `*IT` test uses Testcontainers. If it fails with Docker Engine 29’s minimum client API error, run it in a compatible Docker/Testcontainers environment or update the Testcontainers dependency when an upstream-compatible release is available.

## 7. Deploy to a server or cloud VM

Use a Linux server with Docker, a persistent disk for PostgreSQL, and an HTTPS reverse proxy or load balancer. Copy the unzipped source to the server, set secrets in the server’s secret manager or protected shell environment, and run:

```bash
export REPORADAR_DATABASE_PASSWORD="a-long-unique-production-password"
export REPORADAR_JWT_SECRET="a-base64-secret-produced-by-openssl-rand-base64-32"
export REPORADAR_GITHUB_TOKEN="your-server-side-fine-grained-token"
export FRONTEND_PORT=8081
docker compose up --build -d
```

Place your HTTPS proxy in front of the frontend service and route your domain to port `8081`. Because the production frontend uses the same-origin `/api/v1` path, the Nginx container handles API forwarding automatically. Configure database backups, monitoring, HTTPS certificates, and secret rotation before exposing the product publicly.

If you deploy the frontend and API on separate domains, rebuild the frontend with `VITE_API_BASE_URL=https://api.example.com/api/v1` and set `CORS_ALLOWED_ORIGINS=https://app.example.com` on the API. Frontend environment values are bundled at build time, so changing this value requires a new frontend image build.

## 8. Troubleshooting

| Symptom | Likely cause | Resolution |
|---|---|---|
| Registration says the account API is not deployed | You opened the static `manus.space` preview. | Start the Compose stack or deploy the Spring Boot API and PostgreSQL services. |
| Registration returns `401` or `403` | Token is invalid or the request is accessing another user’s data. | Sign in again; verify the browser is using the same API deployment. |
| Analysis returns `429` | GitHub’s API rate limit was reached. | Configure `REPORADAR_GITHUB_TOKEN` server-side and retry later. |
| API fails at startup | Database variables or JWT secret are missing/invalid. | Read `docker compose logs api`; confirm the variables in Section 3. |
| Frontend loads but APIs fail on a server | Reverse proxy or CORS is misconfigured. | Keep the Compose same-origin path, or set both `VITE_API_BASE_URL` and `CORS_ALLOWED_ORIGINS` correctly. |

## References

[1] [Docker Compose documentation](https://docs.docker.com/compose/)

[2] [GitHub REST API rate limits](https://docs.github.com/en/rest/using-the-rest-api/rate-limits-for-the-rest-api)

[3] [GitHub guidance on creating fine-grained personal access tokens](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens)
