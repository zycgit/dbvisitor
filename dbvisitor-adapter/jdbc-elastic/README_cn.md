# jdbc-elastic

## 介绍

jdbc-elastic 是一个 ElasticSearch 的 JDBC 驱动适配器，它允许开发者使用标准的 JDBC 接口和 ElasticSearch 原生 REST API 风格的命令来操作 ElasticSearch。
目的是通过熟悉 JDBC 编程模型，使开发者能够无缝地集成和使用 ElasticSearch。

## 特性

- **原生命令支持**：支持 ElasticSearch 的常用 REST API 命令，包括 `GET`, `POST`, `PUT`, `DELETE`, `HEAD`。
- **JDBC 标准接口**：支持 `Connection`, `Statement`, `PreparedStatement`, `ResultSet` 等标准接口。
- **参数占位符**：支持在 URL 路径和 JSON Body 中使用 `?` 占位符，并使用 `PreparedStatement` 设置参数。
- **结果集映射**：自动将 ElasticSearch 的 JSON 响应映射为 JDBC `ResultSet`。
- **多命令支持**：支持 `_mget`, `_msearch` 等批量操作。
- **索引管理**：支持索引的创建、删除、Mapping 设置、Settings 设置、别名管理等。
- **预读优化**：支持大结果集的预读配置，优化读取性能。
- **多版本兼容**：无需调整依赖，同时兼容 ES6/ES7/ES8。

## 快速开始

### 引入依赖

```xml
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>jdbc-elastic</artifactId>
    <version>6.3.2</version> <!-- 请使用最新版本 -->
</dependency>
```

### 连接配置

使用标准的 JDBC URL 格式连接 ElasticSearch：

```java
String url = "jdbc:dbvisitor:elastic://127.0.0.1:9200";
Properties props = new Properties();
props.setProperty("username", "elastic");
props.setProperty("password", "changeme");
Connection conn = DriverManager.getConnection(url, props);
```

**支持的连接参数：**

| 参数名 | 说明 | 默认值 |
| --- | --- | --- |
| `username` | 认证用户名 | 无 |
| `password` | 认证密码 | 无 |
| `connectTimeout` | 连接超时时间（毫秒） | - |
| `socketTimeout` | Socket 读取超时时间（毫秒） | - |
| `indexRefresh` | 是否在写入操作后自动刷新索引 | `false` |
| `preRead` | 是否开启预读 | `true` |
| `preReadThreshold` | 预读阈值（字节），超过该大小触发文件缓存 | `5MB` |
| `preReadMaxFileSize` | 预读最大文件大小 | `20MB` |
| `preReadCacheDir` | 预读缓存目录 | 系统临时目录 |

### 示例代码

#### 1. 执行查询 (Search)

```java
try (Connection conn = DriverManager.getConnection(url, props)) {
    try (Statement stmt = conn.createStatement()) {
        // 执行 DSL 查询
        String dsl = "POST /my_index/_search { \"query\": { \"match_all\": {} } }";
        try (ResultSet rs = stmt.executeQuery(dsl)) {
            while (rs.next()) {
                System.out.println("ID: " + rs.getString("_ID"));
                System.out.println("Source: " + rs.getString("_DOC")); // _DOC 返回原始 JSON
            }
        }
    }
}
```

#### 2. 使用 PreparedStatement (带参数)

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

#### 3. 插入/更新文档

```java
String insertSql = "POST /my_index/_doc/1 { \"user\": \"kimchy\", \"post_date\": \"2009-11-15T14:12:12\", \"message\": \"trying out Elasticsearch\" }";
try (Statement stmt = conn.createStatement()) {
    stmt.executeUpdate(insertSql);
}
```

#### 4. 索引管理

```java
try (Statement stmt = conn.createStatement()) {
    // 创建索引
    stmt.execute("PUT /new_index");
    
    // 设置 Mapping
    stmt.execute("PUT /new_index/_mapping { \"properties\": { \"field1\": { \"type\": \"text\" } } }");
    
    // 删除索引
    stmt.execute("DELETE /new_index");
}
```

#### 5. 查看集群信息 (_cat)

```java
try (Statement stmt = conn.createStatement()) {
    try (ResultSet rs = stmt.executeQuery("GET /_cat/nodes?h=ip,port,heapPercent,name")) {
        while (rs.next()) {
            System.out.println(rs.getString("name") + " - " + rs.getString("ip"));
        }
    }
}
```

## Hint 支持

jdbc-elastic 支持通过 SQL Hint 方式来覆盖或增强查询行为。Hint 格式为 `/*+ hint_name=value */`，必须位于 SQL 语句的开头。

| Hint 名称 | 说明 | 示例 |
| --- | --- | --- |
| `overwrite_find_limit` | 覆盖查询的 `size` 参数，用于分页或限制返回条数。 | `/*+ overwrite_find_limit=10 */ POST /idx/_search` |
| `overwrite_find_skip` | 覆盖查询的 `from` 参数，用于分页跳过指定条数。 | `/*+ overwrite_find_skip=20 */ POST /idx/_search` |
| `overwrite_find_as_count` | 将查询转换为 Count 操作，忽略返回的文档内容，仅返回匹配数量。 | `/*+ overwrite_find_as_count */ POST /idx/_search` |

## 支持的命令概览

jdbc-elastic 通过解析 SQL 风格的命令，将其转换为底层的 REST 请求。支持的命令模式如下：

- **查询**: `GET/POST .../_search`, `_count`, `_msearch`, `_mget`, `_explain`, `_source`
- **文档操作**: `PUT/POST .../_doc/...`, `_create`, `_update`, `DELETE ...`
- **索引操作**: `PUT/DELETE /index`, `_open`, `_close`, `_mapping`, `_settings`, `_aliases`, `_reindex`, `_refresh`
- **集群信息**: `GET /_cat/...`
- **通用请求**: 支持任意 `GET`, `POST`, `PUT`, `DELETE`, `HEAD` 请求。

## 限制

- 事务支持：ElasticSearch 本身不支持 ACID 事务，因此 `commit` 和 `rollback` 操作无效（默认为自动提交）。
- SQL 支持：目前主要支持 REST API 风格的命令，暂不支持标准 SQL 语法（如 `SELECT * FROM ...`）。
