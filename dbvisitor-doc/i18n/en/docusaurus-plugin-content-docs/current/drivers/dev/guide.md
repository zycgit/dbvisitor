---
id: guide
sidebar_label: 2. Custom Drivers
toc_max_heading_level: 3
sidebar_position: 1
title: 2. Custom Drivers
description: Implement a dbVisitor adapter using NewDB as an example.
---

This guide uses a fictional data source, **NewDB**, to explain adapter components and implementation steps. `NewDBClient` and `NewDBResult` are illustrative types showing where SDK calls belong; replace them with the target database API. Imports are omitted, and the snippets are not a complete runnable project.

Before starting, read [Architecture](./about) for the core components and execution model.

## Step 1: Create a Gradle Module

Create a `jdbc-newdb` module under `dbvisitor-adapter/` in the dbVisitor repository and register the project and directory in the root `settings.gradle`. Depend on the shared JDBC driver layer in the module's `build.gradle`:

```groovy
dependencies {
    api project(':dbvisitor-driver')
    // Add the target database's official SDK dependency here.
}
```

An independent project can also depend on `net.hasor:dbvisitor-driver` through Maven or Gradle, using the same version as dbVisitor. There is no `dbvisitor-adapter` Maven parent module to depend on.

Suggested package: `net.hasor.dbvisitor.adapter.newdb`

## Step 2: Define Configuration Key Constants

Centralize the names of all JDBC URL parameters and accepted Properties entries:

```java
public final class NewDBKeys {
    public static final String ADAPTER_NAME       = JdbcDriver.P_ADAPTER_NAME;
    public static final String ADAPTER_NAME_VALUE = "newdb";

    // Connection parameters
    public static final String SERVER   = JdbcDriver.P_SERVER;
    public static final String USERNAME = JdbcDriver.P_USER;
    public static final String PASSWORD = JdbcDriver.P_PASSWORD;
    public static final String DATABASE = "database";

    // Timeout/pool configuration...
    public static final String CONN_TIMEOUT = "connectTimeout";
}
```

## Step 3: Implement AdapterFactory

The factory parses the URL, creates the underlying SDK client and constructs the connection:

```java
public class NewDBConnFactory implements AdapterFactory {

    @Override
    public String getAdapterName() {
        return NewDBKeys.ADAPTER_NAME_VALUE; // "newdb"
    }

    @Override
    public String[] getPropertyNames() {
        return new String[] {
            NewDBKeys.ADAPTER_NAME, NewDBKeys.SERVER,
            NewDBKeys.USERNAME, NewDBKeys.PASSWORD, NewDBKeys.DATABASE,
            NewDBKeys.CONN_TIMEOUT
        };
    }

    @Override
    public TypeSupport createTypeSupport(Properties properties) {
        return new AdapterTypeSupport(properties);
    }

    @Override
    public NewDBConn createConnection(
            Connection owner, String jdbcUrl, Properties props
    ) throws SQLException {
        // JdbcDriver has identified the adapter and merged URL parameters into props.
        // Preserve property name case, such as connectTimeout.
        Map<String, String> config = new HashMap<>();
        props.forEach((k, v) -> config.put(
            k.toString(), v.toString()
        ));

        // 3. Create the underlying SDK client
        String server = config.getOrDefault(NewDBKeys.SERVER, "localhost:9000");
        NewDBClient client = NewDBClient.connect(server);

        // 4. Construct the connection
        try {
            NewDBConn conn = new NewDBConn(owner, client, jdbcUrl, config);
            conn.initConnection();
            return conn;
        } catch (Exception e) {
            try {
                client.close();
            } catch (Exception closeError) {
                e.addSuppressed(closeError);
            }
            throw new SQLException("Cannot initialize NewDB connection", e);
        }
    }
}
```

:::tip
If `createConnection` fails, close any underlying resources already created before propagating the exception.
:::

## Step 4: Implement AdapterConnection

The connection is the adapter's core, managing lifecycle and request execution:

