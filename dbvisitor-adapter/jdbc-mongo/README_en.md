## Introduction

jdbc-mongo is a MongoDB JDBC driver adapter that allows developers to operate MongoDB using the standard JDBC interfaces and MongoDB commands.
The goal is to enable seamless use of MongoDB by leveraging the familiar JDBC programming model.

## Features

- Supports common MongoDB commands, including CRUD, aggregation, index management, etc.
- Implements standard JDBC interfaces, including Connection, Statement, PreparedStatement, ResultSet, etc.
- Supports command parameter placeholders "?" and setting parameters via PreparedStatement.
- Supports multi-command execution and obtaining multi-command results via standard JDBC methods.
- Supports Statement properties: maxRows, fetchSize, timeoutSec.
- Supports command interceptors for logging, performance monitoring, etc.
- Supports type conversion; e.g., when a result set returns LONG, you may retrieve it via ResultSet.getInt or ResultSet.getString.
- Supports pre-read configuration to optimize reading performance for large files or large amounts of data.

## Technical

### Architecture

The jdbc-mongo project uses the Adapter pattern to map standard JDBC interfaces to the MongoDB command model. Main components include:
- MongoConn: implements the JDBC Connection interface; core of the adapter, responsible for connection management and command execution.
- MongoCmd: wraps MongoDB client command interfaces.
- MongoRequest: represents a MongoDB command request.
- ANTLR4 parser: used to parse SQL-style MongoDB commands and generate execution plans.

### Command Execution

- Users create Connection and Statement via the JDBC API and execute MongoDB commands.
- MongoConn receives the command and parses it using the ANTLR4 parser.
- Parsed commands are forwarded by MongoCmd to the underlying MongoDB official driver for execution.
- Results are returned to users via standard ResultSet or update counts.

### Dependencies

- MongoDB Driver: The official Java client for MongoDB.
- ANTLR4: powerful parser generator used to parse MongoDB commands.
- dbVisitor-driver: the base database driver framework.

## Usage

### Dependency

```xml title='Maven Dependency'
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>jdbc-mongo</artifactId>
    <version>6.3.0</version>
</dependency>
```

### Connection

Connect to MongoDB using the JDBC URL format:

```java
String url = "jdbc:dbvisitor:mongo://127.0.0.1:27017/testdb?connectTimeout=5000";
Properties props = new Properties();
props.setProperty("username", "user");
props.setProperty("password", "pass");
Connection conn = DriverManager.getConnection(url, props);
```

Main connection parameters:
- host/port: MongoDB server address.
- database: Database name.
- username/password: Authentication credentials.
- mechanism: Authentication mechanism, supports PLAIN, SCRAM-SHA-1, SCRAM-SHA-256, GSSAPI, X-509.
- connectTimeout: Connection timeout in milliseconds.
- socketTimeout: Socket read timeout in milliseconds.
- retryWrites/retryReads: Read/Write retry strategy.

### Examples

```java
public class MongoJdbcExample {
  public static void main(String[] args) throws Exception {
    String url = "jdbc:dbvisitor:mongo://127.0.0.1:27017/testdb";
    try (Connection conn = DriverManager.getConnection(url)) {
      // Statement Example
      try (Statement stmt = conn.createStatement()) {
        try (ResultSet rs = stmt.executeQuery("find my_collection limit 10")) {
          while (rs.next()) {
            System.out.println(rs.getString("name"));
          }
        }
      }

      // PreparedStatement Example
      try (PreparedStatement pstmt = conn.prepareStatement("test.user_info.insert({name: ?, age: ?})")) {
        pstmt.setString(1, "acc");
        pstmt.setString(2, "123");
        pstmt.executeUpdate();
      }
    }
  }
}
```

## Hint Support

jdbc-mongo supports overriding or enhancing query behavior via SQL Hints. The Hint format is `/*+ hint_name=value */` and must be placed at the beginning of the SQL statement.

| Hint Name | Description | Example |
| --- | --- | --- |
| `overwrite_find_limit` | Overrides the `limit` parameter of the query, used for pagination or limiting the number of returned documents. | `/*+ overwrite_find_limit=10 */ db.collection.find({})` |
| `overwrite_find_skip` | Overrides the `skip` parameter of the query, used for pagination to skip a specified number of documents. | `/*+ overwrite_find_skip=20 */ db.collection.find({})` |
| `overwrite_find_as_count` | Converts the query into a Count operation, ignoring the returned document content and returning only the match count. | `/*+ overwrite_find_as_count */ db.collection.find({})` |

