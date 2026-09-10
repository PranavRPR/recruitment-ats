# AI Recruitment ATS - Spring Boot Backend

Backend for the AI-Powered Recruitment & Applicant Tracking System.

## Stack

- Java 21
- Spring Boot 3.5.16
- Spring Web
- Spring Data JPA / Hibernate
- Spring Security
- JWT
- PostgreSQL
- Lombok
- Apache Tika
- Apache PDFBox

Spring Boot 3.5 requires Java 17+; this project uses Java 21. See the official Spring Boot documentation for supported versions.

## Run PostgreSQL

```bash
docker compose up -d
```

Or create a local PostgreSQL database named `recruitment_ats`.

## Run backend

```bash
mvn clean spring-boot:run
```

Backend:
http://localhost:8080

For local overrides, configure `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`.

## Default admin

Email: `admin@ats.com`
Password: `Admin@12345`

Change this before production.

## Main endpoints

POST `/api/auth/register`
POST `/api/auth/login`
GET `/api/auth/me`

Admin APIs require an ADMIN JWT:

GET `/api/admin/users`
GET `/api/admin/users/{id}`
PUT `/api/admin/users/{id}`
DELETE `/api/admin/users/{id}`
GET `/api/admin/jobs`
GET `/api/admin/applications`
GET `/api/admin/reports`
GET `/api/admin/notifications`

GET `/api/jobs`
GET `/api/jobs/{id}`
POST `/api/jobs`
PATCH `/api/jobs/{id}/status`

POST `/api/candidate/profile/{userId}`
GET `/api/candidate/profile/{userId}`

POST `/api/resumes/upload/{candidateUserId}`

POST `/api/applications/apply/{jobId}/{candidateUserId}`
GET `/api/applications/candidate/{userId}`
GET `/api/applications/job/{jobId}`
PATCH `/api/applications/{id}/status`

POST `/api/ai/match/{applicationId}`
GET `/api/ai/match/{applicationId}`

POST `/api/interviews`
PATCH `/api/interviews/{id}/status`

GET `/api/notifications/user/{userId}`
PATCH `/api/notifications/{id}/read`
GET `/api/notifications`
GET `/api/notifications/unread`
PUT `/api/notifications/{id}/read`
PUT `/api/notifications/read-all`

## Example registration

```json
{
  "name": "Rahul Sharma",
  "email": "rahul@example.com",
  "password": "Password@123",
  "role": "CANDIDATE"
}
```

## Example login

```json
{
  "email": "rahul@example.com",
  "password": "Password@123"
}
```

Use the returned token:

```text
Authorization: Bearer <token>
```

## AI matching

The first implementation uses a transparent weighted score:

- Skills 40%
- Experience 25%
- Education 10%
- Certifications 5%
- Role 10%
- Semantic/text similarity 10%

This is a baseline decision-support implementation. It does not automatically hire or reject candidates.
