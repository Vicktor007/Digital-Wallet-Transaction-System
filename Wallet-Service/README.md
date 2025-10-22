**CurrencyExchanger** Java project, complete with setup instructions for IntelliJ IDEA and links to necessary libraries.

---

```markdown
# Wallet Service

A distributed system demonstrating PostgreSQL transactions and Kafka event-driven architecture for digital wallet operations.
The Wallet Service provides real-time financial operations including wallet creation, funding, and transfers. It uses PostgreSQL for immediate consistency and publishes events to Kafka for asynchronous processing.

## 🏗️ Architecture Overview

┌─────────────────┐    ┌──────────────┐    ┌─────────────────┐
│  Wallet Service │───▶│    Kafka     │───▶│ History Service │
│ (Spring Boot)   │    │              │    │ (Spring Boot)   │
└─────────────────┘    │wallet_events │    └─────────────────┘
         │             └──────────────┘             │
         └──────────────────────────────────────────┘
                   Shared PostgreSQL Database
                   
## 🌟 Features

- Postgres database

- Flyway for database migration

- Kafka producer implementation

- Error handling for failures and kafka connectivity issues

- API documentation

## 🧰 Technologies Used

- Java 21
- Springboot 3.5.6
- Kafka
- Docker
- OpenApi Docs

## 📦 Project Structure



## 🔌 API Endpoints

Wallet Service (:8080)
POST /wallets - Create new wallet

POST /wallets/{id}/fund - Add funds

POST /wallets/{id}/transfer - Transfer money

GET /wallets/{id} - Get wallet details

GET /users/{userId}/wallets - List user wallets


## API documentation

- wallet-service: http://localhost:8080/swagger-ui.html



---

