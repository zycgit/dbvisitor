## 介绍
jdbc-elastic 是 Elasticsearch 的 JDBC 驱动适配器，允许开发者使用标准 JDBC 接口和原生 REST 风格命令操作 Elasticsearch。

核心价值：
- 使用标准 JDBC API（Connection、Statement、PreparedStatement、ResultSet）。
- 使用原生 REST 风格命令映射到 Elasticsearch 操作。
- 通过 dbVisitor 为异构数据源提供统一的编码风格。

## 特性
- 实现 JDBC 核心接口并支持 `PreparedStatement` 占位符。
- 支持 REST 风格命令，支持多条命令以分号顺序执行。
- 支持搜索/统计/批量查询、文档 CRUD、索引管理与 `_cat` 查询。
- 支持 `HEAD` 请求并返回 `STATUS` 列。
- 结果映射：搜索类响应映射为 `_ID` 与 `_DOC` 列；预读模式下会展开字段列。
- 预读模式支持阈值、最大文件大小、缓存目录配置。
- 可选 `indexRefresh` 在写入时追加 `refresh=true`。
- dbVisitor 提供 Elastic6/Elastic7 方言与 realdb 场景化测试（`Elastic6Dialect`、`Elastic7Dialect`、`realdb/elastic6|elastic7`）。

## 使用

### 4.1 引入依赖
```xml
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>jdbc-elastic</artifactId>
    <version>最新版本</version>
</dependency>
```

### 4.2 建立连接
```java
String url = "jdbc:dbvisitor:elastic://127.0.0.1:9200";
Properties props = new Properties();
props.setProperty("user", "elastic");
props.setProperty("password", "changeme");
props.setProperty("connectTimeout", "5000");
props.setProperty("socketTimeout", "10000");
Connection conn = DriverManager.getConnection(url, props);
```

JDBC URL 格式：
```
jdbc:dbvisitor:elastic://{host}:{port}
```

集群示例（多主机）：
```
jdbc:dbvisitor:elastic://host1:9200;host2:9200
```

### 4.3 连接参数详解

| 参数 | 说明 | 默认值 |
| --- | --- | --- |
| `server` | JDBC URL 的 host 部分。支持 `host:port` 或 `host1:port;host2:port`。 | 来自 URL |
| `user` / `username` | 认证用户名。 | 无 |
| `password` | 认证密码。 | 空 |
| `connectTimeout` | 连接超时（毫秒）。 | 驱动默认值 |
| `socketTimeout` | Socket 读取超时（毫秒）。 | 驱动默认值 |
| `timeZone` | 驱动用于类型转换的时区（例如 `+08:00`）。 | 空 |
| `indexRefresh` | 写入操作追加 `refresh=true`。 | `false` |
| `preRead` | 是否启用预读模式。 | `true` |
| `preReadThreshold` | 预读阈值，支持 `B/KB/MB/GB`。 | `5MB` |
| `preReadMaxFileSize` | 预读最大文件大小，支持 `B/KB/MB/GB`。 | `20MB` |
| `preReadCacheDir` | 预读缓存目录。 | `java.io.tmpdir` |
| `customElastic` | 实现 `CustomElastic` 的类全名。 | 无 |
| `clientName` | 已声明但未应用的参数。 | 未应用 |

## 支持的指令
命令语法参考 Elasticsearch 官方文档。

