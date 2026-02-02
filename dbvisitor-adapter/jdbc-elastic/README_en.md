## Introduction
jdbc-elastic is a JDBC driver adapter for Elasticsearch. It allows developers to operate Elasticsearch using standard JDBC interfaces and native REST-style commands.

Core value:
- Use standard JDBC APIs (Connection, Statement, PreparedStatement, ResultSet).
- Use native REST-style command text that maps to Elasticsearch operations.
- Provide a unified programming style for heterogeneous data sources via dbVisitor.

## Features
- Implements the JDBC core interfaces and supports `PreparedStatement` placeholders.
- Supports REST-style commands and multiple commands separated by semicolons.
- Supports search/count/multi-search/multi-get, document CRUD, index management, and `_cat` queries.
- Supports `HEAD` requests and returns a `STATUS` column.
- Result mapping: search-like responses map to `_ID` and `_DOC` columns; pre-read expands fields as columns.
- Pre-read mode for large result sets with configurable threshold, max file size, and cache directory.
- Optional `indexRefresh` to append `refresh=true` for write operations.
- Elasticsearch 6/7 scenarios are covered by dbVisitor dialects and realdb tests (see `Elastic6Dialect`, `Elastic7Dialect`, and `realdb/elastic6|elastic7`).

## Usage

### 4.1 Dependency
```xml
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>jdbc-elastic</artifactId>
    <version>Latest Version</version>
</dependency>
```

### 4.2 Connection
```java
String url = "jdbc:dbvisitor:elastic://127.0.0.1:9200";
Properties props = new Properties();
props.setProperty("user", "elastic");
props.setProperty("password", "changeme");
props.setProperty("connectTimeout", "5000");
props.setProperty("socketTimeout", "10000");
Connection conn = DriverManager.getConnection(url, props);
```

JDBC URL format:
```
jdbc:dbvisitor:elastic://{host}:{port}
```

Cluster example (multiple hosts):
```
jdbc:dbvisitor:elastic://host1:9200;host2:9200
```

### 4.3 Connection Parameters

| Parameter | Description | Default |
| --- | --- | --- |
| `server` | Host segment from the JDBC URL. Supports `host:port` or `host1:port;host2:port`. | From URL |
| `user` / `username` | Username for authentication. | None |
| `password` | Password for authentication. | Empty |
| `connectTimeout` | Connection timeout (ms). | Driver default |
| `socketTimeout` | Socket read timeout (ms). | Driver default |
| `timeZone` | Driver time zone used for type conversion (for example `+08:00`). | Empty |
| `indexRefresh` | Append `refresh=true` for write operations. | `false` |
| `preRead` | Enable pre-read mode. | `true` |
| `preReadThreshold` | Pre-read threshold size. Accepts `B/KB/MB/GB`. | `5MB` |
| `preReadMaxFileSize` | Maximum pre-read file size. Accepts `B/KB/MB/GB`. | `20MB` |
| `preReadCacheDir` | Cache directory for pre-read mode. | `java.io.tmpdir` |
| `customElastic` | Fully qualified class name implementing `CustomElastic`. | None |
| `clientName` | Declared parameter but not applied by this adapter. | Not applied |

## Supported Commands
The command syntax follows Elasticsearch REST endpoints.

