---
id: guide
sidebar_position: 1
title: 实现指南
description: 以接入 NewDB 为例，手把手实现一个 dbVisitor 适配器。
---

本文以一个假想数据源 **NewDB** 为例，介绍从零开始实现一个 dbVisitor 适配器的完整步骤。

开始之前，建议先阅读 [架构设计](./about.md) 了解核心组件和执行模型。

## 步骤 1：创建 Maven 模块

在 `dbvisitor-adapter/` 下创建 `jdbc-newdb` 模块：

```xml
<project>
    <parent>
        <groupId>net.hasor</groupId>
        <artifactId>dbvisitor-adapter</artifactId>
        <version>${revision}</version>
    </parent>

    <artifactId>jdbc-newdb</artifactId>

    <dependencies>
        <!-- dbVisitor 核心（编译时依赖，运行时由用户提供） -->
        <dependency>
            <groupId>net.hasor</groupId>
            <artifactId>dbvisitor</artifactId>
            <scope>provided</scope>
        </dependency>

        <!-- Cobble 工具库 -->
        <dependency>
            <groupId>net.hasor</groupId>
            <artifactId>cobble-all</artifactId>
        </dependency>

        <!-- NewDB 官方 Java SDK -->
        <dependency>
            <groupId>com.newdb</groupId>
            <artifactId>newdb-java-client</artifactId>
            <version>1.0.0</version>
        </dependency>
    </dependencies>
</project>
```

推荐的包结构：`net.hasor.dbvisitor.adapter.newdb`

## 步骤 2：定义配置键常量

配置键常量集中管理所有 JDBC URL 参数和 Properties 接受的属性名：

```java
public final class NewDBKeys {
    public static final String ADAPTER_NAME       = "adapter_name";
    public static final String ADAPTER_NAME_VALUE = "newdb";

    // 连接参数
    public static final String SERVER   = "server";
    public static final String USERNAME = "user";
    public static final String PASSWORD = "password";
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
            NewDBKeys.USERNAME, NewDBKeys.PASSWORD, NewDBKeys.DATABASE
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
        // 1. 校验 URL 前缀
        if (!jdbcUrl.startsWith("jdbc:dbvisitor:newdb//")) {
            throw new SQLException("invalid URL: " + jdbcUrl);
        }

        // 2. 解析配置
        Map<String, String> config = new HashMap<>();
        props.forEach((k, v) -> config.put(
            k.toString().toLowerCase(), v.toString()
        ));

        // 3. 创建底层 SDK 客户端
        String server = config.getOrDefault(NewDBKeys.SERVER, "localhost:9000");
        NewDBClient client = NewDBClient.connect(server);

        // 4. 构造连接
        NewDBConn conn = new NewDBConn(owner, client, jdbcUrl, config);
        conn.initConnection();
        return conn;
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
    private volatile boolean  cancelled = false;

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
        info.setDriverName("jdbc-newdb");
        // 通过 SDK 获取服务端版本
        String version = client.getServerVersion();
        info.setDbProductName("NewDB");
        // 解析版本号填充到 info...
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
        this.cancelled = false;
        String command = ((NewDBRequest) request).getCommandBody();

        try {
            // 1. 解析命令（可选：ANTLR4 parser）
            // 2. 调用底层 SDK 执行
            NewDBResult result = this.client.execute(command);

            // 3. 通过 receive 回调返回结果
            if (result.isQuery()) {
                AdapterCursor cursor = buildCursor(result);
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
        this.cancelled = true;
    }

    // --- 关闭 ---

    @Override
    protected void doClose() throws IOException {
        this.client.close();
    }
}
```

### 关键实现要点

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
private AdapterCursor buildCursor(NewDBResult result) {
    // 1. 定义列
    List<String> columns = result.getColumnNames();
    List<Integer> types = result.getColumnTypes(); // java.sql.Types

    // 2. 构造 cursor
    AdapterCursor cursor = new AdapterCursor(columns.size());
    for (int i = 0; i < columns.size(); i++) {
        cursor.setColumn(i, columns.get(i), types.get(i));
    }

    // 3. 填充数据行
    for (Object[] row : result.getRows()) {
        cursor.addRow(row);
    }
    return cursor;
}
```

## 步骤 7：添加 DSL 解析器（可选）

如果需要支持用户直接编写 NewDB 的原生查询语法，建议使用 ANTLR4：

```text
jdbc-newdb/
└── src/main/antlr4/
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
Connection conn = DriverManager.getConnection(
    "jdbc:dbvisitor:newdb//localhost:9000?database=mydb",
    "user", "password"
);

// dbVisitor 方式
Configuration config = new Configuration();
LambdaTemplate lambda = config.newLambda(dataSource);
List<User> users = lambda.query(User.class)
    .eq(User::getAge, 18)
    .queryForList();
```

## 步骤 9：测试

参考现有适配器的测试结构，推荐使用 Docker 容器进行集成测试：

```java
public class NewDBAdapterTest {
    @Test
    public void testBasicQuery() throws Exception {
        // 1. 建立连接
        Connection conn = DriverManager.getConnection(
            "jdbc:dbvisitor:newdb//localhost:9000"
        );
        // 2. 通过 dbVisitor API 执行
        JdbcTemplate jdbc = new JdbcTemplate(conn);
        List<Map<String, Object>> result = jdbc.queryForList("...");

        // 3. 验证结果
        assertNotNull(result);

        // 4. 清理
        conn.close();
    }
}
```

## 最佳实践

| 实践 | 说明 |
| ------ | ------ |
| **复用 Cobble** | 使用 `net.hasor.cobble.*` 中的工具类（StringUtils、ClassUtils 等），减少外部依赖 |
| **暴露原生客户端** | 在 `unwrap()` 中返回底层 SDK 对象，允许高级用户绕过适配层 |
| **异常包装** | 将 SDK 异常包装为 `SQLException`，保留原始错误信息和错误码 |
| **资源安全** | `createConnection` 和 `doClose` 中确保异常时不泄漏底层连接 |
| **Java 8 兼容** | 全项目要求 `source 1.8` / `target 1.8`，禁止使用 `var`、Record 等新语法 |
| **命名规范** | 遵循 `XxxConnFactory` / `XxxConn` / `XxxCmd` / `XxxRequest` / `XxxKeys` 的命名惯例 |

## 完整文件清单

一个最小可用的适配器模块需要以下文件：

```text
jdbc-newdb/
├── pom.xml
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
    ├── antlr4/.../parser/
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
