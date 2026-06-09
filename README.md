<p align="center">
  <h1 align="center">🌐 Translation Nexus</h1>
  <p align="center">
    <strong>Translation Management System</strong><br>
    Coordinate, translate, review, and export localized content across multiple versions, categories, and languages — with role-based access control, automated translation, and observability built in.
  </p>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white" alt="Java 21">
  <img src="https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?logo=springboot&logoColor=white" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Angular-21-DD0031?logo=angular&logoColor=white" alt="Angular 21">
  <img src="https://img.shields.io/badge/MongoDB-6.0-47A248?logo=mongodb&logoColor=white" alt="MongoDB">
  <img src="https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white" alt="Docker">
  <img src="https://img.shields.io/badge/Prometheus-Grafana-E6522C?logo=prometheus&logoColor=white" alt="Observability">
</p>

---

## 📸 Screenshots

| Login | Translation Grid |
|:---:|:---:|
| ![Login](docs/screenshots/login.png) | ![Translation List](docs/screenshots/translation-list.png) |

| Admin — Categories | Review Queue |
|:---:|:---:|
| ![Categories](docs/screenshots/admin-categories.png) | ![Review Queue](docs/screenshots/review-queue.png) |

---

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Option 1 — Local Development](#option-1--local-development)
  - [Option 2 — Docker Compose (Full Stack)](#option-2--docker-compose-full-stack)
- [Default Credentials](#default-credentials)
- [Environment Variables](#environment-variables)
- [API Reference](#api-reference)
- [Role-Based Access Control (RBAC)](#role-based-access-control-rbac)
- [Translation Workflow](#translation-workflow)
- [Observability](#observability)
- [Tooling](#tooling)
- [License](#license)

---

## Architecture Overview

```
┌──────────────────────────────────────────────────────────────────────┐
│                          Client (Browser)                            │
│                    Angular 21 SPA + Vanilla CSS                      │
└───────────────────────────────┬──────────────────────────────────────┘
                                │ HTTP / REST (JSON)
                                ▼
┌──────────────────────────────────────────────────────────────────────┐
│                   NGINX Reverse Proxy (Docker only)                  │
│                 Serves SPA (:80) + proxies /api → backend            │
└───────────────────────────────┬──────────────────────────────────────┘
                                │
                                ▼
┌──────────────────────────────────────────────────────────────────────┐
│                     Spring Boot API (:8080)                           │
│                                                                      │
│  ┌──────────────┐  ┌──────────────┐  ┌────────────────────────────┐  │
│  │  Controllers │→ │   Services   │→ │   Spring Data MongoDB      │  │
│  │  (REST DTOs) │  │  (Business)  │  │   Repositories             │  │
│  └──────────────┘  └──────┬───────┘  └────────────────────────────┘  │
│                           │                                          │
│  ┌────────────────────────┼────────────────────────────────────────┐  │
│  │  Cross-Cutting:        │                                        │  │
│  │  • JWT Auth Filter     • Google Translate Client                │  │
│  │  • CORS Config         • MDC Logging (traceId + user)           │  │
│  │  • Security Config     • Micrometer / Prometheus Metrics        │  │
│  │  • OpenAPI / Swagger   • Data Seeding (CommandLineRunner)       │  │
│  └─────────────────────────────────────────────────────────────────┘  │
└───────────────────────────────┬──────────────────────────────────────┘
                                │
                                ▼
┌──────────────────────────────────────────────────────────────────────┐
│                         MongoDB (:27017)                             │
│              Collections: users, translations, categories,           │
│              locales, versions                                       │
└──────────────────────────────────────────────────────────────────────┘
                                │
              ┌─────────────────┴─────────────────┐
              ▼                                   ▼
┌──────────────────────┐           ┌───────────────────────┐
│  Prometheus (:9090)  │ ────────→ │   Grafana (:3000)     │
│  Scrapes /actuator/  │           │   Dashboards &        │
│  prometheus          │           │   Alerting            │
└──────────────────────┘           └───────────────────────┘
```

---

## Features

### Translation Management
- **Multi-version support** — Create new versions that clone all keys/translations from the active version; older versions become locked (read-only).
- **Category-based organization** — Group keys by category (e.g., `Items`, `Skills`, `UI`) with configurable path mappings for CSV export.
- **Wildcard path patterns** — Categories define patterns like `items.*.name` that map to export filenames (`items.csv`), enabling structured bulk exports.
- **Full-text search** — Filter keys by `keyCode` or English source text, with additional filters for version, category, and tags.
- **Pagination** — Server-side paging with configurable page size.

### Priority System
- **5-Level priority** — Visual bar indicators (1–5); higher priority keys float to the top of the grid.
- **Category inheritance** — New keys automatically inherit the default priority of their assigned category.
- **Manager overrides** — Managers can click directly on priority bars to re-prioritize any key in real time.

### AI-Assisted Translation
- **Google Cloud Translation API** integration — One-click auto-translate from English source to any target locale.
- **Locale-aware mapping** — Supports custom Google language codes per locale (e.g., `pt-BR` for Brazilian Portuguese).

### Review & Approval Workflow
- Three-state pipeline: **Pending → Review → Approved**.
- Saving a translation automatically sets status to `REVIEW`.
- Dedicated **Review Queue** page for reviewers to audit and approve pending translations.
- Reviewers can change status at any stage via dropdown.

### User & Access Control
- **Four roles:** `ADMIN`, `MANAGER`, `REVIEWER`, `TRANSLATOR`.
- **Locale-scoped permissions** — Translators can only edit locales explicitly assigned to them.
- **Admin panel** — Manage users, reset passwords, assign roles and language permissions.

### Export
- **Bulk CSV export** — Export all translations as a ZIP archive of CSVs, organized by category path mappings.
- **Manager-only** — Export endpoints are restricted to the `MANAGER` role.

### Observability
- **Structured logging** with MDC (traceId + authenticated user per request).
- **Prometheus metrics** exposed via Spring Actuator (`/actuator/prometheus`).
- **Grafana dashboards** — Pre-provisioned via Docker Compose for real-time monitoring.

---

## Tech Stack

| Layer | Technology | Details |
|:---|:---|:---|
| **Backend** | Java 21, Spring Boot 3.x | REST API, Spring Security, Spring Data MongoDB |
| **Frontend** | Angular 21 | Standalone components, lazy-loaded routes, Vanilla CSS with HSL theming |
| **Database** | MongoDB 6.0 | Document store for translations, users, versions, categories, locales |
| **Auth** | JWT (Bearer) | Stateless authentication with BCrypt password hashing |
| **API Docs** | OpenAPI 3.0 / Swagger UI | Available at `/swagger-ui/index.html` |
| **Metrics** | Micrometer + Prometheus | Custom counters and gauges for translation activity |
| **Dashboards** | Grafana | Auto-provisioned datasource and dashboards |
| **Containerization** | Docker, Docker Compose | Multi-stage builds for both backend (Maven→JRE) and frontend (Node→NGINX) |
| **Reverse Proxy** | NGINX | Serves SPA and proxies API requests in production |

---

## Project Structure

```
translation-nexus/
├── backend/
│   └── src/main/java/dev/mooka/translationnexus/
│       ├── config/              # SecurityConfig, CORS, OpenAPI, DataInitializer, MDC Filter
│       ├── domain/
│       │   ├── entity/          # MongoDB document classes (Translation, Category, User, Locale, Version…)
│       │   ├── enums/           # TranslationStatusEnum
│       │   └── model/           # Domain models (intermediate layer)
│       ├── exception/           # Business exceptions + global handler
│       ├── metrics/             # Micrometer custom metrics (MetricService, AppMetricService)
│       ├── repository/          # Spring Data MongoDB repositories
│       ├── resource/
│       │   ├── controller/      # REST controllers (Auth, Translation, Category, Locale, Version, User, Export)
│       │   └── dto/             # Request/Response DTOs
│       ├── security/            # JwtAuthFilter, JwtTokenProvider, Roles constants
│       ├── service/             # Business logic (TranslationService, CategoryService, MapperService…)
│       └── shared/              # Shared utilities
├── frontend/
│   └── src/app/
│       ├── core/
│       │   ├── guards/          # Route guards (auth, admin, manager, reviewer)
│       │   ├── interceptors/    # HTTP interceptors (JWT token injection)
│       │   ├── models/          # TypeScript interfaces
│       │   └── services/        # ApiService, AuthService, ConfigService
│       └── features/
│           ├── admin/           # Admin pages (Users, Categories, Locales)
│           ├── login/           # Login page
│           ├── review/          # Review Queue page
│           └── translations/    # Translation Grid (main workspace)
├── docker/
│   ├── Dockerfile.backend       # Multi-stage: Maven build → Eclipse Temurin 21 JRE
│   ├── Dockerfile.frontend      # Multi-stage: Node build → NGINX Alpine
│   ├── docker-compose.yml       # Full stack: MongoDB + Backend + Frontend + Prometheus + Grafana
│   ├── nginx.conf               # SPA routing + API proxy
│   ├── prometheus.yml           # Scrape config for backend metrics
│   └── grafana/                 # Provisioning (datasources + dashboards)
└── tools/
    ├── csv-parser/              # Node.js script to parse CSV translation files to JSON
    └── csv-importer/            # Node.js script to import CSV translations via the API
```

---

## Getting Started

### Prerequisites

| Tool | Required For |
|:---|:---|
| **Java 21** + **Maven 3.9+** | Backend (local development) |
| **Node.js 20+** + **npm** | Frontend (local development) |
| **Docker** + **Docker Compose** | Containerized deployment |
| **MongoDB 6.0+** | Database (or use the Docker container) |

### Option 1 — Local Development

Run backend and frontend as separate processes for a hot-reload development experience.

#### 1. Start MongoDB

```bash
docker run -d -p 27017:27017 \
  -e MONGO_INITDB_ROOT_USERNAME=root \
  -e MONGO_INITDB_ROOT_PASSWORD=root \
  --name translation-mongo mongo:6.0
```

#### 2. Start the Backend API

```bash
cd backend
mvn spring-boot:run
```

The API server starts at **http://localhost:8080**.  
Swagger UI is available at **http://localhost:8080/swagger-ui/index.html**.

> [!TIP]
> On first startup with `APP_SEEDING_ENABLED=true` (default), the system automatically creates default users, locales, and an initial version `1.0.0`.

#### 3. Start the Frontend Dev Server

```bash
cd frontend
npm install
npm start
```

The Angular dev server starts at **http://localhost:4200** with live reload.

---

### Option 2 — Docker Compose (Full Stack)

Launch all five services with a single command:

```bash
docker compose -f docker/docker-compose.yml up --build
```

| Service | URL | Notes |
|:---|:---|:---|
| **Frontend** | http://localhost | NGINX-served SPA |
| **Backend API** | http://localhost:8080 | Spring Boot REST API |
| **Swagger UI** | http://localhost:8080/swagger-ui/index.html | Interactive API docs |
| **Grafana** | http://localhost:3000 | `admin` / `admin` |
| **Prometheus** | http://localhost:9090 | Metrics scraper UI |

> [!IMPORTANT]
> For production deployments, override `APP_JWT_SECRET` with a strong, unique secret (minimum 32 characters) via environment variable or `.env` file.

---

## Default Credentials

When seeding is enabled (`APP_SEEDING_ENABLED=true`), the following users are created automatically:

| Username | Password | Roles | Locale Restrictions |
|:---|:---|:---|:---|
| `admin` | `admin` | ADMIN, REVIEWER, TRANSLATOR | None (all locales) |
| `manager` | `manager` | MANAGER, REVIEWER, TRANSLATOR | None (all locales) |
| `reviewer` | `reviewer` | REVIEWER, TRANSLATOR | None (all locales) |
| `translator` | `translator` | TRANSLATOR | `pt`, `es`, `fr`, `de`, `ja` |

> [!CAUTION]
> Change all default passwords immediately in production. The admin panel (`/admin`) allows password resets for all users.

---

## Environment Variables

All backend configuration is externalized via environment variables with sensible defaults:

| Variable | Description | Default |
|:---|:---|:---|
| `MONGO_DB_URI` | MongoDB connection string | `mongodb://root:root@localhost:27017/mooka?authSource=admin` |
| `GOOGLE_TRANSLATE_API_KEY` | Google Cloud Translation API key | *(empty — auto-translate disabled)* |
| `APP_JWT_SECRET` | HMAC secret for signing JWT tokens (≥ 32 chars) | `nexus-secret-key-2024-change-in-prod` |
| `APP_JWT_EXPIRATION` | JWT token TTL in milliseconds | `86400000` (24 hours) |
| `APP_SEEDING_ENABLED` | Seed default users, locales, and version on startup | `true` |
| `APP_CORS_ALLOWED_ORIGINS` | Comma-separated list of allowed CORS origins | `http://localhost,http://localhost:4200` |
| `APP_PORT` | HTTP port for the backend server | `8080` |
| `APP_METRICS_NAME` | Application name tag for Prometheus metrics | `translation-nexus` |

---

## API Reference

All endpoints require a valid JWT `Bearer` token (except `/api/auth/login`).  
Full interactive documentation is available at **Swagger UI** (`/swagger-ui/index.html`).

### Authentication

| Method | Endpoint | Role | Description |
|:---|:---|:---|:---|
| `POST` | `/api/auth/login` | Public | Authenticate and receive a JWT token |

### Translations

| Method | Endpoint | Role | Description |
|:---|:---|:---|:---|
| `GET` | `/api/translations` | Authenticated | Paginated list with filters (version, category, tag, search) |
| `POST` | `/api/translations/keys` | MANAGER | Create a new translation key |
| `PUT` | `/api/translations/{id}/{locale}` | TRANSLATOR* | Submit or update a translation value |
| `PUT` | `/api/translations/{id}/priority` | MANAGER | Update translation priority (1–5) |
| `DELETE` | `/api/translations/{id}` | MANAGER | Delete a translation key |
| `GET` | `/api/translations/pending` | REVIEWER | List translations in REVIEW status |
| `POST` | `/api/translations/{id}/{locale}/approve` | REVIEWER | Approve a translation |
| `PUT` | `/api/translations/{id}/{locale}/status` | REVIEWER | Change translation status |
| `GET` | `/api/translations/{id}/history` | Authenticated | Full audit history for a key |
| `GET` | `/api/translations/locales` | Authenticated | List all available locales |
| `POST` | `/api/translations/translate` | Authenticated | Auto-translate via Google API |

_* Translators can only edit locales assigned to their user profile._

### Categories, Locales, Versions, Users

| Method | Endpoint | Role | Description |
|:---|:---|:---|:---|
| `GET/POST/PUT/DELETE` | `/api/categories/**` | MANAGER | CRUD for translation categories |
| `GET/POST/PUT/DELETE` | `/api/locales/**` | MANAGER | CRUD for supported locales |
| `GET/POST` | `/api/versions/**` | MANAGER | List and create versions |
| `GET/POST/PUT/DELETE` | `/api/users/**` | ADMIN | User management |

### Export

| Method | Endpoint | Role | Description |
|:---|:---|:---|:---|
| `GET` | `/api/export` | MANAGER | Export all translations as ZIP of CSVs |

---

## Role-Based Access Control (RBAC)

The system enforces a hierarchical permission model through Spring Security:

```
ADMIN ──────────── Full system access (user management, all operations)
  │
MANAGER ────────── Content management (keys, categories, versions, export, priority)
  │
REVIEWER ────────── Translation QA (approve/reject, status changes, import)
  │
TRANSLATOR ──────── Content creation (translate assigned locales only)
```

Permissions are enforced at **two levels**:
1. **Endpoint-level** — Spring Security `HttpSecurity` rules in `SecurityConfig`.
2. **Locale-level** — Runtime checks in controllers verify the translator has access to the specific locale.

---

## Translation Workflow

```
┌─────────────┐     Save      ┌─────────────┐    Approve    ┌─────────────┐
│   PENDING   │ ────────────→ │   REVIEW    │ ────────────→ │  APPROVED   │
│  (No value) │               │  (Awaiting  │               │  (Ready for │
│             │               │   review)   │               │   export)   │
└─────────────┘               └──────┬──────┘               └─────────────┘
                                     │
                                     │ Reject / Edit
                                     ▼
                              ┌─────────────┐
                              │   PENDING   │
                              │ (Back to    │
                              │  translator)│
                              └─────────────┘
```

1. **Manager** creates a translation key with English source text, category, tags, and context info.
2. **Translator** submits a translation → status moves to `REVIEW`.
3. **Reviewer** audits the translation in the Review Queue:
   - **Approve** → status moves to `APPROVED`.
   - **Reject** → status reverts to `PENDING` for re-translation.
4. **Manager** exports approved translations as structured CSV bundles.

---

## Observability

### Logging
Every HTTP request is tagged with a unique `traceId` and the authenticated `user` via MDC, enabling end-to-end request tracing:

```
2024-06-09 12:34:56.789 [http-nio-8080-exec-1] INFO [traceId=abc123] [user=manager] TranslationService - Updated priority for key items.cyclone.name to 5
```

### Metrics
Custom Micrometer metrics are exposed at `/actuator/prometheus`:

- Translation activity counters (creates, updates, approvals)
- Request latency histograms
- JVM and Spring Boot standard metrics

### Grafana
Docker Compose auto-provisions:
- **Prometheus datasource** pointing to `http://nexus-prometheus:9090`
- **Pre-built dashboards** for translation activity and system health

---

## Tooling

The `tools/` directory contains standalone Node.js utilities:

| Tool | Description |
|:---|:---|
| `csv-parser` | Parses CSV translation files into structured JSON for review or programmatic use. |
| `csv-importer` | Imports CSV translation data into Translation Nexus via the REST API. |

---

## License

This project is proprietary software. All rights reserved.