- 查询
  - `GET/POST /{index}/_search`（[Search API](https://www.elastic.co/guide/en/elasticsearch/reference/current/search-search.html)）
  - `GET/POST /{index}/_count`（[Count API](https://www.elastic.co/guide/en/elasticsearch/reference/current/search-count.html)）
  - `GET/POST /{index}/_msearch`（[Multi search API](https://www.elastic.co/guide/en/elasticsearch/reference/current/search-multi-search.html)）
  - `GET/POST /{index}/_mget`（[Multi get API](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-multi-get.html)）
  - `GET/POST /{index}/_explain/{id}`（[Explain API](https://www.elastic.co/guide/en/elasticsearch/reference/current/search-explain.html)）
  - `GET/POST /{index}/_source/{id}`（[Get source API](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-get-source.html)）

- 文档操作
  - `PUT/POST /{index}/_doc/{id}`（[Index API](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-index_.html)）
  - `PUT/POST /{index}/_create/{id}`（[Create op](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-index_.html#docs-index-api-op_type)）
  - `POST /{index}/_update/{id}`（[Update API](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html)）
  - `POST /{index}/_update_by_query`（[Update by query API](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update-by-query.html)）
  - `DELETE /{index}/_doc/{id}`（[Delete API](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-delete.html)）
  - `POST /{index}/_delete_by_query`（[Delete by query API](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-delete-by-query.html)）

- 索引操作
  - `GET /{index}/_mapping`（[Get mapping API](https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-get-mapping.html)）
  - `PUT/POST /{index}/_mapping`（[Put mapping API](https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-put-mapping.html)）
  - `GET /{index}/_settings`（[Get settings API](https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-get-settings.html)）
  - `PUT /{index}/_settings`（[Update settings API](https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-update-settings.html)）
  - `GET/POST /_aliases`（[Aliases API](https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-aliases.html)）
  - `POST /{index}/_open` / `POST /{index}/_close`（[Open/Close index API](https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-open-close.html)）
  - `GET/POST /{index}/_refresh`（[Refresh API](https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-refresh.html)）
  - `POST /_reindex`（[Reindex API](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-reindex.html)）

- `_cat` 查询
  - `GET /_cat/indices`（[cat indices](https://www.elastic.co/guide/en/elasticsearch/reference/current/cat-indices.html)）
  - `GET /_cat/nodes`（[cat nodes](https://www.elastic.co/guide/en/elasticsearch/reference/current/cat-nodes.html)）
  - `GET /_cat/health`（[cat health](https://www.elastic.co/guide/en/elasticsearch/reference/current/cat-health.html)）

- 其他
  - `HEAD /{path}`（返回 `STATUS` 列，参考 [REST APIs](https://www.elastic.co/guide/en/elasticsearch/reference/current/rest-apis.html)）
  - 通用 REST：`GET/POST/PUT/DELETE /{path}`（[REST APIs](https://www.elastic.co/guide/en/elasticsearch/reference/current/rest-apis.html)）

## 常用操作示例

### 1) 创建索引与 Mapping（DDL）
```java
try (Statement stmt = conn.createStatement()) {
    stmt.execute("PUT /my_index");
    stmt.execute("PUT /my_index/_mapping { \"properties\": { \"title\": { \"type\": \"text\" } } }");
}
```

### 2) 插入文档（DML）
```java
try (Statement stmt = conn.createStatement()) {
    stmt.executeUpdate("POST /my_index/_doc/1 { \"user\": \"kimchy\", \"message\": \"hello\" }");
}
```

### 3) 使用 `PreparedStatement` 查询（DQL）
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

### 4) 路径占位符 `{?}`
```java
try (PreparedStatement pstmt = conn.prepareStatement("GET /my_index/_doc/{?}")) {
    pstmt.setString(1, "1");
    pstmt.executeQuery();
}
```

### 5) `_cat` 节点
```java
try (Statement stmt = conn.createStatement()) {
    stmt.executeQuery("GET /_cat/nodes");
}
```

## Hint 支持
Hint 必须位于命令开头，格式为 `/*+ name=value */`。

| Hint | 说明 | 示例 |
| --- | --- | --- |
| `overwrite_find_limit` | 覆盖搜索请求的 `size`。 | `/*+ overwrite_find_limit=10 */ POST /idx/_search` |
| `overwrite_find_skip` | 覆盖搜索请求的 `from`。 | `/*+ overwrite_find_skip=20 */ POST /idx/_search` |
| `overwrite_find_as_count` | 将 `/_search` 转换为 `/_count`。 | `/*+ overwrite_find_as_count */ POST /idx/_search` |

## 限制与注意事项
- `_cat` 查询会自动追加 `format=json` 参数（若手动指定则必须为 json）。
- 仅支持 REST 风格命令语法，不支持 Elasticsearch SQL。

## 兼容性
- JDK 8+
- Elasticsearch REST Client：`elasticsearch-rest-client` 7.17.10
- Jackson：`jackson-databind` 2.18.0
- dbVisitor 含 Elastic6/Elastic7 方言与 ES6/ES7 realdb 场景化测试。

## 更多资源
- dbVisitor 文档中心：https://www.dbvisitor.net/docs/guides/overview
