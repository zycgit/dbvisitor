<p align="center">
Switch Language:
    [<a target="_blank" href='./SYNTAX_MANUAL_en.md'>English</a>]
    [<a target="_blank" href='./SYNTAX_MANUAL_cn.md'>中文</a>]
</p>

# Milvus JDBC Adapter Syntax Manual

> **Note**: Some advanced features of this driver (such as `count` statistics, BulkInsert, etc.) depend on Milvus 2.2+ versions. It is recommended to use Milvus 2.3.x or higher for the best experience.

This document details the SQL syntax supported by the Milvus JDBC Adapter. Compared to the official CLI, this Adapter adopts a syntax structure more compatible with standard SQL usage, supporting database management, table management, data manipulation, and query functions.

## 1. Database Management

### Create Database
```sql
CREATE DATABASE [IF NOT EXISTS] db_name;
```

### Drop Database
```sql
DROP DATABASE [IF EXISTS] db_name;
```

### Alter Database Properties
```sql
ALTER DATABASE db_name SET PROPERTIES ("key" = "value", ...);
```

### Show Databases
```sql
SHOW DATABASES;
```

---

## 2. Table & Partition Management

### Create Table (Collection)
Supports defining primary keys, vector fields, and other attributes.
```sql
CREATE TABLE [IF NOT EXISTS] table_name (
    id INT64 PRIMARY KEY,
    vector_col FLOAT_VECTOR(128),
    age INT32 DEFAULT 0
) WITH (
    consistency_level = "Strong"
);
```

### Vector Data Format Support
In statements involving vector operations such as `INSERT`, `SEARCH` (SELECT ... ORDER BY vector), `DELETE`, multiple vector expression forms are supported:
1. **JSON Array Literal**:
   - `[0.1, 0.2, 0.3, ...]`
2. **JDBC Parameter Binding**:
   - `?` (PreparedStatement)
   - Supported Java types for binding: `List<Float>`, `List<Double>` (automatic conversion), `float[]`, `double[]`.
3. **Batch Vector**:
   - In `SEARCH`, supports passing multiple vectors at once for batch search: `[[0.1, 0.2], [0.3, 0.4]]`.

### Drop Table
```sql
DROP TABLE [IF EXISTS] table_name;
```

### Rename Table
```sql
ALTER TABLE old_name RENAME TO new_name;
```

### Show Tables
```sql
SHOW TABLES;                        -- List all tables
SHOW CREATE TABLE table_name;       -- View detailed create table statement
```

### Partition Management
```sql
CREATE PARTITION [IF NOT EXISTS] partition_name ON TABLE table_name;
DROP PARTITION [IF EXISTS] partition_name ON TABLE table_name;
SHOW PARTITIONS FROM table_name;    -- List all partitions of a table
SHOW PARTITION p_name ON TABLE t_name;
```

### Alias Management
```sql
CREATE ALIAS alias_name FOR TABLE table_name;
ALTER ALIAS alias_name FOR TABLE table_name;
DROP ALIAS [IF EXISTS] alias_name;
```

---

## 3. Index Management

### Create Index
```sql
CREATE INDEX index_name ON TABLE table_name (vector_col) USING "IVF_FLAT" WITH (nlist = 1024);
```

#### Supported Index Types (USING)
- **Vector Indices**:
  - Memory Indices: `FLAT`, `IVF_FLAT`, `IVF_SQ8`, `IVF_PQ`, `HNSW`, `SCANN`, `AUTOINDEX`
  - Disk Indices: `DISKANN`
  - GPU Indices: `GPU_IVF_FLAT`, `GPU_IVF_PQ`
  - Legacy/Other: `RNSG`, `ANNOY`
  - Sparse Vector: `SPARSE_INVERTED_INDEX`
- **Scalar Indices**:
  - `STL_SORT` (Sorting index, for numerical values)
  - `Trie` (Trie index, for strings)
  - `INVERTED` (Inverted index, for keyword search)

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

## 4. User & Role Management

### User Management
```sql
CREATE USER [IF NOT EXISTS] username PASSWORD 'password';
DROP USER [IF EXISTS] username;
SHOW USERS;
```

### Role Management
```sql
CREATE ROLE [IF NOT EXISTS] role_name;
DROP ROLE [IF EXISTS] role_name;
SHOW ROLES;
```

### Grant & Revoke
```sql
GRANT ROLE role_name TO username;
REVOKE ROLE role_name FROM username;
SHOW GRANTS FOR ROLE role_name ON GLOBAL;
SHOW GRANTS FOR ROLE role_name ON TABLE table_name;
```

---

## 5. Data Manipulation (DML)

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

### Update Data

> **Note**: Milvus's Update operation is essentially an Upsert (Insert or Replace). The Update implemented by this driver is actually "Search-to-Upsert": it first queries records matching the conditions, modifies the corresponding fields in memory, and then writes (Upsert) back to the database. Therefore, **it is not recommended to perform full-table Update operations on massive data**.

#### 1. Basic Update (Scalar Filtering)
Supports standard SQL `WHERE` clause for filtering updates.

```sql
-- Update by Primary Key
UPDATE table_name SET age = 20 WHERE id = 1;

-- Update by scalar condition (Query then Write-back)
UPDATE table_name SET status = 'active' WHERE age > 18;
```

