# RepoRadar Spring Boot Backend Handoff

> **Current delivery status:** the React frontend is implemented and calls the versioned `/api/v1` contract defined below. This static frontend workspace does not permit adding a Java server, database schema, or server routes, so the Spring Boot and PostgreSQL implementation remains intentionally unclaimed rather than replaced with mock data.

## Integration boundary

The frontend reads `VITE_API_BASE_URL`, defaulting to `/api/v1`. It sends JSON requests and, after authentication, supplies `Authorization: Bearer <JWT>`. The UI is designed to visibly handle loading, validation, authentication failure, upstream GitHub failure, rate limiting, missing data, and empty saved-record states.

| Endpoint | Request | Expected response used by the UI |
|---|---|---|
| `POST /auth/register` | `username`, `email`, `password` | `{ accessToken, user }` |
| `POST /auth/login` | `email`, `password` | `{ accessToken, user }` |
| `GET /auth/me` | JWT | `{ id, username, email }` |
| `POST /analyses` | `{ repositoryUrl }` | Complete `Analysis` record |
| `GET /analyses` | JWT | `Analysis[]` for the signed-in user |
| `GET /analyses/{id}` | JWT | Complete `Analysis` record owned by the user |

The client expects errors to use a stable JSON problem shape, preferably including `status`, `code`, and `detail`. For example, invalid URLs should return `400`, an invalid or expired JWT should return `401`, another user’s record should return `403` or `404` according to the chosen non-disclosure policy, GitHub throttling should surface as `429`, and temporary upstream problems should use `503`.

## DTO contract

```text
Analysis {
  id: string
  status: PENDING | RUNNING | COMPLETED | FAILED
  requestedAt: ISO-8601 timestamp
  completedAt?: ISO-8601 timestamp
  analyzedCommitSha?: string
  repository: RepositorySummary
  languages: LanguageStat[]
  contributors: Contributor[]
  recentCommits: CommitSnapshot[]
  activity: ActivityPoint[]
  insights: Insight[]
}
```

`RepositorySummary` contains the canonical `owner`, `name`, `fullName`, `htmlUrl`, description, stars, forks, open issue count, watcher count, primary language, default branch, pushed timestamp, and creation timestamp. `LanguageStat` contains the language name, raw bytes, and a backend-calculated percentage. A derived observation must include a stable code, severity, title, and explanation; the UI does not require or display fabricated “AI” insights.

## Spring Boot implementation sequence

| Stage | Spring Boot responsibility | Completion criteria |
|---:|---|---|
| 1 | Create the Java 21 Maven project with Web, Validation, Security, Data JPA, PostgreSQL, Flyway, and test dependencies. | Application starts with environment-driven configuration. |
| 2 | Implement `User`, password hashing, registration, login, JWT creation and validation, plus ownership enforcement. | Protected endpoint tests pass for valid, missing, and cross-user tokens. |
| 3 | Add Flyway migrations for users, repositories, analyses, languages, contributors, commits, and insights. | Migrations run against PostgreSQL in Testcontainers. |
| 4 | Create a GitHub REST adapter for repository metadata, languages, and a bounded recent-commit query. | DTO mapping and upstream-error translation are unit tested. |
| 5 | Implement the analysis application service and deterministic insight rules. | A complete analysis snapshot can be stored and returned. |
| 6 | Add Caffeine caching and ETag support; later add Redis only for horizontally scaled services. | Repeat requests avoid unnecessary GitHub calls. |
| 7 | Add Docker, Compose, GitHub Actions, health checks, and structured logging. | The full Java/PostgreSQL stack is reproducible outside this static workspace. |

## Suggested relational model

`users` owns many `analyses`. `repositories` is deduplicated by GitHub repository ID or canonical `owner/name`; it owns many time-stamped `analyses`. Each `analysis` owns language rows, contributor snapshots, commit snapshots, and deterministic insight rows. This snapshot approach preserves the observation time and avoids silently rewriting past reports when a repository changes.

Use a unique index for user email and username, unique `(analysis_id, language_name)` and `(analysis_id, sha)` composite constraints, and indexes on `(requested_by, requested_at)` and `(repository_id, requested_at)`. Store only product-relevant API properties; a limited `jsonb` raw-response column may be useful for troubleshooting, but it should not become the default model.

## GitHub adapter notes

The relevant GitHub operations are repository metadata, languages, and bounded recent commits. GitHub documents standard REST headers such as `Accept: application/vnd.github+json`, an API version header, and bearer-token authentication when a server-side token is configured.[1] The repository payload contains useful product fields such as topics, default branch, timestamps, stars, forks, watchers, issues, and the primary language.[1] The commits API supports pagination and an explicit `per_page` bounded at 100, so the MVP should request a limited first page rather than attempting an unbounded history.[2]

Repository-statistics calls are a later enhancement because GitHub may return `202 Accepted` while computing statistics. If introduced, model the analysis as asynchronous and retry safely instead of presenting absent statistics as zeroes.[3] GitHub also documents REST rate limits and supports conditional requests; persist ETags for upstream cache entries and translate a `304 Not Modified` result into a cache refresh rather than a user-facing failure.[4] [5]

## Verification matrix

| Test level | Tooling | Essential checks |
|---|---|---|
| Domain and service unit tests | JUnit 5, Mockito | URL parsing, percentages, insight rules, ownership, GitHub exception mapping. |
| MVC/security tests | Spring Boot test support | Validation, problem responses, JWT boundaries, endpoint status codes. |
| Persistence integration | Testcontainers PostgreSQL | Migrations, entity mappings, uniqueness, saved-record queries. |
| GitHub adapter tests | Controlled HTTP server or mocked transport | Headers, pagination, `404`, `202`, `304`, `429`, timeout, and `5xx` behavior. |
| End-to-end backend tests | Spring Boot + Testcontainers | Register, authenticate, create analysis, retrieve owned record, list records. |

Testcontainers provides JUnit Jupiter support for managing containers in Java integration tests.[6]

## References

[1] [GitHub REST API endpoints for repositories](https://docs.github.com/en/rest/repos/repos?apiVersion=2026-03-10)

[2] [GitHub REST API endpoints for commits](https://docs.github.com/en/rest/commits/commits?apiVersion=2026-03-10)

[3] [GitHub REST API endpoints for repository statistics](https://docs.github.com/en/rest/metrics/statistics?apiVersion=2026-03-10)

[4] [GitHub REST API rate limits](https://docs.github.com/en/rest/using-the-rest-api/rate-limits-for-the-rest-api?apiVersion=2026-03-10)

[5] [GitHub REST API best practices](https://docs.github.com/en/rest/using-the-rest-api/best-practices-for-using-the-rest-api?apiVersion=2026-03-10)

[6] [Testcontainers JUnit 5 integration](https://java.testcontainers.org/test_framework_integration/junit_5/)
