# Deploying to GCP's free tier, with GitHub Actions for CI/CD

This puts the whole app — MySQL, the Spring Boot API, and the React
frontend — on a single GCP **e2-micro** VM, which is part of GCP's
"Always Free" tier (not just the 90-day trial credit), so it keeps
costing $0/month indefinitely as long as you stay within the limits
below. GitHub Actions builds a Docker image on every push and redeploys
the VM automatically.

## Why this shape, not Cloud Run + Cloud SQL

Cloud Run and Artifact Registry both have generous always-free quotas,
but **Cloud SQL (managed MySQL) has no always-free tier** — only the
one-time $300/90-day trial credit, after which it bills. Connecting
Cloud Run to a self-hosted MySQL also requires a Serverless VPC
Connector, which isn't free either. Running MySQL + backend + frontend
together on the one free VM avoids both of those costs entirely and is
the standard way students host a project like this on GCP for nothing.

**Trade-off:** an e2-micro has 1 shared vCPU and 1GB RAM. That's enough
for MySQL + this Spring Boot app + nginx together, but only with the
memory limits already baked into `docker-compose.yml` in this repo
(MySQL's buffer pool and the JVM heap are both capped, and the setup
steps below add swap space as a safety margin). It's fine for a
capstone demo or a small placement cell — not for real production load.

---

## Architecture

```
                    ┌───────────────────────────────────────┐
                    │   e2-micro VM (Always Free)            │
                    │                                         │
  Browser  ───80───▶│  frontend (nginx)                       │
                    │   ├─ serves the built React app          │
                    │   └─ proxies /api/* ──▶ backend:8080     │
                    │                            │             │
                    │                     backend (Spring Boot)│
                    │                            │             │
                    │                        mysql:3306         │
                    └───────────────────────────────────────┘
                              ▲
                              │ docker compose pull && up -d
                              │ (over SSH)
                    ┌───────────────────────┐
                    │   GitHub Actions        │
                    │   on every push to main │
                    │   builds + pushes images│
                    │   to ghcr.io            │
                    └───────────────────────┘
```

Frontend and backend share one origin (port 80, via nginx), so there's
no CORS to configure and no separate backend hostname for the frontend
to know about.

---

## 1. One-time GCP setup

You need a GCP account (console.cloud.google.com — the $300/90-day
trial credit is separate from and not required for the always-free
resources used here) and either `gcloud` installed locally or Cloud
Shell (browser-based, already has `gcloud`).

```bash
gcloud config set project YOUR_PROJECT_ID

# Allow HTTP/HTTPS in to any instance tagged "web"
gcloud compute firewall-rules create allow-http-https \
  --allow=tcp:80,tcp:443 \
  --target-tags=web \
  --direction=INGRESS

# The Always Free e2-micro is only free in these regions:
# us-west1, us-central1, us-east1. One instance per billing account.
gcloud compute instances create pms-vm \
  --zone=us-central1-a \
  --machine-type=e2-micro \
  --image-family=ubuntu-2204-lts \
  --image-project=ubuntu-os-cloud \
  --boot-disk-size=30GB \
  --boot-disk-type=pd-standard \
  --tags=web
```

SSH in (this also registers your key with the VM):

```bash
gcloud compute ssh pms-vm --zone=us-central1-a
```

**Add swap.** 1GB of RAM is tight for MySQL + a JVM + nginx together;
a swap file is cheap insurance against an OOM kill during a burst
(e.g. a slow Maven-built first request):

```bash
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
```

**Install Docker:**

```bash
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker $USER
newgrp docker
```

