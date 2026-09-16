# Solon + dbVisitor

使用 Solon 4.1.0 的 `solon-web` 启动组合和 `Solon.start()` 加载应用，`dbvisitor-solon-plugin` 自动装配数据源、Session 和 Mapper。
启动及打包方式见 [Solon 官方示例](https://solon.noear.org/article/preview)。

## 配置与启动

修改 `src/main/resources/app.yml` 中的 MySQL 连接。在本目录执行：

```bash
mvn package
java -jar target/example-solon.jar
```

也可在 IDE 中运行 `DemoApp.main()`。`solon-maven-plugin` 负责将启动类与依赖打包为可执行 JAR。

## 运行测试

```bash
mvn test
```

`@SolonTest(DemoApp.class)` 从应用入口启动，验证数据源、Session、Mapper 注入及 XML 查询。
测试使用随机 HTTP 端口和 H2 内存数据库，不访问配置中的 MySQL。
