# PROGRESS.md — Completed / Pending Tracker

**Rule for using this file:** the moment you check a box, immediately run:
```
git add -A
git commit -m "feat(<service>): <what you built>"
git push origin main
```
Don't batch several modules before pushing. Frequent small pushes are what make your GitHub history actually show real incremental development over the weeks — and it's your backup if anything happens to your machine.

Update the **"Current Module"** section in `AGENTS.md` every time you move to a new row below.

---

## Stage 0 — Setup (Day 0)
- [x] Repo created on GitHub, cloned locally
- [x] `/docs` folder added with SRS, architecture doc, module roadmap
- [x] `AGENTS.md` added at repo root
- [x] Empty Spring Boot service skeletons created (no Docker yet)
- [x] MongoDB Atlas (free tier) + Redis (Upstash/Redis Cloud) set up and reachable
- [x] **Pushed to GitHub** — initial commit

---

## Stage 1 — Core Booking (Phase 1 / MVP) — Weeks 1–4

| # | Module | Status | Branch | Completed On | Pushed? |
|---|---|---|---|---|---|
| 1 | Shared foundation + gateway skeleton | ☑ Done | `feature/module-01-foundation` | 2026-07-15 | ☑ |
| 2 | Auth service (JWT, register/login) | ☑ Done | `feature/module-02-auth` | 2026-07-15 | ☑ |
| 3 | Turf service (CRUD + search) | ☑ Done | `feature/module-03-turf` | 2026-07-15 | ☑ |
| 4 | Slot generation & availability | ☑ Done | `feature/module-04-slots` | 2026-07-15 | ☑ |
| 5 | Booking service + Redis lock | ☑ Done | `feature/module-05-booking-lock` | 2026-07-15 | ☑ |
| 6 | Payment service (test mode) | ☑ Done | `feature/module-06-payment` | 2026-07-15 | ☑ |
| 7 | Minimal frontend (Antigravity) | ☑ Done | `feature/module-07-frontend` | 2026-07-15 | ☑ |

**Stage 1 milestone:** ☑ Full booking flow demoed end-to-end in a browser, race-condition test on Module 5 passes and is documented in `DEV_LOG.md`.
**Pushed to GitHub:** ☑

---

## Stage 2 — Enhanced Features (Phase 2) — Weeks 5–6

| # | Module | Status | Branch | Completed On | Pushed? |
|---|---|---|---|---|---|
| 9 | RabbitMQ + notification service | ☑ Done | `feature/module-09-notifications` | 2026-07-16 | ☑ |
| 10 | Reviews & ratings | ☐ Not started | `feature/module-10-reviews` | | ☐ |
| 11 | Refunds + payment state extension | ☐ Not started | `feature/module-11-refunds` | | ☐ |
| 12 | Caching + pagination/filtering | ☐ Not started | `feature/module-12-caching` | | ☐ |

**Pushed to GitHub:** ☑

---

## Stage 3 — Community Features (Phase 3) — Weeks 7–8

| # | Module | Status | Branch | Completed On | Pushed? |
|---|---|---|---|---|---|
| 13 | Teams & invitations | ☐ Not started | `feature/module-13-teams` | | ☐ |
| 14 | Matches | ☐ Not started | `feature/module-14-matches` | | ☐ |
| 15 | Tournaments + leaderboard | ☐ Not started | `feature/module-15-tournaments` | | ☐ |

**Stage 3 milestone:** ☐ This is a complete, strong submission on its own if Stage 4/5 time runs out.
**Pushed to GitHub:** ☐

---

## Stage 4 — AI Features (Phase 4, stretch) — Weeks 9–10

| # | Module | Status | Branch | Completed On | Pushed? |
|---|---|---|---|---|---|
| 16 | Recommendation service (heuristic v1) | ☐ Not started | `feature/module-16-recommend` | | ☐ |
| 17 | Fraud-signal service | ☐ Not started | `feature/module-17-fraud` | | ☐ |
| 18 | Admin analytics dashboard | ☐ Not started | `feature/module-18-analytics` | | ☐ |

**Pushed to GitHub:** ☐

---

## Stage 5 — Enterprise (Phase 5) — Weeks 11–12

| # | Module | Status | Branch | Completed On | Pushed? |
|---|---|---|---|---|---|
| 19 | RBAC expansion + franchise hierarchy | ☐ Not started | `feature/module-19-rbac` | | ☐ |
| 20 | Audit logging | ☐ Not started | `feature/module-20-audit` | | ☐ |
| 8 | **Docker + basic CI** *(moved here — must precede K8s)* | ☐ Not started | `feature/module-08-docker` | | ☐ |
| 21 | Kubernetes manifests (minikube) | ☐ Not started | `feature/module-21-k8s` | | ☐ |
| 22 | Sharding & multi-region strategy (**doc only**) | ☐ Not started | `feature/module-22-scale-doc` | | ☐ |

**Pushed to GitHub:** ☐

---

## Final Submission Checklist
- [ ] Stage 1 (Core) fully working — non-negotiable
- [ ] `DEV_LOG.md` filled in for every completed module
- [ ] `README.md` has clear setup/run instructions for a fresh clone
- [ ] All stages you completed are demoed and screen-recorded/screenshotted
- [ ] Final commit tagged: `git tag v1.0-submission && git push origin v1.0-submission`
- [ ] **Final push to GitHub confirmed** — check the repo on github.com itself, not just your local `git log`, to be sure everything landed

---

## Notes / Blockers
_(running space for anything you're stuck on — useful to glance back at before a viva)_
-