```java
public class NewDBConn extends AdapterConnection {
    private final Connection  owner;
    private final NewDBClient client;
    private       String      database;

    NewDBConn(Connection owner, NewDBClient client,
              String jdbcUrl, Map<String, String> config) {
        super(jdbcUrl, config.get(NewDBKeys.USERNAME));
        this.owner = owner;
        this.client = client;
        this.database = config.getOrDefault(NewDBKeys.DATABASE, "default");
    }

    /** Initialize database version information */
    public void initConnection() {
        AdapterInfo info = this.getInfo();
        info.getDriverVersion().setName("jdbc-newdb");
        // Get the server version through the SDK
        String version = client.getServerVersion();
        info.getDbVersion().setName("NewDB");
        info.getDbVersion().setVersion(version);
        // Populate majorVersion and minorVersion according to the database version format.
    }

    // --- catalog / schema ---

    @Override
    public void setCatalog(String catalog) { /* NewDB has no catalog concept */ }

    @Override
    public String getCatalog() { return null; }

    @Override
    public void setSchema(String schema) { this.database = schema; }

    @Override
    public String getSchema() { return this.database; }

    // --- unwrap: expose the underlying client ---

    @Override
    protected <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(this.client)) {
            return iface.cast(this.client);
        }
        return null;
    }

    // --- Request management ---

    @Override
    public AdapterRequest newRequest(String sql) {
        return new NewDBRequest(sql);
    }

    @Override
    public synchronized void doRequest(
            AdapterRequest request, AdapterReceive receive
    ) throws SQLException {
        String command = ((NewDBRequest) request).getCommandBody();

        try {
            // 1. Parse the command (optionally using ANTLR4)
            // 2. Execute through the underlying SDK
            NewDBResult result = this.client.execute(command);

            // 3. Return results through receive
            if (result.isQuery()) {
                AdapterCursor cursor = buildCursor(request, result);
                receive.responseResult(request, cursor);
            } else {
                receive.responseUpdateCount(request, result.getAffectedRows());
            }
        } catch (Exception e) {
            receive.responseFailed(request, e);
        } finally {
            receive.responseFinish(request);
        }
    }

    @Override
    public void cancelRequest() {
        throw new UnsupportedOperationException("NewDB cancellation is not implemented");
    }

    // --- Close ---

    @Override
    protected void doClose() throws IOException {
        this.client.close();
    }
}
```

### Key Implementation Details

The example shows synchronous execution only. For parameter binding, read parameters from `AdapterRequest` and prefer SDK parameter APIs; do not concatenate untreated values into commands. For cancellation, associate each request with its SDK call and prefer overriding `cancelRequest(AdapterRequest)` to avoid cancelling another statement on the same connection. Setting a flag that execution never checks does not implement cancellation.

**`doRequest`** contains the core execution logic. A typical flow is:

1. **Parse** — convert SQL or command text into an executable command structure
2. **Execute** — call the underlying SDK
3. **Respond** — send results to the JDBC layer through `AdapterReceive`

Choose the callback according to the operation:

| Operation | Callback |
| --------- | -------------- |
| Query (SELECT) | `receive.responseResult(request, cursor)` |
| Update (INSERT/UPDATE/DELETE) | `receive.responseUpdateCount(request, count)` |
| Update with generated keys | `receive.responseUpdateCount(request, count, generatedKeys)` |
| Execution failure | `receive.responseFailed(request, exception)` |
| Execution complete | `receive.responseFinish(request)` **(required)** |

## Step 5: Implement AdapterRequest

Wrap the command body and parameters:

```java
public class NewDBRequest extends AdapterRequest {
    private final String commandBody;

    public NewDBRequest(String commandBody) {
        this.commandBody = commandBody;
    }

    public String getCommandBody() {
        return this.commandBody;
    }
}
```

## Step 6: Build a Result Set (AdapterCursor)

`AdapterCursor` is the result-set abstraction in dbvisitor-driver. Convert SDK results into rows and columns:

