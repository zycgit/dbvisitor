## 介绍
jdbc-mongo 是 MongoDB 的 JDBC 驱动适配器，允许开发者使用标准 JDBC 接口和原生命令风格命令操作 MongoDB。

核心价值：
- 使用标准 JDBC API（Connection、Statement、PreparedStatement、ResultSet）。
- 使用原生命令风格的命令文本映射到 MongoDB 操作。
- 通过 dbVisitor 为异构数据源提供统一的编码风格。

## 特性
- 实现 JDBC 核心接口并支持 `PreparedStatement` 占位符。
- 支持原生命令风格 Mongo 命令，支持多条命令以分号顺序执行。
- 覆盖集合、索引、用户、数据库管理类操作。
- `find` 支持链式调用 `limit(...)`、`skip(...)`、`sort(...)`、`hint(...)`。
- 结果映射：`find` 返回 `_ID` 与 `_JSON` 列；预读模式下会把文档字段展开为列。
- 预读模式可通过阈值、最大文件大小、缓存目录进行配置。

## 使用

### 4.1 引入依赖
```xml
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>jdbc-mongo</artifactId>
    <version>最新版本</version>
</dependency>
```

### 4.2 建立连接
```java
String url = "jdbc:dbvisitor:mongo://127.0.0.1:27017/testdb?socketTimeout=5000";
Properties props = new Properties();
props.setProperty("user", "root");
props.setProperty("password", "123456");
props.setProperty("mechanism", "SCRAM-SHA-256");
Connection conn = DriverManager.getConnection(url, props);
```

JDBC URL 格式：
```
jdbc:dbvisitor:mongo://{host}:{port}/{database}?{param1=value1&param2=value2}
```

集群示例（多主机）：
```
jdbc:dbvisitor:mongo://host1:27017;host2:27017/mydb
```

### 4.3 连接参数详解

| 参数 | 说明 | 默认值 |
| --- | --- | --- |
| `server` | JDBC URL 的 host 部分。支持 `host:port` 或 `host1:port;host2:port`。 | 来自 URL |
| `database` | 当 URL 路径为空时使用的默认数据库。 | 无 |
| `user` / `username` | 认证用户名。 | 无 |
| `password` | 认证密码。 | 空 |
| `mechanism` | 认证机制：`PLAIN`、`SCRAM-SHA-1`、`SCRAM-SHA-256`、`GSSAPI`、`X-509`。为空时使用 `createCredential`。 | 空 |
| `clientName` | MongoDB 应用名。 | `Mongo-JDBC-Client` |
| `socketTimeout` | Socket 读取超时（毫秒）。 | 驱动默认值 |
| `socketSndBuffer` | Socket 发送缓冲区大小（字节）。 | 驱动默认值 |
| `socketRcvBuffer` | Socket 接收缓冲区大小（字节）。 | 驱动默认值 |
| `retryWrites` | 是否启用重试写入。 | 驱动默认值 |
| `retryReads` | 是否启用重试读取。 | 驱动默认值 |
| `timeZone` | 驱动用于类型转换的时区（例如 `+08:00`）。 | 空 |
| `customMongo` | 实现 `CustomMongo` 的类全名。 | 无 |
| `connectTimeout` | 已声明但适配器未应用的参数。 | 未应用 |
| `preRead` | 是否启用预读模式。 | `true` |
| `preReadThreshold` | 预读阈值，支持 `B/KB/MB/GB`。 | `5MB` |
| `preReadMaxFileSize` | 预读最大文件大小，支持 `B/KB/MB/GB`。 | `20MB` |
| `preReadCacheDir` | 预读缓存目录。 | `java.io.tmpdir` |

## 支持的指令
命令语法参考 MongoDB 官方文档：https://www.mongodb.com/docs/manual/reference/command/

- 数据库级别
  - `use <database>`
  - `show dbs` / `show databases`
  - `db.dropDatabase()`
  - `db.getCollectionNames()`
  - `db.getCollectionInfos()`
  - `db.createCollection(...)`
  - `db.createView(...)`
  - `db.runCommand({...})`
  - `db.serverStatus()`
  - `db.version()`
  - `db.stats()`

