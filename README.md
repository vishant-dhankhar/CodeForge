# ⚔️ CodeForge — Distributed Online Judge & Code Execution Platform

[![React](https://img.shields.io/badge/Frontend-React%2018%20%2B%20Vite-61DAFB?logo=react&logoColor=black)](https://react.dev/)
[![Node.js](https://img.shields.io/badge/Backend-Node.js%20%2B%20Express-339933?logo=node.js&logoColor=white)](https://nodejs.org/)
[![JavaScript](https://img.shields.io/badge/Language-JavaScript%20(ESM)-F7DF1E?logo=javascript&logoColor=black)](https://developer.mozilla.org/en-US/docs/Web/JavaScript)
[![Prisma](https://img.shields.io/badge/ORM-Prisma-2D3748?logo=prisma&logoColor=white)](https://www.prisma.io/)
[![Redis](https://img.shields.io/badge/Queue%20%26%20Cache-Redis-DC382D?logo=redis&logoColor=white)](https://redis.io/)
[![PostgreSQL](https://img.shields.io/badge/Database-PostgreSQL-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Vercel](https://img.shields.io/badge/Deployment-Vercel-000000?logo=vercel&logoColor=white)](https://vercel.com/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

**CodeForge** is a high-performance, distributed Online Judge and competitive programming platform designed for executing and benchmarking user code in real time. It features a modern IDE interface with Monaco Editor, asynchronous task queue evaluation via Redis, and isolated process sandboxing.

---

## 🌟 Key Features

- **💻 In-Browser IDE Workspace**: Powered by **Monaco Editor** (the code editor underlying VS Code) with syntax highlighting, autocomplete, code resetting, and multi-language support (**Java 21** & **C++ 17**).
- **⚡ Asynchronous Code Execution Engine**: Submissions are queued asynchronously using **Redis Task Pipelines**, keeping API controllers non-blocking and highly responsive under heavy submission spikes.
- **⏱️ Precision Benchmarking & Verdict Evaluation**: Measures execution runtime in milliseconds and memory consumption in kilobytes, detecting verdicts including `ACCEPTED`, `WRONG_ANSWER`, `TIME_LIMIT_EXCEEDED`, `COMPILATION_ERROR`, and `RUNTIME_ERROR`.
- **🛡️ Sliding Window Rate Limiting**: Intercepts abusive submission bursts with a custom **Redis Sliding Window Rate Limiter** to protect execution nodes against DDoS and resource starvation.
- **🔐 JWT Authentication & User Statistics**: Secure user registration, authentication, problem solving counters, rating tracking, and submission history.
- **🌐 Zero-Downtime Demo Mode**: Built-in intelligent fallback for static frontend deployments (e.g., Vercel) allowing immediate catalog exploration and code evaluation even when disconnected from a live backend.

---

## 🏗️ System Architecture

```mermaid
flowchart TD
    subgraph Client ["Frontend (React 18 + Vite)"]
        UI["Monaco Code Editor & Problem Catalog"]
    end

    subgraph Gateway ["Node.js + Express API Server"]
        RL["Redis Sliding Window Rate Limiter"]
        Auth["JWT Auth Middleware & Routes"]
        ProblemAPI["Problem Catalog & Management Service"]
        SubAPI["Submission Controller"]
    end

    subgraph Processing ["Async Judge Infrastructure"]
        Queue[("Redis Task Queue / Broker")]
        Worker["Sandboxed Process Code Executor"]
        Evaluator["Verdict & Output Benchmarker"]
    end

    subgraph Storage ["Persistence Layer"]
        DB[("PostgreSQL Database (Prisma ORM)")]
    end

    UI -->|HTTPS / REST API| Gateway
    RL --> Gateway
    Auth --> DB
    ProblemAPI --> DB
    SubAPI -->|Push Task| Queue
    Queue -->|Consume Task| Worker
    Worker --> Evaluator
    Evaluator -->|Save Verdict & Stats| DB
    UI -->|Poll Verdict Status| Gateway
```

---

## 🛠️ Tech Stack

| Domain | Technologies |
| :--- | :--- |
| **Frontend** | React 18, Vite, Monaco Editor (`@monaco-editor/react`), Lucide Icons, Canvas Confetti |
| **Backend Framework** | Node.js, Express.js (JavaScript ESM), Zod Validation |
| **ORM & Database** | Prisma ORM (`@prisma/client`), PostgreSQL |
| **Code Execution Languages** | Java 21 (`javac`/`java`), C++ 17 (`g++`) |
| **Queue & Caching** | Redis (`ioredis` Sliding Window Rate Limiter & Async Task Worker) |
| **Build & Tooling** | `npm` / `node` (Backend), `npm` / `vite` (Frontend) |
| **Hosting & Deployment**| Vercel (Frontend SPA), Render / Railway / Docker (Backend) |

---

## 🚀 Quick Start & Local Setup

### Prerequisites

Ensure you have the following installed locally:
- **Node.js**: `v18.x` or higher
- **PostgreSQL**: `v14+` running on port `5432`
- **Redis**: running on port `6379`
- **Java JDK** (`javac`/`java`) & **C++ Compiler** (`g++`) for local process code execution

---

### 1. Database Setup

Create a PostgreSQL database named `codeforge_db`:

```sql
CREATE DATABASE codeforge_db;
```

---

### 2. Backend Setup (Node.js + Express + Prisma)

Navigate to the `backend/` directory:

```bash
cd backend
```

Install dependencies:

```bash
npm install
```

Configure environment variables in `.env`:

```env
PORT=8080
DATABASE_URL="postgresql://postgres:postgres@localhost:5432/codeforge_db?schema=public"
REDIS_HOST=localhost
REDIS_PORT=6379
JWT_SECRET=your_jwt_secret_key
```

Run Prisma database migrations and seed initial problem dataset:

```bash
npx prisma migrate dev --name init
npx prisma db seed
```

Start the backend application in development mode:

```bash
npm run dev
```

The Node.js backend will start on **`http://localhost:8080`**.

---

### 3. Frontend Setup (React + Vite)

In a new terminal window, navigate to the `frontend/` directory:

```bash
cd frontend
npm install
npm run dev
```

The frontend application will start on **`http://localhost:5173`**.

---

## 📡 API Endpoints Overview

| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :---: |
| `POST` | `/api/v1/auth/register` | Register a new user account | ❌ |
| `POST` | `/api/v1/auth/login` | Authenticate user & return JWT token | ❌ |
| `GET` | `/api/v1/users/me` | Fetch authenticated user profile & stats | ✅ |
| `GET` | `/api/v1/problems` | List problems with search & difficulty filters | ❌ |
| `GET` | `/api/v1/problems/{slug}` | Get full problem statement & sample test cases | ❌ |
| `POST` | `/api/v1/submissions` | Submit code solution for execution | ✅ |
| `GET` | `/api/v1/submissions/{id}` | Poll submission execution status & verdict | ✅ |
| `GET` | `/api/v1/submissions/problem/{slug}` | List past submissions for a specific problem | ❌ |

---

## ☁️ Deployment

### Frontend (Vercel)
The `frontend/` directory includes a pre-configured `vercel.json` for single-page routing:
1. Connect your repository to **Vercel**.
2. Set **Root Directory** to `frontend`.
3. Set Environment Variable `VITE_API_BASE_URL` to point to your live backend endpoint.

### Backend (Render / Railway / Docker)
The Node.js backend can be deployed using standard Docker containers or Node buildpacks on platforms like Render or Railway alongside a managed PostgreSQL database and Redis instance.

---

## 📜 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.
