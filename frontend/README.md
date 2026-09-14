# Placement Cell — Frontend

A React (Vite) frontend for the Placement Management System backend in the parent
directory. Talks to the Spring Boot API over REST with JWT auth.

## Project Live Link

```bash
https://placement-management-frontend-vzjd.onrender.com/students
```

## Setup

```bash
cd frontend
npm install
npm run dev
```

The dev server runs at `http://localhost:5173` and proxies any request to `/api/*`
through to `http://localhost:8080` (the Spring Boot app), so you don't need to
configure CORS for local development — just make sure the backend is running.

If your backend runs somewhere else, copy `.env.example` to `.env` and set
`VITE_API_BASE_URL` to its full URL (e.g. `https://api.example.com/api`).

## Building for production

```bash
npm run build
```

Output goes to `frontend/dist/`. Serve it with any static file host (nginx,
Netlify, the `spring-boot-starter-webmvc` static resources folder, etc.) and
set `VITE_API_BASE_URL` at build time to point at your deployed backend.

## How it's organized

- `src/api/client.js` — a small fetch wrapper that attaches the JWT, and
  throws a typed `ApiError` on non-2xx responses.
- `src/context/AuthContext.jsx` — login/register/logout, persists the token
  and user (`username`, `role`) to `localStorage`. Registering as a student
  returns no token (the account is `PENDING`); the register page shows a
  confirmation screen instead of logging in.
- `src/context/StudentProfileContext.jsx` — for a `STUDENT` user, fetches
  `GET /api/students/me` once on load and exposes it as `profile`. This is
  how Jobs/Applications/Interviews/Offers know which student is signed in —
  no manual linking step needed, since the backend links the `User` and
  `Student` records at registration time.
- `src/pages/Approvals.jsx` — staff-only page listing `PENDING` student
  registrations with Approve/Reject actions.
- `src/components/CrudPage.jsx` — a declarative table + modal-form component
  used by most entity pages (Departments, Students, Companies, Jobs,
  Placement Drives, Applications, Users). You hand it columns, form fields,
  and the REST endpoint, and it handles listing, pagination, filtering,
  create/edit/delete.
- `src/pages/Interviews.jsx` and `src/pages/Offers.jsx` are bespoke rather
  than built on `CrudPage`, because those endpoints are scoped to a single
  application (`GET /api/interviews?applicationId=`, `GET
  /api/offers?applicationId=`) rather than listing everything.

## Roles

The UI mirrors the backend's `SecurityConfig`:

- **ADMIN** — full access, plus user account management and approvals.
- **PLACEMENT_OFFICER** — full access to the placement workflow and student
  approvals, no user deletion.
- **STUDENT** — registers with a full profile (name, roll number,
  department, CGPA, graduation year) and waits for approval before they can
  sign in. Once approved: read access to companies/jobs, can apply to jobs,
  and sees only their own applications/interviews/offers.
