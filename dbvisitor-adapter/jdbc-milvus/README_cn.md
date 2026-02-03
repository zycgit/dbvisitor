## 介绍

jdbc-milvus 是面向 Milvus 向量数据库的 JDBC 驱动适配器。

核心价值：
- 允许使用标准 JDBC 接口（`Connection`、`Statement`、`PreparedStatement`、`ResultSet`）。
- 允许使用 SQL 风格命令映射到 Milvus 原生命令。

解决的问题：
- 降低熟悉 SQL/JDBC 的 Java 开发者使用门槛。
- 通过 dbVisitor 为异构数据源提供统一编码风格。
- 更容易集成到 JDBC 生态（Spring、MyBatis）。

## 特性

- JDBC 核心兼容，支持 `PreparedStatement` 与 `?` 占位符。
- 集合 DDL：创建、删除、重命名、查看、查看建表语句。
- 数据 DML：插入、Upsert 与 删除（标量过滤、向量检索、向量范围删除）。
- 查询 DQL：标量过滤、`ORDER BY field <-> vector` 向量检索、`WHERE` 向量范围检索。
- 索引管理：创建、删除、查看索引与构建进度。
- 分区管理：创建、删除、查看分区。
- 数据库管理：创建、修改属性、删除、查看数据库列表。
- 用户与角色管理：创建/删除用户或角色、授权/撤权、查看用户/角色/授权。
- 别名管理与加载进度查询。

## 快速开始

### 4.1 引入依赖

```xml
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>jdbc-milvus</artifactId>
    <version>最新版本</version>
</dependency>
```

### 4.2 建立连接

```java
String url = "jdbc:dbvisitor:milvus://127.0.0.1:19530/default";
Connection conn = DriverManager.getConnection(url);
```

集群示例（多个主机用 `;` 分隔，可包含健康端口）：

```java
String url = "jdbc:dbvisitor:milvus://host1:19530:9091;host2:19530:9091/default";
Connection conn = DriverManager.getConnection(url);
```

### 4.3 连接参数详解

JDBC URL 支持以下参数（来源于 `MilvusKeys` 与 `getPropertyNames`）。

| 参数 | 描述 | 备注 |
| --- | --- | --- |
| adapterName | 适配器名称 | 只读，由 JDBC URL 设置（`milvus`） |
| server | 服务器列表 | `host:port[:healthPort]`，多个主机用 `;` 分隔 |
| timeZone | 时区 | 驱动层属性，Milvus 适配器逻辑未使用 |
| database | 默认数据库 | 也可通过 URL Path `/database` 指定 |
| token | Token 认证 | 设置后优先于 user/password |
| user | 用户名 | 与 `password` 一起使用 |
| password | 密码 | 与 `user` 一起使用 |
| timeout | 超时 | 仅声明，未在 `MilvusConnFactory` 中使用 |
| connectTimeout | 连接超时 | 毫秒 |
| keepAliveTime | Keep-Alive 时间 | 毫秒 |
| keepAliveTimeout | Keep-Alive 超时 | 毫秒 |
| keepAliveWithoutCalls | 无调用保持活跃 | `true` 或 `false` |
| idleTimeout | 空闲超时 | 毫秒 |
| rpcDeadline | RPC 截止时间 | 毫秒 |
| interceptor | 客户端拦截器 | `InvocationHandler` 的全限定类名 |
| customMilvus | 自定义客户端 | `CustomMilvus` 的全限定类名 |

## 支持的指令

完整语法请参考 [SYNTAX_MANUAL_cn.md](SYNTAX_MANUAL_cn.md)。已实现的指令分类如下：

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

## 常用操作示例

### 1) 创建集合

```java
try (Statement stmt = conn.createStatement()) {
    stmt.executeUpdate("CREATE TABLE book_vectors (" +
            "book_id int64 primary key, " +
            "word_count int64, " +
            "book_intro float_vector(2)" +
            ")");
}
```

### 2) 创建索引并加载

```java
try (Statement stmt = conn.createStatement()) {
    stmt.executeUpdate("CREATE INDEX idx_book_intro ON book_vectors (book_intro) USING \"IVF_FLAT\" WITH (nlist = 1024, metric_type = \"L2\")");
    stmt.executeUpdate("LOAD TABLE book_vectors");
}
```

### 3) 使用 `PreparedStatement` 插入

```java
String insertSql = "INSERT INTO book_vectors (book_id, word_count, book_intro) VALUES (?, ?, ?)";
try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
    ps.setLong(1, 1L);
    ps.setLong(2, 1000L);
    ps.setObject(3, java.util.Arrays.asList(0.1f, 0.2f));
    ps.executeUpdate();
}
```

### 4) 向量搜索

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

### 5) 使用 Hints

```java
try (Statement stmt = conn.createStatement()) {
    ResultSet rs = stmt.executeQuery("/*+ overwrite_find_limit=1 */ SELECT * FROM book_vectors");
    while (rs.next()) {
        // handle rows
    }
}
```

## Hint 支持

jdbc-milvus 支持通过 SQL Hint 覆盖或增强查询行为。Hint 格式为 `/*+ hint_name=value */`，必须位于 SQL 语句开头。

| Hint 名称 | 说明 | 允许值 | 示例 |
| --- | --- | --- | --- |
| `consistency_level` | 设置查询的一致性级别。 | `Strong`, `Bounded`, `Session`, `Eventually` | `/*+ consistency_level=Strong */ SELECT ...` |
| `overwrite_find_limit` | 覆盖查询的 `LIMIT`/TopK 参数。 | 数字 | `/*+ overwrite_find_limit=100 */ SELECT ...` |
| `overwrite_find_skip` | 覆盖查询的 `OFFSET` 参数。 | 数字 | `/*+ overwrite_find_skip=10 */ SELECT ...` |
| `overwrite_find_as_count` | 将查询转换为 Count 操作，仅返回匹配数量。 | 任意值 | `/*+ overwrite_find_as_count=true */ SELECT ...` |

## 限制与注意事项

- 不支持切换到不同的 catalog/schema（`setCatalog`/`setSchema` 会拒绝变更）。
- `DELETE` 必须带 `WHERE` 子句，否则会报错。
- 仅支持 [SYNTAX_MANUAL_cn.md](SYNTAX_MANUAL_cn.md) 中定义的 SQL 语法。

## 兼容性

- JDK：8
- Milvus Java SDK：2.5.4（见 [dbvisitor-adapter/jdbc-milvus/pom.xml](dbvisitor-adapter/jdbc-milvus/pom.xml) 依赖）

## 更多资源

- 语法手册：[SYNTAX_MANUAL_cn.md](SYNTAX_MANUAL_cn.md)
- dbVisitor 文档：[dbvisitor-doc/README.md](../../dbvisitor-doc/README.md)
- 示例工程：[dbvisitor-example](../../dbvisitor-example)
