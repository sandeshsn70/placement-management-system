# Placement Management System

A full-stack app for managing campus placements: departments, students,
companies, jobs, placement drives, applications, interviews, offers, and
JWT-based login/roles.

[`https://placement-management-frontend-vzjd.onrender.com/students`]


- **`/` (this directory)** — Spring Boot 4.1.1 REST API. See below.
- **`/frontend`** — React (Vite) single-page app that consumes the API. See
  [`frontend/README.md`](frontend/README.md) for setup.

Want to put this online for free? Two walkthroughs, pick one:
- [`DEPLOYMENT.md`](DEPLOYMENT.md) — Render + Aiven MySQL, zero server
  management, cold starts after idle.
- [`DEPLOYMENT_GCP.md`](DEPLOYMENT_GCP.md) — one GCP Always-Free e2-micro
  VM running everything via Docker Compose, deployed with GitHub Actions.
  No cold starts, more one-time setup.

## Running the full stack locally

```powershell
# Terminal 1 — backend (http://localhost:8080)
.\mvnw spring-boot:run

# Terminal 2 — frontend (http://localhost:5173)
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173` and register an account — pick `ADMIN` for full
access while you're setting things up (student registrations need approval
first; see below). The frontend's dev server proxies `/api/*` to the backend,
so no extra CORS setup is needed locally — though `SecurityConfig` already
allows any origin if you serve them separately in production.

---

## Stack

- Java 17 (compiled with Maven `release=17`), Spring Boot 4.1.1
- Spring Data JPA + Hibernate, MySQL 8
- Spring Security 7 with stateless JWT auth (JJWT 0.12.6)
- Bean Validation (`spring-boot-starter-validation`)
- Maven (wrapper included: `mvnw` / `mvnw.cmd`)

## Running it

1. Make sure MySQL is running and the `placement_management` schema exists (the
   datasource config in `application.properties` is unchanged from what you had
   working).
2. Optionally override the JWT signing key with an environment variable before
   starting the app (a working default is baked in for local dev):
   ```powershell
   $env:JWT_SECRET = "some-long-random-string-at-least-32-characters"
   ```
3. Build and run:
   ```powershell
   .\mvnw clean
   .\mvnw spring-boot:run
   ```
   `spring.jpa.hibernate.ddl-auto=update` will create all tables automatically on
   first startup.

## Student approval workflow

Registering as `STUDENT` requires the full academic profile (name, roll
number, department, CGPA, graduation year) up front, and creates the account
with `status = PENDING`. A pending account **cannot log in** — the API
returns a 403 with a clear message — until a `PLACEMENT_OFFICER` or `ADMIN`
approves it from `GET /api/users/pending` / `PATCH /api/users/{id}/approve`
(or `.../reject`). Registering as `ADMIN` or `PLACEMENT_OFFICER` is
unaffected and gets a token immediately, so bootstrap your first admin
account that way.

Once approved, the student's own profile is available at `GET
/api/students/me` (resolved from their JWT identity), which is how the
frontend scopes their applications/interviews/offers/job-eligibility without
needing a separate linking step.

## Auth flow

Every endpoint except `/api/auth/**` and `GET /api/departments` requires a
`Bearer` token.

```powershell
# Register as a student (submitted for approval, no token returned)
curl -X POST http://localhost:8080/api/auth/register `
  -H "Content-Type: application/json" `
  -d '{"username":"jdoe","email":"jdoe@example.com","password":"password123","role":"STUDENT","fullName":"Jane Doe","rollNumber":"CSE001","departmentId":1,"cgpa":8.5,"graduationYear":2027}'

# Register as an admin (token returned immediately)
curl -X POST http://localhost:8080/api/auth/register `
  -H "Content-Type: application/json" `
  -d '{"username":"admin1","email":"admin1@example.com","password":"password123","role":"ADMIN"}'

# Login
curl -X POST http://localhost:8080/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{"username":"admin1","password":"password123"}'

# Use the returned token
curl http://localhost:8080/api/departments `
  -H "Authorization: Bearer <token>"
```

## Authorization rules

| Access | Rule |
|---|---|
| `POST /api/auth/**` | Public |
| `GET /api/departments` (list only) | Public — needed for the registration form |
| `GET` on departments/{id}, students, companies, jobs, drives, applications, interviews, offers | Any authenticated user |
| `POST /api/applications` (applying to a job) | Any authenticated user |
| `GET /api/users/pending`, `PATCH /api/users/{id}/approve`, `PATCH /api/users/{id}/reject` | `ADMIN` or `PLACEMENT_OFFICER` |
| Everything else that creates/updates/deletes those resources | `ADMIN` or `PLACEMENT_OFFICER` |
| `/api/users/**` (other than the above) | `ADMIN` only |

**Known simplification:** authorization is role-based, not record-owner-based — a
`STUDENT` can currently `GET` any application, not only their own. Adding
per-record ownership checks (e.g. via `@PreAuthorize`) is a natural next step.

## Endpoints

| Module | Base path | Notes |
|---|---|---|
| Auth | `/api/auth` | `POST /register`, `POST /login` |
| Departments | `/api/departments` | CRUD, no pagination; `GET` (list) is public |
| Students | `/api/students` | CRUD, paginated, `?departmentId=` filter; `GET /me` returns the caller's own profile |
| Companies | `/api/companies` | CRUD, no pagination |
| Jobs | `/api/jobs` | CRUD, paginated, `?companyId=` / `?jobType=` / `?maxCgpaRequired=` filters |
| Placement Drives | `/api/placement-drives` | CRUD, paginated, `?companyId=` filter |
| Applications | `/api/applications` | `POST` to apply, `PATCH /{id}/status`, `?studentId=` / `?jobId=` filters |
| Interviews | `/api/interviews` | `POST` to schedule, `PATCH /{id}/result`, `?applicationId=` filter |
| Offers | `/api/offers` | `POST` to create, `PATCH /{id}/status`, `?applicationId=` filter |
| Users | `/api/users` | `GET` (paginated), `DELETE` (`ADMIN`); `GET /pending`, `PATCH /{id}/approve`, `PATCH /{id}/reject` (`ADMIN`/`PLACEMENT_OFFICER`) |

Pagination/sorting on any paginated list: `?page=0&size=20&sort=fullName,asc`.

## Error responses

All errors come back as JSON via a centralized `@RestControllerAdvice`:

```json
{
  "timestamp": "2026-09-13T10:15:30",
  "status": 404,
  "error": "Not Found",
  "message": "Student not found with id: 42",
  "path": "/api/students/42"
}
```

Validation failures (400) additionally include a `fieldErrors` map.

## Tests

```powershell
.\mvnw test
```

Includes the original `@SpringBootTest` context-load test plus Mockito-based unit
tests for `DepartmentServiceImpl`, `ApplicationServiceImpl`, and `JwtService` —
a representative sample, not full coverage of every module.

## Known simplifications / good next steps

- No Flyway/Liquibase — schema is Hibernate-managed (`ddl-auto=update`). If
  you're pulling these changes into a database that already has data, the new
  `users.status` (`NOT NULL`) and `students.user_id` columns need either a
  fresh schema or a manual `ALTER TABLE` with a backfilled default, since
  `ddl-auto=update` won't backfill existing rows itself.
- Config (DB credentials, JWT secret, port) is environment-variable driven
  with local-friendly defaults — see `application.properties` and
  `DEPLOYMENT.md`.
- A `Dockerfile` is included for container-based hosts (Render, Railway,
  Fly.io); see `DEPLOYMENT.md` for a free walkthrough.
- Authorization is role-based only (see above).
