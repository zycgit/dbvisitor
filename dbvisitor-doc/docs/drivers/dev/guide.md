---
id: guide
sidebar_position: 1
title: 2. 自定义驱动
sidebar_label: 2. 自定义驱动
toc_max_heading_level: 3
description: 以接入 NewDB 为例，手把手实现一个 dbVisitor 适配器。
---

以下内容以假想数据源 **NewDB** 为例，说明适配器的组成和接入步骤。示例中的 `NewDBClient`、`NewDBResult` 是用于说明 SDK 调用位置的假想类型，需要替换为目标数据库的 API；代码片段省略了 import，不是可以直接运行的完整项目。

开始之前，建议先阅读 [架构设计](./about) 了解核心组件和执行模型。

## 步骤 1：创建 Gradle 模块

在 dbVisitor 仓库的 `dbvisitor-adapter/` 下创建 `jdbc-newdb` 模块，并在根目录 `settings.gradle` 注册项目及其目录。模块的 `build.gradle` 依赖公共 JDBC 驱动层：

```groovy
dependencies {
    api project(':dbvisitor-driver')
    // 在此添加目标数据库的官方 SDK 依赖。
}
```

独立项目也可以通过 Maven 或 Gradle 依赖 `net.hasor:dbvisitor-driver`，版本应与使用的 dbVisitor 版本一致；无需依赖不存在的 `dbvisitor-adapter` Maven 父模块。

推荐的包结构：`net.hasor.dbvisitor.adapter.newdb`

## 步骤 2：定义配置键常量

配置键常量集中管理所有 JDBC URL 参数和 Properties 接受的属性名：

```java
public final class NewDBKeys {
    public static final String ADAPTER_NAME       = JdbcDriver.P_ADAPTER_NAME;
    public static final String ADAPTER_NAME_VALUE = "newdb";

    // 连接参数
    public static final String SERVER   = JdbcDriver.P_SERVER;
    public static final String USERNAME = JdbcDriver.P_USER;
    public static final String PASSWORD = JdbcDriver.P_PASSWORD;
    public static final String DATABASE = "database";

    // 超时/池配置...
    public static final String CONN_TIMEOUT = "connectTimeout";
}
```

## 步骤 3：实现 AdapterFactory

工厂负责解析 URL、创建底层 SDK 客户端、构造连接对象：

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
        // JdbcDriver 已识别适配器并将 URL 参数合并到 props。
        // 保留参数名称的大小写，例如 connectTimeout。
        Map<String, String> config = new HashMap<>();
        props.forEach((k, v) -> config.put(
            k.toString(), v.toString()
        ));

        // 3. 创建底层 SDK 客户端
        String server = config.getOrDefault(NewDBKeys.SERVER, "localhost:9000");
        NewDBClient client = NewDBClient.connect(server);

        // 4. 构造连接
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
`createConnection` 中如果发生异常，务必关闭已创建的底层资源再抛出，防止资源泄漏。
:::

## 步骤 4：实现 AdapterConnection

