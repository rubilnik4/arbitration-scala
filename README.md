# 🏦 Arbitration Market Service

A backend service for synthetic arbitrage between futures on Binance.  
Written in **Scala 3** using **ZIO** ecosystem, with in-memory caching, OpenTelemetry logging, and PostgreSQL storage.

---

## 🚀 Features

- 📈 **Endpoints** for:
    - Getting price
    - Getting spread
    - Saving spread between two assets
- 🔀 **CQRS-like** split (commands & queries separated)
- 🔒 **Pure services** with no shared mutable state
- ⚡️ Uses **ZLayer** for dependency injection
- 🗂️ **Endpoints auto-documented** with OpenAPI + Swagger UI
- 🗄️ Uses **PostgreSQL** as the main database
- 🚀 **In-memory caching** (via ZIO Cache)
- 📊 **Logs and traces** collected via **OpenTelemetry**
- 🐳 Ready to run via **Docker Compose** or via IDE with local infra

---

## 🐳 Running with Docker Compose

This will start:

- **Arbitration Market**
- **PostgreSQL**
- **OpenTelemetry Collector**

```bash
docker-compose -f docker-compose.yml up
```

---

## ⚙️ Running Server via IDE

You can run the infrastructure (Postgres + OTEL) with **Docker Compose**,  
and run the server itself from your IDE (e.g., IntelliJ IDEA):

```bash
sbt run
```
Important: Startup parameters must be set manually in your IDE as Environment Variables.

```
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_DB=postgres
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4317
LOG_LEVEL=DEBUG
PROMETHEUS_PORT=9464
```