# dbVisitor Adapter 开发指南

本文档旨在介绍 `dbvisitor-adapter` 模块的架构设计，并为开发者提供接入新数据源（如 Milvus, Neo4j 等）的实施指南。

## 1. 架构概览

dbVisitor 的 Adapter 机制允许开发者将非关系型数据库（NoSQL）或非标准 JDBC 数据源封装为类似 JDBC 的接口，从而能够利用 dbVisitor 提供的统一 API 进行操作。

核心架构基于 `net.hasor.dbvisitor.driver` 包下的以下抽象：

*   **AdapterFactory**: 负责解析 JDBC URL，配置并创建连接。
*   **AdapterConnection**: 核心连接对象，负责管理生命周期、元数据加载以及 SQL/命令的执行调度。
*   **AdapterRequest**: 封装一次查询或操作请求（类似 Statement）。
*   **AdapterReceive**: 接收执行结果的回调接口。

现有的 `jdbc-elastic`, `jdbc-mongo`, `jdbc-redis` 均遵循此模式，通常包含以下层级：

1.  **JDBC 适配层**: 实现上述接口，对外暴露 JDBC 行为。
2.  **解析层 (Parser)**: (可选) 使用 ANTLR4 将伪 SQL 或该数据库的原生 Shell 命令解析为语法树。
3.  **执行层 (Executor/Cmd)**: 将解析后的命令转换为底层 SDK (Native Driver) 的调用。

## 2. 现有适配器分析

通过分析 Elastic, Mongo, Redis 适配器，我们总结出统一的实现模式：

| 组件 | Elasticsearch | MongoDB | Redis | 说明 |
| :--- | :--- | :--- | :--- | :--- |
| **底层驱动** | `RestClient` (Official) | `MongoClient` (Official) | `Jedis` | Adapter 本质是这些原生驱动的 Wrapper。 |
| **Connection** | `ElasticConn` | `MongoConn` | `JedisConn` | 继承自 `AdapterConnection`。 |
| **Factory** | `ElasticConnFactory` | `MongoConnFactory` | `JedisConnFactory` | 注册到 SPI，处理 URL 参数。 |
| **DSL 解析** | JSON 风格 (ANTLR4) | JS Shell 风格 (ANTLR4) | Cmd Line 风格 (ANTLR4) | 使得用户可以直接写该数据库熟悉的 Query。 |
| **核心执行器** | `ElasticDistributeCall` | `MongoDistributeCall` | `JedisDistributeCall` | 遍历 AST 节点并调用驱动 API。 |

### 2.1 连接管理
所有 `Conn` 类都继承自 `AdapterConnection`。
*   **initConnection()**: 在连接建立后立即调用，用于获取数据库版本信息 (`initConnection` 方法中实现)。
*   **unwrap()**: 允许用户通过 `conn.unwrap(Client.class)` 获取底层的原生驱动对象。

### 2.2 查询执行流程
`AdapterConnection.doRequest` 是执行的核心入口：
1.  **Parser**: 将 `sql` 字符串传递给 ANTLR4 Lexer/Parser 生成 AST (Abstract Syntax Tree)。
2.  **Visitor**: 遍历 AST，提取参数和命令结构。
3.  **Execute**: 遍历命令列表，通过 `XxxDistributeCall` 分发到具体的逻辑处理类（如 `ElasticCommandsForIndex`, `MongoCommandsForCrud` 等）。
4.  **ResultSet**: 将原生 SDK 返回的结果封装为 JDBC `ResultSet` 或 dbVisitor 友好的格式。

## 3. 新数据源接入指南

假设我们要接入一个新的数据源 `NewDB`，推荐步骤如下：

### 步骤 1: 创建模块与依赖
在 `dbvisitor-adapter` 下创建新模块 `jdbc-newdb`，并在 `pom.xml` 中引入：
*   `dbvisitor` (scope: provided)
*   `cobble-all` (基础工具库)
*   NewDB 的官方 Java SDK

### 步骤 2: 定义 Command 接口 (可选)
如果 NewDB 有复杂的 SDK 操作，建议定义一个 `NewDBCmd` 接口来隔离 Adapter 逻辑与 SDK 调用。

### 步骤 3: 实现 Parser (推荐)
为了支持复杂查询，建议在 `src/main/antlr4` 下定义 `.g4` 文件。
*   **Lexer**: 定义关键字和 Token。
*   **Parser**: 定义语法规则。
*   目标是让用户能直接粘贴 NewDB 的控制台命令或 JSON 查询到 dbVisitor 中执行。

### 步骤 4: 实现 Core 组件

#### 4.1 NewDBConnFactory
实现 `AdapterFactory` 接口：
```java
public class NewDBConnFactory implements AdapterFactory {
    @Override
    public String getAdapterName() { return "newdb"; } // JDBC URL 前缀，如 jdbc:newdb:...

    @Override
    public AdapterConnection createConnection(Connection owner, String jdbcUrl, Properties properties) {
        // 1. 解析 properties 和 URL
        // 2. 初始化 NewDB SDK Client
        // 3. 返回 NewDBConn
    }
}
```

#### 4.2 NewDBConn
继承 `AdapterConnection`：
```java
public class NewDBConn extends AdapterConnection {
    private final NewDBClient client;

    @Override
    public void initConnection() {
        // 调用 client 获取版本信息，填充到 this.getInfo()
    }

    @Override
    public synchronized void doRequest(AdapterRequest request, AdapterReceive receive) throws SQLException {
        // 1. 解析 request.getCommandBody() -> AST
        // 2. 遍历 AST，调用 client.query(...)
        // 3. 将结果通过 receive.addDataSet(...) 或 receive.addRecord(...) 返回
    }
}
```

### 步骤 5: 结果集封装
dbVisitor 需要标准的行/列数据结构。
*   如果是查询：调用 `AdapterReceive.addDataSet` 创建各种 Column，然后填充数据。
*   如果是更新：调用 `AdapterReceive.setAffectedCount`。

### 步骤 6: SPI 注册
在 `src/main/resources/META-INF/services/net.hasor.dbvisitor.driver.AdapterFactory` 文件中添加 `NewDBConnFactory` 的全限定名。

## 4. 最佳实践
*   **复用 Cobble**: 尽量使用 `net.hasor.cobble` 中的工具类，减少外部依赖。
*   **支持原生 API**: 务必在 `unwrap` 方法中暴露底层的 Native Client，方便高级用户 bypass Adapter 限制。
*   **异常处理**: 将 Native SDK 的异常包装为 `SQLException`，并尽量保留原始错误信息。
*   **单元测试**: 参考 `jdbc-elastic` 的测试结构，使用 TestContainers 或 Mock 进行测试。
