---
id: import
sidebar_position: 13
title: SHOW IMPORT / IMPORTS
---

:::info[说明]
使用官方 Import REST API，不对应 Java SDK 方法。

- `SHOW IMPORT` / `SHOW PROGRESS OF IMPORT`：`POST /v2/vectordb/jobs/import/describe`
- `SHOW IMPORTS`：`POST /v2/vectordb/jobs/import/list`
:::

```text
SHOW [PROGRESS OF] IMPORT job_id;
SHOW IMPORTS FROM [TABLE] collection_name [WITH (option=value, ...)];
```

返回 JOB_ID（VARCHAR）、STATE（VARCHAR）、PROGRESS、TOTAL_ROWS、IMPORTED_ROWS（BIGINT）、REASON（VARCHAR）和 DETAILS（JSON）。服务端未提供的字段为 NULL，DETAILS 保留文件级状态。

```sql
SHOW IMPORT ?;
SHOW PROGRESS OF IMPORT ?;
SHOW IMPORTS FROM docs WITH (page_size=20,current_page=1);
```

任务 ID 使用字符串参数；应连接任务所属服务。列表分页支持取决于服务端 REST 版本。
