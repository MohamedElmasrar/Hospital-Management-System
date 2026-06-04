# Hospital Management System

## Technologies

[![Java](https://img.shields.io/badge/Java-21%20LTS-orange.svg?style=flat&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-brightgreen.svg?style=flat&logo=springboot)](https://spring.io/projects/spring-boot)
[![Gradle](https://img.shields.io/badge/Gradle-8.x-02303A.svg?style=flat&logo=gradle)](https://gradle.org/)
[![Next.js](https://img.shields.io/badge/Next.js-16.4.2-black.svg?style=flat&logo=next.js)](https://nextjs.org/)
[![React](https://img.shields.io/badge/React-19-blue.svg?style=flat&logo=react)](https://react.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.x-blue.svg?style=flat&logo=typescript)](https://www.typescriptlang.org/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind%20CSS-v4-38B2AC.svg?style=flat&logo=tailwind-css)](https://tailwindcss.com/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-336791.svg?style=flat&logo=postgresql)](https://www.postgresql.org/)
[![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-4.1.2-231F20.svg?style=flat&logo=apachekafka)](https://kafka.apache.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose%20v2-2496ED.svg?style=flat&logo=docker)](https://www.docker.com/)
[![GitHub Actions](https://img.shields.io/badge/CI-GitHub%20Actions-2088FF.svg?style=flat&logo=githubactions)](https://github.com/features/actions)

**Frontend:** Next.js, React, TypeScript, Tailwind CSS, shadcn/ui, TanStack
React Query, and Orval  
**Backend:** Java, Spring Boot, Spring Security, Spring Data JPA, MapStruct,
and Lombok  
**Services:** Auth, HR, Reception, Billing, Analytics, and Notifications  
**Communication:** REST, gRPC, and Apache Kafka  
**Databases:** PostgreSQL  
**Infrastructure:** Docker Compose, Gradle, and GitHub Actions

## Overview

The Hospital Management System is a modular application for managing hospital
operations. It provides authenticated workflows for employees, doctors,
reception staff, HR teams, billing users, and administrators.

The backend is organized as independent Spring Boot services. Each service owns
its domain and database, while the API Gateway provides a single HTTP entry
point for the frontend. Kafka is used for asynchronous events such as analytics
and notifications, and the HR service exposes gRPC operations used by billing
and reception.

## Architecture

```mermaid
flowchart LR
    Browser[Next.js Frontend<br/>localhost:3000]
    Gateway[API Gateway<br/>localhost:8000]

    Auth[Auth Service]
    HR[HR Service]
    Reception[Reception Service]
    Billing[Billing Service]
    Analytics[Analytics Service]
    Notifications[Notification Service]
    Kafka[(Apache Kafka)]

    AuthDB[(Auth PostgreSQL)]
    HRDB[(HR PostgreSQL)]
    ReceptionDB[(Reception PostgreSQL)]
    BillingDB[(Billing PostgreSQL)]
    AnalyticsDB[(Analytics PostgreSQL)]
    NotificationsDB[(Notification PostgreSQL)]

    Browser --> Gateway
    Gateway --> Auth
    Gateway --> HR
    Gateway --> Reception
    Gateway --> Billing
    Gateway --> Analytics
    Gateway --> Notifications

    Auth --> AuthDB
    HR --> HRDB
    Reception --> ReceptionDB
    Billing --> BillingDB
    Analytics --> AnalyticsDB
    Notifications --> NotificationsDB

    Auth -. events .-> Kafka
    HR -. events .-> Kafka
    Reception -. events .-> Kafka
    Billing -. events .-> Kafka
    Kafka -. analytics .-> Analytics
    Kafka -. notifications .-> Notifications
    HR -. gRPC .-> Billing
    HR -. gRPC .-> Reception
```

### Main components

- **API Gateway:** Routes frontend requests to backend services.
- **Auth Service:** Handles users, roles, JWT authentication, and authorization.
- **HR Service:** Manages employees, physicians, departments, schedules, leave,
  attendance, and payroll.
- **Reception Service:** Manages patients, reservations, queues, admissions,
  prescriptions, and schedules.
- **Billing Service:** Manages invoices, payments, insurance, cash desks, and
  service tariffs.
- **Analytics Service:** Consumes domain events and provides analytics data.
- **Notification Service:** Stores and serves authenticated user notifications.
- **Frontend:** Provides the browser-based hospital operations interface.

Each backend service has its own PostgreSQL database. The databases are not
shared directly between services; cross-service data is accessed through
service APIs or gRPC.

## Prerequisites

- Docker and Docker Compose
- Node.js 22 or newer
- npm
- Java 21 (only required for running backend services outside Docker)

## Running with Docker Compose

From the repository root, build and start the backend infrastructure and
services:

```bash
docker compose up --build
```

The API Gateway is available at:

```text
http://localhost:8000
```

To stop the containers:

```bash
docker compose down
```

To stop the containers and remove persisted database volumes:

```bash
docker compose down -v
```

The `-v` option deletes local development data and should only be used when a
fresh database is required.

## Running the frontend

Start the backend with Docker Compose first, then open a second terminal:

```bash
cd frontend
npm install
npm run dev
```

Open the frontend at:

```text
http://localhost:3000
```

The frontend communicates with the API Gateway at `http://localhost:8000`.

## Development checks

Backend checks are run from the individual service directories:

```bash
cd auth-service
./gradlew spotlessCheck test
```

Replace `auth-service` with the service being changed. Frontend checks are:

```bash
cd frontend
npm run format:check
npm run lint
npx tsc --noEmit
```

GitHub Actions uses path-based change detection so only the services affected
by a commit are linted and tested.
