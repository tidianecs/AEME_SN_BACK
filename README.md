# AEME Energy Manager — Backend

REST API for the AEME Energy Manager Platform, built for the Senegalese Ministry of Energy to manage and track 254 energy managers across 14 regions.

> Academic project — DAUST (Dakar American University of Science and Technology) · 2025 – 2026

---

## Stack

- **Java 17** + **Spring Boot 3.2**
- **PostgreSQL** — relational database
- **Keycloak 24** — authentication & role management
- **WebSocket STOMP** — real-time chat
- **Resend API** — transactional emails
- **Docker / Docker Compose** — containerization
- **Railway** — cloud deployment
- **Sentry** — error monitoring

---

## Features

- JWT authentication via Keycloak (roles: `admin`, `user`)
- Report submission, approval and rejection workflow
- Automatic scoring system per user
- Real-time chat between managers via WebSocket
- Geographic location management via structure table
- Regional statistics endpoint
- Paginated user management
- Email invitation system with temporary password

---

## Project Structure

```
src/main/java/com/ditix/backend/
├── Auth/
│   ├── Controllers/
│   │   ├── AuthController.java
│   │   └── AdminController.java
│   └── Services/
│       └── AuthService.java
├── Core/
│   ├── SecurityConfig.java
│   ├── CorsConfig.java
│   ├── EmailService.java
│   └── GlobalExceptionHandler.java
├── Keycloak/
│   └── KeycloakService.java
├── Report/
│   ├── Model/
│   ├── Repository/
│   ├── Services/
│   ├── Controllers/
│   └── DTO/
├── Structure/
│   ├── Model/
│   ├── Repository/
│   ├── Services/
│   └── Controllers/
├── Meeting/
├── Chat/
└── BackendApplication.java
```

---

## API Endpoints

### Auth
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/api/v1/me` | User | Current user info |
| GET | `/api/v1/me/profile` | User | Full profile + score |
| PATCH | `/api/v1/me/profile` | User | Update profile |
| PATCH | `/api/v1/me/location` | User | Update location |
| PATCH | `/api/v1/me/membership` | User | Update service |
| GET | `/api/v1/users/locations` | User | All located users |
| GET | `/api/v1/stats/regions` | User | Regional statistics |

### Admin
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/api/v1/admin/users` | Admin | Invite user |
| GET | `/api/v1/admin/users` | Admin | Paginated user list |
| DELETE | `/api/v1/admin/users/:id` | Admin | Delete user |
| PATCH | `/api/v1/admin/users/:id/membership` | Admin | Update membership |
| GET | `/api/v1/admin/users/:id/reports` | Admin | User reports |
| PATCH | `/api/v1/admin/reports/:id/status` | Admin | Approve / reject report |

### Reports
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/api/v1/reports` | User | Submit report |
| GET | `/api/v1/reports` | User | My reports |
| GET | `/api/v1/reports/:id` | User | Report detail |
| DELETE | `/api/v1/reports/:id` | User | Delete report |
| GET | `/api/v1/reports/:id/download/:fileType` | User | Download file |

### Structures
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/api/v1/structures` | Public | All structures |
| GET | `/api/v1/structures/:id` | Public | Structure detail |
| POST | `/api/v1/structures` | Admin | Create structure |
| PATCH | `/api/v1/structures/:id` | Admin | Update structure |
| DELETE | `/api/v1/structures/:id` | Admin | Delete structure |

---

## Environment Variables

```properties
# Server
PORT=8081

# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://...
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=...

# Keycloak
SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI=https://.../realms/aeme
SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI=https://.../realms/aeme/protocol/openid-connect/certs
KEYCLOAK_SERVER_URL=https://...
KEYCLOAK_ADMIN_CLIENT_ID=aeme-client
KEYCLOAK_ADMIN_CLIENT_SECRET=...

# Email
RESEND_API_KEY=...
RESEND_FROM_EMAIL=noreply@mail.daust.cloud

# App
FRONTEND_URL=https://...
CORS_ALLOWED_ORIGINS=https://...

# Monitoring
SENTRY_DSN=...
```

---

## Local Setup

**Requirements:** Java 17+ · PostgreSQL · Keycloak instance (local or remote)

```bash
# Clone the repo
git clone https://github.com/tidianecs/AEME_SN_BACK.git
cd AEME_SN_BACK

# Run with Maven
./mvnw spring-boot:run
```

---

## Deployment

Deployed on **Railway** with automatic deploys on push to `main`.

---

## Related Repository

Frontend — [AEME_SN_FRONT](https://github.com/tidianecs/AEME_SN_FRONT)

---

## Author

**Cheikh Ahmed Tidiane Cissé** — 3rd-year CS Engineering Student at DAUST, Dakar

[Portfolio](https://porfolio-ashy-five-89.vercel.app) · [LinkedIn](https://linkedin.com/in/tidianecs)
