---
id: about
sidebar_position: 0
title: 架构设计
description: dbvisitor-driver 适配器层的核心架构、组件职责和执行流程。
---

dbVisitor 的协议适配层（dbvisitor-driver）允许开发者将非关系型数据库封装为类 JDBC 接口，从而复用 dbVisitor 的全部上层 API（JdbcTemplate、LambdaTemplate、BaseMapper 等）。

本文介绍适配器层的核心组件和执行模型。动手实现请参考 [实现指南](./guide.md)。

## 核心组件

适配器层由 4 个核心接口/抽象类组成，位于 `net.hasor.dbvisitor.driver` 包：

| 组件 | 类型 | 职责 |
| ------ | ------ | ------ |
| **AdapterFactory** | 接口 | 解析 JDBC URL、创建连接、提供类型支持 |
| **AdapterConnection** | 抽象类 | 管理连接生命周期、执行请求调度 |
| **AdapterRequest** | 抽象类 | 封装一次查询/操作请求（类似 Statement） |
| **AdapterReceive** | 接口 | 接收执行结果的回调（结果集、更新计数、异常） |

## AdapterFactory

适配器的入口。每个数据源对应一个 Factory 实现，通过 SPI 注册到 `AdapterManager`。

```java
public interface AdapterFactory {
    /** 适配器名称，对应 JDBC URL 中的 adapterName */
    String getAdapterName();

    /** 支持的配置属性名列表 */
    String[] getPropertyNames();

    /** 创建类型转换支持 */
    TypeSupport createTypeSupport(Properties properties);

    /** 解析 URL 和属性，创建适配器连接 */
    AdapterConnection createConnection(
        Connection owner, String jdbcUrl, Properties properties
    ) throws SQLException;
}
```

JDBC URL 格式统一为：`jdbc:dbvisitor:<adapterName>//<server?param=value>`

## AdapterConnection

适配器的核心，每个连接实例管理一个底层数据源客户端。需要实现以下关键方法：

```java
public abstract class AdapterConnection implements Closeable {

    public AdapterConnection(String jdbcUrl, String userName) { ... }

    /** 连接信息（URL、用户名、版本等） */
    public AdapterInfo getInfo();

    /** 特性开关 */
    public AdapterFeatures getFeatures();

    /** catalog / schema 管理 */
    public abstract void setCatalog(String catalog) throws SQLException;
    public abstract String getCatalog() throws SQLException;
    public abstract void setSchema(String schema) throws SQLException;
    public abstract String getSchema() throws SQLException;

    /** 暴露底层原生客户端 */
    protected <T> T unwrap(Class<T> iface) throws SQLException;

    /** 创建请求对象 */
    public abstract AdapterRequest newRequest(String sql);

    /** 执行请求并通过 receive 回调返回结果 */
    public abstract void doRequest(
        AdapterRequest request, AdapterReceive receive
    ) throws SQLException;

    /** 取消正在执行的请求 */
    public abstract void cancelRequest();

    /** 关闭连接，释放底层资源 */
    protected abstract void doClose() throws IOException;
}
```

## AdapterRequest

封装一次操作的参数和元信息：

```java
public abstract class AdapterRequest {
    private final String traceId;   // 自动生成的唯一追踪 ID
    protected boolean generatedKeys;
    protected long    maxRows;
    protected int     fetchSize;
    protected int     timeoutSec;

    // 参数映射（命名参数 → JdbcArg）
    public Map<String, JdbcArg> getArgMap();
    public void setArgMap(Map<String, JdbcArg> argMap);
}
```

每个适配器通常定义自己的 Request 子类（如 `JedisRequest`），在 `newRequest()` 中创建。

## AdapterReceive

执行结果通过回调接口逐条返回，dbvisitor-driver 的 JDBC 实现层会自动将其转换为标准 `ResultSet`：

```java
public interface AdapterReceive {
    /** 执行失败 */
    boolean responseFailed(AdapterRequest request, Throwable e);

    /** 查询结果（游标） */
    boolean responseResult(AdapterRequest request, AdapterCursor cursor);

    /** 查询结果 + 自增键 */
    boolean responseResult(
        AdapterRequest request, AdapterCursor cursor, AdapterCursor generatedKeys
    );

    /** 更新计数 */
    boolean responseUpdateCount(AdapterRequest request, long updateCount);

    /** 更新计数 + 自增键 */
    boolean responseUpdateCount(
        AdapterRequest request, long updateCount, AdapterCursor generatedKeys
    );

    /** 输出参数 */
    boolean responseParameter(
        AdapterRequest request, String paramName, String paramType, Object value
    );

    /** 执行完毕 */
    boolean responseFinish(AdapterRequest request);
}
```

## 执行流程

一次完整的查询从 JDBC API 到底层 SDK 的调用链路：

```text
应用代码 → JdbcTemplate.queryForList(sql)
         → JDBC Driver (JdbcDriver)
         → AdapterConnection.newRequest(sql)  // 创建 Request
         → AdapterConnection.doRequest(req, receive)
             ├─ Parser: SQL/命令 → AST (ANTLR4)
             ├─ Visitor: 遍历 AST，提取参数和命令结构
             ├─ Execute: 调用底层 SDK (Jedis/MongoClient/RestClient...)
             └─ Receive: 结果回调 → responseResult / responseUpdateCount
         → JDBC ResultSet ← AdapterCursor
         → dbVisitor TypeHandler 映射
         → List<User>
```

## 现有适配器

目前已实现 4 个适配器，均遵循相同模式：

| 适配器 | 底层 SDK | URL 前缀 | 解析方式 |
| -------- | --------- | --------- | --------- |
| **jdbc-redis** | Jedis | `jdbc:dbvisitor:redis//` | 命令行风格 (ANTLR4) |
| **jdbc-mongo** | MongoDB Java Driver | `jdbc:dbvisitor:mongo//` | JS Shell 风格 (ANTLR4) |
| **jdbc-elastic** | Elasticsearch RestClient | `jdbc:dbvisitor:elastic//` | JSON 风格 (ANTLR4) |
| **jdbc-milvus** | Milvus Java SDK | `jdbc:dbvisitor:milvus//` | SQL-like 风格 (ANTLR4) |

每个适配器模块的标准目录结构：

```text
jdbc-xxx/
├── src/main/antlr4/          # .g4 语法文件
├── src/main/java/.../
│   ├── XxxConnFactory.java    # AdapterFactory 实现
│   ├── XxxConn.java           # AdapterConnection 实现
│   ├── XxxCmd.java            # 底层 SDK 命令委托
│   ├── XxxRequest.java        # AdapterRequest 子类
│   ├── XxxKeys.java           # 配置键常量
│   ├── XxxCommands*.java      # 分类命令实现
│   ├── XxxDistributeCall.java # AST 遍历 → 命令分发
│   ├── CustomXxx.java         # 自定义扩展点
│   └── parser/                # ANTLR4 生成的 Lexer/Parser/Visitor
└── src/main/resources/
    └── META-INF/services/
        └── net.hasor.dbvisitor.driver.AdapterFactory  # SPI 注册
```

