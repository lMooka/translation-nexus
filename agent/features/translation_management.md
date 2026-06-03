# 📝 Translation Management

This feature is the core of the system, enabling querying, creating/deleting keys, and submitting translations by translators.

---

## ⚙️ Business Rules and Validations

1.  **Unique Composite Key**: The unique identification of a translatable item is based on the combination of `keyCode` + `version`.
2.  **Required Fields**: Every registered translation key requires:
    *   `keyCode` (Unique identifier, e.g., `item.weapon.wooden_sword.name`)
    *   `version` (Game version, e.g., `1.0.0`)
    *   `category` (Logical category, e.g., `ITEM`, `SKILL`, `UI`)
    *   `baseValue` (Original source text in English)
    *   `tags` (At least one descriptive tag, e.g., `["weapon", "warrior"]`)
3.  **Placeholder Validation**: The system rejects any translation update if the translated text does not contain the exact same placeholders as the original text (`baseValue`).
    *   *Example*: If the original value is `Deals {damage}% damage.`, the Portuguese translation must contain `{damage}`. Otherwise, the API returns a 400 Bad Request error.

---

## 🚀 API Endpoints

### 1. Get Translation Keys (Paginated)
*   **URL**: `/api/translations`
*   **Method**: `GET`
*   **Access**: Any authenticated user
*   **Query Parameters**:
    *   `version` (optional): Filter by exact version.
    *   `tag` (optional): Filter by associated tags.
    *   `category` (optional): Filter by category.
    *   `page` (optional): Page number (defaults to `0`).
    *   `size` (optional): Page size (defaults to `10`).

### 2. Create New Translation Key
*   **URL**: `/api/translations/keys`
*   **Method**: `POST`
*   **Access**: `MANAGER` only
*   **Request Body**:
    ```json
    {
      "keyCode": "quest.tutorial.welcome",
      "version": "1.0.0",
      "category": "QUEST",
      "tags": ["tutorial", "welcome"],
      "contextInfo": "Welcome message when starting the game.",
      "baseValue": "Welcome to Translation Nexus, {player_name}!"
    }
    ```

### 3. Delete Translation Key
*   **URL**: `/api/translations/{version}/{keyCode}`
*   **Method**: `DELETE`
*   **Access**: `MANAGER` only

### 4. Save / Update Translation for a Locale
*   **URL**: `/api/translations/{version}/{keyCode}/{locale}`
*   **Method**: `PUT`
*   **Access**: `TRANSLATOR`, `REVIEWER`, `MANAGER`, or `ADMIN`
*   **Rule**: Always changes the translation status to `PENDING_APPROVAL` (unless updated directly by a `REVIEWER`).
*   **Request Body**:
    ```json
    {
      "translatedValue": "Welcome, {player_name}!"
    }
    ```

### 5. Get Change History for a Key
*   **URL**: `/api/translations/{version}/{keyCode}/history`
*   **Method**: `GET`
*   **Access**: Any authenticated user

---

## 🛠️ Implementation Details

### Backend
*   **`TranslationDocument.java`**: MongoDB entity mapping keys, metadata, translations by language, and the audit history trail.
*   **`TranslationService.java`**: Orchestrates key creation, deletion, translation updates, and paginated searches.
*   **`PlaceholderValidator.java`**: Validation component that uses regular expressions to assert that placeholder tokens (e.g., `{damage}`) match between the base language and the target translation.

### Frontend
*   **`TranslationListComponent`**: Main workspace containing:
    *   Filters for querying keys (version selector, category dropdown, tag chips, and text search).
    *   Paginated table.
    *   Dynamic inputs for editing translations per locale, providing real-time placeholder validation feedback and inline saving.
