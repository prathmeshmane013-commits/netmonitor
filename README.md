# NetMonitor — Real-Time Network & Server Monitoring Platform

A full-stack monitoring platform that continuously checks the health of servers,
APIs, and network endpoints (via HTTP, TCP port, or ICMP ping), stores historical
results, calculates uptime percentages, and sends email alerts on status changes.

## Tech Stack

- **Backend:** Java 17, Spring Boot 3, Spring Data JPA, Spring Security (JWT), Spring Scheduling, Spring Mail
- **Frontend:** React 18, React Router, Axios, Recharts
- **Database:** PostgreSQL
- **Deployment target:** AWS (EC2 for the app, RDS for PostgreSQL)

## Why this project

Most fresher portfolios have a CRUD app or a to-do list. This one uses raw
socket/TCP checks and HTTP health checks — genuine networking knowledge —
combined with a real full-stack architecture: JWT auth, a scheduled background
job engine, historical time-series data, and computed uptime metrics.

## Architecture

```
React Dashboard  --(REST + JWT)-->  Spring Boot API
                                          |
                                   MonitoringScheduler (ticks every 10s)
                                          |
                                   HealthCheckService
                                    /      |       \
                              HTTP check  TCP check  Ping check
                                          |
                                   PostgreSQL (services + check history)
                                          |
                                    AlertService --> Email (SMTP)
```

## Getting started

### 1. Database
```bash
createdb netmonitor
# or with Docker:
docker run --name netmonitor-pg -e POSTGRES_DB=netmonitor \
  -e POSTGRES_USER=netmonitor -e POSTGRES_PASSWORD=netmonitor \
  -p 5432:5432 -d postgres:16
```

### 2. Backend
```bash
cd backend
./mvnw spring-boot:run
```
Runs on `http://localhost:8080`.

### 3. Frontend
```bash
cd frontend
npm install
npm run dev
```
Runs on `http://localhost:5173`.

### 4. Create a user and log in
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password123"}'
```
Then log in from the React app.

### 5. Add a service to monitor
```bash
curl -X POST http://localhost:8080/api/services \
  -H "Authorization: Bearer <your-jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "My API",
    "checkType": "HTTP",
    "host": "https://example.com/health",
    "checkIntervalSeconds": 30,
    "alertEmail": "you@example.com"
  }'
```

## What's left for you to build (roadmap)

This is a working foundation, not a finished product — building the rest
yourself is what makes this genuinely your project in an interview:

- [ ] Frontend form to add/edit monitored services (currently API-only)
- [ ] History chart per service using Recharts (data already available via `/services/{id}/history`)
- [ ] Slack/Discord webhook alerts as an alternative to email
- [ ] Role-based access (multiple users, read-only viewers)
- [ ] Dockerfile + docker-compose for one-command local spin-up
- [ ] Deploy: EC2 + RDS, or containerize and push to ECS
- [ ] Unit tests for `HealthCheckService` (mock the socket/HTTP calls)
- [ ] Rate-limit / backoff logic so a flapping service doesn't spam alerts

## API Reference

| Method | Endpoint                          | Description                          |
|--------|------------------------------------|---------------------------------------|
| POST   | `/api/auth/register`               | Create a user                         |
| POST   | `/api/auth/login`                  | Get a JWT                             |
| GET    | `/api/services`                    | List all monitored services           |
| POST   | `/api/services`                    | Add a new service to monitor          |
| PUT    | `/api/services/{id}`               | Update a service                      |
| DELETE | `/api/services/{id}`               | Remove a service                      |
| GET    | `/api/services/{id}/history?hours=24` | Historical check results           |
| GET    | `/api/services/{id}/uptime?hours=24`  | Uptime % over a window             |