连接是适配器的核心，管理生命周期和请求执行：

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

    /** 初始化：获取数据库版本信息 */
    public void initConnection() {
        AdapterInfo info = this.getInfo();
        info.getDriverVersion().setName("jdbc-newdb");
        // 通过 SDK 获取服务端版本
        String version = client.getServerVersion();
        info.getDbVersion().setName("NewDB");
        info.getDbVersion().setVersion(version);
        // 按目标数据库的版本格式填充 majorVersion、minorVersion。
    }

    // --- catalog / schema ---

    @Override
    public void setCatalog(String catalog) { /* NewDB 无 catalog 概念 */ }

    @Override
    public String getCatalog() { return null; }

    @Override
    public void setSchema(String schema) { this.database = schema; }

    @Override
    public String getSchema() { return this.database; }

    // --- unwrap：暴露底层客户端 ---

    @Override
    protected <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(this.client)) {
            return iface.cast(this.client);
        }
        return null;
    }

    // --- 请求管理 ---

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
            // 1. 解析命令（可选：ANTLR4 parser）
            // 2. 调用底层 SDK 执行
            NewDBResult result = this.client.execute(command);

            // 3. 通过 receive 回调返回结果
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

    // --- 关闭 ---

    @Override
    protected void doClose() throws IOException {
        this.client.close();
    }
}
```

### 关键实现要点

示例只展示同步执行。实现参数绑定时，从 `AdapterRequest` 读取参数并优先使用 SDK 的参数接口，不要将未经处理的值拼回命令。实现取消时，需要将请求与 SDK 调用关联，优先覆盖 `cancelRequest(AdapterRequest)`，避免取消同连接的其他语句；仅设置一个未被执行流程检查的标志并不能实现取消。

**`doRequest` 方法** 是适配器最核心的代码。典型流程：

1. **解析阶段** — 将 SQL 或命令字符串解析为可执行的命令结构
2. **执行阶段** — 调用底层 SDK 执行操作
3. **回调阶段** — 通过 `AdapterReceive` 把结果送回 JDBC 层

结果回调的选择取决于操作类型：

| 操作类型 | 调用的回调方法 |
| --------- | -------------- |
| 查询（SELECT） | `receive.responseResult(request, cursor)` |
| 更新（INSERT/UPDATE/DELETE） | `receive.responseUpdateCount(request, count)` |
| 带自增键的更新 | `receive.responseUpdateCount(request, count, generatedKeys)` |
| 执行失败 | `receive.responseFailed(request, exception)` |
| 执行结束 | `receive.responseFinish(request)` **（必须调用）** |

## 步骤 5：实现 AdapterRequest

简单封装命令体和参数：

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

## 步骤 6：构建结果集（AdapterCursor）

`AdapterCursor` 是 dbvisitor-driver 提供的结果集抽象，需要将 SDK 返回的数据转换为行列格式：

```java
private AdapterCursor buildCursor(AdapterRequest request, NewDBResult result) throws SQLException {
    // 1. 定义列
    List<String> columns = result.getColumnNames();
    List<String> types = result.getColumnTypes(); // TypeSupport 可识别的类型名，如 VARCHAR

    // 2. 构造 cursor
    List<JdbcColumn> metadata = new ArrayList<>();
    for (int i = 0; i < columns.size(); i++) {
        metadata.add(new JdbcColumn(columns.get(i), types.get(i), "", "", "",
                ResultSetMetaData.columnNullableUnknown, false, ""));
    }
    AdapterResultCursor cursor = new AdapterResultCursor(request, metadata);

    // 3. 填充数据行
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

`AdapterCursor` 是接口，`AdapterResultCursor` 是缓冲行的实现。上例适合小结果集；大结果集应实现按需取页的 `AdapterCursor`，并在 `close()` 中释放 SDK 游标。即使查询结果为空，也应提供列元数据；`pushFinish()` 表示数据生产结束，与 `responseFinish()` 表示请求结束不同。

## 步骤 7：添加 DSL 解析器（可选）

如果需要支持用户直接编写 NewDB 的原生查询语法，建议使用 ANTLR4：

```text
jdbc-newdb/
└── src/main/antlr/
    └── net/hasor/dbvisitor/adapter/newdb/parser/
        ├── NewDBLexer.g4     # 词法规则
        └── NewDBParser.g4    # 语法规则
```

解析后的 AST 通过 Visitor 模式遍历，分发到不同的命令执行器。现有 4 个适配器都采用了这一模式：

- **Redis**：命令行风格 — `SET key value`, `HGET hash field`
- **MongoDB**：JS Shell 风格 — `db.users.find({age: {$gt: 18}})`
- **Elasticsearch**：JSON 风格 — `GET /index/_search { "query": {...} }`
- **Milvus**：SQL-like 风格 — `SELECT * FROM collection WHERE ...`

## 步骤 8：SPI 注册

在 `src/main/resources/META-INF/services/` 下创建文件：

**文件名**：`net.hasor.dbvisitor.driver.AdapterFactory`

**内容**：

```text
net.hasor.dbvisitor.adapter.newdb.NewDBConnFactory
```

注册后，用户只需将 `jdbc-newdb` 加入 classpath，即可通过标准 JDBC 方式连接：

```java
// 标准 JDBC 方式
try (Connection conn = DriverManager.getConnection(
        "jdbc:dbvisitor:newdb://localhost:9000?database=mydb", "user", "password")) {
    // 使用 Statement 或 JdbcTemplate 执行该适配器支持的命令。
}
```

SPI 注册解决 JDBC 驱动接入。若要支持 LambdaTemplate 或 BaseMapper 自动生成命令，还需实现并注册相应的数据库方言。

## 步骤 9：测试

命令解析、参数绑定、SDK 请求组装和 JDBC 结果访问可以通过命令拦截器及 SDK mock 测试；数据库实际行为还需要真实服务测试。下面的集成测试片段假设服务已经启动，且 `SELECT 1` 是 NewDB 支持的命令：

```java
public class NewDBAdapterTest {
    @Test
    public void testBasicQuery() throws Exception {
        // 1. 建立连接
        try (Connection conn = DriverManager.getConnection(
                "jdbc:dbvisitor:newdb://localhost:9000")) {
            JdbcTemplate jdbc = new JdbcTemplate(conn);
            assertEquals(Integer.valueOf(1), jdbc.queryForObject("SELECT 1", Integer.class));
        }
    }
}
```

## 按需接入扩展能力

先完成基本命令执行，再根据底层 SDK 的实际能力选择扩展。以下代码是在前面示例上追加的接入片段。

| 接口 | 负责什么 | 接入位置 |
| --- | --- | --- |
| `TransactionSupport` | 自动提交、隔离级别、提交与回滚 | 适配器连接实现接口，由 JDBC 连接自动识别 |
| `TypeSupport` | 类型名称、JDBC 类型编号、Java 类型及转换器选择 | `AdapterFactory.createTypeSupport(properties)` 返回实例 |
| `TypeConvert` | 将一个结果值转换为目标 Java 类型 | 由 `TypeSupport.findConvert(typeName, targetType)` 返回 |
| `MetadataSupport` | 查询库、Schema、表、视图和字段 | 适配器连接实现接口，由 JDBC 连接自动识别 |

事务和元信息能力随连接创建，不需要额外注册 SPI。应用可通过 `connection.unwrap(TransactionSupport.class)`、`connection.unwrap(TypeSupport.class)` 或 `connection.unwrap(MetadataSupport.class)` 获取已接入的能力；未接入的接口不能解包。通常直接使用标准 JDBC 方法即可。

### TransactionSupport

让标准 JDBC 事务方法真正调用数据库事务。仅当底层客户端能让同一连接内的命令共享事务时，才实现此接口。

| JDBC 调用 | 适配器需要实现的方法 |
| --- | --- |
| `setAutoCommit(value)` / `getAutoCommit()` | `setAutoCommit(value)` / `isAutoCommit()` |
| `setTransactionIsolation(level)` / `getTransactionIsolation()` | `setIsolation(level)` / `getIsolation()` |
| `commit()` / `rollback()` | `commit()` / `rollback()` |
| `getMetaData().supportsTransactionIsolationLevel(level)` | `supportIsolation(level)` |

在步骤 4 的 `NewDBConn` 上增加接口。以下是接入片段，`client` 代表目标 SDK 的事务客户端，其方法需按实际 SDK 替换；其余接口方法按上表实现：

```java
public class NewDBConn extends AdapterConnection implements TransactionSupport {
    // 保留连接构造、请求执行和其它方法。

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

接入后，应用仍使用标准 JDBC：

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

这里的 `command1`、`command2` 是目标数据源的写入命令。请求执行必须使用该连接的同一事务上下文，不能每条命令另建客户端。

:::note
- `setIsolation` 自行校验支持的隔离级别；JDBC 层不会先调用 `supportIsolation` 替它校验。
- `getIsolation`、`isAutoCommit` 不声明 `SQLException`。若维护本地状态，应在 SDK 操作成功后更新。
- 接口不包含保存点；实现它不会开放 JDBC 保存点方法。
- 不实现此接口时，仅允许自动提交和 `TRANSACTION_NONE`；关闭自动提交、提交及回滚会报不支持。
:::

### TypeSupport

描述适配器使用的类型名称，并为 JDBC 类型信息、参数类型识别和结果值转换提供依据。优先继承 `AdapterTypeSupport`，保留它已有的基础类型映射。

| 方法 | 用途 |
| --- | --- |
| `getTypeName(int)` | JDBC 类型编号转为适配器类型名称 |
| `getTypeName(Class<?>)` | Java 参数类型转为适配器类型名称 |
| `getTypeNumber(String)` | 适配器类型名称对应的 JDBC 类型编号 |
| `getTypeClassName(String)` | 适配器类型名称对应的 Java 类名 |
| `findConvert(String, Class<?>)` | 为结果列类型和目标 Java 类型选择转换器 |

例如，某 SDK 以字符串返回 UUID，适配器希望将该列声明为 `uuid`，并支持读取为 `UUID`：

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

在工厂中返回它：

```java
@Override
public TypeSupport createTypeSupport(Properties properties) {
    return new UuidTypeSupport(properties);
}
```

游标的 `JdbcColumn.type` 也应使用 `uuid`。应用通过 `resultSet.getObject("id", UUID.class)` 读取。没有自定义需求时返回 `new AdapterTypeSupport(properties)`；工厂返回 `null` 时，JDBC 连接也会使用这个默认实现。

### TypeConvert

只转换单个结果值，不负责执行命令或描述类型。上例中的 Lambda 表达式就是一个 `TypeConvert` 实现：

```java
Object convert(Class<?> targetType, Object value);
```

调用过程是：`ResultSet.getObject(column, UUID.class)` 指定目标类型，JDBC 层用列类型和目标类型查找转换器，再执行 `convert`。按字段名读取时会先解析为列编号。

:::note
- 不带目标类型的 `getObject(column)` 返回游标原始值，不会因声明了 Java 类名就自动转换。
- SQL NULL 不调用转换器；JDBC 层直接返回空值。
- 转换器抛出的运行时异常会被包装为 `SQLException`。
- 默认 `AdapterTypeSupport` 优先查找目标 Java 类型转换器，再查找列类型转换器。若要限定“某种列类型转某种 Java 类型”，像上例一样覆盖 `findConvert`。
:::

`TypeConvert` 不通过连接单独注册，也不同于 dbVisitor 的实体字段 `TypeHandler`。写入 SDK 所需的 UUID、JSON、向量等参数序列化，仍由适配器命令实现处理。

### MetadataSupport

连接实现接口，JDBC 层自动识别并复用。具体查询可放在独立的 `NewDBMetadata` 类，连接只委托：

```java
public class NewDBConn extends AdapterConnection implements MetadataSupport {
    private final NewDBMetadata metadata; // 在连接构造时创建一次。

    @Override
    public Set<MetadataType> supportedTypes() {
        return metadata.supportedTypes();
    }

    @Override
    public List<MetadataNode> query(MetadataPath path) throws SQLException {
        return metadata.query(path);
    }

    // 保留构造方法和 AdapterConnection 的其它实现。
}
```

这是连接内的接入片段；`NewDBMetadata` 需要实现 `MetadataSupport` 并调用实际 SDK。没有这项能力的连接无需实现接口，标准 JDBC 元信息方法返回空结果。

#### 声明支持的对象类型

`supportedTypes()` 返回可查询的对象种类，而不是已有对象。例如只有库、表和字段的数据库：

```java
@Override
public Set<MetadataType> supportedTypes() {
    return Set.of(MetadataType.CATALOG, MetadataType.TABLE, MetadataType.COLUMN);
}
```

空库仍返回同样的声明；不要为了 JDBC 补出不存在的 Schema 或表层级。默认返回空集合，提供元信息时应覆盖此方法。

#### 按路径查询

`MetadataPath` 包含目标类型和父级名称。下面表示查询 `app` 库中 `users` 表的字段：

```java
MetadataPath path = new MetadataPath(MetadataType.COLUMN, List.of(
        new MetadataPath.Level(MetadataType.CATALOG, "app"),
        new MetadataPath.Level(MetadataType.TABLE, "users")
));

MetadataSupport metadata = connection.unwrap(MetadataSupport.class);
List<MetadataNode> columns = metadata.query(path);
```

实现 `query` 时，通过 `path.type()` 分派查询，通过 `path.name(MetadataType.CATALOG)`、`path.name(MetadataType.TABLE)` 获取父级名称。它们是原始名称，不按斜杠拆分，也不作为 JDBC 通配符解析。

无父级的 `CATALOG` 查询列出库；库路径下的 `TABLE` 查询列出表；表路径下的 `COLUMN` 查询列出字段。没有库层级的数据源可直接在根路径查询表。

#### 返回节点与 JDBC 结果

例如，一个不可空的 BIGINT 字段可表示为：

```java
MetadataNode idColumn = new MetadataNode(MetadataType.COLUMN, "id", Map.of(
        MetadataNode.TYPE_NAME, "BIGINT",
        MetadataNode.JDBC_TYPE, Types.BIGINT,
        MetadataNode.NULLABLE, false,
        MetadataNode.ORDINAL, 1
));
```

节点名称和属性应来自实际 schema；未知属性不填推测值。`NULLABLE`、`AUTO_INCREMENT`、`GENERATED` 使用布尔值，类型编号、长度和字段序号使用数值，字段序号从 1 开始。

应用一般不必直接访问节点，而是使用 `connection.getMetaData().getTables(...)`、`getColumns(...)`。JDBC 层统一负责名称模式过滤、排序、标准列结构和空结果；适配器只返回原生节点列表。

不支持的路径返回空列表，不能返回 `null`。连接、权限等异常应抛出，不得转换为空列表。

## 自定义方言 {#custom-dialect}

如果内置方言不满足需求，可以通过继承 `AbstractDialect` 并实现所需接口来自定义方言。下表列出主要方言接口，`SqlDialect` 是公共基础接口：

| 接口 | 职责 |
|------|------|
| `SqlDialect` | 基础接口，管理关键词清单、生成表名/列名/排序列名 |
| `ConditionSqlDialect` | 条件相关的 SQL 生成（如 LIKE 语句） |
| `InsertSqlDialect` | 高级 INSERT 语句生成（如 [写入冲突策略](../../guides/core/lambda/insert#conflict)） |
| `PageSqlDialect` | 分页语句生成（`countSql` + `pageSql`） |
| `SeqSqlDialect` | 序列查询语句生成 |
| `VectorSqlDialect` | 向量排序与范围条件生成 |

:::info[提示]
继承 `AbstractDialect` 抽象类并实现 `PageSqlDialect` 接口即可自定义分页方言。
- `countSql` — 生成计算 count 的 SQL 语句
- `pageSql` — 生成分页 SQL 语句
:::

```java title='注册自定义方言'
SqlDialectRegister.registerDialectAlias(JdbcHelper.MYSQL, MyDialect.class);
```

显式配置的方言优先；未配置时，dbVisitor 根据连接元数据中的 JDBC URL、驱动名称和数据库版本查找方言，未匹配时使用默认方言。配置值可以是方言别名或全限定类名。

## 最佳实践

| 实践 | 说明 |
| ------ | ------ |
| **复用 Cobble** | 使用 `net.hasor.cobble.*` 中的工具类（StringUtils、ClassUtils 等），减少外部依赖 |
| **暴露原生客户端** | 在 `unwrap()` 中返回底层 SDK 对象，允许高级用户绕过适配层 |
| **异常包装** | 将 SDK 异常包装为 `SQLException`，保留原始错误信息和错误码 |
| **资源安全** | `createConnection` 和 `doClose` 中确保异常时不泄漏底层连接 |
| **Java 版本** | 当前项目使用 Java 17，适配器及 SDK 应与目标运行环境兼容 |
| **命名规范** | 遵循 `XxxConnFactory` / `XxxConn` / `XxxCmd` / `XxxRequest` / `XxxKeys` 的命名惯例 |

## 完整文件清单

一个最小可用的适配器模块需要以下文件：

```text
jdbc-newdb/
├── build.gradle
└── src/main/
    ├── java/net/hasor/dbvisitor/adapter/newdb/
    │   ├── NewDBKeys.java          # 配置键常量
    │   ├── NewDBConnFactory.java   # AdapterFactory 实现
    │   ├── NewDBConn.java          # AdapterConnection 实现
    │   └── NewDBRequest.java       # AdapterRequest 子类
    └── resources/META-INF/services/
        └── net.hasor.dbvisitor.driver.AdapterFactory
```

如需支持复杂查询语法，追加：

```text
    ├── antlr/.../parser/
    │   ├── NewDBLexer.g4
    │   └── NewDBParser.g4
    └── java/.../
        ├── NewDBCmd.java              # SDK 命令委托
        ├── NewDBCommands*.java        # 分类命令实现
        ├── NewDBDistributeCall.java   # AST → 命令分发
        ├── CustomNewDB.java           # 自定义扩展点
        └── parser/
            ├── NewDBArgVisitor.java   # AST 遍历
            └── ThrowingListener.java  # 解析错误处理
```
