---
id: commands
sidebar_position: 3
title: Syntax Manual
description: Current jdbc-milvus SQL subset, parameters, DDL, DML, vector and hybrid search, and JDBC results.
---

> **Version baseline**: Current 6.7.1-SNAPSHOT implementation, Java 17, Java SDK 2.6.22, minimum Milvus server 2.6.2. Version 2.5.x and earlier are unsupported; later versions are not automatically supported. See [Versions and Support](./compatibility.md).

See the [Install and Use](./usecase.mdx) for dependencies, connections and a complete program. This manual follows the current parser and command implementations; the full Milvus SDK and relational SQL are not implicitly supported.

TLS, certificates and Cloud use JDBC connection properties, not new SQL syntax or hints. SDK and Import REST share `secure`, `caPemPath`/`serverPemPath`, `clientPemPath`/`clientKeyPath` and `serverName`, and always use the JDBC URL host and port. Native Milvus 2.6.2 TLS listeners need a unified ingress for full Import support; the driver never switches to another port automatically. See [Connection Parameters and TLS](./params.md#tls) for combinations, certificate formats, deployment limits and examples.

## Reading Conventions

- `text` blocks are syntax templates: brackets such as `[IF EXISTS]` mark optional parts and must not be executed literally. `sql` blocks are syntax examples; substitute collection/field names and satisfy the schema before use. Vector brackets such as `[0.1, 0.2]` are actual syntax.
- Keywords are case-insensitive. Use ordinary identifiers; examples start with letters/underscores. Quoted identifiers and cross-database qualified-name resolution are not provided. Single/double quotes delimit strings, not quoted field names.
- ? binds WHERE, vectors, SET, VALUES, LIMIT/OFFSET, hints and query/field/function/index WITH values. Names/types are not bindable; collection-level CREATE TABLE WITH still uses constant consistency_level.
- SELECT projects `*` or fields. AS, JOIN, GROUP BY, arbitrary projection expressions and scalar ORDER BY are unsupported. Distance ordering supports the documented single query vector, not multiple sort keys.
- Scalar filters support comparisons, AND/OR/NOT, parentheses, LIKE, IN lists, IS NULL/IS NOT NULL and selected expression forwarding, not arbitrary relational functions. IN accepts [1,2], (1,2) or bound Lists. SQL NULL is a value; bind general negative numbers as parameters (DEFAULT separately supports signs).
- Semicolons separate multiple commands. Parameter numbering continues across statements and results use JDBC multiple-result access. This is neither a transaction nor JDBC batch.


## 1. Database Management {#database}

### Create Database

```text
CREATE DATABASE [IF NOT EXISTS] db_name;
```

### Drop Database

```text
DROP DATABASE [IF EXISTS] db_name;
```

### Alter Database Properties

```text
ALTER DATABASE db_name SET PROPERTIES ("key" = "value", ...);
```

### Show Databases

```sql
SHOW DATABASES;
```

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
| `ARRAY<element_type>(max_capacity)` | BOOL, INT8/16/32/64, FLOAT, DOUBLE or VARCHAR(n); capacity 1–4096. Nested/NULL elements and bare ARRAY are rejected. |
| `PRIMARY KEY`, `AUTO_ID`, `NULL`, `NOT NULL`, `COMMENT 'text'` | NOT NULL by default; NULL enables nullable. Nullable primary keys and conflicting constraints are rejected. Dynamic fields remain disabled. |

CREATE TABLE WITH currently consumes only `consistency_level`, such as Strong/Bounded/Session/Eventually. Other collection options are not generally forwarded. DQL WITH and connection-property rules do not apply to DDL.

### Default Values

`DEFAULT` is supported on non-primary scalar fields: `BOOL`, `INT8/INT16/INT32/INT64`, `FLOAT/DOUBLE`, and `VARCHAR`. Numeric defaults may have a sign. Integer defaults must fit exactly, floating defaults must be finite, and strings must fit the declared UTF-8 byte length. Invalid or duplicate defaults, and defaults on primary keys, JSON, Array, or vector fields, are rejected before CREATE is sent.

For omitted/null-bound fields, Milvus applies DEFAULT according to SDK rules. Nullable fields without defaults store NULL; the SDK/server validates non-nullable fields. SHOW CREATE preserves defaults, nullable, Array element/capacity, analyzers and functions. Nullable vectors require 2.6.18+ and do not support IS NULL/IS NOT NULL predicates; scalar nullable/Array retain the 2.6.2 baseline.

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

### Rename Table

```sql
ALTER TABLE old_name RENAME TO new_name;
```

### Show Tables

```sql
SHOW TABLES;                        -- List all tables
SHOW TABLE table_name;              -- Inspect fields
SHOW CREATE TABLE table_name;       -- View detailed create table statement
```

### Partition Management {#partition}

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
```

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

### Show Indexes

```sql
SHOW INDEXES FROM table_name;
SHOW INDEX index_name ON TABLE table_name;
```

---

## 4. User & Role Management {#user}

### User Management

```text
CREATE USER [IF NOT EXISTS] username PASSWORD 'password';
DROP USER [IF EXISTS] username;
SHOW USERS;
```

### Role Management

```text
CREATE ROLE [IF NOT EXISTS] role_name;
DROP ROLE [IF EXISTS] role_name;
SHOW ROLES;
```

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

Privilege names and GRANT/REVOKE object types are forwarded unchanged to the SDK. The example object type is Collection, not TABLE; use server-recognized spelling and privileges. SHOW GRANTS instead uses this adapter's ON TABLE/USER/GLOBAL filter syntax.

Without ON, all grant records for the role are returned. ON TABLE / USER filters by object type and exact name; ON GLOBAL returns only Global records. This lists grants, not effective permissions derived from global or wildcard grants.

---

## 5. Data Manipulation (DML) {#dml}

INSERT/UPSERT accepts multiple VALUES tuples with explicit columns, or VALUES ? bound to an Iterable/Iterator of Maps (columns optional), Lists or Object[] (columns required). INSERT SELECT is unsupported. Counts reflect SDK acknowledgements; RETURN_GENERATED_KEYS additionally exposes the standard JDBC key cursor.

### Insert Data

```sql
-- Insert into default partition
INSERT INTO table_name (id, vector, age) VALUES (1, [0.1, 0.2], 10);

-- Insert into specific partition
INSERT INTO table_name PARTITION partition_name (id, vector) VALUES (2, [0.3, 0.4]);
```

### Upsert (Insert or Replace)

```sql
-- Upsert into default partition
UPSERT INTO table_name (id, vector, age) VALUES (1, [0.1, 0.2], 10);

-- Upsert into specific partition
UPSERT INTO table_name PARTITION partition_name (id, vector) VALUES (2, [0.3, 0.4]);
```

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

> **Note**: Scalar DELETE without LIMIT directly submits a server filter. With LIMIT it selects primary keys page by page before deletion. Vector DELETE searches primary keys page by page before deleting, without whole-SQL atomicity or rollback. Scalar DELETE requires WHERE; plain `DELETE FROM table_name` cannot be executed.

#### 1. Basic Delete (Scalar Filtering)

Use the scalar WHERE syntax documented here.

```sql
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
LOAD TABLE table_name [PARTITION partition_name];
RELEASE TABLE table_name [PARTITION partition_name];

-- Set the load wait timeout
/*+ timeout=60000 */ LOAD TABLE table_name;

-- Submit the release request without waiting
/*+ sync=false */ RELEASE TABLE table_name;
```

`LOAD TABLE` waits until the collection or partition is Loaded by default, and `RELEASE TABLE` waits until it becomes NotLoad. Use `sync=false` to disable waiting.

---

### Flush {#flush}

```sql
FLUSH table_name;
```

Triggers flush and waits, with a current SDK wait limit of 60000ms. Returns update count 0 and does not consume sync/timeout hints. FLUSH is not JDBC commit or a cross-statement transaction boundary.

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

This WITH section applies to vector SELECT search parameters. Scalar SELECT can parse the clause but does not apply its search parameters.

WITH values are bound last in SQL order. Keys are fixed names; values support strings, numbers, booleans, and `?`. JSON serialization escapes quotes, backslashes, and control characters. Quoted numbers remain strings. Write index search parameters such as `nprobe` or `ef` directly in WITH.

If provided, `metric_type` must match the SQL distance operator. Use SQL OFFSET or a hint instead of WITH offset. WITH forwards search parameters; it does not expose every SDK builder property. Configure consistency through the JDBC connection and output fields through SELECT.

```sql
SELECT id, score FROM table_name
WHERE age > ?
ORDER BY vector_col <=> ?
LIMIT ? OFFSET ?
WITH (metric_type='COSINE', nprobe=?);
```

### JDBC Result Metadata

Column order follows SELECT, including metadata for empty results. Int8/16/32/64 map to TINYINT/SMALLINT/INTEGER/BIGINT; Float/Double/Bool/VarChar to corresponding types. JSON is OTHER (JsonElement), FloatVector/Array is ARRAY (List/getArray), Binary/FP16/BF16 is VARBINARY (byte[]/getBytes), Sparse is OTHER (SPARSE_FLOAT_VECTOR, SortedMap&lt;Long,Float>). Array element type, isNullable and isAutoIncrement follow schema. Vector score is Float and is never requested as a stored field; vector SELECT * or explicit score projections include it. Scalar SELECT has no score.

### Count Statistics

This adapter supports using `count` syntax to query the total number of records in a collection or partition.
The result is one row with `COUNT` (BIGINT), read using `rs.getLong("COUNT")`. This is not SELECT COUNT(*) and does not accept vector-range filters.

```sql
-- Query total count of full table
count from table_name;

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

Configure query consistency with the connection property `consistencyLevel`; `consistency_level` is not an implemented query hint. These three overwrite hints apply only to SELECT, not UPDATE/DELETE. See the earlier sections for IMPORT/LOAD/RELEASE sync/timeout.

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

This fragment uses an open `conn` and `books_demo` from the [getting-started program](./usecase.mdx). Each ResultSet belongs to a SQL statement, not to a group of query vectors inside one SELECT.

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
| COUNT FROM | One COUNT (BIGINT) row. |
| SHOW DATABASES / TABLES / PARTITIONS | DATABASE / TABLE / PARTITION (VARCHAR). |
| SHOW PARTITION | PARTITION (VARCHAR), not partition statistics. |
| SHOW TABLE | FIELD, TYPE, DIMENSION, PRIMARY, AUTO_ID, DESCRIPTION, NULLABLE, ELEMENT_TYPE, MAX_CAPACITY, MAX_LENGTH. |
| SHOW CREATE TABLE | CREATE SCRIPT (VARCHAR), a schema description, not a lossless reconstruction guarantee for complex external schemas. |
| SHOW INDEX / INDEXES | INDEX, FIELD, ID, PARAMS. |
| SHOW USERS / ROLES | USER / ROLE (VARCHAR). |
| SHOW GRANTS | DATABASE, ROLE, OBJECT, OBJECT_NAME, PRIVILEGE. |
| SHOW PROGRESS OF LOADING | PROGRESS (BIGINT). |
| SHOW PROGRESS OF INDEX | TOTAL, INDEXED (BIGINT). Without an index name, counts are summed over returned SDK index records, not a completion percentage. |
| INSERT / UPSERT | Actual SDK-acknowledged long count; requested generated keys use a separate JDBC cursor. |
| UPDATE / DELETE | Affected count; use Large methods for large values. |
| DDL, grants, ordinary IMPORT, LOAD/RELEASE, FLUSH | Update count 0; IMPORT RETURNING JOB_ID instead returns one task ID row. |

### JDBC Boundaries

- ResultSets are read-only and forward-only; close promptly. Read FloatVector/Array as List/getArray, Binary/FP16/BF16 through getBytes and Sparse as SortedMap via getObject; do not assume all vectors are float[].
- RETURN_GENERATED_KEYS supports INSERT/UPSERT Int64/VarChar keys, including AUTO_ID and explicit keys. Only SDK IDs are returned; NO_GENERATED_KEYS returns an empty cursor. Defaults remain unchanged; requesting all keys requires O(number of keys) memory.
- JDBC addBatch/executeBatch, savepoints, updatable ResultSets and cross-page transactions are unsupported. PreparedStatement parameterization and JDBC batch are different capabilities.
- Documented SQL/SHOW results are the supported access paths; not every DatabaseMetaData method satisfies relational assumptions made by ORM/BI/migration tools.

## 9. Types, Multi-row Writes and Hybrid Search {#extended}

### Type binding and schema

See [Type binding and generated keys](./usecase.mdx#typed-values) for Java binding/keys. Binary accepts byte[], ByteBuffer or byte Lists. FP16/BF16 numeric inputs use official Float16Utils; packed byte[]/ByteBuffer uses little endian. Sparse accepts nonempty Map&lt;Number,Number>, integer indices in [0,4294967295), and finite weights. ByteBuffer uses remaining without changing position. FloatVector byte[] remains a numeric sequence.

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

Each candidate has one query vector; candidates may target the same or different fields/metrics. Nested lists are not an NQ batch interface. Common WHERE is copied to every candidate. Bind in SQL order: WHERE, each vector/LIMIT/WITH, final LIMIT/OFFSET/WITH.

Candidate LIMITs control candidate counts; outer LIMIT/OFFSET applies to one server-fused result. RRF k defaults to the SDK value when omitted. Weighted requires one finite [0,1] weight per candidate. Select reranker explicitly; unknown ranker options are rejected. There is no SDK Hybrid Iterator: outer LIMIT is mandatory, fetchSize does not paginate fusion, and server window/candidate limits apply without a magic fallback total. Ordinary ORDER BY retains its single-vector rule.

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

REST must be available and uses JDBC database/credentials. Submission and inspection use the same JDBC address within a connection; reconnect to the service hosting the original job. Files must already reside in accessible object storage. The driver does not generate/upload files; official BulkWriter can prepare them. Formats, resource limits and large-scale throughput require deployment validation.

Default sync=true waits. Hint timeout is a client millisecond wait; WITH timeout is a server job duration string. Timeout/cancellation, Failed state or later HTTP failure retains the known Job ID and last progress, without cancelling/resubmitting the server job. A lost create response may leave an existing job with unknown ID; reconcile through the job list. Imports do not promise transactions or exactly-once execution.

Note: Statement.cancel() or a shared JDBC timeout may return its standard cancellation/timeout exception before adapter progress is available; it does not imply zero writes. For reliable recording of an import ID, submit with sync=false RETURNING JOB_ID and inspect separately.

See [Versions and Support](./compatibility.md) for version and feature requirements.
