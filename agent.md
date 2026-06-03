# Translation Nexus - Localization Tool

This file contains the instructions and specifications for the translation management system. It serves as a guide for rapid development and future reference.

---

## 📁 Agent Conventions

*   **Implementation Plans**: All implementation plans must be saved under `agent/plan/` in the project root (e.g. `agent/plan/implementation_plan.md`). Never save plans outside this directory.

---

## 🛠️ Technology Stack

1.  **Backend**: Java 21 + Spring Boot 4 + Spring Data MongoDB + Spring Security (JWT)
2.  **Database**: MongoDB (Collection: `translations`)
3.  **Frontend**: Angular (Decoupled, consuming the Spring Boot REST API)

---

## ⚙️ Domain & Business Rules

*   **Logical Composite Key**: The unique identifier for a translatable string is composed of **`keyCode` + `version`**. Never use the original source text as the key.
    *   Key Example: `item.weapon.wooden_sword.name` (Version: `1.0.0`)
*   **Required Fields**: Every record must contain `keyCode`, `version`, `category`, `baseValue` (English source text), and an array/list of `tags`.
*   **Approval Workflow**:
    *   **Translator** (`TRANSLATOR`): Can only suggest/edit translations. Saving updates the translation status to `PENDING_APPROVAL`.
    *   **Reviewer** (`REVIEWER`): Can approve translations (`APPROVED`), or edit and approve them instantly.
*   **Placeholder Validation**: Reject saving if the translated text does not contain the exact same placeholders as the original English text.
    *   Example: Original text `Deals {damage}% damage` requires the Portuguese translation to include `{damage}`.

---

## 📊 MongoDB Document Structure (`translations`)

```json
{
  "keyCode": "skill.cyclone.desc",
  "version": "1.0.0",
  "category": "SKILL",
  "tags": ["berserker", "active", "aoe"],
  "contextInfo": "Spin attack dealing AoE damage to all nearby enemies.",
  "baseValue": "Deals {damage}% damage.",
  "translations": {
    "pt": {
      "translatedValue": "{damage}% damage.",
      "status": "PENDING_APPROVAL",
      "lastModifiedBy": "translator_01",
      "updatedAt": "2026-06-01T19:00:00Z"
    }
  },
  "history": [
    {
      "locale": "pt",
      "modifiedBy": "translator_01",
      "previousValue": "",
      "newValue": "{damage}% damage",
      "action": "EDIT",
      "timestamp": "2026-06-01T19:00:00Z"
    }
  ]
}
```

---

## 🚀 Primary REST API Endpoints

*   `POST /api/auth/login` -> Simple authentication (Username/Password), returns JWT Token + Role.
*   `GET /api/translations?version={v}&tag={t}` -> Paged list of keys filtered by version and tag.
*   `PUT /api/translations/{version}/{keyCode}/{locale}` -> Inserts/Edits a translation, setting status to `PENDING_APPROVAL`.
*   `GET /api/translations/pending` -> Lists all items pending approval (Only accessible to `REVIEWER`).
*   `POST /api/translations/{version}/{keyCode}/{locale}/approve` -> Approves a translation, changing status to `APPROVED`.
*   `GET /api/translations/export/{locale}/{version}?format=json` -> Exports approved translations as a simple key-value JSON map:
    ```json
    {
      "skill.cyclone.name": "Cyclone",
      "skill.cyclone.desc": "{damage}% damage.",
      "item.chaos_reaver.name": "Chaos Reaver",
      "item.chaos_reaver.desc": "Some description."
    }
    ```
