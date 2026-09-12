---
id: import
sidebar_position: 13
title: SHOW IMPORT / IMPORTS
---

:::info[Note]
Uses the official Import REST API, not a Java SDK method.

- `SHOW IMPORT` / `SHOW PROGRESS OF IMPORT`：`POST /v2/vectordb/jobs/import/describe`
- `SHOW IMPORTS`：`POST /v2/vectordb/jobs/import/list`
:::

```text
SHOW [PROGRESS OF] IMPORT job_id;
SHOW IMPORTS FROM [TABLE] collection_name [WITH (option=value, ...)];
```

Returns JOB_ID (VARCHAR), STATE (VARCHAR), PROGRESS, TOTAL_ROWS, IMPORTED_ROWS (BIGINT), REASON (VARCHAR), and DETAILS (JSON). Missing server fields remain NULL. DETAILS retains file-level status.

```sql
SHOW IMPORT ?;
SHOW PROGRESS OF IMPORT ?;
SHOW IMPORTS FROM docs WITH (page_size=20,current_page=1);
```

Bind the job ID as a string. Connect to the service that owns the job. List pagination depends on the server REST version.