## Limit

- Properties obtained via DatabaseMetaData are not reliable.
- When using resultSetType, resultSetConcurrency, resultSetHoldability, fetchDirection parameters only the following default values are supported:
  - resultSetType = TYPE_FORWARD_ONLY
  - resultSetConcurrency = CONCUR_READ_ONLY
  - resultSetHoldability = HOLD_CURSORS_OVER_COMMIT
  - fetchDirection = FETCH_FORWARD
- ResultSet does not support update/insert/deleteXXX methods.
- Statement and PreparedStatement do not support overloaded methods with these signatures:
  - xxx(String sql, int[] columnIndexes)
  - xxx(String sql, String[] columnNames)
- Unsupported JDBC types:
  - SQLXML, REF_CURSOR, RowId, Ref, Struct, DISTINCT
- addBatch and clearBatch batch operations are not supported.
- Savepoint operations are not supported.

## Driver Parameters

``` title='JDBC URL Format'
jdbc:dbvisitor:mongo://server:17017/database?param1=value1&param2=value2
```

| Parameter Name         | Description                                                        | Default Value       |
|----------------------|--------------------------------------------------------------------|---------------------|
| `username`           | Database username                                                  | None                |
| `password`           | Database password                                                  | None                |
| `database`           | Database name                                                      | None                |
| `mechanism`          | Authentication mechanism, supports `PLAIN`, `SCRAM-SHA-1`, `SCRAM-SHA-256`, `GSSAPI`, `X-509` | Auto-negotiated     |
| `clientName`         | Client name, displayed in MongoDB server logs                      | `Mongo-JDBC-Client` |
| `connectTimeout`     | Connection timeout (ms)                                            | Driver default      |
| `socketTimeout`      | Socket read timeout (ms)                                           | Driver default      |
| `socketSndBuffer`    | Socket send buffer size (bytes)                                    | Driver default      |
| `socketRcvBuffer`    | Socket receive buffer size (bytes)                                 | Driver default      |
| `retryWrites`        | Whether to enable retry writes                                     | `true`              |
| `retryReads`         | Whether to enable retry reads                                      | `true`              |
| `preRead`            | Whether to enable pre-read                                         | `false`             |
| `preReadThreshold`   | Pre-read threshold (MB), triggers pre-read if exceeded             | -                   |
| `preReadMaxFileSize` | Pre-read max file size (MB)                                        | -                   |
| `preReadCacheDir`    | Pre-read cache directory                                           | -                   |

## Supported Commands List

- Value: Use executeUpdate / getUpdateCount to get the number of affected rows.
- Result Set: Use executeQuery / getResultSet to get the result set.