- Query
  - `GET/POST /{index}/_search` ([Search API](https://www.elastic.co/guide/en/elasticsearch/reference/current/search-search.html))
  - `GET/POST /{index}/_count` ([Count API](https://www.elastic.co/guide/en/elasticsearch/reference/current/search-count.html))
  - `GET/POST /{index}/_msearch` ([Multi search API](https://www.elastic.co/guide/en/elasticsearch/reference/current/search-multi-search.html))
  - `GET/POST /{index}/_mget` ([Multi get API](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-multi-get.html))
  - `GET/POST /{index}/_explain/{id}` ([Explain API](https://www.elastic.co/guide/en/elasticsearch/reference/current/search-explain.html))
  - `GET/POST /{index}/_source/{id}` ([Get source API](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-get-source.html))

- Document operations
  - `PUT/POST /{index}/_doc/{id}` ([Index API](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-index_.html))
  - `PUT/POST /{index}/_create/{id}` ([Create op](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-index_.html#docs-index-api-op_type))
  - `POST /{index}/_update/{id}` ([Update API](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html))
  - `POST /{index}/_update_by_query` ([Update by query API](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update-by-query.html))
  - `DELETE /{index}/_doc/{id}` ([Delete API](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-delete.html))
  - `POST /{index}/_delete_by_query` ([Delete by query API](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-delete-by-query.html))

- Index operations
  - `GET /{index}/_mapping` ([Get mapping API](https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-get-mapping.html))
  - `PUT/POST /{index}/_mapping` ([Put mapping API](https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-put-mapping.html))
  - `GET /{index}/_settings` ([Get settings API](https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-get-settings.html))
  - `PUT /{index}/_settings` ([Update settings API](https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-update-settings.html))
  - `GET/POST /_aliases` ([Aliases API](https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-aliases.html))
  - `POST /{index}/_open` / `POST /{index}/_close` ([Open/Close index API](https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-open-close.html))
  - `GET/POST /{index}/_refresh` ([Refresh API](https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-refresh.html))
  - `POST /_reindex` ([Reindex API](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-reindex.html))

- `_cat` APIs
  - `GET /_cat/indices` ([cat indices](https://www.elastic.co/guide/en/elasticsearch/reference/current/cat-indices.html))
  - `GET /_cat/nodes` ([cat nodes](https://www.elastic.co/guide/en/elasticsearch/reference/current/cat-nodes.html))
  - `GET /_cat/health` ([cat health](https://www.elastic.co/guide/en/elasticsearch/reference/current/cat-health.html))

- Other
  - `HEAD /{path}` (returns `STATUS` column; see [REST APIs](https://www.elastic.co/guide/en/elasticsearch/reference/current/rest-apis.html))
  - Generic REST: `GET/POST/PUT/DELETE /{path}` ([REST APIs](https://www.elastic.co/guide/en/elasticsearch/reference/current/rest-apis.html))

## Code Examples

### 1) Create index and mapping (DDL)
```java
try (Statement stmt = conn.createStatement()) {
    stmt.execute("PUT /my_index");
    stmt.execute("PUT /my_index/_mapping { \"properties\": { \"title\": { \"type\": \"text\" } } }");
}
```

### 2) Insert document (DML)
```java
try (Statement stmt = conn.createStatement()) {
    stmt.executeUpdate("POST /my_index/_doc/1 { \"user\": \"kimchy\", \"message\": \"hello\" }");
}
```

### 3) Search with `PreparedStatement` (DQL)
```java
String dsl = "POST /my_index/_search { \"query\": { \"term\": { \"user\": ? } } }";
try (PreparedStatement pstmt = conn.prepareStatement(dsl)) {
    pstmt.setString(1, "kimchy");
    try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
            rs.getString("_ID");
            rs.getString("_DOC");
        }
    }
}
```

### 4) Path placeholder `{?}`
```java
try (PreparedStatement pstmt = conn.prepareStatement("GET /my_index/_doc/{?}")) {
    pstmt.setString(1, "1");
    pstmt.executeQuery();
}
```

### 5) `_cat` nodes
```java
try (Statement stmt = conn.createStatement()) {
    stmt.executeQuery("GET /_cat/nodes");
}
```

## Hint Support
Hints must appear at the beginning of the command text. Format: `/*+ name=value */`.

| Hint | Description | Example |
| --- | --- | --- |
| `overwrite_find_limit` | Overrides the `size` in search requests. | `/*+ overwrite_find_limit=10 */ POST /idx/_search` |
| `overwrite_find_skip` | Overrides the `from` in search requests. | `/*+ overwrite_find_skip=20 */ POST /idx/_search` |
| `overwrite_find_as_count` | Converts `/_search` to `/_count`. | `/*+ overwrite_find_as_count */ POST /idx/_search` |

## Limitations
- `_cat` queries automatically append `format=json` (if specified, it must be json).
- Only REST-style command grammar is supported; Elasticsearch SQL is not supported.

## Compatibility
- JDK 8+
- Elasticsearch REST client: `elasticsearch-rest-client` 7.17.10
- Jackson: `jackson-databind` 2.18.0
- dbVisitor includes Elastic6/Elastic7 dialects and realdb test suites for ES6/ES7 scenarios.

## More Resources
- dbVisitor Documentation: https://www.dbvisitor.net/docs/guides/overview

