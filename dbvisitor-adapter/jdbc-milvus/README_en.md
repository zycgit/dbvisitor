## Introduction

jdbc-milvus is a JDBC driver adapter for the Milvus vector database.

Core value:
- Use standard JDBC interfaces (`Connection`, `Statement`, `PreparedStatement`, `ResultSet`).
- Use SQL-style commands that map to Milvus native operations.

Problems it solves:
- Lowers the learning barrier for Java developers familiar with SQL/JDBC.
- Provides a unified coding style for heterogeneous data sources through dbVisitor.
- Integrates more easily with the JDBC ecosystem (e.g., Spring, MyBatis).

## Features

- JDBC core compatibility with `PreparedStatement` and `?` placeholders.
- DDL for collections: create, drop, rename, show, show create.
- DML: insert, upsert, and delete (scalar filter, vector search, vector range).
- DQL: select with scalar filters, vector search via `ORDER BY field <-> vector`, range search in `WHERE`.
- Index management: create, drop, show indexes, show build progress.
- Partition management: create, drop, show partition(s).
- Database management: create, alter properties, drop, show databases.
- User and role management: create/drop user or role, grant/revoke, show users/roles/grants.
- Alias management and loading progress.

## Usage

### 4.1 Maven Dependency

```xml
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>jdbc-milvus</artifactId>
    <version>latest version</version>
</dependency>
```

### 4.2 Connect

```java
String url = "jdbc:dbvisitor:milvus://127.0.0.1:19530/default";
Connection conn = DriverManager.getConnection(url);
```

Cluster example (multiple hosts are separated by `;` and can include health ports):

```java
String url = "jdbc:dbvisitor:milvus://host1:19530:9091;host2:19530:9091/default";
Connection conn = DriverManager.getConnection(url);
```

### 4.3 Connection Parameters

The adapter exposes the following JDBC URL properties (from `MilvusKeys` and `getPropertyNames`).

| Parameter | Description | Notes |
| --- | --- | --- |
| adapterName | Adapter name | Read-only, set by JDBC URL (`milvus`) |
| server | Server list | `host:port[:healthPort]`, multiple hosts separated by `;` |
| timeZone | Time zone | Driver-level property, not used by Milvus adapter logic |
| database | Default database | Can also be set via URL path `/database` |
| token | Token auth | Used when set; overrides user/password |
| user | Username | Used with `password` when `token` is absent |
| password | Password | Used with `user` when `token` is absent |
| timeout | Timeout | Declared property; not applied in `MilvusConnFactory` |
| connectTimeout | Connect timeout | Milliseconds |
| keepAliveTime | Keep-alive time | Milliseconds |
| keepAliveTimeout | Keep-alive timeout | Milliseconds |
| keepAliveWithoutCalls | Keep-alive without calls | `true` or `false` |
| idleTimeout | Idle timeout | Milliseconds |
| rpcDeadline | RPC deadline | Milliseconds |
| interceptor | Client interceptor | Fully qualified `InvocationHandler` class name |
| customMilvus | Custom client factory | Fully qualified `CustomMilvus` class name |

## Supported Commands

The grammar is defined in [SYNTAX_MANUAL_en.md](SYNTAX_MANUAL_en.md). The adapter implements the following command families:

- Database
  - `CREATE DATABASE [IF NOT EXISTS] db`
  - `ALTER DATABASE db SET PROPERTIES (...)`
  - `DROP DATABASE [IF EXISTS] db`
  - `SHOW DATABASES`

- Table / Collection
  - `CREATE TABLE [IF NOT EXISTS] name (...)`
  - `DROP TABLE [IF EXISTS] name`
  - `ALTER TABLE name RENAME TO new_name`
  - `SHOW TABLES`
  - `SHOW TABLE name`
  - `SHOW CREATE TABLE name`

- Index
  - `CREATE INDEX [name] ON name (field) USING "IVF_FLAT" WITH (...)`
  - `DROP INDEX name ON table`
  - `SHOW INDEX name ON table`
  - `SHOW INDEXES FROM table`
  - `SHOW PROGRESS OF INDEX [name] ON table`

- Partition
  - `CREATE PARTITION [IF NOT EXISTS] p ON table`
  - `DROP PARTITION [IF EXISTS] p ON table`
  - `SHOW PARTITION p ON table`
  - `SHOW PARTITIONS FROM table`

- Data and Query
  - `INSERT INTO table (...) VALUES (...)`
  - `DELETE FROM table WHERE ...`
  - `SELECT ... FROM table WHERE ...`
  - `SELECT ... FROM table ORDER BY vector_field <-> [..] LIMIT n`
  - `SELECT ... FROM table WHERE vector_field <-> [..] < radius`
  - `COUNT FROM table WHERE ...`