```java
private AdapterCursor buildCursor(AdapterRequest request, NewDBResult result) throws SQLException {
    // 1. Define columns
    List<String> columns = result.getColumnNames();
    List<String> types = result.getColumnTypes(); // Type names recognized by TypeSupport, such as VARCHAR

    // 2. Create the cursor
    List<JdbcColumn> metadata = new ArrayList<>();
    for (int i = 0; i < columns.size(); i++) {
        metadata.add(new JdbcColumn(columns.get(i), types.get(i), "", "", "",
                ResultSetMetaData.columnNullableUnknown, false, ""));
    }
    AdapterResultCursor cursor = new AdapterResultCursor(request, metadata);

    // 3. Populate rows
    for (Object[] row : result.getRows()) {
        Map<String, Object> values = new LinkedHashMap<>();
        for (int i = 0; i < columns.size(); i++) {
            values.put(columns.get(i), row[i]);
        }
        cursor.pushData(values);
    }
    cursor.pushFinish();
    return cursor;
}
```

`AdapterCursor` is an interface; `AdapterResultCursor` buffers rows. The example suits small results. For large results, implement on-demand paging in an `AdapterCursor` and release the SDK cursor in `close()`. Supply column metadata even for empty results. `pushFinish()` ends data production, whereas `responseFinish()` ends the request.

## Step 7: Add a DSL Parser (Optional)

To accept NewDB's native query syntax, consider ANTLR4:

```text
jdbc-newdb/
└── src/main/antlr/
    └── net/hasor/dbvisitor/adapter/newdb/parser/
        ├── NewDBLexer.g4     # Lexer rules
        └── NewDBParser.g4    # Parser rules
```

Traverse the parsed AST using visitors and dispatch to command executors. The four existing adapters use this pattern:

- **Redis**：Command-line style — `SET key value`, `HGET hash field`
- **MongoDB**：JS Shell style — `db.users.find({age: {$gt: 18}})`
- **Elasticsearch**：JSON style — `GET /index/_search { "query": {...} }`
- **Milvus**：SQL-like style — `SELECT * FROM collection WHERE ...`

## Step 8: Register Through SPI

Create a file under `src/main/resources/META-INF/services/`:

**File name**：`net.hasor.dbvisitor.driver.AdapterFactory`

**Contents**：

```text
net.hasor.dbvisitor.adapter.newdb.NewDBConnFactory
```

Once registered, users can add `jdbc-newdb` to the classpath and connect through JDBC:

```java
// Standard JDBC
try (Connection conn = DriverManager.getConnection(
        "jdbc:dbvisitor:newdb://localhost:9000?database=mydb", "user", "password")) {
    // Use Statement or JdbcTemplate to execute supported commands.
}
```

SPI registration integrates the JDBC driver. To generate commands through LambdaTemplate or BaseMapper, also implement and register a database dialect.

## Step 9: Test the Adapter

Command parsing, parameter binding, SDK request construction and JDBC result access can be tested with command interceptors and SDK mocks. Database behavior also needs tests against a real service. The integration test below assumes the service is running and NewDB supports `SELECT 1`:

```java
public class NewDBAdapterTest {
    @Test
    public void testBasicQuery() throws Exception {
        // 1. Open a connection
        try (Connection conn = DriverManager.getConnection(
                "jdbc:dbvisitor:newdb://localhost:9000")) {
            JdbcTemplate jdbc = new JdbcTemplate(conn);
            assertEquals(Integer.valueOf(1), jdbc.queryForObject("SELECT 1", Integer.class));
        }
    }
}
```

## Add Optional Capabilities

Complete basic command execution first, then add capabilities supported by the underlying SDK. The following fragments extend the preceding example.

