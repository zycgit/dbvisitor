# jdbc-elastic

## Introduction

jdbc-elastic is a JDBC driver adapter for ElasticSearch that allows developers to operate ElasticSearch using standard JDBC interfaces and ElasticSearch's native REST API style commands.
The goal is to enable developers to seamlessly integrate and use ElasticSearch by leveraging the familiar JDBC programming model.

## Features

- **Native Command Support**: Supports common ElasticSearch REST API commands, including `GET`, `POST`, `PUT`, `DELETE`, `HEAD`.
- **JDBC Standard Interface**: Supports standard interfaces such as `Connection`, `Statement`, `PreparedStatement`, `ResultSet`, etc.
- **Parameter Placeholders**: Supports using `?` placeholders in URL paths and JSON bodies, and setting parameters using `PreparedStatement`.
- **Result Set Mapping**: Automatically maps ElasticSearch JSON responses to JDBC `ResultSet`.
- **Multi-Command Support**: Supports batch operations like `_mget`, `_msearch`.
- **Index Management**: Supports index creation, deletion, Mapping settings, Settings configuration, alias management, etc.
- **Pre-read Optimization**: Supports pre-read configuration for large result sets to optimize reading performance.
- **Multi-Version Compatibility**: Compatible with ES6/ES7/ES8 simultaneously without dependency adjustments.

## Quick Start

### Dependency

```xml
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>jdbc-elastic</artifactId>
    <version>6.3.2</version> <!-- Please use the latest version -->
</dependency>
```

### Connection Configuration

Connect to ElasticSearch using the standard JDBC URL format:

```java
String url = "jdbc:dbvisitor:elastic://127.0.0.1:9200";
Properties props = new Properties();
props.setProperty("username", "elastic");
props.setProperty("password", "changeme");
Connection conn = DriverManager.getConnection(url, props);
```

**Supported Connection Parameters:**

| Parameter Name | Description | Default Value |
| --- | --- | --- |
| `username` | Authentication username | None |
| `password` | Authentication password | None |
| `connectTimeout` | Connection timeout (milliseconds) | - |
| `socketTimeout` | Socket read timeout (milliseconds) | - |
| `indexRefresh` | Whether to automatically refresh the index after write operations | `false` |
| `preRead` | Whether to enable pre-reading | `true` |
| `preReadThreshold` | Pre-read threshold (bytes), triggers file caching if exceeded | `5MB` |
| `preReadMaxFileSize` | Maximum pre-read file size | `20MB` |
| `preReadCacheDir` | Pre-read cache directory | System temporary directory |

### Example Code

#### 1. Execute Search

```java
try (Connection conn = DriverManager.getConnection(url, props)) {
    try (Statement stmt = conn.createStatement()) {
        // Execute DSL query
        String dsl = "POST /my_index/_search { \"query\": { \"match_all\": {} } }";
        try (ResultSet rs = stmt.executeQuery(dsl)) {
            while (rs.next()) {
                System.out.println("ID: " + rs.getString("_ID"));
                System.out.println("Source: " + rs.getString("_DOC")); // _DOC returns the original JSON
            }
        }
    }
}
```

#### 2. Use PreparedStatement (With Parameters)

```java
String dsl = "POST /my_index/_search { \"query\": { \"term\": { \"user\": ? } } }";
try (PreparedStatement pstmt = conn.prepareStatement(dsl)) {
    pstmt.setString(1, "kimchy");
    try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
            // ...
        }
    }
}
```

#### 3. Insert/Update Document

```java
String insertSql = "POST /my_index/_doc/1 { \"user\": \"kimchy\", \"post_date\": \"2009-11-15T14:12:12\", \"message\": \"trying out Elasticsearch\" }";
try (Statement stmt = conn.createStatement()) {
    stmt.executeUpdate(insertSql);
}
```

#### 4. Index Management

```java
try (Statement stmt = conn.createStatement()) {
    // Create Index
    stmt.execute("PUT /new_index");
    
    // Set Mapping
    stmt.execute("PUT /new_index/_mapping { \"properties\": { \"field1\": { \"type\": \"text\" } } }");
    
    // Delete Index
    stmt.execute("DELETE /new_index");
}
```

#### 5. View Cluster Information (_cat)

```java
try (Statement stmt = conn.createStatement()) {
    try (ResultSet rs = stmt.executeQuery("GET /_cat/nodes?h=ip,port,heapPercent,name")) {
        while (rs.next()) {
            System.out.println(rs.getString("name") + " - " + rs.getString("ip"));
        }
    }
}
```

## Supported Commands Overview

jdbc-elastic converts SQL-style commands into underlying REST requests by parsing them. The supported command patterns are as follows:

- **Query**: `GET/POST .../_search`, `_count`, `_msearch`, `_mget`, `_explain`, `_source`
- **Document Operations**: `PUT/POST .../_doc/...`, `_create`, `_update`, `DELETE ...`
- **Index Operations**: `PUT/DELETE /index`, `_open`, `_close`, `_mapping`, `_settings`, `_aliases`, `_reindex`, `_refresh`
- **Cluster Information**: `GET /_cat/...`
- **Generic Requests**: Supports arbitrary `GET`, `POST`, `PUT`, `DELETE`, `HEAD` requests.

## Limitations

- **Transaction Support**: ElasticSearch itself does not support ACID transactions, so `commit` and `rollback` operations are ineffective (default is auto-commit).
- **SQL Support**: Currently mainly supports REST API style commands, standard SQL syntax (such as `SELECT * FROM ...`) is not supported yet.

