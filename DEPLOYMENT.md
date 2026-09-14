# Deploying for free

This walks through putting the whole app online without paying anything:
a free MySQL database, a free host for the Spring Boot API, and a free
static host for the React frontend. Verified against each provider's
current (September 2026) free-tier terms — no credit card required
anywhere in this path.

| Piece | Where | Cost |
|---|---|---|
| MySQL database | [Aiven](https://aiven.io/free-mysql-database) | Free forever (1GB RAM/storage, auto-pauses when idle, wakes on next connection) |
| Backend (Spring Boot, Docker) | [Render](https://render.com) | Free web service (512MB RAM, sleeps after 15 min idle, ~750 hrs/month) |
| Frontend (React static build) | Render Static Site (or Netlify) | Free |

This is fine for a class project, portfolio piece, or small placement
cell — not for production traffic. The honest limitations, upfront:

- **Cold starts.** The backend spins down after 15 minutes with no
  requests. The first request after that takes 30–60 seconds to wake it
  back up. An uptime pinger (e.g. [UptimeRobot](https://uptimerobot.com),
  free) hitting your API every 10 minutes keeps it warm during hours you
  care about.
- **Database pausing.** Aiven's free MySQL pauses after a period of
  inactivity too. It resumes automatically on the next connection, which
  adds a similar delay the first time.
- **No horizontal scaling, no SLA.** Fine for demos and small user bases.

If you outgrow this, the same Docker image and `application.properties`
work unchanged on any paid tier of the same providers, or on Railway /
Fly.io / a VPS — just point the environment variables at the new database.

---

## 1. Create the free MySQL database (Aiven)

1. Sign up at [aiven.io](https://aiven.io) (no card needed).
2. Create a new service → **MySQL** → Free plan → pick a region close to
   where you'll run the backend.
3. Once it's up, open the service and copy the connection details:
   **Host**, **Port**, **User**, **Password**, **Database name** (default
   is usually `defaultdb`).
4. Build your JDBC URL:
   ```
   jdbc:mysql://<host>:<port>/<database>?useSSL=true&requireSSL=true
   ```
   Aiven requires SSL — the `useSSL=true&requireSSL=true` query params
   above enable it (the MySQL driver already bundled in this project
   supports this out of the box).

Keep this tab open — you'll paste these into Render's environment
variables next.

## 2. Deploy the backend (Render, Docker)

The repo already includes a `Dockerfile` and `application.properties`
wired to read everything from environment variables, so no code changes
are needed.

1. Push this project to a GitHub repository (Render deploys from Git).
2. On [render.com](https://render.com), **New → Web Service**, connect
   the repo, and set:
   - **Root directory**: leave blank (the `Dockerfile` is at the repo root)
   - **Runtime**: Docker
   - **Instance type**: Free
3. Add these environment variables (Render → your service → Environment):

   | Key | Value |
   |---|---|
   | `SPRING_DATASOURCE_URL` | the JDBC URL from step 1 |
   | `SPRING_DATASOURCE_USERNAME` | Aiven username (usually `avnadmin`) |
   | `SPRING_DATASOURCE_PASSWORD` | Aiven password |
   | `JWT_SECRET` | any long random string (e.g. generate one with `openssl rand -base64 48`) |
   | `SPRING_JPA_HIBERNATE_DDL_AUTO` | `update` |

   Render sets `PORT` for you automatically — `application.properties`
   already reads it (`server.port=${PORT:8080}`), so you don't need to
   set it yourself.
4. Deploy. Watch the build logs; the first Docker build (Maven download +
   compile) takes a few minutes. Once live, note the service URL, e.g.
   `https://placement-api.onrender.com`.
5. Sanity check: `https://placement-api.onrender.com/api/departments`
   should return `[]` (empty list, since nothing's been added yet) rather
   than an error.

## 3. Deploy the frontend (Render Static Site)

1. On Render, **New → Static Site**, same GitHub repo.
2. Set:
   - **Root directory**: `frontend`
   - **Build command**: `npm install && npm run build`
   - **Publish directory**: `dist`
3. Add one environment variable:

   | Key | Value |
   |---|---|
   | `VITE_API_BASE_URL` | `https://placement-api.onrender.com/api` (your backend URL from step 2, with `/api` appended) |

4. Deploy. You'll get a URL like `https://placement-cell.onrender.com`.
5. **Client-side routing fix**: this is a single-page app, so Render needs
   to serve `index.html` for any path (otherwise refreshing `/jobs`
   directly gives a 404). In the Static Site's **Redirects/Rewrites**
   settings, add a rewrite rule: source `/*`, destination `/index.html`,
   action **Rewrite**.

## 4. First login

Register your first account as **ADMIN** at
`https://placement-cell.onrender.com/register` — admin/officer accounts
are approved instantly, so this becomes the account that reviews future
student sign-ups from the **Pending Approvals** page.

Anyone who registers as a **Student** afterward will need that admin (or
a placement officer) to approve them from that page before they can sign
in.

---

## Alternative: everything on one Render account without a separate DB host

If you'd rather not juggle two providers, Render also offers a free
managed **PostgreSQL** database (expires after 30 days on the free plan,
so you'd need to recreate it periodically). Swapping from MySQL to
PostgreSQL means changing the `mysql-connector-j` dependency in `pom.xml`
to `org.postgresql:postgresql` and adjusting the JDBC URL scheme to
`jdbc:postgresql://...` — Spring Data JPA/Hibernate handles the SQL
dialect differences automatically. Given Aiven's MySQL free tier has no
expiry, the two-provider setup above is the lower-maintenance choice.