**Clone the repo and set up secrets** (use your repo's HTTPS URL):

```bash
git clone https://github.com/YOUR_GITHUB_USERNAME/YOUR_REPO.git ~/placement-management-system
cd ~/placement-management-system
cp .env.example .env
nano .env   # fill in real passwords + JWT_SECRET (see comments in the file)
```

Leave `BACKEND_IMAGE` / `FRONTEND_IMAGE` in `.env` for now — you'll set
the real values after step 3 builds them for the first time.

---

## 2. Generate a deploy key (for GitHub Actions → VM)

From your **local machine** (not the VM), generate a dedicated keypair:

```bash
ssh-keygen -t ed25519 -f pms-deploy-key -C "github-actions-deploy" -N ""
```

Add the **public** key to the VM. Open `pms-deploy-key.pub` in a text
editor, copy its single line of content, and paste it into this command
in place of `PASTE_PUBLIC_KEY_CONTENTS` (this form avoids shell-specific
syntax, so it works the same from PowerShell, Cloud Shell, or bash):

```
gcloud compute instances add-metadata pms-vm --zone=us-central1-a `
  --metadata=ssh-keys="YOUR_VM_USERNAME:PASTE_PUBLIC_KEY_CONTENTS"
```

(`YOUR_VM_USERNAME` is whatever OS Login user you SSH in as — check
with `whoami` on the VM. Drop the trailing backtick and run it as one
line if you're not in PowerShell.)

Note the VM's external IP for later:

```bash
gcloud compute instances describe pms-vm --zone=us-central1-a \
  --format='get(networkInterfaces[0].accessConfigs[0].natIP)'
```

---

## 3. Push to GitHub and add secrets

Push this project to a GitHub repository, then go to
**Settings → Secrets and variables → Actions** and add:

| Secret | Value |
|---|---|
| `GCP_VM_HOST` | the VM's external IP from step 2 |
| `GCP_VM_USER` | the same username you used in step 2 |
| `GCP_VM_SSH_KEY` | contents of the **private** key file `pms-deploy-key` (not `.pub`) |

`.github/workflows/deploy.yml` (already in this repo) runs on every
push to `main`: it builds the backend and frontend Docker images,
pushes them to `ghcr.io/YOUR_GITHUB_USERNAME/...`, then SSHes into the
VM to pull and restart them. No GCP credentials are needed in GitHub —
only the SSH key above.

Push to `main` now and watch it run under the repo's **Actions** tab.

**Make the images pullable.** GitHub Container Registry packages are
private by default, which would make the VM's `docker compose pull`
fail with an auth error. After the first successful workflow run, go
to your GitHub profile → **Packages**, open each of the two new
packages (`placement-management-system-backend` and `-frontend`), and
under **Package settings** change visibility to **Public**.

---

## 4. First deploy on the VM

Back on the VM, fill in the real image names now that they exist:

```bash
cd ~/placement-management-system
nano .env
# set:
# BACKEND_IMAGE=ghcr.io/YOUR_GITHUB_USERNAME/placement-management-system-backend:latest
# FRONTEND_IMAGE=ghcr.io/YOUR_GITHUB_USERNAME/placement-management-system-frontend:latest

docker compose pull
docker compose up -d
docker compose logs -f backend   # watch it come up; Ctrl+C once it settles
```

Visit `http://<VM_EXTERNAL_IP>/` in a browser. `http://<VM_EXTERNAL_IP>/api/departments`
should return `[]` rather than an error.

---

## 5. Every deploy after this

Just `git push` to `main`. GitHub Actions builds new images and
redeploys the VM automatically — nothing to run by hand.

---

## 6. First login

Register your first account as **ADMIN** at `http://<VM_EXTERNAL_IP>/register`
— admin/officer accounts are approved instantly. That becomes the
account that reviews student sign-ups from the **Pending Approvals**
page. Students who register afterward stay `PENDING` until that admin
(or a placement officer) approves them.

---

## Troubleshooting

- **Backend keeps restarting / OOM-killed:** check `free -h` on the VM
  — if swap usage is high under load, the memory caps in
  `docker-compose.yml` may need to go lower still, or it's time to
  size up (e2-small, no longer free) instead of pushing e2-micro
  further.
- **`docker compose pull` fails with "unauthorized":** the GHCR
  packages are still private — see the visibility step in section 3.
- **Action's SSH step fails:** double check `GCP_VM_HOST` is the
  current external IP (it can change if the VM is stopped/started —
  reserve a static IP with `gcloud compute addresses create` if that's
  a problem) and that the public key was added under the exact
  `GCP_VM_USER` account.

## Optional next step: a real domain + HTTPS

Point a domain's A record at the VM's external IP, then swap the
`frontend` nginx container for [Caddy](https://caddyserver.com/) (a
two-line Caddyfile gets you automatic Let's Encrypt HTTPS) — everything
else in this guide stays the same.