| Interface | Responsibility | Integration point |
| --- | --- | --- |
| `TransactionSupport` | Auto-commit, isolation, commit and rollback | Implement on the adapter connection; JDBC detects it automatically |
| `TypeSupport` | Type names, JDBC codes, Java classes and converter selection | Return from `AdapterFactory.createTypeSupport(properties)` |
| `TypeConvert` | Convert one result value to a Java type | Return from `TypeSupport.findConvert(typeName, targetType)` |
| `MetadataSupport` | Discover catalogs, schemas, tables, views and columns | Implement on the adapter connection; JDBC detects it automatically |

Transactions and metadata require no additional SPI registration. Applications can obtain installed capabilities with `connection.unwrap(TransactionSupport.class)`, `connection.unwrap(TypeSupport.class)` or `connection.unwrap(MetadataSupport.class)`. Unavailable capabilities cannot be unwrapped. Most applications should use standard JDBC methods instead.

### TransactionSupport

This interface connects JDBC transaction methods to real database transactions. Implement it only when commands on the connection can share the same native transaction.

| JDBC call | Adapter method |
| --- | --- |
| `setAutoCommit(value)` / `getAutoCommit()` | `setAutoCommit(value)` / `isAutoCommit()` |
| `setTransactionIsolation(level)` / `getTransactionIsolation()` | `setIsolation(level)` / `getIsolation()` |
| `commit()` / `rollback()` | `commit()` / `rollback()` |
| `getMetaData().supportsTransactionIsolationLevel(level)` | `supportIsolation(level)` |

Add the interface to `NewDBConn` from Step 4. This is an integration fragment: `client` represents the target SDK's transactional client. Replace its calls with the actual SDK API and implement the remaining methods listed above.

```java
public class NewDBConn extends AdapterConnection implements TransactionSupport {
    // Keep the constructor, request execution and remaining methods.

    @Override
    public void commit() throws SQLException {
        client.commit();
    }

    @Override
    public void rollback() throws SQLException {
        client.rollback();
    }
}
```

Application code continues to use JDBC:

```java
connection.setAutoCommit(false);
try (Statement statement = connection.createStatement()) {
    statement.executeUpdate(command1);
    statement.executeUpdate(command2);
    connection.commit();
} catch (SQLException e) {
    connection.rollback();
    throw e;
}
```

`command1` and `command2` are native write commands. Execute both in the connection's transaction context, not through newly created clients for each request.

:::note
- `setIsolation` must validate the requested level itself; JDBC does not call `supportIsolation` first.
- `getIsolation` and `isAutoCommit` do not declare `SQLException`. Update any local state only after the SDK operation succeeds.
- This interface does not expose savepoints. Implementing it does not enable JDBC savepoint methods.
- Without this interface, only auto-commit and `TRANSACTION_NONE` are allowed. Disabling auto-commit, committing or rolling back is unsupported.
:::

### TypeSupport

Describes adapter type names for JDBC type information, parameter type identification and result conversion. Prefer extending `AdapterTypeSupport` to retain its standard mappings.

| Method | Purpose |
| --- | --- |
| `getTypeName(int)` | Map a JDBC type code to an adapter type name |
| `getTypeName(Class<?>)` | Map a Java parameter class to an adapter type name |
| `getTypeNumber(String)` | Obtain the JDBC code for an adapter type |
| `getTypeClassName(String)` | Obtain the Java class name for an adapter type |
| `findConvert(String, Class<?>)` | Select a converter for a column type and target Java class |

For example, an SDK returns UUIDs as strings. Declare those columns as `uuid` and support explicit reads as `UUID`:

```java
import java.sql.Types;
import java.util.Properties;
import java.util.UUID;
import net.hasor.dbvisitor.driver.AdapterTypeSupport;
import net.hasor.dbvisitor.driver.TypeConvert;

public class UuidTypeSupport extends AdapterTypeSupport {
    private static final String UUID_TYPE = "uuid";

    public UuidTypeSupport(Properties properties) {
        super(properties);
        addTypeMappingTo(UUID_TYPE, Types.OTHER, UUID.class);
        addClassMapping(UUID.class, UUID_TYPE);
    }

    @Override
    public TypeConvert findConvert(String typeName, Class<?> targetType) {
        if (UUID_TYPE.equals(typeName) && targetType == UUID.class) {
            return (type, value) -> value instanceof UUID
                    ? value : UUID.fromString(value.toString());
        }
        return super.findConvert(typeName, targetType);
    }
}
```

