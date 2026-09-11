---
id: commands
sidebar_position: 3
title: Syntax Manual
description: Current jdbc-milvus SQL subset, parameters, DDL, DML, vector and hybrid search, and JDBC results.
---

> **Version baseline**: Current 6.7.1-SNAPSHOT implementation, Java 17, Java SDK 2.6.22, minimum Milvus server 2.6.2. Version 2.5.x and earlier are unsupported; later versions are not automatically supported. See [Versions and Support](./compatibility.md).

See [Installation and Connection](../../drivers/milvus/connection.mdx) for dependencies and connections, and [JDBC Operations](./jdbc.mdx) for a complete program. This manual follows the current parser and command implementations; the full Milvus SDK and relational SQL are not implicitly supported.

TLS, certificates and Cloud use JDBC connection properties, not new SQL syntax or hints. SDK and Import REST share `secure`, `caPemPath`/`serverPemPath`, `clientPemPath`/`clientKeyPath` and `serverName`, and always use the JDBC URL host and port. Native Milvus 2.6.2 TLS listeners need a unified ingress for full Import support; the driver never switches to another port automatically. See [Connection Parameters and TLS](../../drivers/milvus/connection.mdx#tls) for combinations, certificate formats, deployment limits and examples.

## Reading Conventions

- `text` blocks are syntax templates: brackets such as `[IF EXISTS]` mark optional parts and must not be executed literally. `sql` blocks are syntax examples; substitute collection/field names and satisfy the schema before use. Vector brackets such as `[0.1, 0.2]` are actual syntax.
- Keywords are case-insensitive. Use ordinary identifiers; examples start with letters/underscores. Quoted identifiers and cross-database qualified-name resolution are not provided. Single/double quotes delimit strings, not quoted field names.
- ? binds WHERE, vectors, SET, VALUES, LIMIT/OFFSET, hints, query/field/function/index WITH values, and collection-level CREATE TABLE WITH values. Names, types, and constraint keywords are not bindable.
- SELECT supports `*`, a field list or a standalone `COUNT(*)`. AS, JOIN, GROUP BY, arbitrary projection expressions and scalar ORDER BY are unsupported. Distance ordering supports the documented single query vector, not multiple sort keys.
- Scalar filters support comparisons, AND/OR/NOT, parentheses, BETWEEN/NOT BETWEEN, LIKE, IN/NOT IN lists, IS NULL/IS NOT NULL and selected expression forwarding, not arbitrary relational functions. BETWEEN includes both bounds and maps to native `>=` and `<=` comparisons; bounds accept value parameters and reversed bounds are not swapped. IN and NOT IN accept [1,2], (1,2), or bound Lists; individual list elements can also be parameters. SQL NULL is a value; bind general negative numbers as parameters (DEFAULT separately supports signs).
- Milvus 2.6.2 does not accept a template parameter on the right of LIKE. Although `LIKE ?` parses and binds through the SDK, the server rejects it. Fixed SQL patterns are usable; do not concatenate untrusted input. Native SDK integer template comparisons such as `NOT (age = ?)` and double NOT can also trigger a 2.6.2 QueryNode assertion. Ordinary `age != ?`, literal NOT, and `NOT (field IS NULL)` are outside this issue. The driver preserves SQL NOT precedence and translates `<>` to native `!=`, but does not embed parameters or eliminate NOT to bypass server failures.
- Semicolons separate multiple commands. Parameter numbering continues across statements and results use JDBC multiple-result access. This is neither a transaction nor JDBC batch.


## 1. Database Management {#database}

### Create Database

```text
CREATE DATABASE [IF NOT EXISTS] db_name;
CREATE DATABASE [IF NOT EXISTS] db_name WITH ("key" = "value", ...);
```

WITH values support `?` binding and are passed as strings to the native CreateDatabaseReq. IF NOT EXISTS leaves an existing database's properties unchanged; use ALTER DATABASE to modify them.

### Drop Database

```text
DROP DATABASE [IF EXISTS] db_name;
```

### Alter Database Properties

```text
ALTER DATABASE db_name SET PROPERTIES ("key" = "value", ...);
ALTER DATABASE db_name DROP PROPERTIES ("key", ...);
```

SET values can be bound with `?`; property names cannot. DROP removes only the listed properties, not the database.

### Show Databases

```sql
SHOW DATABASES;
SHOW DATABASE db_name;
```

SHOW DATABASES returns database names. SHOW DATABASE returns one row with `DATABASE` and `PROPERTIES`. PROPERTIES is JSON object text containing the properties returned by the server; read it using `ResultSet.getString("PROPERTIES")` and parse it as needed. Missing databases or permission errors produce SQLException, not fabricated empty properties. Allowed property names, values and dynamic-application rules are determined by the Milvus server.

---

## 2. Table & Partition Management {#table}

### Create Table (Collection)

Supports defining primary keys, vector fields, and other attributes.
```text
CREATE TABLE [IF NOT EXISTS] table_name (
    id INT64 PRIMARY KEY,
    vector_col FLOAT_VECTOR(2),
    age INT32 DEFAULT 0
) WITH (
    consistency_level = "Strong"
);
```

### Field Types and Collection Options

| Type/constraint | Current implementation boundary |
| --- | --- |
| `BOOL`, `INT8/16/32/64`, `FLOAT`, `DOUBLE` | Scalar field types; the Boolean DDL keyword is BOOL. |
| `VARCHAR(n)` | n maps to max_length. Int64 or VarChar can be primary keys, subject to SDK/server validation. |
| `JSON` | JSON fields; schema-aware INSERT/UPSERT encoding does not imply support for every JSON query expression. |
| `FLOAT_VECTOR(dim)` | Main read/write/search paths support numeric Lists and primitive arrays. |
| `BINARY_VECTOR(dim)`, `FLOAT16_VECTOR(dim)`, `BFLOAT16_VECTOR(dim)`, `SPARSE_FLOAT_VECTOR` | Read/write and KNN/range search mappings. Sparse has no dimension; legacy (dim) parses but is not forwarded. |
| `INT8_VECTOR(dim)` | One signed integer from -128 to 127 per dimension, stored as bytes. Supports KNN/range and Hybrid search with HNSW indexes; floating-point vectors are not automatically quantized. |
| `ARRAY<element_type>(max_capacity)` | BOOL, INT8/16/32/64, FLOAT, DOUBLE or VARCHAR(n); capacity 1–4096. Nested/NULL elements and bare ARRAY are rejected. |
| `PRIMARY KEY`, `AUTO_ID`, `NULL`, `NOT NULL`, `COMMENT 'text'` | NOT NULL by default; NULL enables nullable. Nullable primary keys and conflicting constraints are rejected. Dynamic fields remain disabled. |

CREATE TABLE WITH accepts the following options, whose values can be bound with `?`. Unknown or duplicate names, NULL, and incorrect types produce errors rather than being silently ignored. Bind field, function, and collection-option parameters in their SQL order. DQL WITH and connection-property rules do not apply to DDL.

| Creation option | SDK field | Type and default |
| --- | --- | --- |
| `consistency_level` | consistencyLevel | Strong / Bounded / Session / Eventually; SDK default Bounded. |
| `num_partitions` | numPartitions | Positive INT32 integer; requires a PARTITION KEY. If omitted, the server chooses its default partition count. |
| `num_shards` | numShards | Positive INT32 integer; SDK default 1. The server validates its supported range. |
| `description` | description | String, empty by default; bind with setString or setObject. It is not executed as SQL. |

### Partition and Clustering Keys {#collection-keys}

```sql
CREATE TABLE tenant_books (
    id INT64 PRIMARY KEY,
    tenant VARCHAR(64) PARTITION KEY,
    age INT32 CLUSTERING KEY,
    v FLOAT_VECTOR(2)
) WITH (num_partitions=4, num_shards=1, consistency_level=Strong);
```

`PARTITION KEY` maps to SDK `isPartitionKey`, using a non-primary, non-nullable INT64 or VARCHAR field. Each collection allows at most one partition key. Milvus assigns physical partitions from key values; the driver does not calculate routing. `num_partitions` is not a tenant or host count. Filter by bound key values in WHERE; do not treat partitions as an authorization boundary. Manual partition operations in partition-key mode are subject to server restrictions; see [Partition Keys](https://milvus.io/docs/v2.6.x/use-partition-key.md).

`CLUSTERING KEY` maps to `isClusteringKey`. Each collection allows at most one, which can share a field with the partition key. Supported scalar types and compaction requirements are determined by the SDK/server. Setting the flag does not trigger compaction or guarantee a performance gain. The existing `COMPACT ... WITH (is_clustering=true)` submits a native task, but the cluster needs clustering compaction and pruning configured; see [Clustering Compaction](https://milvus.io/docs/v2.6.x/clustering-compaction.md). ADD COLUMN cannot introduce either key after creation.

SHOW TABLE appends Boolean `PARTITION_KEY` and `CLUSTERING_KEY` columns. SHOW CREATE preserves these constraints and the partition count, shard count, consistency, and description returned by the server. It is not a complete backup script containing indexes, permissions, and arbitrary collection properties.

### Default Values

`DEFAULT` is supported on non-primary scalar fields: `BOOL`, `INT8/INT16/INT32/INT64`, `FLOAT/DOUBLE`, and `VARCHAR`. Numeric defaults may have a sign. Integer defaults must fit exactly, floating defaults must be finite, and strings must fit the declared UTF-8 byte length. Invalid or duplicate defaults, and defaults on primary keys, JSON, Array, or vector fields, are rejected before CREATE is sent.

For ordinary INSERT or full UPSERT with omitted/null-bound fields, Milvus applies DEFAULT according to SDK rules. Nullable fields without defaults store NULL; the SDK/server validates non-nullable fields. SHOW CREATE preserves defaults, nullable, Array element/capacity, analyzers and functions. Nullable vectors require 2.6.18+ and do not support IS NULL/IS NOT NULL predicates; scalar nullable/Array retain the 2.6.2 baseline.

### Vector Data Format Support

In statements involving vector operations such as `INSERT`, `SEARCH` (SELECT ... ORDER BY vector), `DELETE`, multiple vector expression forms are supported:

1. **SQL Array Literal**:
   - `[0.1, 0.2]`
2. **JDBC Parameter Binding**:
   - `?` (PreparedStatement)
   - FloatVector accepts numeric lists (`List<? extends Number>`), including `List<Byte>`, `List<Short>`, `List<Integer>`, `List<Long>`, `List<Float>`, and `List<Double>`.
   - Six one-dimensional numeric primitive arrays are supported: `byte[]`, `short[]`, `int[]`, `long[]`, `float[]`, and `double[]`. Bind with `PreparedStatement.setObject(index, vector)`.
   - Applies to FloatVector INSERT/UPSERT and KNN/range vector conditions in SELECT, UPDATE, and DELETE (`vector_range` or distance comparisons). UPDATE SET values are sent through native partial update.
   - Vector queries and INSERT/UPSERT convert elements to Float; large integers and doubles may lose precision. A `byte[]` is converted element by element as signed numbers, not interpreted as BinaryVector bits.
   - `boolean[]`, `char[]`, boxed arrays such as `Float[]`, and multidimensional Java arrays are not supported as a single FloatVector. Dimensions must match the field schema.
3. **Single Query Vector**:
   - `ORDER BY vector_col <-> ?` accepts one query vector, such as `[1, 1]`, with or without LIMIT.
   - Nested vector lists such as `[[1, 1], [99, 99]]` (including `[[1, 1]]`) cause a parameter error. The same restriction applies to UPDATE/DELETE distance ordering and vector range conditions.
   - Milvus SDK batch search with multiple query vectors is not exposed through this ORDER BY syntax and is distinct from JDBC `addBatch`/`executeBatch`. SELECT continues to return one result set with the requested output fields.

### Drop Table

```text
DROP TABLE [IF EXISTS] table_name;
```

### Truncate a Collection {#truncate}

```sql
TRUNCATE TABLE books;
TRUNCATE TABLE books IN DATABASE archive_db;
```

**This clears all data in the specified collection.** It directly calls official `truncateCollection`, preserving the schema, indexes, and aliases. It is neither a DELETE alias nor a DROP/CREATE reconstruction. WHERE, LIMIT, partition targets, and wildcards are not available. `fetchSize`, `setMaxRows`, and query hints do not limit the number of cleared rows. Names are plain SQL identifiers, not `?` value parameters. The current connection database is the default; explicit `IN DATABASE` does not change `Connection.getCatalog()`.

Use `executeUpdate()` or `execute()`. Success returns update count 0 as SDK acknowledgement, not zero deleted rows; there is no affected-row count, generated key, or ResultSet. JDBC transactions cannot roll this operation back. A timeout or disconnected client does not prove the server did not execute it. The driver adds no retries, entity scans, or alternate deletion strategy. Confirm the target and recovery plan before execution.

The native API was introduced in Milvus 2.6.11; see the [official release notes](https://milvus.io/docs/v2.6.x/release_notes.md#v2611) and [Java API](https://milvus.io/api-reference/java/v2.6.x/v2/Collections/truncateCollection.md). The 2.6.2 baseline returns native `UNIMPLEMENTED`; that failure has been verified to preserve the schema, indexes, and rows and prevent a later DELETE in the same SQL sequence. No legacy fallback is provided. Successful clearing, schema/index/alias preservation, and subsequent writes on newer servers have not yet been verified against a real service.

### Rename Table {#rename}

```text
ALTER TABLE old_name RENAME TO new_name [IN DATABASE target_database];
```

Without `IN DATABASE`, the collection is renamed within the current connection's database. With it, the driver sets SDK `RenameCollectionReq.targetDbName`, and Milvus performs the native move and rename. The source database always comes from the JDBC connection. `Connection.getCatalog()` does not change, and subsequent statements still use the original database.

```sql
ALTER TABLE books RENAME TO archived_books IN DATABASE archive;
```

The destination database must exist, and the caller needs the required permissions. This is an operation within one Milvus instance/cluster, not cross-cluster copying. The driver does not create databases, copy entities, rebuild indexes, rewrite application SQL, or adjust grants. Name conflicts, missing databases, and server restrictions propagate as SQLException without fallback operations. Success returns update count 0, not the number of moved rows.

Database and collection names are SQL identifiers, not `?` value parameters. After the operation, use a JDBC URL targeting the destination database to access the new name. The old name does not automatically become an alias. See the [native rename API](https://milvus.io/api-reference/java/v2.6.x/v2/Collections/renameCollection.md).

Milvus 2.6.2 verifies local renaming of an unloaded collection, cross-database round trips, vector search and data updates after moving, and unchanged connection databases. The test explicitly releases the collection before moving it back. Check the target server's rules for grant inheritance, alias associations, and other load-state combinations.

### Online Field and Property Changes

```sql
ALTER TABLE books ADD COLUMN priority INT64 NULL DEFAULT 7;
ALTER TABLE books ALTER COLUMN title SET PROPERTIES (max_length=1024);
ALTER TABLE books SET PROPERTIES ('collection.ttl.seconds'=3600);
ALTER TABLE books DROP PROPERTIES ('collection.ttl.seconds');
ALTER INDEX title_idx ON books SET PROPERTIES ('mmap.enabled'=true);
ALTER INDEX title_idx ON books DROP PROPERTIES ('mmap.enabled');
```

Field properties can also be removed with `ALTER TABLE books ALTER COLUMN title DROP PROPERTIES ('key')`. SET accepts scalar parameter values; NULL does not remove a property. Use DROP PROPERTIES instead.

These commands invoke native SDK online-change APIs without rebuilding collections or copying entities. ADD COLUMN reuses field definitions from CREATE TABLE; new fields must explicitly declare NULL and cannot be primary keys or AUTO_ID fields. Allowed field types, mutable/removable properties and load-state requirements depend on the server version. An API being present in the SDK does not imply support in every 2.6.x patch release. Server rejection becomes SQLException, with no emulated fallback. Successful DDL returns update count 0, not an affected-entity count.

### Show Tables

```sql
SHOW TABLES;                        -- List all tables
SHOW TABLE table_name;              -- Inspect fields
SHOW CREATE TABLE table_name;       -- View detailed create table statement
```

### Partition Management {#partition}

Read native collection and partition statistics with:

```sql
SHOW STATS FROM table_name;
SHOW STATS FROM table_name PARTITION partition_name;
```

The result is one row containing `NUM_ENTITIES` (BIGINT) and `STATS` (VARCHAR, the original statistics map serialized as a JSON object). These expose the SDK's entity count and statistics details. The command reads server statistics without WHERE, implicit FLUSH or a client-side scan. Use COUNT for a filtered logical row count; do not interpret a statistics snapshot as a strongly consistent, transactional count. Native APIs: [collection statistics](https://milvus.io/api-reference/java/v2.6.x/v2/Collections/getCollectionStats.md) and [partition statistics](https://milvus.io/api-reference/java/v2.6.x/v2/Partitions/getPartitionStats.md).

```text
CREATE PARTITION [IF NOT EXISTS] partition_name ON TABLE table_name;
DROP PARTITION [IF EXISTS] partition_name ON TABLE table_name;
SHOW PARTITIONS FROM table_name;    -- List all partitions of a table
SHOW PARTITION p_name ON TABLE t_name;
```

### Alias Management {#alias}

```text
CREATE ALIAS alias_name FOR TABLE table_name;
ALTER ALIAS alias_name FOR TABLE table_name;
DROP ALIAS [IF EXISTS] alias_name;
SHOW ALIASES FROM [TABLE] table_name;
SHOW ALIAS alias_name;
```

SHOW ALIASES lists aliases for the specified collection, returning `ALIAS` and `TABLE` (VARCHAR) per row. An empty list retains column metadata. SHOW ALIAS describes one alias in a single row with `DATABASE`, `ALIAS` and `TABLE` (VARCHAR); missing aliases or permission errors produce SQLException. Names are not value parameters and cannot be replaced with `?`. The server determines list order; `Statement.setMaxRows()` can limit returned rows.

Milvus changes alias targets without copying entities. Queries newly executed after the change address the new collection; already-open ResultSets are not reexecuted, and there is no cross-query transaction guarantee.

---

## 3. Index Management {#index}

### Create Index

```sql
CREATE INDEX index_name ON TABLE table_name (vector_col) USING 'IVF_FLAT' WITH (nlist = 1024, metric_type = 'L2');
```

#### Index Options and Boundaries

USING is resolved against SDK 2.6.22 IndexType; omission keeps SDK AUTOINDEX. Common FloatVector types include FLAT, IVF_FLAT, IVF_SQ8, IVF_PQ, HNSW, SCANN, DISKANN and AUTOINDEX. Common scalar types include STL_SORT, TRIE, INVERTED and BITMAP. RNSG and ANNOY from the old documentation are absent from this SDK enum.

Availability depends on server version, field type and environment. Binary indexes include BIN_FLAT/BIN_IVF_FLAT; sparse indexes include SPARSE_INVERTED_INDEX/SPARSE_WAND. SDK enums are not a guarantee for every deployment.

Index WITH maps metric_type (or metric) to the metric and other entries to extraParams. Values support strings, integers, decimals, booleans, identifiers and scalar ? bindings, not inline nested JSON objects. Special formats must follow the SDK string contract. Index metrics must match query distance operators.

CREATE INDEX currently waits synchronously with an SDK wait limit of 600000ms. It does not consume the IMPORT/LOAD/RELEASE sync/timeout hints.

### Drop Index

```sql
DROP INDEX index_name ON TABLE table_name;
```

### Show Indexes {#index-metadata}

```sql
SHOW INDEXES FROM table_name;
SHOW INDEX index_name ON TABLE table_name;
SHOW PROGRESS OF INDEX ON TABLE table_name;
SHOW PROGRESS OF INDEX index_name ON TABLE table_name;
```

Index details return four columns: `INDEX`, `FIELD`, `ID`, and `PARAMS`. A collection without indexes returns an empty ResultSet with column metadata preserved. For all indexes, the driver calls SDK `listIndexes` followed by `describeIndex` for each name; a named request describes that index directly. These calls are not an atomic snapshot. If an index disappears or a native request fails, SQLException is propagated rather than returning the collected partial list as success.

Progress returns one row containing BIGINT `TOTAL` and `INDEXED`, the SDK total-row and indexed-row counters. Omitting the index name sums the counters across indexes. The same entities may be counted twice when they have two indexes, so this is not collection cardinality or a replacement for COUNT. Both counters are 0 when there are no indexes; Milvus controls when native counters refresh. Empty lists, multiple index creation/removal, named queries, and progress aggregation over two FLAT indexes have been verified on 2.6.2.

---

## 4. User & Role Management {#user}

### User Management

```text
CREATE USER [IF NOT EXISTS] username PASSWORD 'password' [WITH (description='...')];
ALTER USER username PASSWORD 'new-password' REPLACE 'old-password';
DROP USER [IF EXISTS] username;
SHOW USERS;
SHOW USER username;
```

The CREATE USER password and both ALTER USER passwords accept `?`, bound with `PreparedStatement.setString()`. Usernames remain SQL identifiers, not value parameters. ALTER USER binds the new password followed by the old password in SQL order, calls SDK `updatePassword` directly, and returns update count 0 on success. The server determines password constraints, authorization, and old-password validation. The driver does not implement local authentication or automatically replay a successful password change.

```java
try (PreparedStatement stmt = conn.prepareStatement(
        "ALTER USER app_user PASSWORD ? REPLACE ?")) {
    stmt.setString(1, newPassword);
    stmt.setString(2, oldPassword);
    stmt.executeUpdate();
}
```

ALTER USER optionally accepts `WITH (reset_connection=false, description='...')`, with parameter binding for both values. `reset_connection` is a boolean and preserves the SDK default of false. Setting it to true makes the SDK rebuild the current client connection as the modified user with the new password and clears the previous token, potentially switching the connection identity. It is not a side-effect-free refresh switch. `description` is a string passed directly to the SDK; support depends on the server version. Omitting it preserves the SDK default. Password modification and subsequent reconnection are not atomic: a reconnect failure does not mean the password remained unchanged. Other pooled connections are not updated automatically.

SHOW USER returns one row with VARCHAR columns USER, ROLES, and DESCRIPTION. ROLES is a JSON array of role names, or `[]` if there are none. This reads SDK user information; it does not return passwords or expand roles into effective privileges. The driver does not fabricate description information absent from the SDK/server.

### User and Role Descriptions {#principal-descriptions}

Creation also accepts `WITH (description=?)`, passed to SDK `CreateUserReq.description` or `CreateRoleReq.description`. Descriptions must be non-NULL strings; empty strings are allowed. Omitting the option preserves the SDK empty-string default. User creation binds the password and then the description in SQL order. If `IF NOT EXISTS` finds an existing principal, it changes neither its password nor its description. Parameters are still consumed and validated, preserving the bindings of subsequent statements in the same execution.

```sql
CREATE USER app_user PASSWORD ? WITH (description=?);
CREATE ROLE reader WITH (description=?);
```

**Milvus 2.6.2 accepts these creation requests but returns empty descriptions through SHOW USER / SHOW ROLE**, as does the native SDK. Successful creation does not prove description persistence. The driver does not cache the input to populate DESCRIPTION. If stored descriptions are required, confirm native support in the deployed server version.

```sql
ALTER USER app_user WITH (description='Application query account');
ALTER ROLE reader WITH (description='Read-only access role');
ALTER USER app_user WITH (description=?);
```

These independent commands call SDK `updateUser` and `alterRole`, respectively, without calling password-change, role-recreation, or authorization APIs. WITH accepts only `description`, a non-NULL string; an empty string clears the description. Bind description values with `PreparedStatement.setString()`; names remain SQL identifiers. Successful SDK calls return update count 0, which does not mean that zero users or roles were changed.

**Milvus 2.6.2 does not support these two description updates**: its user update validates the request as a password change and returns a password-length error; its role update returns `UNIMPLEMENTED`. The driver propagates failures without resetting passwords or recreating roles to simulate support. Use these commands only when the deployed server supports the corresponding native APIs. Their availability in SDK 2.6.22 does not imply support on every 2.6.x server. Description updates and reads on other server versions have not yet been verified against a real server.

### Role Management

```text
CREATE ROLE [IF NOT EXISTS] role_name [WITH (description='...')];
DROP ROLE [IF EXISTS] role_name [WITH (force_drop=false)];
SHOW ROLES;
SHOW ROLE role_name;
```

`force_drop` is a non-NULL boolean, bindable with `setBoolean()`. Omitting it preserves the SDK default of false. Setting it to true passes `DropRoleReq.forceDrop` directly to the server to remove the role, its grants, and its memberships. This can revoke users' access, so enable it only when forced deletion is intended. On Milvus 2.6.2, false fails for a role with grants and members; true removes the role and its associations but keeps the user. The driver does not emulate this through individual revocations, provide a transaction, or replay failed operations. `IF EXISTS` handles only a missing role, not invalid parameters, permission errors, or SDK failures. Success or an absent role returns update count 0.

SHOW ROLE calls SDK `describeRole` and returns one row with VARCHAR columns ROLE, DESCRIPTION, and GRANTS. GRANTS is a JSON array of direct grants scoped to the current connection database; an empty array (`[]`) still retains the role detail row. Entries preserve SDK fields such as `objectType`, `objectName`, `privilege`, `grantor`, and `dbName`; fields absent from the SDK response may be omitted. The description is returned as provided by the SDK; an empty string or NULL does not imply support for modifying descriptions. This query does not expand role membership or compute all effective privileges of a user. The SDK reads grants and then role information in separate requests, not a transactional snapshot.

### Grant & Revoke

```sql
GRANT ROLE role_name TO username;
REVOKE ROLE role_name FROM username;

GRANT Search ON Collection table_name TO ROLE role_name;
REVOKE Search ON Collection table_name FROM ROLE role_name;
GRANT Query ON Collection * TO ROLE role_name;
SHOW GRANTS FOR ROLE role_name;
SHOW GRANTS FOR ROLE role_name ON GLOBAL;
SHOW GRANTS FOR ROLE role_name ON TABLE table_name;
SHOW GRANTS FOR ROLE role_name ON USER username;
```

Use a connection identity authorized to perform grants. Even with authentication checks disabled, Milvus 2.6.2 needs the connection's authentication metadata to identify the grantor; anonymous connections may receive a missing-authorization error. The driver never supplies administrator credentials automatically. The ROLE column is taken from SDK role details when an individual grant does not contain the role name.

Privilege names and GRANT/REVOKE object types are forwarded unchanged to the SDK. The example object type is Collection, not TABLE; use server-recognized spelling and privileges. SHOW GRANTS instead uses this adapter's ON TABLE/USER/GLOBAL filter syntax.

Without ON, grants within the SDK query scope are returned, not an aggregate across every database. ON TABLE / USER filters by object type and exact name; ON GLOBAL returns only Global records. This lists grants, not effective permissions derived from global or wildcard grants.

### Privileges with an Explicit Database Scope {#scoped-privileges}

```sql
GRANT PRIVILEGE Search ON DATABASE app_db TABLE books TO ROLE reader;
REVOKE PRIVILEGE Search ON DATABASE app_db TABLE books FROM ROLE reader;
-- Explicitly grant access to all collections in app_db; the server interprets the privilege group
GRANT PRIVILEGE CollectionReadOnly ON DATABASE app_db TABLE * TO ROLE reader;
REVOKE PRIVILEGE CollectionReadOnly ON DATABASE app_db TABLE * FROM ROLE reader;
```

The `PRIVILEGE ... ON DATABASE ... TABLE ...` forms call SDK `grantPrivilegeV2` and `revokePrivilegeV2`, respectively, and are distinct from the legacy `GRANT Search ON Collection ...` commands above. Both database and collection scopes are required: the driver neither substitutes the current connection database nor inserts `*`. Either scope can explicitly be `*`; the server validates the effective scope and privilege-group names. Names and privileges are SQL identifiers, not values bindable with `?`.

Execution leaves the current connection database unchanged and returns update count 0 on success (SDK acknowledgment, not the number of grants). Use `SHOW ROLE reader ON DATABASE app_db` to inspect grants in a specific database, or explicitly use `ON DATABASE *` for the SDK wildcard database scope. Omitting ON DATABASE uses the connection database. Switching the current connection database with JDBC `setCatalog()` is neither needed nor supported. Grant records do not prove authorization enforcement. An unsupported V2 API or rejected operation raises SQLException without falling back to the legacy API that lacks an explicit database scope.

---

### Privilege Group Definitions {#privilege-groups}

```sql
CREATE PRIVILEGE GROUP app_query;
ALTER PRIVILEGE GROUP app_query ADD (Search, Query);
SHOW PRIVILEGE GROUPS;
ALTER PRIVILEGE GROUP app_query DROP (Query);
DROP PRIVILEGE GROUP app_query;
```

These call SDK `createPrivilegeGroup`, `addPrivilegesToGroup`, `listPrivilegeGroups`, `removePrivilegesFromGroup`, and `dropPrivilegeGroup`, respectively. `ALTER ... DROP (...)` removes only the listed privileges; `DROP PRIVILEGE GROUP` deletes the group itself. ADD/DROP lists require at least one SQL identifier. Group and privilege names are not `?` value parameters. Each list is submitted in one SDK request, without splitting it into individual privilege requests or promising cross-command transactions.

SHOW returns VARCHAR columns PRIVILEGE_GROUP and PRIVILEGES, with one row per group. PRIVILEGES is a JSON array of privilege names, or `[]` for an empty group. An empty group listing retains column metadata without returning data rows. Group and member ordering follows the SDK response without a sorting guarantee. JDBC `setMaxRows` limits group rows only, not the privileges within a group.

These commands manage group definitions; they neither create roles nor grant privileges automatically, and dropping a group does not automatically revoke role grants. To assign a group to a role, use the preceding `GRANT PRIVILEGE ... ON DATABASE ... TABLE ...` syntax. Modifying an assigned group may affect roles that use it. The server determines built-in group protection, privilege-name validity, and whether an in-use group can be dropped; failures raise SQLException. Successful changes return update count 0 as SDK acknowledgment, not the number of privileges changed.

### Inspect Analyzer Results {#analyze}

```sql
ANALYZE 'hello world' WITH (analyzer_params='{"tokenizer":"standard"}');
ANALYZE ['hello world', '', 'milvus'] WITH (with_detail=true, with_hash=true);
ANALYZE ? WITH (analyzer_params=?, with_detail=true);
-- Collection field context depends on the server version and field configuration
ANALYZE ? ON TABLE books(body) WITH (analyzer_names=?);
```

ANALYZE calls official SDK `runAnalyzer`; it neither scans a collection nor tokenizes in Java. Input is a string or a nonempty list of strings. Bind a single text with `setString`, or a string list/`String[]` with `setObject`. List elements cannot be NULL, but empty strings are allowed. Inputs and WITH options bind in SQL order; text is never interpolated into an expression.

WITH accepts the following options and rejects unknown names:

| Option | Value and behavior |
| --- | --- |
| `analyzer_params` | Map, JsonObject, or JSON object string, passed to the SDK as analyzer configuration. |
| `with_detail` | Boolean; preserves the SDK default of false. |
| `with_hash` | Boolean; preserves the SDK default of false. |
| `analyzer_names` | List of non-NULL strings; preserves the SDK empty-list default. The server validates selection semantics and combinations with field configuration. |

Every option value supports `?`. Optional `ON TABLE collection(field)` sets the SDK collection and field context, using the connection database. Omitting it preserves the SDK's empty collection/field defaults. No collection is created, modified, or loaded. The server determines supported analyzer names, languages, field contexts, and option combinations.

One JDBC ResultSet is returned, with a row for each SDK text result: TEXT_INDEX is a 1-based BIGINT ordinal, and TOKENS is a VARCHAR JSON array. Tokens preserve SDK fields `token`, `startOffset`, `endOffset`, `position`, `positionLength`, and `hash`. Missing SDK fields may be omitted; disabled detail fields may contain default zero values, which are not meaningful measurements. Offsets retain server semantics rather than being converted to Java character indexes. The SDK's unsigned 32-bit hash is represented as a long. Texts without tokens still have a row containing `[]`. `setMaxRows` limits text-result rows, not the token array for a text or the number of inputs analyzed by the server.

Milvus 2.6.2 has been verified for a standalone standard analyzer, multiple and empty texts, detail/hash options, parameter binding, and statement reuse. Collection-field context and named-analyzer options are mapped and unit-tested but have not been verified against a real server; SDK fields alone do not establish support on every 2.6.x server.

## 5. Data Manipulation (DML) {#dml}

INSERT/UPSERT accepts multiple VALUES tuples with explicit columns, or VALUES ? bound to an Iterable/Iterator of Maps (columns optional), Lists or Object[] (columns required). INSERT SELECT is unsupported. Counts reflect SDK acknowledgements; RETURN_GENERATED_KEYS additionally exposes the standard JDBC key cursor.

### Insert Data

```sql
-- Insert into default partition
INSERT INTO table_name (id, vector, age) VALUES (1, [0.1, 0.2], 10);

-- Insert into specific partition
INSERT INTO table_name PARTITION partition_name (id, vector) VALUES (2, [0.3, 0.4]);
```

### Upsert (Insert or Replace) {#upsert}

```sql
-- Upsert into default partition
UPSERT INTO table_name (id, vector, age) VALUES (1, [0.1, 0.2], 10);

-- Upsert into specific partition
UPSERT INTO table_name PARTITION partition_name (id, vector) VALUES (2, [0.3, 0.4]);

-- Native partial update: change only age for existing keys; new keys follow server insert rules
/*+ partial_update=true */ UPSERT INTO table_name (id, age) VALUES (?, ?);
```

`partial_update` is a boolean UPSERT hint, optionally bound with `?`, mapped to SDK `UpsertReq.partialUpdate`. Omission or false retains full replacement; true uses Milvus 2.6.2+ native partial update, preserving omitted fields of existing entities. New keys must still satisfy required fields, defaults, and function inputs in the schema; the driver does not first query a complete entity. The hint is invalid for INSERT and rejects non-boolean values. Multiple pages are not atomic or collectively reversible, and ambiguous writes are not automatically replayed.

INSERT/UPSERT and UPDATE SET convert JSON, vectors and Array according to schema. JSON strings, Maps, Lists, JsonElements and numeric arrays are encoded as JSON without FloatVector precision conversion. This is not a lossless guarantee for arbitrary inputs; SDK/server validation still applies.

### Update Data

> **Note**: UPDATE uses Milvus 2.6.2+ native partial update. The driver selects primary keys page by page and writes only the primary key and SET fields, without collecting the entire selection or rewriting untouched fields. Pages do not form a transaction and cannot be rolled back together. Concurrent writes to the same SET field follow Milvus write semantics.

#### 1. Basic Update (Scalar Filtering)

Use the scalar WHERE syntax in this manual. Omitting WHERE and vector conditions updates the entire collection; specify conditions or LIMIT as needed. Primary-key updates are prohibited. SET assigns values/constants/parameters, not per-row expressions such as `age = age + 1`.

```sql
-- Update by Primary Key
UPDATE table_name SET age = 20 WHERE id = 1;

-- Select primary keys by scalar condition, then submit partial updates
UPDATE table_name SET status = 'active' WHERE age > 18;
```

#### 2. KNN Update (Nearest Neighbor Update)

Update the K records closest to the target vector.
Underlying mechanism: iterate selected primary keys by distance -> partial upsert each page.

```sql
-- Update status to 1 for the 1 record closest to [0.1, 0.2]
UPDATE table_name SET status = 1 ORDER BY vector_col <-> [0.1, 0.2] LIMIT 1;
```

#### 3. Range Update

Update all records falling within a specified distance (radius) of the target vector.
Can use `vector_range` function or `<->` comparison expression.

```sql
-- Using vector_range function (Recommended)
-- Syntax: vector_range(vector_field, target_vector, radius)
UPDATE table_name SET tag = 'A' WHERE vector_range(vector_col, [0.1, 0.2], 0.5);

-- Using comparison expression
UPDATE table_name SET tag = 'A' WHERE vector_col <-> [0.1, 0.2] < 0.5;
```

### Delete Data

> **Note**: DELETE without WHERE deletes all records in the collection, or only in the specified PARTITION. Confirm the scope before executing it. Scalar DELETE without LIMIT directly submits a server filter. With LIMIT it selects primary keys page by page before deletion. Vector DELETE searches primary keys page by page before deleting, without whole-SQL atomicity or rollback.

For scalar DELETE without WHERE, the driver builds a `primary_key IS NOT NULL` filter using the collection's actual primary-key field. Milvus primary keys cannot be NULL, so this covers all records, including negative, zero, and string keys; the field does not need to be named `id`. This conversion applies only when WHERE is omitted and never rewrites a supplied condition. It does not substitute `1=1`, which the native Milvus 2.6.2 execution path does not support as an always-true filter. Existing LIMIT, partition, and JDBC fetchSize semantics are preserved; fetchSize controls the read page size, not the total deletion limit.

#### 1. Basic Delete (Scalar Filtering)

Use the scalar WHERE syntax documented here.

```sql
-- Delete all records while preserving the collection, schema, and indexes
DELETE FROM table_name;

-- Delete at most 1000 records; without ordering, the selected records are unspecified
DELETE FROM table_name LIMIT 1000;

-- Delete all records only from the specified partition
DELETE FROM table_name PARTITION partition_name;

-- Delete by Primary Key
DELETE FROM table_name WHERE id = 1;
DELETE FROM table_name WHERE id IN [1, 2, 3];

-- Delete by scalar condition (direct server-side filter deletion without LIMIT)
DELETE FROM table_name WHERE age > 18 AND status = 'inactive';

-- Delete from specific partition
DELETE FROM table_name PARTITION partition_name WHERE age > 10;
```

#### 2. KNN Delete (Nearest Neighbor Delete)

Use ORDER BY for distance ordering. LIMIT caps the selected count; without LIMIT, the driver iterates the complete matching search result.
Underlying mechanism: select primary keys by distance page by page -> delete each page.

```sql
-- Delete 100 records closest to [0.1, 0.2]
DELETE FROM table_name ORDER BY vector_col <-> [0.1, 0.2] LIMIT 100;

-- Combined with scalar filtering: Delete 10 most similar records where category='book'
DELETE FROM table_name WHERE category = 'book' ORDER BY vector_col <-> [0.1, 0.2] LIMIT 10;
```

#### 3. Range Delete

Delete all records falling within a specified distance (radius) of the target vector. Can use `vector_range` function or `<->` comparison expression.
Underlying mechanism: iterate primary keys selected by range search -> delete each page.

```sql
-- Using vector_range function (Recommended)
DELETE FROM table_name WHERE vector_range(vector_col, [0.1, 0.2], 0.5);

-- Using comparison expression: Delete all records with distance < 0.5 from [0.1, 0.2]
DELETE FROM table_name WHERE vector_col <-> [0.1, 0.2] < 0.5;

-- Combined with LIMIT for protection (Delete at most 1000 matching records)
DELETE FROM table_name WHERE vector_range(vector_col, [0.1, 0.2], 0.5) LIMIT 1000;
```

### Paged Writes, Cancellation, and Retries

- UPDATE/DELETE take LIMIT from SQL itself. SELECT overwrite_find_limit/skip hints and JDBC maxRows do not cap DML writes. Explicit vector UPDATE/DELETE LIMIT currently accepts at most Integer.MAX_VALUE; scalar LIMIT uses long. Omitted LIMIT still imposes no driver total cap.
- UPDATE, scalar DELETE with LIMIT, and KNN/range DELETE use iterators. Omitted LIMIT has no arbitrary total cap. Scalar DELETE without LIMIT uses server-side filter deletion directly.
- `Statement.setFetchSize(n)` sets page size, not a total limit. Unset fetchSize uses the SDK default; values above the SDK page maximum are capped to that page maximum.
- After `Statement.cancel()` or query timeout takes effect, the driver stops initiating subsequent reads, writes, and retries, and closes the iterator. In-flight RPCs and completed pages are not undone.
- The JDBC connection parameter `maxRetry` limits retries after the initial write attempt. It is shared by paged partial upsert and DELETE writes, not every SDK operation. Exhausted retries may leave partial success; retries do not promise exactly-once execution.
- Only recognized transient errors are retried, such as gRPC UNAVAILABLE, RESOURCE_EXHAUSTED, ABORTED, DEADLINE_EXCEEDED, SDK rate limiting/temporary unavailability, and JDBC transient connection errors. Parameter, permission, missing-collection, and unknown errors fail immediately. Backoff starts at 100ms, doubles up to 1000ms per wait, and checks cancellation and query timeout while waiting.
- Reading the next page is not automatically retried: an uncertain iterator position could otherwise skip or duplicate data. Ordinary INSERT/UPSERT and Import creation do not automatically use this retry policy either.
- Paged DML errors include `phase=read/write/close`, `iterator`, `page`, `confirmedPages`, `confirmedRows`, and `currentPageRows`, preserving SQLState, error code, and cause. Iterator-close failures are suppressed when a primary error already exists. Confirmed progress is not a complete account of server state: part of a failing page may already have taken effect.
- Update counts, including totals across pages, remain `long` and are available through `executeLargeUpdate()` / `getLargeUpdateCount()`. Above `Integer.MAX_VALUE`, ordinary `executeUpdate()` / `getUpdateCount()` follow the common driver's policy of returning `Statement.SUCCESS_NO_INFO`, without truncating counts or rejecting a completed paged operation for integer overflow.

### Data Import (Import)

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

### Load & Release {#load}

Milvus requires collections to be loaded into memory before searching.
```text
LOAD TABLE table_name [PARTITION partition_name] [WITH (option=value, ...)];
RELEASE TABLE table_name [PARTITION partition_name];

-- Set the load wait timeout
/*+ timeout=60000 */ LOAD TABLE table_name;

-- Submit the release request without waiting
/*+ sync=false */ RELEASE TABLE table_name;
```

`LOAD TABLE` uses the SDK's synchronous wait by default. Refresh loads wait for SDK refresh progress, not merely an existing Loaded state. `RELEASE TABLE` waits for NotLoad by default; use `sync=false` to submit without waiting. The LOAD `timeout` hint is in milliseconds, defaults to 60000, must be positive, and is passed to the SDK load request and synchronous wait. Asynchronous submission does not certify background completion. The command returns update count 0, not a loaded-row count.

| LOAD WITH option | SDK request field | Type and default |
| --- | --- | --- |
| num_replicas | numReplicas | Positive INT32 integer, default 1; availability depends on cluster resources. |
| refresh | refresh | boolean, default false; refreshes a loaded collection or partition. |
| load_fields | loadFields | String list, empty by default, preserving the SDK's default field-loading scope. |
| skip_load_dynamic_field | skipLoadDynamicField | boolean, default false; true makes dynamic fields unavailable for filtering or output. |
| resource_groups | resourceGroups | String list, empty by default, leaving resource-group placement to the server. |

These options apply to both collections and partitions. Lists accept SQL list literals, JDBC `setObject` with `List<String>` / `String[]`, or JSON string arrays. Elements cannot be null or blank. Unknown or duplicate options and invalid types fail before the load API call. Keep `sync` / `timeout` in hints, not WITH.

```sql
LOAD TABLE books WITH (num_replicas=1, load_fields=['id','book_intro'], skip_load_dynamic_field=true);
/*+ timeout=30000 */ LOAD TABLE books WITH (refresh=true);
/*+ sync=false */ LOAD TABLE books PARTITION p WITH (resource_groups=['query_group']);
```

Official guidance requires the primary key and at least one vector field for partial loading, with filters and output restricted to loaded fields. Explicitly RELEASE and then LOAD to change the field set. This feature is documented as beta; check the target version's conditions before production use. See the [official loading guide](https://milvus.io/docs/load-and-release.md). The driver neither releases collections automatically nor turns the field list into access control. In the current 2.6.2 environment, both the native SDK and JDBC can still read an omitted scalar field. Rejection of omitted-field access is therefore not guaranteed for this version, and memory savings have not been verified. `SHOW PROGRESS OF LOADING` does not expose separate refresh-task progress.

The Milvus 2.6.2 environment verifies selected fields, full loading after explicit release, synchronous collection/partition refresh, and single-replica reads. Multiple replicas, non-default resource-group scheduling, and the business effects of skipping dynamic fields are outside those real-service conclusions; unit tests cover their SDK request mappings.

---

### Flush {#flush}

```sql
FLUSH table_name;
FLUSH first_table, second_table IN DATABASE archive WITH (wait_flushed_timeout_ms=60000);
FLUSH ALL TABLES;
FLUSH ALL TABLES IN DATABASE archive WITH (wait_flushed_timeout_ms=?);
SHOW FLUSH ALL ? IN DATABASE archive;
```

`FLUSH table_name[, ...]` submits one SDK `FlushReq.collectionNames` list, waits, and returns update count 0. `FLUSH ALL TABLES` calls SDK `flushAll` and **also waits synchronously for completion**, returning one BIGINT `FLUSH_ALL_TS` row on success. Use `executeQuery()` or `execute()` to obtain it, not `getGeneratedKeys()`. It is not an asynchronous submission-only operation. `FLUSH all` still flushes the single collection named `all`, not the whole database.

Both forms default to the current connection database. `IN DATABASE db_name` explicitly selects another database without changing the connection catalog. Only `FLUSH ALL TABLES IN DATABASE *` requests all databases; use the same scope for observation, such as `SHOW FLUSH ALL ? IN DATABASE *`. Collection and database names must be non-empty SQL identifiers, not `?` bindings. Collection-list flush does not accept a database wildcard.

`WITH` accepts only `wait_flushed_timeout_ms`, bindable as a non-negative integer within the BIGINT range. The default preserves the existing 60000ms wait limit; explicit zero selects unlimited SDK waiting, so use it cautiously. This controls SDK completion waiting, not total JDBC call duration: submission RPCs, network behavior, and SDK retries also depend on connection settings. sync/timeout hints are not consumed, and `sync=false` cannot make the operation asynchronous.

`SHOW FLUSH ALL timestamp` calls `getFlushAllState` once and returns one row with BIGINT `FLUSH_ALL_TS` and BOOLEAN `FLUSHED`. Supply a non-negative BIGINT literal or `setLong()` binding. Preserve the native integer token rather than converting it to a Java date or floating-point value. Observation neither triggers flush nor waits automatically. False means the native completion condition has not been met; absent SDK state remains NULL rather than fabricated success.

**Database-scoped FlushAll completion waiting has a server limitation on 2.6.2**: `GetFlushAllState` filters the requested database but then checks checkpoints across every database, allowing unrelated databases to delay completion until timeout. See the [official 2.6.2 implementation](https://github.com/milvus-io/milvus/blob/v2.6.2/internal/datacoord/services.go#L1519). Both JDBC and native SDK waiting are affected. Use explicit collection-list FLUSH on this baseline; database-wide completion waiting has not passed acceptance, and the all-databases wildcard is not guaranteed to work. Check native fixes before relying on other versions.

FLUSH is not JDBC commit or a cross-statement transaction boundary. Failure, timeout, or cancellation does not imply that no data was flushed. SDK waiting failures may leave no timestamp available to return. The driver does not fall back to collection-by-collection flushing, extra polling, or fabricated success, and does not resubmit outside SDK behavior. Frequent flushes are subject to server rate limits.

### Replica and shard status {#replicas}

```sql
SHOW REPLICAS FROM TABLE books;
SHOW REPLICAS FROM books;
```

Use `executeQuery()` to obtain a snapshot of the specified collection's replicas in the connection's current database, one row per replica. `TABLE` is optional. The collection name is a trusted SQL identifier, not a `?` value parameter. This calls SDK `describeReplicas` without implicitly loading, releasing, transferring replicas or scanning entities.

| Returned column (in order) | JDBC type | Meaning |
| --- | --- | --- |
| REPLICA_ID | BIGINT | Replica ID |
| COLLECTION_ID | BIGINT | Collection ID |
| PARTITION_IDS | VARCHAR | Native partition ID list as JSON array text |
| SHARD_REPLICAS | VARCHAR | Shard information as JSON array text; objects contain leaderID, leaderAddress, channelName and nodeIDs |
| NODE_IDS | VARCHAR | Replica query-node IDs as JSON array text, including leaders |
| RESOURCE_GROUP | VARCHAR | Resource group name |
| NUM_OUTBOUND_NODE | VARCHAR | Native numOutboundNode mapping as JSON object text, with resource-group names as keys and node counts as values |

Nested shards do not expand into extra rows or result sets. IDs retain Long/JSON integer precision; JSON consumers should avoid conversion to floating-point numbers that could lose precision. Empty SDK containers remain `[]` / `{}`, and NULL containers remain SQL NULL. An empty replica list yields an empty result set with column metadata. Missing or unloaded collections and permission restrictions retain the server's actual response; errors do not become empty results.

`setMaxRows()` caps JDBC replica rows; `fetchSize` does not turn this SDK snapshot into a paginated request. Node addresses are for observation only: the driver does not use them for multiple-address connections or read/write routing. A snapshot is not transactionally consistent across nodes and does not prove replica failover or scheduling performance.

In the current single-instance 2.6.2 environment, shard `nodeIDs` and replica `partitionIDs` can remain empty even after insertion and FLUSH; native SDK and JDBC results agree. The driver does not synthesize these fields from replica nodes or other queries. An empty shard node list does not mean the replica has no query nodes.

API reference: [describeReplicas](https://milvus.io/api-reference/java/v2.6.x/v2/Collections/describeReplicas.md).

### Server and segment status {#diagnostics}

```sql
SHOW VERSION;
SHOW HEALTH;
SHOW PERSISTENT SEGMENTS FROM TABLE books;
SHOW QUERY SEGMENTS FROM TABLE books;
```

All four commands return a ResultSet through `executeQuery()` or `execute()`, not an update count. `TABLE` is optional in segment queries. Collection names cannot be bound as `?`; the database is the connection's current catalog.

| Command | Result columns, in order |
| --- | --- |
| SHOW VERSION | VERSION, BUILD_TIME, GIT_COMMIT, GO_VERSION, DEPLOY_MODE: VARCHAR |
| SHOW HEALTH | IS_HEALTHY: BOOLEAN; REASONS, QUOTA_STATES: JSON array text / VARCHAR |
| SHOW PERSISTENT SEGMENTS | SEGMENT_ID, COLLECTION_ID, PARTITION_ID: BIGINT; COLLECTION_NAME: VARCHAR; NUM_ROWS: BIGINT; STATE, LEVEL: VARCHAR; STORAGE_VERSION: BIGINT; IS_SORTED: BOOLEAN |
| SHOW QUERY SEGMENTS | SEGMENT_ID, COLLECTION_ID, PARTITION_ID, MEM_SIZE, NUM_ROWS: BIGINT; INDEX_NAME: VARCHAR; INDEX_ID: BIGINT; STATE, LEVEL, NODE_IDS: VARCHAR (NODE_IDS contains JSON array text); STORAGE_VERSION: BIGINT; IS_SORTED: BOOLEAN |

VERSION and HEALTH each return one row. An unhealthy response remains readable with `IS_HEALTHY=false`, reasons and quota states. RPC, authentication or permission errors throw SQLException instead of becoming an unhealthy flag or an empty result. Each segment occupies one row; an empty snapshot retains result metadata. SDK NULL values remain SQL NULL; the driver does not infer missing fields.

These commands call the SDK directly without implicit FLUSH, LOAD, entity queries or another port. `Statement.setMaxRows()` limits returned segment rows, not the snapshot already fetched by the SDK. NUM_ROWS describes physical segment state: summing it is not a logical COUNT accounting for deletes, filters or replicas. MEM_SIZE is in bytes. Snapshots from different nodes are not transactionally consistent. Build details and newer segment fields depend on the server response; older versions may return empty values or protocol defaults, which do not prove that the corresponding feature is enabled.

A single-instance Milvus 2.6.2 environment verifies version/health results, empty-collection state, and both segment queries after explicit insertion, FLUSH and LOAD. Multi-node failures, real quota limiting, and the business semantics of newer version-specific fields have not been verified.

API references: [health](https://milvus.io/api-reference/java/v2.6.x/v2/Management/checkHealth.md), [persistent segments](https://milvus.io/api-reference/java/v2.6.x/v2/Management/getPersistentSegmentInfo.md), and [query-node segments](https://milvus.io/api-reference/java/v2.6.x/v2/Management/getQuerySegmentInfo.md).

### Resource-group queries {#resource-groups}

```sql
SHOW RESOURCE GROUPS;
SHOW RESOURCE GROUP __default_resource_group;
```

These commands read native cluster-level resource-group information. They do not filter by the current JDBC database, change resource configuration, or move nodes. The list returns one VARCHAR `RESOURCE_GROUP` per row with no ordering guarantee. JDBC maxRows can limit returned rows.

A description returns one row: `RESOURCE_GROUP` is the name; `CAPACITY` and `AVAILABLE_NODES` are INTEGER; `LOADED_REPLICAS`, `OUTGOING_NODES`, and `INCOMING_NODES` are VARCHAR JSON objects preserving the SDK's collection replica-count, outgoing-node-count, and incoming-node-count maps. `CONFIG` is a VARCHAR JSON configuration object, or SQL NULL when absent from the SDK response. `NODES` is a VARCHAR JSON node array. Field meanings and state come from the server; queries neither wait for transfers nor infer additional scheduling state from counts. List and description calls are independent snapshots.

### Resource-group configuration {#resource-group-config}

```sql
CREATE RESOURCE GROUP rg_demo CONFIG '{"requests":{"nodeNum":0},"limits":{"nodeNum":0}}';
ALTER RESOURCE GROUP rg_demo CONFIG ?;
ALTER RESOURCE GROUPS CONFIG ?;
DROP RESOURCE GROUP rg_demo;
```

CONFIG is optional during creation, preserving SDK default creation behavior when omitted. CONFIG accepts a JSON object string (`setString`), Map, or JsonObject (`setObject`). A single-group CONFIG is a configuration object. Plural GROUPS expects a nonempty map from resource-group names to configuration objects. The driver validates all configurations before submitting one SDK `updateResourceGroups` call, rather than separate per-group requests.

Configurations use official ResourceGroupConfig Protobuf JSON: `requests.nodeNum` is the requested node count and `limits.nodeNum` is the node-count limit; `transferFrom` and `transferTo` are arrays of objects containing a `resourceGroup` name; `nodeFilter.nodeLabels` is an array of string key/value objects, such as `[ {"key":"zone","value":"east"} ]`. The official JSON parser rejects unknown fields, invalid types, and out-of-range integers. SHOW returns CONFIG in the same format, allowing it to be bound again. Omitted fields take Protobuf defaults rather than preserving existing values, so submit the complete desired configuration when updating.

Successful writes return update count 0, not a migrated-node count. Configuration changes can trigger server scheduling; SQL neither waits for scheduling nor promises multi-group transactions. The server determines whether deletion is allowed; the driver does not implicitly release collections or move nodes. The example uses zero-node configurations. Real validation covers empty-group creation, configuration round-tripping, single/multiple-group updates, and deletion; scheduling from nonzero requests and label-selection effects have not been verified.

### Node and replica transfers {#resource-group-transfers}

```sql
TRANSFER NODES 1 FROM RESOURCE GROUP source_group TO RESOURCE GROUP target_group;
TRANSFER REPLICAS 1 OF TABLE table_name FROM RESOURCE GROUP source_group TO RESOURCE GROUP target_group;
```

These commands call SDK `transferNode` and `transferReplica` respectively. Counts accept `?` binding and must be positive integers. Node counts must fit Integer.MAX_VALUE; replica counts retain the Long range. Resource-group and collection names are SQL identifiers, not value parameters. Node transfer is cluster-level; replica transfer targets the specified collection in the current JDBC database.

Success returns update count 0. The SDK does not return a transfer task ID or migrated count, and the driver does not invent them. SQL does not wait for scheduling or implicitly create groups, change configurations, or load collections. The server determines resource availability, group existence, and allowed transfers. SHOW RESOURCE GROUP exposes incoming, outgoing, and replica information, but these are group snapshots rather than per-call task status.

Transfers are administrative writes that can affect query capacity and collection availability. Errors or disconnected clients do not guarantee that an operation was not applied; inspect server state before resubmitting. SQL parsing, SDK field mapping, count bounds, and failure propagation are tested. Existing nodes and replicas have not been moved in the unified test container, so actual transfer effects are outside the verified matrix.

### Compaction tasks {#compaction}

```sql
COMPACT TABLE table_name;
COMPACT table_name WITH (is_clustering=false, is_l0=false, target_size=512);
SHOW COMPACTION 123456789;
SHOW COMPACTION PLANS 123456789;
```

`COMPACT` submits a native asynchronous compaction task and returns a one-row ResultSet with a BIGINT `COMPACTION_ID`. Use `executeQuery()` or `execute()`, not an update count or `getGeneratedKeys()`. The driver does not poll for completion, implicitly FLUSH, or treat compaction as commit. A submission failure without a known task ID does not prove the server never received the request; confirm task state before resubmitting.

Optional WITH settings map to the SDK: `is_clustering` and `is_l0` are booleans; `target_size` is a positive long integer in MB. Omitting them preserves SDK defaults. Support and effects of special compaction modes and target size depend on server version, collection configuration, and the SDK; the driver does not emulate them. Values accept `?`, bound using `setBoolean` / `setLong` respectively.

`SHOW COMPACTION ?` accepts a task ID bound with `setLong` and returns one row containing `COMPACTION_ID`, `STATE`, `EXECUTING_PLANS`, `COMPLETED_PLANS`, and `TIMEOUT_PLANS`. STATE is the SDK state name; the last three columns are BIGINT plan counts, not percentages or affected-row counts.

`SHOW COMPACTION PLANS ?` returns one row containing `COMPACTION_ID`, `STATE`, and `PLANS`. PLANS is a VARCHAR JSON array; each plan contains a `sources` list of source segment IDs and a `target` segment ID. An empty plan list still returns the state row with `[]`. Separate SHOW calls are independent snapshots and can differ as the task progresses. Missing-task or server errors are surfaced as SQLException. SQL exposes only the state and failure information provided by the SDK.

## 6. Query & Search (DQL) {#dql}

This adapter unifies scalar queries (Query) and vector similarity searches (Search) using `SELECT` syntax.

### Scalar Query (Query)

Used for exact matching or range filtering.
```sql
-- Query all fields
SELECT * FROM table_name;

-- With conditional filtering
SELECT * FROM table_name WHERE age > 20 AND status = 1;

-- Specify return fields and pagination
SELECT id, name FROM table_name LIMIT 10 OFFSET 0;

-- Query specific partition
SELECT * FROM table_name PARTITION partition_name WHERE tag = 'A';
```

### Query-level options {#query-options}

Scalar SELECT, COUNT and regular KNN/range SELECT accept these WITH options. Bounded requests and native paginated iterators retain the same settings.

| Option | JDBC parameter type | SDK field and behavior |
| --- | --- | --- |
| ignore_growing | Boolean / setBoolean | ignoreGrowing; true skips growing segments and can exclude recently inserted, unsealed data. The SDK default is false. |
| timezone | String / setString | timezone; a zone name such as UTC or Asia/Shanghai for server-side time expressions. The server determines valid names and time semantics; omission or an empty string retains the SDK default without sending a timezone parameter. |

```sql
SELECT id FROM books WHERE id > ? LIMIT ? WITH(ignore_growing=?,timezone=?);
SELECT COUNT(*) FROM books WHERE id > ? WITH(ignore_growing=?,timezone=?);
COUNT FROM books WITH(ignore_growing=false,timezone='UTC');
```

WITH parameters bind after WHERE and LIMIT/OFFSET, in SQL order. Do not supply booleans as strings such as `'true'`. Scalar SELECT/COUNT reject vector-search parameters such as nprobe, ef and round_decimal, and do not allow limit/offset through WITH. Unknown options throw SQLException instead of being silently ignored. The existing shared WITH parser retains the last value for duplicate keys while consuming every `?` in SQL order; specify each key once to avoid ambiguity.

`timezone` only enters the SDK request. It does not change the JVM/JDBC timezone or turn VARCHAR date fields into native temporal types. Passing it does not imply support for every Milvus temporal type or function. Rewriting SELECT to COUNT with `overwrite_find_as_count` retains these two scalar options. Hybrid rejects both as outer options; configure its timezone per candidate as described in [Hybrid Search](#hybrid). API references: [Query](https://milvus.io/api-reference/java/v2.6.x/v2/Vector/query.md), [QueryIterator](https://milvus.io/api-reference/java/v2.6.x/v2/Vector/queryIterator.md).

### Vector Search (Search)

Use the specific `<->` operator or `vector_range` function to represent vector distance calculation.

```sql
-- Basic Search (KNN, default params)
SELECT * FROM table_name ORDER BY vector_col <-> [0.1, 0.2] LIMIT 10;

-- Search with pre-filtering
SELECT * FROM table_name WHERE category = 'book' ORDER BY vector_col <-> [0.1, 0.2] LIMIT 5;

-- Range Search / Distance Filtering
-- Method 1: Using vector_range function (Recommended)
SELECT * FROM table_name WHERE vector_range(vector_col, [0.1, 0.2], 0.8) LIMIT 5;

-- Method 2: Using comparison expression (WHERE vector_col <-> [vector] < distance_threshold)
SELECT * FROM table_name WHERE vector_col <-> [0.1, 0.2] < 0.8 LIMIT 5;
```

### Metrics and Range Constraints

`<->` means L2 (smaller is nearer), `<=>` means COSINE, and `<#>` means IP (larger is more similar). SELECT, UPDATE, and DELETE share these mappings.

Supported ranges are `v <-> ? < radius`, `v <=> ? > threshold`, and `v <#> ? > threshold`. `vector_range(v, vector, radius)` means an L2 distance below the radius. Bounds must be finite numeric literals or parameters: L2 is nonnegative, COSINE lies in [-1,1], and IP may be negative. Bind negative thresholds with a PreparedStatement parameter. Strings, null, NaN, and Infinity fail before search or mutation.

One vector range may be combined with scalar predicates using AND. Vector ranges under OR/NOT, multiple ranges, other comparison directions, or a range combined with ORDER BY are explicitly rejected. WITH radius/range_filter cannot override a WHERE range. Bound directions follow [Milvus 2.6 range search](https://milvus.io/docs/v2.6.x/range-search.md).

### Pagination and JDBC Row Limits {#pagination}

- LIMIT requires a positive integer; OFFSET requires a nonnegative integer. Fractions, numeric strings, and overflow are rejected, not truncated. Bind Byte/Short/Integer/Long/BigInteger, or an integral, in-range BigDecimal; Float/Double are not count parameters.
- Hint `overwrite_find_limit/skip` overrides SQL LIMIT/OFFSET; positive `setMaxRows` further caps returned rows. Overridden SQL parameters are still bound and validated in their original positions.
- Scalar, KNN, and range SELECT read on demand. Unlimited queries, effective limits larger than one page, and queries with OFFSET use official iterators. Bounded, single-page queries without OFFSET retain ordinary Query/Search requests. COUNT still returns one aggregate row.
- Set `Statement.setFetchSize(n)` before execution to control page size. Unset values use the SDK default; values above its maximum are capped per page, not in total. The JDBC cursor skips OFFSET page by page and then returns up to the effective LIMIT/maxRows. Large offsets still incur scanning cost. An OFFSET plus effective limit overflowing long is rejected before querying.
- SQL execution establishes the query; SDK initialization may prefetch initial data. Further pages are consumed as the ResultSet cursor advances, without traversing the whole collection before returning ResultSet. The driver retains the current page and row only; SDK-internal caches and server search restrictions remain SDK/server concerns. Omitted LIMIT adds no fixed total cap.
- Reaching the limit or EOF, closing ResultSet, reexecuting/closing Statement, and closing Connection release iterators. Multiple SQL results use standard `getMoreResults`: KEEP_CURRENT_RESULT retains independent cursors, and CLOSE_ALL_RESULTS releases retained results.
- After execute returns, `Statement.cancel()` still cancels this execution's unread results, without affecting other Statements on the connection. Query timeout starts at execution and includes subsequent reads and caller pauses. Checks run before and after page reads; an idle timeout is reported and released on the next read. Cancellation/close does not wait for an in-flight SDK page read; its result is discarded and its iterator released when the call returns. RPC interruption is not guaranteed.
- Later page failures may surface from `ResultSet.next()`. Cursors are not automatically reopened or replayed. Concurrent read/close failures retain the primary and suppressed errors. Always use try-with-resources for results that might not be fully consumed.

Iterator parameters: [QueryIterator](https://milvus.io/api-reference/java/v2.6.x/v2/Vector/queryIterator.md), [SearchIterator](https://milvus.io/api-reference/java/v2.6.x/v2/Vector/searchIterator.md).

### Advanced Search Parameters (WITH Clause)

This section describes vector SELECT search parameters. Scalar SELECT/COUNT only accept [query-level options](#query-options), not index-search or grouping parameters.

WITH values are bound last in SQL order. Keys are fixed names; values support strings, numbers, booleans, and `?`. JSON serialization escapes quotes, backslashes, and control characters. Quoted numbers remain strings. Write index search parameters such as `nprobe` or `ef` directly in WITH.

Regular KNN and range SELECT also accept `round_decimal` (an integer specifying distance-score decimal places, with the allowed range validated by the server), plus the ignore_growing/timezone options above. These enter dedicated SDK request fields, not index searchParams, and remain set for bounded, iterator and grouped searches. Example: `WITH (round_decimal=2, ignore_growing=false, timezone='UTC')`. Bind them with setInt, setBoolean and setString, not quoted numbers or boolean strings. This does not apply to Hybrid Search.

If provided, `metric_type` must match the SQL distance operator. Use SQL OFFSET or a hint instead of WITH offset. WITH forwards search parameters; it does not expose every SDK builder property. Configure consistency through the JDBC connection and output fields through SELECT.

```sql
SELECT id, score FROM table_name
WHERE age > ?
ORDER BY vector_col <=> ?
LIMIT ? OFFSET ?
WITH (metric_type='COSINE', nprobe=?);
```

### Grouped vector search {#grouping}

When multiple chunks from one document dominate the nearest matches, Milvus can select matching entities per document ID or category. This is native ANN/Hybrid grouping, not relational GROUP BY, DISTINCT or SUM/COUNT aggregation.

```sql
SELECT id, category, score FROM books ORDER BY vector_col <-> ?
WITH (group_by_field='category', group_limit=3, group_size=2, strict_group_size=true);

SELECT id, category FROM books ORDER BY vector_col <-> ? LIMIT 3 OFFSET 1
WITH (group_by_field='category', group_limit=2, group_offset=1, group_size=2, strict_group_size=true);
```

| WITH option | Meaning |
| --- | --- |
| group_by_field | Required nonempty field-name string, mapped to SDK groupByFieldName; Milvus validates field-type and index compatibility |
| group_limit | Required positive INT32: maximum number of groups, mapped to native Search topK / Hybrid limit |
| group_offset | Nonnegative BIGINT group offset, default 0; adding group_limit must not overflow |
| group_size | Optional positive INT32 target entities per group; omission preserves SDK/server defaults |
| strict_group_size | Optional boolean asking the server to try to meet group_size; the driver does not fill undersized groups |

All values accept `?` bindings; use the corresponding JDBC integer and boolean types. Grouping options belong only to the final SELECT WITH clause, not a Hybrid ANN candidate's WITH. SQL LIMIT/OFFSET, their override hints, and JDBC maxRows always count **rows**, applied after the native group window; they may truncate a group. Without SQL LIMIT, all rows in the selected group window are readable, not all groups in the collection. group_limit remains mandatory, is not inferred from SQL LIMIT, and has no magic fallback total.

The first example selects up to 3 groups with a target of 2 entities each, potentially returning 6 rows. The second skips 1 group and selects 2 groups on the server, then skips 1 returned row and exposes at most 3 rows through JDBC. Grouping, within-group selection and ordering happen on the server; the driver neither regroups, fills groups nor reorders results. There is still one flat ResultSet containing only SELECT columns; explicitly select the grouping field if needed.

Grouping uses one bounded Search/Hybrid request, not a grouping iterator. fetchSize does not split the group window into server requests, and maxRows does not reduce requested group counts. Choose group_limit and group_size for the workload and respect server search-window limits. Unsupported range-search, type, index or server-version combinations return SQLException without client-side grouping fallback.

Although Java SDK 2.6.22 declares groupByFieldName on SearchIteratorReqV2, a native call against Milvus 2.6.2 rejects this combination with `Not allowed to do groupBy when doing iteration`. Ungrouped iteration works in the same environment. A request field alone does not establish grouping-pagination support, and this result is not automatically extended to later server versions.

Hybrid accepts reranker and grouping options together in its final WITH. In this mode, mandatory group_limit replaces the requirement for an outer SQL LIMIT; each ANN candidate LIMIT still counts candidate entities:

```sql
SELECT id,category,score FROM books ORDER BY HYBRID (
    vector_col <-> ? LIMIT 20,
    other_vector <-> ? LIMIT 20
) WITH (reranker='rrf',group_by_field='category',group_limit=3,group_size=2,strict_group_size=true);
```

Milvus 2.6.2 verifies dense FLAT/L2 search grouped by VARCHAR, default single-entity groups, strict multi-entity groups, combined group/row windows and RRF Hybrid grouping. This does not certify all index/field/ranker combinations; sparse, binary and other vector grouping combinations require separate verification. See the [official grouping guide](https://blog.milvus.io/docs/v2.6.x/grouping-search.md) for native semantics.

### JDBC Result Metadata

Column order follows SELECT, including metadata for empty results. Int8/16/32/64 map to TINYINT/SMALLINT/INTEGER/BIGINT; Float/Double/Bool/VarChar to corresponding types. JSON is OTHER (JsonElement), FloatVector/Array is ARRAY (List/getArray), Binary/FP16/BF16/Int8Vector is VARBINARY (byte[]/getBytes), Sparse is OTHER (SPARSE_FLOAT_VECTOR, SortedMap&lt;Long,Float>). Array element type, isNullable and isAutoIncrement follow schema. Vector score is Float and is never requested as a stored field; vector SELECT * or explicit score projections include it. Scalar SELECT has no score.

### Count Statistics

Use `COUNT FROM ...` or `SELECT COUNT(*) FROM ...` to count entities in a collection or partition. Both use the same native Milvus count request, without scanning results on the client.
The result is one row with `COUNT` (BIGINT), read using `rs.getLong("COUNT")`; an empty match returns 0. Append WITH for [query-level options](#query-options); ignore_growing=true also excludes growing segments from counts. COUNT(field), mixed projections, GROUP BY and vector-range filters are unsupported.

```sql
-- Query total count of full table
count from table_name;
SELECT COUNT(*) FROM table_name;

-- Query total count of specific partition
COUNT FROM table_name PARTITION partition_name;

-- Count with conditional filtering (supports scalar filtering)
count from table_name where age > 18;
```

### Query Hints {#hint}

This adapter supports determining pagination or query behavior using SQL Hints (`/*+ ... */`), similar to usage in Elastic/Mongo adapters.

Supported Hints:

- `overwrite_find_limit`: Force overwrite query LIMIT (TopK).
- `overwrite_find_skip`: Force overwrite query OFFSET.
- `overwrite_find_as_count`: Presence switches to COUNT, ignoring projection/sorting rather than counting a vector selection. Vector ranges are rejected. Even false triggers the hint; remove it to disable. WHERE/vector/pagination/WITH placeholders must still be bound and validated in their original SQL positions.

Ordinary Query/Search requests use connection property `consistencyLevel`; `consistency_level` is not an implemented query hint. QueryIterator in Java SDK 2.6.22 uses the collection's default consistency. For read-after-write guarantees with paginated queries, explicitly create the collection with `WITH (consistency_level='Strong')` rather than relying only on the connection property. These three overwrite hints apply only to SELECT, not UPDATE/DELETE. See the earlier sections for IMPORT/LOAD/RELEASE sync/timeout.

Example:

```sql
-- Force limit return to 5 records, skip first 10
/*+ overwrite_find_limit=5, overwrite_find_skip=10 */
SELECT * FROM table_name WHERE status = 1;

-- Use Hint to get total number of records matching conditions (Equivalent to count from ... where ...)
/*+ overwrite_find_as_count=true */
SELECT * FROM table_name WHERE age > 20;
```

---

## 7. Progress Monitoring {#progress}

### Check Loading Progress

```sql
SHOW PROGRESS OF LOADING ON TABLE table_name;
SHOW PROGRESS OF LOADING ON TABLE table_name PARTITION partition_name;
```

### Check Index Building Progress

```sql
SHOW PROGRESS OF INDEX ON TABLE table_name;
SHOW PROGRESS OF INDEX index_name ON TABLE table_name;
```


## 8. JDBC Calls and Results {#jdbc-results}

`SELECT/COUNT/SHOW` return ResultSets; writes and administrative commands return update counts. Choose executeQuery/executeUpdate accordingly; use execute for unknown or mixed results.

### Multiple Statements and Results

This fragment uses an open `conn` and `books_demo` from the [getting-started program](./jdbc.mdx). Each ResultSet belongs to a SQL statement, not to a group of query vectors inside one SELECT.

```java
String sql = "SELECT book_id FROM books_demo LIMIT 2; " +
        "UPDATE books_demo SET word_count = ? WHERE book_id = ? LIMIT 1; " +
        "COUNT FROM books_demo";
try (PreparedStatement ps = conn.prepareStatement(sql)) {
    ps.setInt(1, 2000);
    ps.setLong(2, 1L);
    boolean hasResult = ps.execute();
    while (true) {
        if (hasResult) {
            try (ResultSet rs = ps.getResultSet()) {
                ResultSetMetaData md = rs.getMetaData();
                while (rs.next()) {
                    for (int i = 1; i <= md.getColumnCount(); i++) {
                        System.out.println(md.getColumnLabel(i) + "=" + rs.getObject(i));
                    }
                }
            }
        } else {
            long count = ps.getLargeUpdateCount();
            if (count == -1) {
                break;
            }
            System.out.println("updated=" + count);
        }
        hasResult = ps.getMoreResults(Statement.CLOSE_CURRENT_RESULT);
    }
}
```

`getMoreResults()` closes the current result by default. KEEP_CURRENT_RESULT retains it for explicit closing; CLOSE_ALL_RESULTS closes retained results. Reexecuting the same Statement closes its old results. An update count of 0 is not the end: stop only when there is no current ResultSet and the update count is -1.

### Result Columns

| Command | Result |
| --- | --- |
| SELECT | Projected fields; vector results may include score. |
| COUNT FROM / SELECT COUNT(*) | One COUNT (BIGINT) row. |
| SHOW DATABASES / TABLES / PARTITIONS | DATABASE / TABLE / PARTITION (VARCHAR). |
| SHOW DATABASE | DATABASE and PROPERTIES (VARCHAR); PROPERTIES contains JSON object text. |
| SHOW ALIASES FROM / SHOW ALIAS | ALIAS and TABLE; SHOW ALIAS additionally returns DATABASE (all VARCHAR). |
| SHOW PARTITION | PARTITION (VARCHAR), not partition statistics. |
| SHOW STATS FROM | NUM_ENTITIES (BIGINT) and STATS (VARCHAR, JSON object text). |
| SHOW TABLE | FIELD, TYPE, DIMENSION, PRIMARY, AUTO_ID, DESCRIPTION, NULLABLE, ELEMENT_TYPE, MAX_CAPACITY, MAX_LENGTH, PARTITION_KEY, CLUSTERING_KEY. |
| SHOW CREATE TABLE | CREATE SCRIPT (VARCHAR), a schema description, not a lossless reconstruction guarantee for complex external schemas. |
| SHOW INDEX / INDEXES | INDEX, FIELD, ID, PARAMS. |
| SHOW USERS / ROLES | USER / ROLE (VARCHAR). |
| SHOW USER | USER, ROLES (JSON role array), and DESCRIPTION, all VARCHAR. |
| SHOW ROLE | ROLE, DESCRIPTION, and GRANTS (JSON direct-grant array), all VARCHAR. |
| SHOW PRIVILEGE GROUPS | PRIVILEGE_GROUP and PRIVILEGES (JSON privilege-name array), both VARCHAR. |
| ANALYZE | TEXT_INDEX (BIGINT, 1-based) and TOKENS (VARCHAR JSON token array). |
| SHOW GRANTS | DATABASE, ROLE, OBJECT, OBJECT_NAME, PRIVILEGE. |
| SHOW PROGRESS OF LOADING | PROGRESS (BIGINT). |
| SHOW PROGRESS OF INDEX | TOTAL, INDEXED (BIGINT). Without an index name, counts are summed over returned SDK index records, not a completion percentage. |
| INSERT / UPSERT | Actual SDK-acknowledged long count; requested generated keys use a separate JDBC cursor. |
| UPDATE / DELETE | SDK-acknowledged update/delete count; not necessarily the number of entities whose state actually changed. Use Large methods for large values. |
| COMPACT / SHOW COMPACTION / SHOW COMPACTION PLANS | One-row ResultSet: task ID / state and plan counts / state and plan JSON; see [Compaction tasks](#compaction). |
| DDL, grants, ordinary IMPORT, LOAD/RELEASE, collection-list FLUSH | Update count 0; IMPORT RETURNING JOB_ID instead returns one task ID row. |
| FLUSH ALL TABLES / SHOW FLUSH ALL | ResultSet: native flush timestamp / timestamp and completion state. Not an update count or transaction commit. |

DELETE without LIMIT and with a simple primary-key condition is handled directly by the server. On Milvus 2.6.2, repeating `pk = ?` deletion can return 1 even when the entity is already absent: the count represents submitted deletion keys, not existing rows found by a preliminary query. The driver does not query first or rewrite this into a relational affected-row count. Do not use the DELETE return value to infer whether an entity existed beforehand. See the [server deletion implementation](https://github.com/milvus-io/milvus/blob/v2.6.2/internal/proxy/task_delete.go).

### JDBC Boundaries

- ResultSets are read-only and forward-only; close promptly. Read FloatVector/Array as List/getArray, Binary/FP16/BF16/Int8Vector through getBytes and Sparse as SortedMap via getObject; do not assume all vectors are float[].
- RETURN_GENERATED_KEYS supports INSERT/UPSERT Int64/VarChar keys, including AUTO_ID and explicit keys. Only SDK IDs are returned; NO_GENERATED_KEYS returns an empty cursor. Defaults remain unchanged; requesting all keys requires O(number of keys) memory.
- JDBC addBatch/executeBatch, savepoints, updatable ResultSets and cross-page transactions are unsupported. PreparedStatement parameterization and JDBC batch are different capabilities.
- Documented SQL/SHOW results are the supported access paths; not every DatabaseMetaData method satisfies relational assumptions made by ORM/BI/migration tools.

## 9. Types, Multi-row Writes and Hybrid Search {#extended}

### Type binding and schema

See [Type binding and generated keys](./jdbc.mdx#typed-values) for Java binding/keys. Binary accepts byte[], ByteBuffer or byte Lists. FP16/BF16 numeric inputs use official Float16Utils; packed byte[]/ByteBuffer uses little endian. Sparse accepts nonempty Map&lt;Number,Number>, integer indices in [0,4294967295), and finite weights. ByteBuffer uses remaining without changing position. FloatVector byte[] remains a numeric sequence.

```sql
CREATE TABLE types_demo (
    id INT64 PRIMARY KEY AUTO_ID,
    tags ARRAY<VARCHAR(30)>(8) NULL,
    flags ARRAY<BOOL>(8),
    bits BINARY_VECTOR(16),
    half FLOAT16_VECTOR(2),
    brain BFLOAT16_VECTOR(2),
    sparse SPARSE_FLOAT_VECTOR
);
INSERT INTO types_demo (tags,flags,bits,half,brain,sparse) VALUES (?, ?, ?, ?, ?, ?);
SELECT id,bits FROM types_demo ORDER BY bits ~= ? LIMIT 10;
SELECT id,score FROM types_demo WHERE bits <%> ? < 0.5 LIMIT 10;
SELECT id,half FROM types_demo ORDER BY half <=> ? LIMIT 10;
SELECT id,brain FROM types_demo ORDER BY brain <-> ? LIMIT 10;
SELECT id,sparse FROM types_demo ORDER BY sparse <#> ? LIMIT 10;
SELECT id,tags FROM types_demo WHERE tags IS NOT NULL LIMIT 10;
```

ARRAY capacity, element type and VARCHAR byte limit reach the SDK schema. getArray exposes its element JDBC type; arrays may be NULL or empty but elements cannot be NULL. isNullable follows schema. SHOW TABLE appends NULLABLE, ELEMENT_TYPE, MAX_CAPACITY and MAX_LENGTH, preserving previous column positions. SHOW CREATE preserves definitions. Nullable vectors require Milvus 2.6.18+ and do not support IS NULL predicates; see [release notes](https://github.com/milvus-io/milvus/releases/tag/v2.6.18) and [nullable documentation](https://milvus.io/docs/v2.6.x/nullable-and-default.md).

### Multi-row writes and generated keys {#generated-keys}

```sql
INSERT INTO docs (id,body,dense) VALUES (1,'first',[1,2]),(2,'second',[3,4]);
UPSERT INTO docs (id,body,dense) VALUES (1,'changed',[1,2]),(3,'third',[3,4]);
INSERT INTO docs (id,body,dense) VALUES ?;
UPSERT INTO docs VALUES ?;
```

Bind Iterable/Iterator to VALUES ?: rows are Lists/Object[] with column names, or Maps whose keys match the named columns. Omitted column lists require Maps. The SDK handles omitted defaults/nullable/function output fields. fetchSize controls entities per request, zero uses the SDK default, oversized values are capped at the SDK page maximum, and total rows are not capped. Caller-owned iterators/streams are not closed.

Counts use actual SDK long acknowledgements. Request keys using prepareStatement(sql, Statement.RETURN_GENERATED_KEYS) or Statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS). Keys contain SDK IDs under the primary field name. Default/NO_GENERATED_KEYS yields an empty cursor; maxRows does not constrain writes/keys.

Ordinary INSERT/UPSERT is not automatically retried. Failure includes phase, confirmedPages, confirmedRows and currentPageRows; confirmed pages are not rolled back and unacknowledged writes may have taken effect. Keys are delivered on successful completion; requesting all keys requires O(number of keys) memory.

### BM25 / TextEmbedding schema functions {#functions}

Declare functions after fields. Text input is non-nullable VARCHAR; omit server-generated output fields from INSERT/UPSERT.

```sql
CREATE TABLE docs (
    id INT64 PRIMARY KEY,
    body VARCHAR(2000) WITH (enable_analyzer=true, analyzer_params='{"type":"standard"}'),
    dense FLOAT_VECTOR(2), sparse SPARSE_FLOAT_VECTOR,
    FUNCTION bm25_fn USING BM25 (body) INTO (sparse)
);
CREATE INDEX bm25_idx ON docs(sparse) USING SPARSE_INVERTED_INDEX
    WITH (metric_type=BM25,bm25_k1=1.2,bm25_b=0.75);
SELECT id,score FROM docs ORDER BY sparse <?> 'hybrid search' LIMIT 10;

CREATE TABLE embedded_docs (
    id INT64 PRIMARY KEY, body VARCHAR(2000), dense FLOAT_VECTOR(1536),
    FUNCTION embed USING TEXTEMBEDDING (body) INTO (dense)
        WITH (provider='openai',model_name='text-embedding-3-small')
);
SELECT id,score FROM embedded_docs ORDER BY dense <=> 'search text' LIMIT 10;
```

BM25 requires enable_analyzer=true and Sparse output. TextEmbedding uses dense float output. Field WITH accepts enable_analyzer/enable_match booleans and analyzer_params as a JSON object string. Function WITH scalar values become SDK string parameters. TextEmbedding requires suitable server/provider/model/network/credential configuration and matching dimensions; configure the external model service before running the example. Configure secrets on the server instead of exposing them through SQL/SHOW CREATE. Current schema functions are BM25/TEXTEMBEDDING, not every future FunctionType.

Function definitions accept `INTO (...) DESCRIPTION 'text' WITH (...)`. DESCRIPTION also accepts a `?` bound to a non-null string. It maps to the dedicated SDK description field and is preserved by SHOW CREATE TABLE; WITH (description=...) remains a function parameter. Omitting DESCRIPTION uses an empty string. Description round-trips and subsequent BM25-generated vector searches are verified on Milvus 2.6.2.

In description literals, write a single quote as `''` and a backslash as `\\`; `\n`, `\r` and `\t` represent newline, carriage return and tab. PreparedStatement values are passed directly without SQL escaping. SHOW CREATE emits a description literal that can be parsed again.

#### Online function management {#alter-functions}

When the server provides the corresponding APIs, submit these commands against an existing collection:

```sql
ALTER TABLE online_docs ADD FUNCTION embed USING TEXTEMBEDDING (body) INTO (dense)
    DESCRIPTION 'Document embedding'
    WITH (provider='openai',model_name='text-embedding-3-small');
ALTER TABLE online_docs ALTER FUNCTION embed USING TEXTEMBEDDING (body) INTO (dense)
    DESCRIPTION 'Updated description'
    WITH (provider='openai',model_name='text-embedding-3-small');
ALTER TABLE online_docs DROP FUNCTION embed;
```

online_docs must already contain suitable input/output fields; TextEmbedding also requires the model service configuration described above. ADD/ALTER reuse CREATE TABLE's BM25/TEXTEMBEDDING definition. ALTER submits a complete replacement, not a parameter merge. The function name identifies the function being replaced; renaming is not exposed. Operations use the connection's current database without implicitly adding fields, loading collections, rebuilding indexes or backfilling existing data. Schema constraints, required load state and handling of existing data follow server rules.

Each SQL command maps to one official addCollectionFunction, alterCollectionFunction or dropCollectionFunction request. Success returns update count 0 and no ResultSet. Names cannot be bound with `?`; descriptions and WITH values can. Failures surface as SQLException and stop subsequent statements, without a DROP/CREATE fallback.

**Version boundary: Milvus 2.6.2 returns UNIMPLEMENTED for all three online APIs.** Failed requests are verified to preserve the collection definition and existing BM25 behavior; this does not promise rollback for arbitrary in-flight failures. Request mapping is implemented, but successful online changes on newer servers remain unverified. Check server support before deployment; see the official [add function API](https://milvus.io/api-reference/java/v2.6.x/v2/Collections/addCollectionFunction.md) and [alter function API](https://milvus.io/api-reference/java/v2.6.x/v2/Collections/alterCollectionFunction.md).

### Hybrid Search and rerank {#hybrid}

```sql
SELECT id,score FROM docs WHERE id > ? ORDER BY HYBRID (
    dense <-> ? LIMIT 20 WITH (nprobe=10),
    sparse <?> ? LIMIT 30
) LIMIT 10 OFFSET 2 WITH (reranker='rrf',k=60);

SELECT id,score FROM docs ORDER BY HYBRID (
    dense <=> ? LIMIT 20,
    sparse <#> ? LIMIT 30
) LIMIT 10 WITH (reranker='weighted',weights='[0.7,0.3]');
```

Each candidate has one query vector; candidates may target the same or different fields/metrics. Nested lists are not an NQ batch interface. Each candidate may have a scalar WHERE after its vector and before LIMIT. Its effective filter is `(shared WHERE) AND (local WHERE)`; local OR expressions remain parenthesized and cannot bypass the shared scope. Both WHERE clauses are optional, and local conditions do not affect other candidates. Bind in SQL order: shared WHERE, each vector/WHERE/LIMIT/WITH, final LIMIT/OFFSET/WITH. All filter values remain in SDK filterTemplateValues rather than being interpolated into expressions.

```sql
SELECT id,score FROM docs WHERE tenant_id = ? ORDER BY HYBRID (
    dense <-> ? WHERE category = ? OR category = ? LIMIT 20 WITH(timezone='UTC',nprobe=10),
    sparse <?> ? WHERE publish_year >= ? LIMIT 30 WITH(timezone=?)
) LIMIT 10 WITH(reranker='rrf',k=60,round_decimal=3);
```

This example requires existing tenant_id, category, publish_year and vector fields. Each candidate's string timezone enters the dedicated AnnSearchReq field; an empty string retains SDK defaults. Shared temporal filters are interpreted under each candidate's timezone as well. Do not put timezone in the outer WITH clause; native temporal types are not emulated. Candidate WHERE accepts scalar conditions only, not an additional vector-range expression; use that candidate's native search parameters for range settings.

Outer round_decimal is an INT32-range integer mapped to HybridSearchReq.roundDecimal. The server rounds final fused scores; the driver neither changes the JDBC score type nor re-sorts results. Omission retains the SDK default -1 (no rounding); the server determines valid precision values. Do not put round_decimal in candidate WITH clauses. It can accompany outer grouping options. API reference: [hybridSearch](https://milvus.io/api-reference/java/v2.6.x/v2/Vector/hybridSearch.md).

Candidate LIMITs control candidate counts; outer LIMIT/OFFSET applies to one server-fused result. RRF k defaults to the SDK value when omitted. Weighted requires one finite [0,1] weight per candidate. Select reranker explicitly; unknown ranker options are rejected. There is no SDK Hybrid Iterator: ordinary Hybrid requires outer LIMIT; [grouped search](#grouping) instead requires group_limit to bound groups, while SQL LIMIT/OFFSET still bounds result rows. fetchSize does not paginate fusion, and server window/candidate limits apply without a magic fallback total. Ordinary ORDER BY retains its single-vector rule.

### Multi-file Import and job inspection {#import}

```sql
/*+ sync=false */ IMPORT FROM [['prepared/a.parquet'],['prepared/b.parquet']]
    INTO docs WITH (timeout='2h') RETURNING JOB_ID;
/*+ sync=false */ IMPORT FROM [['prepared/id.npy','prepared/dense.npy']]
    INTO docs RETURNING JOB_ID;
SHOW IMPORT 'job-id';
SHOW PROGRESS OF IMPORT ?;
SHOW IMPORTS FROM docs WITH (page_size=20,current_page=1);
```

RETURNING JOB_ID uses executeQuery; without RETURNING, update count remains zero. SHOW returns JOB_ID/STATE/REASON (VARCHAR), PROGRESS/TOTAL_ROWS/IMPORTED_ROWS (BIGINT), DETAILS (JSON). Missing server values are NULL; DETAILS preserves file-level status. List pagination depends on the target REST version; one statement does not promise to enumerate all historical jobs.

REST must be available and uses JDBC database/credentials. Submission and inspection use the same JDBC address within a connection; reconnect to the service hosting the original job. Files must already reside in the storage configured for the Milvus server: object-storage deployments use object paths, while standalone local-storage deployments use absolute paths readable by the server, not paths on the JDBC client. The driver does not generate/upload files; official BulkWriter can prepare them.

JSON file import, RETURNING JOB_ID, SHOW IMPORT with Completed/100% progress, and exact row-count readback have been verified on Milvus 2.6.2 standalone with local storage. Asynchronous and synchronous imports of missing files are also verified: failed job IDs and reasons remain queryable, synchronous exceptions retain that information, and the driver does not resubmit the jobs. This does not validate distributed file visibility, every import format, or large-scale throughput; file preparation, access permissions and resource limits remain deployment responsibilities.

Default sync=true waits. Hint timeout is a client millisecond wait; WITH timeout is a server job duration string. Timeout/cancellation, Failed state or later HTTP failure retains the known Job ID and last progress, without cancelling/resubmitting the server job. A lost create response may leave an existing job with unknown ID; reconcile through the job list. Imports do not promise transactions or exactly-once execution.

Note: Statement.cancel() or a shared JDBC timeout may return its standard cancellation/timeout exception before adapter progress is available; it does not imply zero writes. For reliable recording of an import ID, submit with sync=false RETURNING JOB_ID and inspect separately.

See [Versions and Support](./compatibility.md) for version and feature requirements.