- Import / Load / Release
  - `IMPORT FROM FILE 'path' INTO TABLE name [PARTITION p]`
  - `LOAD TABLE name [PARTITION p]`
  - `RELEASE TABLE name [PARTITION p]`

- Alias
  - `CREATE ALIAS a FOR TABLE name`
  - `ALTER ALIAS a FOR TABLE name`
  - `DROP ALIAS [IF EXISTS] a`
  - `SHOW PROGRESS OF LOADING ON TABLE name [PARTITION p]`

- User / Role
  - `CREATE USER [IF NOT EXISTS] u PASSWORD 'pwd'`
  - `DROP USER [IF EXISTS] u`
  - `CREATE ROLE [IF NOT EXISTS] r`
  - `DROP ROLE [IF EXISTS] r`
  - `GRANT ROLE r TO u`
  - `REVOKE ROLE r FROM u`
  - `GRANT privilege ON objectType objectName|* TO ROLE r`
  - `REVOKE privilege ON objectType objectName|* FROM ROLE r`
  - `SHOW USERS`
  - `SHOW ROLES`
  - `SHOW GRANTS FOR ROLE r`

## Code Examples

### 1) Create a collection

```java
try (Statement stmt = conn.createStatement()) {
    stmt.executeUpdate("CREATE TABLE book_vectors (" +
            "book_id int64 primary key, " +
            "word_count int64, " +
            "book_intro float_vector(2)" +
            ")");
}
```

### 2) Create an index and load

```java
try (Statement stmt = conn.createStatement()) {
    stmt.executeUpdate("CREATE INDEX idx_book_intro ON book_vectors (book_intro) USING \"IVF_FLAT\" WITH (nlist = 1024, metric_type = \"L2\")");
    stmt.executeUpdate("LOAD TABLE book_vectors");
}
```

### 3) Insert with `PreparedStatement`

```java
String insertSql = "INSERT INTO book_vectors (book_id, word_count, book_intro) VALUES (?, ?, ?)";
try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
    ps.setLong(1, 1L);
    ps.setLong(2, 1000L);
    ps.setObject(3, java.util.Arrays.asList(0.1f, 0.2f));
    ps.executeUpdate();
}
```

### 4) Vector search

```java
String querySql = "SELECT book_id FROM book_vectors ORDER BY book_intro <-> ? LIMIT 1";
try (PreparedStatement ps = conn.prepareStatement(querySql)) {
    ps.setObject(1, java.util.Arrays.asList(0.1f, 0.2f));
    try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
            System.out.println(rs.getString("book_id"));
        }
    }
}
```

### 5) Query hints

```java
try (Statement stmt = conn.createStatement()) {
    ResultSet rs = stmt.executeQuery("/*+ overwrite_find_limit=1 */ SELECT * FROM book_vectors");
    while (rs.next()) {
        // handle rows
    }
}
```

## Hint Support

jdbc-milvus supports SQL hints to override or enhance query behavior. Hint format is `/*+ hint_name=value */`, and it must be placed at the beginning of the SQL statement.

| Hint Name | Description | Allowed Values | Example |
| --- | --- | --- | --- |
| `consistency_level` | Sets the consistency level for the query. | `Strong`, `Bounded`, `Session`, `Eventually` | `/*+ consistency_level=Strong */ SELECT ...` |
| `overwrite_find_limit` | Overrides the query `LIMIT`/TopK. | Number | `/*+ overwrite_find_limit=100 */ SELECT ...` |
| `overwrite_find_skip` | Overrides the query `OFFSET`. | Number | `/*+ overwrite_find_skip=10 */ SELECT ...` |
| `overwrite_find_as_count` | Transforms the query into a count operation and returns only the count. | Any value | `/*+ overwrite_find_as_count=true */ SELECT ...` |

## Limitations

- Changing catalog/schema to a different database is not supported (`setCatalog`/`setSchema` will reject changes).
- `DELETE` requires a `WHERE` clause; otherwise it fails with a syntax error.
- Only the SQL grammar defined in [SYNTAX_MANUAL_en.md](SYNTAX_MANUAL_en.md) is supported.

## Compatibility

- JDK: 8
- Milvus Java SDK: 2.5.4 (see dependency in [dbvisitor-adapter/jdbc-milvus/pom.xml](dbvisitor-adapter/jdbc-milvus/pom.xml))

## More Resources

- Syntax Manual: [SYNTAX_MANUAL_en.md](SYNTAX_MANUAL_en.md)
- dbVisitor Docs: [dbvisitor-doc/README.md](../../dbvisitor-doc/README.md)
- Examples: [dbvisitor-example](../../dbvisitor-example)