Return it from the factory:

```java
@Override
public TypeSupport createTypeSupport(Properties properties) {
    return new UuidTypeSupport(properties);
}
```

Set the cursor's `JdbcColumn.type` to `uuid` as well. Applications then read with `resultSet.getObject("id", UUID.class)`. When customization is unnecessary, return `new AdapterTypeSupport(properties)`; a `null` factory result also selects this default.

### TypeConvert

Converts one result value. It does not execute commands or describe types. The lambda in the UUID example implements:

```java
Object convert(Class<?> targetType, Object value);
```

For `ResultSet.getObject(column, UUID.class)`, JDBC selects a converter using the column type and target Java class, then calls `convert`. Reads by label resolve the column index first.

:::note
- Untyped `getObject(column)` returns the raw cursor value. Declaring a Java class name does not automatically convert it.
- SQL NULL bypasses the converter and returns null.
- Runtime conversion errors are wrapped in `SQLException`.
- `AdapterTypeSupport` checks target-Java-type converters before column-type converters. Override `findConvert`, as above, to restrict conversion to a particular source/target pair.
:::

Do not register `TypeConvert` directly on a connection. It is separate from dbVisitor entity-field `TypeHandler` mapping. Serialization of UUID, JSON or vector write parameters into SDK values remains the command implementation's responsibility.

### MetadataSupport

Implement the interface on the connection for automatic JDBC discovery and reuse. Delegate native queries to a separate `NewDBMetadata` object:

```java
public class NewDBConn extends AdapterConnection implements MetadataSupport {
    private final NewDBMetadata metadata; // Create once in the connection constructor.

    @Override
    public Set<MetadataType> supportedTypes() {
        return metadata.supportedTypes();
    }

    @Override
    public List<MetadataNode> query(MetadataPath path) throws SQLException {
        return metadata.query(path);
    }

    // Keep the constructor and other AdapterConnection methods.
}
```

This is an integration fragment. `NewDBMetadata` implements `MetadataSupport` using the real SDK. Connections without metadata support need not implement this interface; JDBC metadata methods return standard empty results.

#### Declare Supported Object Types

`supportedTypes()` declares available object kinds, not existing objects. For a database with catalogs, tables and columns:

```java
@Override
public Set<MetadataType> supportedTypes() {
    return Set.of(MetadataType.CATALOG, MetadataType.TABLE, MetadataType.COLUMN);
}
```

Return the same declaration for an empty database. Do not invent schema or table levels. The default is an empty set, so override this method when providing metadata.

#### Query by Path

`MetadataPath` contains the target type and named parents. This example queries columns of `app.users`:

```java
MetadataPath path = new MetadataPath(MetadataType.COLUMN, List.of(
        new MetadataPath.Level(MetadataType.CATALOG, "app"),
        new MetadataPath.Level(MetadataType.TABLE, "users")
));

MetadataSupport metadata = connection.unwrap(MetadataSupport.class);
List<MetadataNode> columns = metadata.query(path);
```

Dispatch on `path.type()`. Read parent names through `path.name(MetadataType.CATALOG)` and `path.name(MetadataType.TABLE)`. Names are literal: do not split slashes or interpret JDBC wildcards.

A root `CATALOG` query lists databases; a `TABLE` query beneath a catalog lists tables; a `COLUMN` query beneath a table lists fields. Datasources without catalogs can list tables directly at the root.

#### Return Nodes and JDBC Results

For example, a non-nullable BIGINT column:

