---
id: about
sidebar_position: 0
title: Architecture
description: Core architecture, component responsibilities and execution flow of dbvisitor-driver adapters.
---

The protocol adapter layer (dbvisitor-driver) lets applications access non-relational databases through JDBC, reusing command execution and result mapping in JdbcTemplate, annotated Mappers and Mapper files. Command generation in LambdaTemplate and BaseMapper also requires a matching database dialect.

This page describes the core components and execution model. For implementation steps, see the [implementation guide](./guide).

## Core Components

The adapter layer has four core interfaces/abstract classes in the `net.hasor.dbvisitor.driver` package:

| Component | Type | Responsibility |
| ------ | ------ | ------ |
| **AdapterFactory** | Interface | Parse JDBC URLs, create connections and provide type support |
| **AdapterConnection** | Abstract class | Manage connection lifecycle and dispatch requests |
| **AdapterRequest** | Abstract class | Encapsulate a query/operation request (similar to a Statement) |
| **AdapterReceive** | Interface | Receive results through callbacks (result sets, update counts, errors) |

## AdapterFactory

The adapter entry point. Each data source has a Factory implementation registered with `AdapterManager` through SPI.

```java
public interface AdapterFactory {
    /** Adapter name: adapterName in the JDBC URL */
    String getAdapterName();

    /** Supported configuration property names */
    String[] getPropertyNames();

    /** Create type conversion support */
    TypeSupport createTypeSupport(Properties properties);

    /** Parse the URL and properties and create a connection */
    AdapterConnection createConnection(
        Connection owner, String jdbcUrl, Properties properties
    ) throws SQLException;
}
```

The JDBC URL format is `jdbc:dbvisitor:<adapterName>://<server>?param=value`. The legacy form omitting the colon after the adapter name is also accepted.

## AdapterConnection

Each adapter connection manages an underlying data-source client. The following lists key API signatures, omitting existing method bodies; extend this class when implementing an adapter rather than copying this declaration:

```java
public abstract class AdapterConnection implements Closeable {

    public AdapterConnection(String jdbcUrl, String userName) { ... }

    /** Connection information (URL, user name, versions, etc.) */
    public AdapterInfo getInfo();

    /** Feature switches */
    public AdapterFeatures getFeatures();

    /** Catalog/schema management */
    public abstract void setCatalog(String catalog) throws SQLException;
    public abstract String getCatalog() throws SQLException;
    public abstract void setSchema(String schema) throws SQLException;
    public abstract String getSchema() throws SQLException;

    /** Expose the native client */
    protected <T> T unwrap(Class<T> iface) throws SQLException;

    /** Create a request */
    public abstract AdapterRequest newRequest(String sql);

    /** Execute a request and return results through receive */
    public abstract void doRequest(
        AdapterRequest request, AdapterReceive receive
    ) throws SQLException;

    /** Cancel the active request */
    public abstract void cancelRequest();

    /** Cancel a specific request; override for concurrent statements */
    public void cancelRequest(AdapterRequest request);

    /** Close the connection and release underlying resources */
    protected abstract void doClose() throws IOException;
}
```

## AdapterRequest

Encapsulates the parameters and metadata of an operation:

```java
public abstract class AdapterRequest {
    private final String traceId;   // Automatically generated unique trace ID
    protected boolean generatedKeys;
    protected long    maxRows;
    protected int     fetchSize;
    protected int     timeoutSec;

    // Parameter mapping (named parameter -> JdbcArg)
    public Map<String, JdbcArg> getArgMap();
    public void setArgMap(Map<String, JdbcArg> argMap);
}
```

An adapter typically defines its own Request subclass (such as `JedisRequest`), instantiated by `newRequest()`.

## AdapterReceive

Execution results are delivered through callbacks. The JDBC layer of dbvisitor-driver exposes cursors as standard ResultSets:

```java
public interface AdapterReceive {
    /** Execution failure */
    boolean responseFailed(AdapterRequest request, Throwable e);

    /** Query result (cursor) */
    boolean responseResult(AdapterRequest request, AdapterCursor cursor);

    /** Query result + generated keys */
    boolean responseResult(
        AdapterRequest request, AdapterCursor cursor, AdapterCursor generatedKeys
    );

    /** Update count */
    boolean responseUpdateCount(AdapterRequest request, long updateCount);

    /** Update count + generated keys */
    boolean responseUpdateCount(
        AdapterRequest request, long updateCount, AdapterCursor generatedKeys
    );

    /** Output parameter */
    boolean responseParameter(
        AdapterRequest request, String paramName, String paramType, Object value
    );

    /** Execution complete */
    boolean responseFinish(AdapterRequest request);
}
```

## Execution Flow

The call chain from the JDBC API to the underlying SDK:

```text
Application code → JdbcTemplate.queryForList(sql)
         → JDBC Driver (JdbcDriver)
         → AdapterConnection.newRequest(sql)  // Create a Request
         → AdapterConnection.doRequest(req, receive)
             ├─ Parser: SQL/command → AST (ANTLR4)
             ├─ Visitor: Traverse the AST and extract parameters and commands
             ├─ Execute: Call the underlying SDK (Jedis/MongoClient/RestClient...)
             └─ Receive: Result callbacks → responseResult / responseUpdateCount
         → JDBC ResultSet ← AdapterCursor
         → dbVisitor TypeHandler mapping
         → List<Map<String, Object>>
```

## Existing Adapters

Existing adapters follow the same pattern:

| Adapter | Underlying SDK | URL prefix | Parsing style |
| -------- | --------- | --------- | --------- |
| **jdbc-redis** | Jedis | `jdbc:dbvisitor:jedis://` | Command-line style (ANTLR4) |
| **jdbc-mongo** | MongoDB Java Driver | `jdbc:dbvisitor:mongo://` | JS Shell style (ANTLR4) |
| **jdbc-elastic** | Elasticsearch RestClient | `jdbc:dbvisitor:elastic://` | JSON style (ANTLR4) |
| **jdbc-milvus** | Milvus Java SDK | `jdbc:dbvisitor:milvus://` | SQL-like style (ANTLR4) |

Typical adapter module layout:

```text
jdbc-xxx/
├── src/main/antlr/           # .g4 grammar files
├── src/main/java/.../
│   ├── XxxConnFactory.java    # AdapterFactory implementation
│   ├── XxxConn.java           # AdapterConnection implementation
│   ├── XxxCmd.java            # Underlying SDK command delegate
│   ├── XxxRequest.java        # AdapterRequest subclass
│   ├── XxxKeys.java           # Configuration key constants
│   ├── XxxCommands*.java      # Command-family implementations
│   ├── XxxDistributeCall.java # AST traversal -> command dispatch
│   ├── CustomXxx.java         # Custom extension point
│   └── parser/                # ANTLR4-generated Lexer/Parser/Visitor
└── src/main/resources/
    └── META-INF/services/
        └── net.hasor.dbvisitor.driver.AdapterFactory  # SPI registration
```
