---
id: import
slug: /features/milvus/sql/import
sidebar_position: 5
title: IMPORT
---

:::info[Note]
Uses the official Import REST API, not a Java SDK method.

- Create a job: `POST /v2/vectordb/jobs/import/create`
- Poll progress when waiting synchronously: `POST /v2/vectordb/jobs/import/describe`
:::

## Syntax

```text
IMPORT FROM [FILE] files INTO [TABLE] collection_name
    [PARTITION partition_name]
    [WITH (option = value [, ...])]
    [RETURNING JOB_ID];
```

files is a single path or a list of file groups and accepts a ? parameter. Milvus performs the import; the JDBC client does not read the file and issue per-row INSERT commands. Ordinary IMPORT returns update count 0; RETURNING JOB_ID returns a job-ID result set.


## Data Import (Import)

```sql
-- Files must already be in object storage accessible to Milvus
IMPORT FROM 'prepared/1.parquet' INTO TABLE table_name;
IMPORT FROM 'prepared/1.json' INTO TABLE table_name PARTITION partition_name;

-- Wait for the import task to finish, up to 60 seconds
/*+ timeout=60000 */ IMPORT FROM 'prepared/1.json' INTO TABLE table_name;

-- Submit the import task without waiting
/*+ sync=false */ IMPORT FROM 'prepared/1.json' INTO TABLE table_name;
```

Paths refer to prepared files in Milvus object storage, not local Java files; the driver does not read/upload files. Supply one path or [[...],[...]] groups, or bind List&lt;List&lt;String>> to ?. JSON/Parquet uses one file per group; NumPy groups contain related column files. See [official preparation/storage requirements](https://milvus.io/docs/v2.6.x/import-data.md).

Import uses the official REST API with JDBC endpoint/database/credentials; REST must be enabled. Job creation, listing and inspection use the same JDBC address within a connection. Ordinary IMPORT returns update count 0 (not imported rows); RETURNING JOB_ID returns one string ID. SHOW IMPORT and SHOW IMPORTS inspect jobs through JDBC.

`IMPORT FROM` waits for the Milvus Import task to finish by default. Use the `sync=false` hint to return asynchronously. The `timeout` hint sets the sync wait timeout in milliseconds.



## Multi-file Import and job inspection {#import}

```sql
/*+ sync=false */ IMPORT FROM [['prepared/a.parquet'],['prepared/b.parquet']]
    INTO docs WITH (timeout='2h') RETURNING JOB_ID;
/*+ sync=false */ IMPORT FROM [['prepared/id.npy','prepared/dense.npy']]
    INTO docs RETURNING JOB_ID;
```

RETURNING JOB_ID uses executeQuery; without RETURNING, update count remains zero. SHOW returns JOB_ID/STATE/REASON (VARCHAR), PROGRESS/TOTAL_ROWS/IMPORTED_ROWS (BIGINT), DETAILS (JSON). Missing server values are NULL; DETAILS preserves file-level status. List pagination depends on the target REST version; one statement does not promise to enumerate all historical jobs.

REST must be available and uses JDBC database/credentials. Submission and inspection use the same JDBC address within a connection; reconnect to the service hosting the original job. Files must already reside in the storage configured for the Milvus server: object-storage deployments use object paths, while standalone local-storage deployments use absolute paths readable by the server, not paths on the JDBC client. The driver does not generate/upload files; official BulkWriter can prepare them.

File preparation, storage visibility, permissions and resource limits are deployment responsibilities. Job states and failure reasons come from Milvus; the driver does not estimate completion from the submitted file count.

Default sync=true waits. Hint timeout is a client millisecond wait; WITH timeout is a server job duration string. Timeout/cancellation, Failed state or later HTTP failure retains the known Job ID and last progress, without cancelling/resubmitting the server job. A lost create response may leave an existing job with unknown ID; reconcile through the job list. Imports do not promise transactions or exactly-once execution.

Note: Statement.cancel() or a shared JDBC timeout may return its standard cancellation/timeout exception before adapter progress is available; it does not imply zero writes. For reliable recording of an import ID, submit with sync=false RETURNING JOB_ID and inspect separately.

See [Versions and Support](../compatibility.md) for version and feature requirements.

Inspect jobs with [SHOW IMPORT](../show/import.md).