- 集合级别
  - `db.collection.find(...)` 并支持 `.limit(...)`、`.skip(...)`、`.sort(...)`、`.hint(...)`
  - `db.collection.findOne(...)`
  - `db.collection.insert(...)`、`insertOne(...)`、`insertMany(...)`
  - `db.collection.update(...)`、`updateOne(...)`、`updateMany(...)`
  - `db.collection.replaceOne(...)`
  - `db.collection.remove(...)`、`deleteOne(...)`、`deleteMany(...)`
  - `db.collection.count(...)`、`distinct(...)`、`aggregate(...)`
  - `db.collection.bulkWrite(...)`
  - `db.collection.renameCollection(...)`
  - `db.collection.drop(...)`
  - `db.collection.stats(...)`

- 索引级别
  - `db.collection.createIndex(...)`
  - `db.collection.dropIndex(...)`
  - `db.collection.getIndexes()`

- 用户与角色
  - `db.createUser(...)`、`db.dropUser(...)`、`db.updateUser(...)`
  - `db.changeUserPassword(...)`
  - `db.grantRolesToUser(...)`、`db.revokeRolesFromUser(...)`
  - `show users`、`show roles`

- 其他
  - `show collections` / `show tables`
  - `show profile`

## 常用操作示例

### 1) 创建集合（DDL）
```java
try (Statement stmt = conn.createStatement()) {
    stmt.execute("use mydb");
    stmt.executeUpdate("db.createCollection('capped_col', { capped: true, size: 1024, max: 100 })");
}
```

### 2) 插入与更新（DML）
```java
try (Statement stmt = conn.createStatement()) {
    stmt.executeUpdate("mydb1.mycol.insert({name:'zhangsan'})");
    stmt.executeUpdate("db.mycol.update({name:'zhangsan'}, {$set:{age:20}})");
}
```

### 3) 使用 `ObjectId(?)` 查询（DQL）
```java
try (PreparedStatement ps = conn.prepareStatement("db.complex_order.find({_id: ObjectId(?)})")) {
    ps.setString(1, "69399ba1c488515851cecdfb");
    try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
            rs.getString("_JSON");
        }
    }
}
```

### 4) 带参数的聚合
```java
try (PreparedStatement ps = conn.prepareStatement(
        "db.mycol.aggregate([{$match: {}}], { allowDiskUse: ?, batchSize: ? })")) {
    ps.setBoolean(1, true);
    ps.setInt(2, 100);
    ps.executeQuery();
}
```

## Hint 支持
Hint 必须位于命令开头，格式为 `/*+ name=value */`，支持多个 Hint 块。

支持的 Hint：

| Hint | 说明 | 示例 |
| --- | --- | --- |
| `overwrite_find_limit` | 覆盖 `find` / `findOne` 的 `limit`。 | `/*+ overwrite_find_limit=10 */ db.mycol.find({})` |
| `overwrite_find_skip` | 覆盖 `find` / `findOne` 的 `skip`。 | `/*+ overwrite_find_skip=20 */ db.mycol.find({})` |
| `overwrite_find_as_count` | 将 `find` 转换为 `countDocuments` 结果。 | `/*+ overwrite_find_as_count */ db.mycol.find({})` |

## 限制与注意事项
- `db.createCollection(...)` 不支持 `viewOn` 选项。
- `createIndex(...)` 必须提供 `name` 选项，否则会报错。
- `runCommand(...)` 的第一个参数必须是 `Document` 对象。
- `find` 链式调用仅支持 `limit`、`skip`、`sort`、`hint`，其他方法（例如 `explain`）会被拒绝。
- 未选择数据库（URL 路径或 `use <db>`）时使用 `db` 会抛出 “No database selected”。
- `connectTimeout` 已声明但未应用到 MongoClient 设置中。

## 兼容性
- JDK 8+
- MongoDB Java Driver：`mongodb-driver-sync` 5.6.1（兼容该驱动支持的 MongoDB 服务器版本）

## 更多资源
- dbVisitor 文档中心：https://www.dbvisitor.net/docs/guides/overview
