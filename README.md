# 🔐 Email OTP Authentication Service

A production-ready **Spring Boot** application that authenticates users via **email-based One-Time Passwords (OTP)** and issues **JWT tokens** for session management — no passwords required.

> 📺 **Watch the full walkthrough:** [Email-Based-OTP-Authentication on YouTube](https://youtu.be/-tIf34OYG14)

---

## ✨ How It Works

```
REGISTER ──► Save User ──► Send OTP via Email ──► Verify OTP ──► JWT
                                                                    │
LOGIN    ──► Send OTP  ──► Verify OTP ──────────► JWT ─────────────┘
                                                                    │
                                                      Use JWT on all protected routes
```

> **OTP** → proves you own the email (one-time, valid for 5 minutes)  
> **JWT** → proves you are logged in (reusable, valid for 24 hours)

---

## 🏗️ Project Structure

```
src/main/java/com/auth/otp/
├── OtpAuthServiceApplication.java     ← Main class
├── model/
│   ├── User.java                      ← User entity
│   └── OtpToken.java                  ← OTP record entity
├── repository/
│   ├── UserRepository.java
│   └── OtpTokenRepository.java
├── dto/
│   ├── RegisterRequest.java
│   ├── SendOtpRequest.java
│   ├── VerifyOtpRequest.java
│   ├── AuthResponse.java
│   └── ApiResponse.java
├── service/
│   ├── AuthService.java               ← Register / login logic
│   ├── OtpService.java                ← OTP generate / verify
│   └── EmailService.java              ← Gmail SMTP sender
├── util/
│   ├── OtpGenerator.java              ← Cryptographically secure OTP
│   └── JwtUtil.java                   ← JWT create / validate
└── config/
    ├── SecurityConfig.java            ← Spring Security (stateless)
    └── GlobalExceptionHandler.java    ← Unified error responses
```

---

## ⚙️ Setup & Configuration

### 1. Clone the repository

```bash
git clone https://github.com/lavishdev/Email-Otp-Authentication-Service.git
cd Email-Otp-Authentication-Service
```

### 2. Configure Gmail SMTP

In `src/main/resources/application.properties`:

```properties
spring.mail.username=YOUR_GMAIL@gmail.com
spring.mail.password=YOUR_APP_PASSWORD
```

> ⚠️ Use a **Gmail App Password**, NOT your regular Gmail password.  
> To generate one: **Google Account → Security → 2-Step Verification → App Passwords**

### 3. Set JWT Secret

```properties
jwt.secret=your-very-long-secret-key-at-least-32-chars-long
```

### 4. Database (Optional — Switch to MySQL/PostgreSQL)

The app uses **H2 in-memory DB** by default. To switch to MySQL:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/otpdb
spring.datasource.username=root
spring.datasource.password=yourpassword
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
spring.jpa.hibernate.ddl-auto=update
```

Add the MySQL dependency to `pom.xml`:

```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
</dependency>
```

---

## 🚀 Run the Application

```bash
mvn spring-boot:run
```

App runs at: **`http://localhost:8080`**

---

## 📡 API Endpoints

### 1. Register User

```http
POST /api/auth/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Registration successful! OTP sent to john@example.com. Please verify to login."
}
```

---

### 2. Send OTP (Login for returning users)

```http
POST /api/auth/send-otp
Content-Type: application/json

{
  "email": "john@example.com"
}
```

**Response:**
```json
{
  "success": true,
  "message": "OTP sent to john@example.com"
}
```

---

### 3. Verify OTP → Receive JWT

```http
POST /api/auth/verify-otp
Content-Type: application/json

{
  "email": "john@example.com",
  "otp": "482951"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "john@example.com",
  "name": "John Doe",
  "message": "Login successful!"
}
```

---

### 4. Access Protected Routes

Include the JWT in the `Authorization` header for all protected endpoints:

```http
GET /api/some-protected-route
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

---

### 5. Health Check

```http
GET /api/auth/health
```

---

## 🔒 OTP Security Rules

| Rule              | Value                                      |
|-------------------|--------------------------------------------|
| OTP Length        | 6 digits                                   |
| Generator         | `SecureRandom` (cryptographically secure)  |
| Expiry            | 5 minutes                                  |
| Max Attempts      | 3 tries                                    |
| After 3 failures  | OTP deleted — must request a new one       |
| After expiry      | OTP deleted automatically                  |
| One-time use      | Marked `used = true` after success         |
| Old OTPs          | Deleted when a new OTP is requested        |

---

## 🪙 JWT Token Structure

```
eyJhbGciOiJIUzI1NiJ9      ← Header  (algorithm)
.eyJzdWIiOiJqb2huQGV4YW1  ← Payload (claims)
.SflKxwRJSMeKKF2QT4fwpM   ← Signature
```

**Decoded Payload:**
```json
{
  "sub": "john@example.com",
  "iat": 1710000000,
  "exp": 1710086400
}
```

| Claim | Meaning                    |
|-------|----------------------------|
| `sub` | Subject — user's email     |
| `iat` | Issued At — Unix timestamp |
| `exp` | Expiry — issued + 24 hours |

---

## 🗄️ Database Schema

### `users`

| Column       | Type      | Notes                    |
|--------------|-----------|--------------------------|
| `id`         | BIGINT    | Primary key              |
| `email`      | VARCHAR   | Unique                   |
| `name`       | VARCHAR   |                          |
| `verified`   | BOOLEAN   | `true` after OTP success |
| `created_at` | DATETIME  |                          |

### `otp_tokens`

| Column          | Type     | Notes                       |
|-----------------|----------|-----------------------------|
| `id`            | BIGINT   | Primary key                 |
| `email`         | VARCHAR  |                             |
| `otp`           | VARCHAR  | 6-digit code                |
| `expires_at`    | DATETIME | `now + 5 minutes`           |
| `used`          | BOOLEAN  | `true` after verification   |
| `attempt_count` | INT      | Max 3 before invalidation   |
| `created_at`    | DATETIME |                             |

---

## 🛠️ Tech Stack

| Technology               | Purpose                          |
|--------------------------|----------------------------------|
| Java 17                  | Language                         |
| Spring Boot 3.2          | Application framework            |
| Spring Security          | Stateless auth filter chain      |
| Spring Mail              | Gmail SMTP integration           |
| Spring Data JPA          | ORM / DB layer (H2 / MySQL)      |
| JJWT 0.11.5              | JWT creation & validation        |
| Lombok                   | Boilerplate reduction            |

---

## 📄 License

This project is open source. Feel free to use, modify, and distribute it.

---

<div align="center">
  Made with ❤️ by <a href="https://github.com/lavishdev">lavishdev</a>
</div>
