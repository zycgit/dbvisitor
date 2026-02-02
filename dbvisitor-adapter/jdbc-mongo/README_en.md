## Introduction
jdbc-mongo is a JDBC driver adapter for MongoDB. It allows developers to operate MongoDB using standard JDBC interfaces and native-command style Mongo commands.

Core value:
- Use standard JDBC APIs (Connection, Statement, PreparedStatement, ResultSet).
- Use native-command style command text that maps to MongoDB operations.
- Provide a unified programming style for heterogeneous data sources via dbVisitor.

## Features
- Implements the JDBC core interfaces and supports `PreparedStatement` placeholders.
- Supports native-command style Mongo commands and multiple commands separated by semicolons.
- Supports collection, index, user, and database management commands.
- `find` supports method chaining: `limit(...)`, `skip(...)`, `sort(...)`, `hint(...)`.
- Result mapping: `find` returns `_ID` and `_JSON` columns; when pre-read is enabled, document fields are also expanded as columns.
- Pre-read mode for large result sets with configurable threshold, max file size, and cache directory.

## Usage

### 4.1 Dependency
```xml
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>jdbc-mongo</artifactId>
    <version>Latest Version</version>
</dependency>
```

### 4.2 Connection
```java
String url = "jdbc:dbvisitor:mongo://127.0.0.1:27017/testdb?socketTimeout=5000";
Properties props = new Properties();
props.setProperty("user", "root");
props.setProperty("password", "123456");
props.setProperty("mechanism", "SCRAM-SHA-256");
Connection conn = DriverManager.getConnection(url, props);
```

JDBC URL format:
```
jdbc:dbvisitor:mongo://{host}:{port}/{database}?{param1=value1&param2=value2}
```

Cluster example (multiple hosts):
```
jdbc:dbvisitor:mongo://host1:27017;host2:27017/mydb
```

### 4.3 Connection Parameters

| Parameter | Description | Default |
| --- | --- | --- |
| `server` | Host segment from the JDBC URL. Supports `host:port` or `host1:port;host2:port`. | From URL |
| `database` | Default database when URL path is empty. | None |
| `user` / `username` | Username for authentication. | None |
| `password` | Password for authentication. | Empty |
| `mechanism` | Authentication mechanism: `PLAIN`, `SCRAM-SHA-1`, `SCRAM-SHA-256`, `GSSAPI`, `X-509`. Empty means `createCredential`. | Empty |
| `clientName` | MongoDB application name. | `Mongo-JDBC-Client` |
| `socketTimeout` | Socket read timeout (ms). | Driver default |
| `socketSndBuffer` | Socket send buffer size (bytes). | Driver default |
| `socketRcvBuffer` | Socket receive buffer size (bytes). | Driver default |
| `retryWrites` | Enable retry writes. | Driver default |
| `retryReads` | Enable retry reads. | Driver default |
| `timeZone` | Driver time zone used for type conversion (for example `+08:00`). | Empty |
| `customMongo` | Fully qualified class name implementing `CustomMongo`. | None |
| `connectTimeout` | Declared parameter but not applied by this adapter. | Not applied |
| `preRead` | Enable pre-read mode. | `true` |
| `preReadThreshold` | Pre-read threshold size. Accepts `B/KB/MB/GB`. | `5MB` |
| `preReadMaxFileSize` | Maximum pre-read file size. Accepts `B/KB/MB/GB`. | `20MB` |
| `preReadCacheDir` | Cache directory for pre-read mode. | `java.io.tmpdir` |

## Supported Commands
The command syntax is described in the MongoDB manual: https://www.mongodb.com/docs/manual/reference/command/

- Database scope
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

- Collection scope
  - `db.collection.find(...)` with optional chained calls `.limit(...)`, `.skip(...)`, `.sort(...)`, `.hint(...)`
  - `db.collection.findOne(...)`
  - `db.collection.insert(...)`, `insertOne(...)`, `insertMany(...)`
  - `db.collection.update(...)`, `updateOne(...)`, `updateMany(...)`
  - `db.collection.replaceOne(...)`
  - `db.collection.remove(...)`, `deleteOne(...)`, `deleteMany(...)`
  - `db.collection.count(...)`, `distinct(...)`, `aggregate(...)`
  - `db.collection.bulkWrite(...)`
  - `db.collection.renameCollection(...)`
  - `db.collection.drop(...)`
  - `db.collection.stats(...)`

- Index scope
  - `db.collection.createIndex(...)`
  - `db.collection.dropIndex(...)`
  - `db.collection.getIndexes()`

- User & role management
  - `db.createUser(...)`, `db.dropUser(...)`, `db.updateUser(...)`
  - `db.changeUserPassword(...)`
  - `db.grantRolesToUser(...)`, `db.revokeRolesFromUser(...)`
  - `show users`, `show roles`

- Other
  - `show collections` / `show tables`
  - `show profile`

## Code Examples

### 1) Create collection (DDL)
```java
try (Statement stmt = conn.createStatement()) {
    stmt.execute("use mydb");
    stmt.executeUpdate("db.createCollection('capped_col', { capped: true, size: 1024, max: 100 })");
}
```

### 2) Insert and update (DML)
```java
try (Statement stmt = conn.createStatement()) {
    stmt.executeUpdate("mydb1.mycol.insert({name:'zhangsan'})");
    stmt.executeUpdate("db.mycol.update({name:'zhangsan'}, {$set:{age:20}})");
}
```

### 3) Find with `ObjectId(?)` (DQL)
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

### 4) Aggregate with options
```java
try (PreparedStatement ps = conn.prepareStatement(
        "db.mycol.aggregate([{$match: {}}], { allowDiskUse: ?, batchSize: ? })")) {
    ps.setBoolean(1, true);
    ps.setInt(2, 100);
    ps.executeQuery();
}
```

## Hint Support
Hints must appear at the beginning of the command text. Format: `/*+ name=value */`. Multiple hint blocks are allowed.

Supported hints:

| Hint | Description | Example |
| --- | --- | --- |
| `overwrite_find_limit` | Overrides the `limit` applied to `find` / `findOne`. | `/*+ overwrite_find_limit=10 */ db.mycol.find({})` |
| `overwrite_find_skip` | Overrides the `skip` applied to `find` / `findOne`. | `/*+ overwrite_find_skip=20 */ db.mycol.find({})` |
| `overwrite_find_as_count` | Converts `find` into a `countDocuments` result. | `/*+ overwrite_find_as_count */ db.mycol.find({})` |

## Limitations
- `db.createCollection(...)` does not support the `viewOn` option.
- `createIndex(...)` requires the `name` option; missing it causes an error.
- `runCommand(...)` requires the first argument to be a `Document` object.
- `find` method chaining only supports `limit`, `skip`, `sort`, `hint`. Other method calls (for example `explain`) are rejected.
- Using `db` without selecting a database (URL path or `use <db>`) causes “No database selected”.
- `connectTimeout` is declared but not applied in MongoClient settings.

## Compatibility
- JDK 8+
- MongoDB Java driver: `mongodb-driver-sync` 5.6.1 (compatible with the server versions supported by this driver)

## More Resources
- dbVisitor Documentation: https://www.dbvisitor.net/docs/guides/overview
| `version`             | Get server version                                 | [buildInfo](https://www.mongodb.com/docs/manual/reference/command/buildInfo/)                     |
