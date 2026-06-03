# 🔐 Authentication and Security

This feature manages access control for the **Translation Nexus** system, providing secure JWT-based authentication and role-based authorization.

---

## 👥 Access Profiles (Roles)
The system has four permission levels:
*   **`ADMIN`**: Full control over user account administration (creation, listing, and deletion).
*   **`MANAGER`**: Responsible for system configuration. Can create translation keys, delete keys, and manage metadata (locales, categories, versions, and exports).
*   **`REVIEWER`**: Responsible for technical review, bulk import, and translation approvals.
*   **`TRANSLATOR`**: Responsible for suggesting or editing translations for specific locales.

---

## 🚀 API Endpoints

### 1. Login
*   **URL**: `/api/auth/login`
*   **Method**: `POST`
*   **Access**: Public
*   **Request Body (JSON)**:
    ```json
    {
      "username": "translator_01",
      "password": "secure_password"
    }
    ```
*   **Success Response (200 OK)**:
    ```json
    {
      "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
      "username": "translator_01",
      "role": "TRANSLATOR"
    }
    ```

---

## 🛠️ Implementation Details

### Backend
*   **`SecurityConfig.java`**: Configures Spring Security to disable CSRF, enable CORS, and define stateless policies for HTTP requests.
*   **`JwtTokenProvider.java`**: Utility class responsible for generating, validating, and extracting claims from JWT tokens.
*   **`JwtAuthFilter.java`**: Filter executed per request (`OncePerRequestFilter`) that intercepts the `Authorization: Bearer <token>` header, validates the token signature, and injects authentication into Spring's SecurityContext.

### Frontend
*   **`LoginComponent`**: Angular reactive form for username and password entry.
*   **`AuthService`**: Angular service managing login, storing the JWT token and user role in `localStorage`, and tracking session state.
*   **`AuthInterceptor`**: HTTP interceptor that automatically appends the JWT token (`Authorization: Bearer <token>`) to all outgoing requests.
*   **Route Guards (`AuthGuard` and `RoleGuard`)**: Protect routes in Angular, preventing unauthenticated users or those without the required role from accessing restricted views.
