# 👤 User Administration

This feature manages user registration, listings, updates, and deletion of user accounts. Access is restricted to users with the `ADMIN` role.

---

## 🔑 Access Roles
Only `ADMIN` users can manage accounts. The available roles in the system are:
*   `ADMIN` (User account administration)
*   `MANAGER` (Metadata configuration, key creation/deletion, and exports)
*   `REVIEWER` (Review, approve, and bulk import translations)
*   `TRANSLATOR` (Insert/edit translations)

---

## 🚀 API Endpoints

### 1. List Users
*   **URL**: `/api/users`
*   **Method**: `GET`
*   **Access**: `ADMIN` only

### 2. Create User
*   **URL**: `/api/users`
*   **Method**: `POST`
*   **Access**: `ADMIN` only
*   **Request Body (JSON)**:
    ```json
    {
      "username": "reviewer_john",
      "password": "securePassword123",
      "role": "REVIEWER"
    }
    ```

### 3. Delete User
*   **URL**: `/api/users/{id}`
*   **Method**: `DELETE`
*   **Access**: `ADMIN` only

---

## 🛠️ Implementation Details

### Backend
*   **`UserController.java`**: Exposes REST endpoints restricted to the `ADMIN` role for managing the users collection.
*   **`UserRepository.java`**: Spring Data MongoDB repository for accessing the `users` collection.
*   **Encryption**: Spring Security's `BCryptPasswordEncoder` is utilized to hash passwords before storing them in MongoDB. Clear-text passwords are never stored.

### Frontend
*   **`AdminComponent`**: Admin-exclusive panel featuring:
    *   A list/table of active users.
    *   A form to add new users with validation.
    *   Delete actions with safety confirmation dialogs.
