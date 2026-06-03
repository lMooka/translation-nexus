# 📥 Translation Export

This feature enables exporting approved translations for direct consumption by external integration systems using csv.

---

## 🎯 Export Business Rules
1.  **Locale and Version Filters**: Exports are generated for a specific locale and version.
2.  **Approved Items Only**: Only translations with the **`APPROVED`** status are exported. Pending (`PENDING_APPROVAL`) or empty translations are omitted from the export payload to ensure production builds never receive incomplete translations.
3.  **Flat JSON Structure**: Exports are formatted as simple key-value maps (JSON) for high performance and compatibility with game client lookup requirements.

---

## 🚀 API Endpoints

### Export Translations
*   **URL**: `/api/export/{locale}/{version}?format=json`
*   **Method**: `GET`
*   **Access**: `MANAGER` only
*   **Response Payload (JSON)**:
    ```json
    {
      "skill.cyclone.name": "Ciclone",
      "skill.cyclone.desc": "Gira causando {damage}% de dano a inimigos próximos.",
      "item.wooden_sword.name": "Espada de Madeira",
      "item.wooden_sword.desc": "Uma arma simples feita de madeira."
    }
    ```

---

## 🛠️ Implementation Details

### Backend
*   **`ExportController.java`**: Handles incoming export requests and restricts access to the `MANAGER` role.
*   **`ExportService.java`**: Queries MongoDB for documents matching the requested version, filters the inner translations map for the target locale to extract values marked as `APPROVED`, and packages them into a flat key-value map.

### Frontend
*   **Export Component**: An intuitive control inside the workspace allowing users with the `MANAGER` role to select target versions and locales from dropdowns, and download the resulting JSON file.
