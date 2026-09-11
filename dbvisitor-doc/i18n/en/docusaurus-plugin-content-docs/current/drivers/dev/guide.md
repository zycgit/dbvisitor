---
id: guide
sidebar_label: Custom Drivers
sidebar_position: 1
title: Custom Drivers
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

SPI registration only integrates the JDBC driver. To generate commands through LambdaTemplate or BaseMapper, also implement and register a database dialect; see [Custom Dialects](../../features/support.md#custom-dialect).

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
