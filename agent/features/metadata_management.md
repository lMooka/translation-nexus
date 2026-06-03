# ⚙️ Metadata Management

This feature manages the support lookup entities of the system: **Locales (Languages)**, **Categories**, and **Versions**. These are used to standardize search filters and keys stored in the database.

---

## 📂 Metadata Entities

1.  **Locales**: Configuration of languages accepted by the system (e.g., `pt`, `es`, `fr`, `ja`).
2.  **Categories**: Key classifications (e.g., `ITEM`, `SKILL`, `QUEST`, `UI`, `NPC`).
3.  **Versions**: Game build or update versions (e.g., `1.0.0`, `1.1.0-beta`).

---

## 🚀 API Endpoints

### Locales
*   `GET /api/locales` -> Lists all locales (Access: Any profile).
*   `POST /api/locales` -> Creates a locale (Access: `MANAGER` only).
*   `PUT /api/locales/{code}` -> Updates a locale (Access: `MANAGER` only).
*   `DELETE /api/locales/{code}` -> Deletes a locale (Access: `MANAGER` only).

### Categories
*   `GET /api/categories` -> Lists all categories (Access: Any profile).
*   `POST /api/categories` -> Creates a category (Access: `MANAGER` only).
*   `PUT /api/categories/{name}` -> Updates a category (Access: `MANAGER` only).
*   `DELETE /api/categories/{name}` -> Deletes a category (Access: `MANAGER` only).

### Versions
*   `GET /api/versions` -> Lists all versions (Access: Any profile).
*   `POST /api/versions` -> Creates a version (Access: `MANAGER` only).
*   `DELETE /api/versions/{name}` -> Deletes a version (Access: `MANAGER` only).

---

## 🛠️ Implementation Details

### Backend
*   **`LocaleController.java`**, **`CategoryController.java`**, **`VersionController.java`**: Dedicated REST controllers exposing CRUD operations for their respective metadata entities.
*   **Database**: Each metadata entity is stored in its own MongoDB collection (`locales`, `categories`, `versions`).

### Frontend
*   **Configuration Dashboard**: In the config section of the app, users with the `MANAGER` role have specific views/tabs to view and manage these lookup tables.
*   **Filter Options**: Dynamic selectors and dropdown lists across the application query these endpoints to populate list options.