| Command                 | Description                                        | Official Documentation                                                                            |
|-----------------------|----------------------------------------------------|---------------------------------------------------------------------------------------------------|
| **Collection Commands** |                                                    |                                                                                                   |
| `find`                | Query documents, supports `limit`, `skip`, `sort`, `explain`, `hint` | [find](https://www.mongodb.com/docs/manual/reference/command/find/)                               |
| `findOne`             | Query a single document                            | [findOne](https://www.mongodb.com/docs/manual/reference/method/db.collection.findOne/)            |
| `insert`              | Insert documents                                   | [insert](https://www.mongodb.com/docs/manual/reference/command/insert/)                           |
| `insertOne`           | Insert a single document                           | [insertOne](https://www.mongodb.com/docs/manual/reference/method/db.collection.insertOne/)        |
| `insertMany`          | Insert multiple documents                          | [insertMany](https://www.mongodb.com/docs/manual/reference/method/db.collection.insertMany/)      |
| `update`              | Update documents                                   | [update](https://www.mongodb.com/docs/manual/reference/command/update/)                           |
| `updateOne`           | Update a single document                           | [updateOne](https://www.mongodb.com/docs/manual/reference/method/db.collection.updateOne/)        |
| `updateMany`          | Update multiple documents                          | [updateMany](https://www.mongodb.com/docs/manual/reference/method/db.collection.updateMany/)      |
| `replaceOne`          | Replace a single document                          | [replaceOne](https://www.mongodb.com/docs/manual/reference/method/db.collection.replaceOne/)      |
| `remove`              | Remove documents                                   | [delete](https://www.mongodb.com/docs/manual/reference/command/delete/)                           |
| `deleteOne`           | Delete a single document                           | [deleteOne](https://www.mongodb.com/docs/manual/reference/method/db.collection.deleteOne/)        |
| `deleteMany`          | Delete multiple documents                          | [deleteMany](https://www.mongodb.com/docs/manual/reference/method/db.collection.deleteMany/)      |
| `count`               | Count documents                                    | [count](https://www.mongodb.com/docs/manual/reference/command/count/)                             |
| `distinct`            | Get distinct values                                | [distinct](https://www.mongodb.com/docs/manual/reference/command/distinct/)                       |
| `aggregate`           | Aggregation operations                             | [aggregate](https://www.mongodb.com/docs/manual/reference/command/aggregate/)                     |
| `bulkWrite`           | Bulk write operations                              | [bulkWrite](https://www.mongodb.com/docs/manual/reference/method/db.collection.bulkWrite/)        |
| `renameCollection`    | Rename collection                                  | [renameCollection](https://www.mongodb.com/docs/manual/reference/command/renameCollection/)       |
| `drop`                | Drop collection                                    | [drop](https://www.mongodb.com/docs/manual/reference/command/drop/)                               |
| **Database Management** |                                                    |                                                                                                   |
| `createCollection`    | Create collection                                  | [create](https://www.mongodb.com/docs/manual/reference/command/create/)                           |
| `createView`          | Create view                                        | [createView](https://www.mongodb.com/docs/manual/reference/command/create/)                       |
| `dropDatabase`        | Drop current database                              | [dropDatabase](https://www.mongodb.com/docs/manual/reference/command/dropDatabase/)               |
| `getCollectionNames`  | Get list of collection names                       | [listCollections](https://www.mongodb.com/docs/manual/reference/command/listCollections/)         |
| `getCollectionInfos`  | Get collection information                         | [listCollections](https://www.mongodb.com/docs/manual/reference/command/listCollections/)         |
| `runCommand`          | Run arbitrary database command                     | [runCommand](https://www.mongodb.com/docs/manual/reference/command/runCommand/)                   |
| `serverStatus`        | Get server status                                  | [serverStatus](https://www.mongodb.com/docs/manual/reference/command/serverStatus/)               |
| `stats`               | Get database statistics                            | [dbStats](https://www.mongodb.com/docs/manual/reference/command/dbStats/)                         |
| `version`             | Get server version                                 | [buildInfo](https://www.mongodb.com/docs/manual/reference/command/buildInfo/)                     |
| **Index Management**    |                                                    |                                                                                                   |
| `createIndex`         | Create index                                       | [createIndexes](https://www.mongodb.com/docs/manual/reference/command/createIndexes/)             |
| `dropIndex`           | Drop index                                         | [dropIndexes](https://www.mongodb.com/docs/manual/reference/command/dropIndexes/)                 |
| `getIndexes`          | Get index list                                     | [listIndexes](https://www.mongodb.com/docs/manual/reference/command/listIndexes/)                 |
| **User Management**     |                                                    |                                                                                                   |
| `createUser`          | Create user                                        | [createUser](https://www.mongodb.com/docs/manual/reference/command/createUser/)                   |
| `dropUser`            | Drop user                                          | [dropUser](https://www.mongodb.com/docs/manual/reference/command/dropUser/)                       |
| `updateUser`          | Update user                                        | [updateUser](https://www.mongodb.com/docs/manual/reference/command/updateUser/)                   |
| `changeUserPassword`  | Change user password                               | [updateUser](https://www.mongodb.com/docs/manual/reference/command/updateUser/)                   |
| `grantRolesToUser`    | Grant roles to user                                | [grantRolesToUser](https://www.mongodb.com/docs/manual/reference/command/grantRolesToUser/)       |
| `revokeRolesFromUser` | Revoke roles from user                             | [revokeRolesFromUser](https://www.mongodb.com/docs/manual/reference/command/revokeRolesFromUser/) |
| **Other Commands**      |                                                    |                                                                                                   |
| `use <database>`      | Switch current database.                           |                                                                                                   | 
| `show dbs`            | Show all databases.                                |                                                                                                   |    
| `show collections`    | Show all collections in current database.          |                                                                                                   |    
| `show tables`         | Same as `show collections`.                        |                                                                                                   |    