```java
MetadataNode idColumn = new MetadataNode(MetadataType.COLUMN, "id", Map.of(
        MetadataNode.TYPE_NAME, "BIGINT",
        MetadataNode.JDBC_TYPE, Types.BIGINT,
        MetadataNode.NULLABLE, false,
        MetadataNode.ORDINAL, 1
));
```

Use actual schema names and attributes; omit unknown properties. `NULLABLE`, `AUTO_INCREMENT` and `GENERATED` are booleans. Type codes, lengths and ordinal positions are numeric; ordinals start at 1.

Applications normally use `connection.getMetaData().getTables(...)` and `getColumns(...)`. JDBC handles name patterns, sorting, standard columns and empty results; adapters return native node lists only.

Unsupported paths return an empty list, never `null`. Propagate connection and permission errors instead of returning empty results.

## Custom Dialects {#custom-dialect}

If the built-in dialects do not meet your needs, you can customize a dialect by extending `AbstractDialect` and implementing the required interfaces. The main dialect interfaces are listed below, with `SqlDialect` as their common base:

| Interface | Responsibility |
|------|------|
| `SqlDialect` | Base interface: manages keyword lists, generates table/column/sort column names |
| `ConditionSqlDialect` | Condition-related SQL generation (e.g., LIKE statements) |
| `InsertSqlDialect` | Advanced INSERT statement generation (e.g., [write conflict strategies](../../guides/core/lambda/insert#conflict)) |
| `PageSqlDialect` | Pagination statement generation (`countSql` + `pageSql`) |
| `SeqSqlDialect` | Sequence query statement generation |
| `VectorSqlDialect` | Vector ordering and range conditions |

:::info[Tip]
Extend the `AbstractDialect` abstract class and implement the `PageSqlDialect` interface to customize pagination dialect.
- `countSql` — generates the SQL statement for counting
- `pageSql` — generates the paginated SQL statement
:::

```java title='Register a custom dialect'
SqlDialectRegister.registerDialectAlias(JdbcHelper.MYSQL, MyDialect.class);
```

An explicitly configured dialect takes precedence. Otherwise, dbVisitor looks up the dialect using the JDBC URL, driver name and database version from connection metadata, falling back to the default dialect if no match is found. Configuration accepts a dialect alias or a fully qualified class name.

## Best Practices

| Practice | Description |
| ------ | ------ |
| **Reuse Cobble** | Use utilities in `net.hasor.cobble.*` (StringUtils, ClassUtils, etc.) to reduce external dependencies |
| **Expose the native client** | Return the underlying SDK object from `unwrap()` for advanced access |
| **Wrap exceptions** | Wrap SDK errors in SQLException, preserving the original message and error code |
| **Resource safety** | Prevent connection leaks on failure in createConnection and doClose |
| **Java version** | The project uses Java 17; the adapter and SDK must match the target runtime |
| **Naming** | Follow the XxxConnFactory / XxxConn / XxxCmd / XxxRequest / XxxKeys convention |

## File Checklist

A minimal adapter module needs these files:

```text
jdbc-newdb/
├── build.gradle
└── src/main/
    ├── java/net/hasor/dbvisitor/adapter/newdb/
    │   ├── NewDBKeys.java          # Configuration key constants
    │   ├── NewDBConnFactory.java   # AdapterFactory implementation
    │   ├── NewDBConn.java          # AdapterConnection implementation
    │   └── NewDBRequest.java       # AdapterRequest subclass
    └── resources/META-INF/services/
        └── net.hasor.dbvisitor.driver.AdapterFactory
```

For complex query syntax, add:

```text
    ├── antlr/.../parser/
    │   ├── NewDBLexer.g4
    │   └── NewDBParser.g4
    └── java/.../
        ├── NewDBCmd.java              # SDK command delegate
        ├── NewDBCommands*.java        # Command-family implementations
        ├── NewDBDistributeCall.java   # AST -> command dispatch
        ├── CustomNewDB.java           # Custom extension point
        └── parser/
            ├── NewDBArgVisitor.java   # AST traversal
            └── ThrowingListener.java  # Parse error handling
```
