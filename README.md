# 📌 URL Shortener Challenge

A production-ready **URL Shortener API** built with **Java 21, Spring Boot 3.3, MongoDB, and Redis**.  
It provides endpoints to shorten long URLs, resolve short codes back to long URLs, and track hit counts.  

---

## 🏗️ Architecture

- **Spring Boot (Java 21)** – REST API and business logic  
- **MongoDB** – Persistent storage for URL mappings and hit counts  
- **Redis** – Caching layer for fast short→long URL lookups & rate limiting  
- **Docker Compose** – Local development stack (App + Mongo + Redis)  
- **NGINX (optional)** – Load balancer to distribute traffic across multiple app replicas  

---

## ⚙️ Technology Stack

- Java 21  
- Spring Boot 3.3  
- Spring Data JPA (MongoDB)  
- Spring Data Redis  
- Docker & Docker Compose  
- JUnit 5 + Mockito (Unit & Controller tests)  

---

## 📂 Project Structure

```
src/
 ├── main/java/url/shortener/challenge
 │   ├── controller/      # REST controllers
 │   ├── service/         # Service layer
 │   ├── repository/      # Mongo repositories
 │   ├── entity/          # MongoDB entities
 │   ├── dto/             # Request/response DTOs
 │   ├── exception/       # Custom exceptions
 │   ├── constants/       # Error codes/messages
 │   └── config/          # App configuration
 └── test/java/...        # Unit and integration tests
```

---

## 🚀 Running Locally (without Docker)

Make sure you have **Java 21**, **Maven**, **MongoDB**, and **Redis** installed locally.  

```bash
# Build the project
mvn clean install

# Run with dev profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

API available at: [http://localhost:8080/api/v1/urls](http://localhost:8080/api/v1/urls)

---

## 🐳 Running with Docker Compose

Build & run the whole stack (App + Mongo + Redis):

```bash
docker compose up --build
```

API will be available at:  
👉 [http://localhost:8080/api/v1/urls](http://localhost:8080/api/v1/urls)

---

## 🌐 Endpoints

### 1. Create a short URL
```http
POST /api/v1/urls
```

Request:
```json
{
  "longUrl": "https://example.com"
}
```

Response:
```json
{
  "shortUrl": "abc123"
}
```

---

### 2. Resolve a short URL
```http
GET /api/v1/urls/{shortUrl}
```

Example:
```
GET /api/v1/urls/abc123
```

Response:
```json
{
  "longUrl": "https://example.com"
}
```

---

## ✅ Testing

Run unit & integration tests with Maven:

```bash
mvn test
```

---

## 🔧 Scaling with Load Balancer (Optional)

You can scale the app to multiple replicas and use **NGINX** as a load balancer:

```bash
docker compose up --build --scale app=3
```

NGINX will round-robin requests across all app instances.  

---

## 📈 Future Improvements

- Add custom alias support (user-defined short codes)  
- Add analytics (total clicks, per-day stats)  
- Add authentication & API keys for multi-user support  
- Deploy to cloud with Kubernetes + managed DB/cache  