#### 2. KNN Update (Nearest Neighbor Update)
Update the K records closest to the target vector.
Underlying mechanism: Execute TopK Search -> Modify in memory -> Upsert.

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

> **Note**: Milvus's delete operation is logical deletion, implemented by writing into Delete log. Since Milvus native delete operations mainly rely on Primary Key (or filtering based on scalar expressions), this driver adopts a "Search-to-Delete" strategy (Search PK then Delete) when implementing vector-related deletions.

#### 1. Basic Delete (Scalar Filtering)
Supports standard SQL `WHERE` clause for filtered deletion.

```sql
-- Delete by Primary Key
DELETE FROM table_name WHERE id = 1;
DELETE FROM table_name WHERE id IN [1, 2, 3];

-- Delete by scalar condition (Requires partition support enabled or full scan)
DELETE FROM table_name WHERE age > 18 AND status = 'inactive';

-- Delete from specific partition
DELETE FROM table_name PARTITION partition_name WHERE age > 10;
```

#### 2. KNN Delete (Nearest Neighbor Delete)
Delete the K records closest to the target vector. Requires `ORDER BY` and `LIMIT`.
Underlying mechanism: Execute TopK Search to get PKs -> Execute deletion.

```sql
-- Delete 100 records closest to [0.1, 0.2]
DELETE FROM table_name ORDER BY vector_col <-> [0.1, 0.2] LIMIT 100;

-- Combined with scalar filtering: Delete 10 most similar records where category='book'
DELETE FROM table_name WHERE category = 'book' ORDER BY vector_col <-> [0.1, 0.2] LIMIT 10;
```

#### 3. Range Delete
Delete all records falling within a specified distance (radius) of the target vector. Can use `vector_range` function or `<->` comparison expression.
Underlying mechanism: Execute Range Search to get PKs -> Execute deletion.

```sql
-- Using vector_range function (Recommended)
DELETE FROM table_name WHERE vector_range(vector_col, [0.1, 0.2], 0.5);

-- Using comparison expression: Delete all records with distance < 0.5 from [0.1, 0.2]
DELETE FROM table_name WHERE vector_col <-> [0.1, 0.2] < 0.5;

-- Combined with LIMIT for protection (Delete at most 1000 matching records)
DELETE FROM table_name WHERE vector_range(vector_col, [0.1, 0.2], 0.5) LIMIT 1000;
```

### Data Import (Import)
```sql
-- Import from file (For server-side file loading)
IMPORT FROM 'path/to/file.csv' INTO TABLE table_name;
IMPORT FROM 'file.json' INTO TABLE table_name PARTITION partition_name;
```

### Load & Release
Milvus requires collections to be loaded into memory before searching.
```sql
LOAD TABLE table_name [PARTITION partition_name];
RELEASE TABLE table_name [PARTITION partition_name];
```

---

## 6. Query & Search (DQL)

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
SELECT * FROM table_name ORDER BY vector_col <-> [0.1, 0.2, ...] LIMIT 10;

-- Search with pre-filtering
SELECT * FROM table_name WHERE category = 'book' ORDER BY vector_col <-> [0.1, ...] LIMIT 5;

-- Range Search / Distance Filtering
-- Method 1: Using vector_range function (Recommended)
SELECT * FROM table_name WHERE vector_range(vector_col, [0.1, 0.2], 0.8) LIMIT 5;

-- Method 2: Using comparison expression (WHERE vector_col <-> [vector] < distance_threshold)
SELECT * FROM table_name WHERE vector_col <-> [0.1, 0.2] < 0.8 LIMIT 5;
```

### Advanced Search Parameters (WITH Clause)
Use the `WITH` clause at the end of the `SELECT` statement to pass Milvus-specific search parameters.

Supported parameters:
- `metric_type`: Distance metric type (L2, IP, COSINE, etc.)
- `params`: Index-specific parameters (JSON string, e.g., `{"nprobe": 10}`)
- `consistency_level`: Consistency level (Strong, Session, Bounded, Eventually)
- `round_decimal`: Result decimal precision
- `out_fields`: (Usually not manually specified, determined by SELECT list)

Example:
```sql
SELECT id, score FROM table_name 
ORDER BY vector_col <-> [0.1, 0.2] 
LIMIT 10 
WITH (
    metric_type = "L2", 
    params = "{\"nprobe\": 10}", 
    consistency_level = "Strong"
);
```

### Count Statistics
This adapter supports using `count` syntax to query the total number of records in a collection or partition.
> **Requirement**: Milvus server version >= 2.2.0.

```sql
-- Query total count of full table
count from table_name;

-- Query total count of specific partition
count from table_name partition(partition_name);

-- Count with conditional filtering (supports scalar filtering)
count from table_name where age > 18;
```

### Query Hints
This adapter supports determining pagination or query behavior using SQL Hints (`/*+ ... */`), similar to usage in Elastic/Mongo adapters.

Supported Hints:
- `overwrite_find_limit`: Force overwrite query LIMIT (TopK).
- `overwrite_find_skip`: Force overwrite query OFFSET.
- `overwrite_find_as_count`: Force convert current query to COUNT query (ignoring SELECT fields and sorting).

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

## 7. Progress Monitoring

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
