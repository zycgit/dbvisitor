# Hasor Boot + dbVisitor

使用 Hasor Boot 5.2.0 启动应用，通过 `AutoConfigModule` 创建数据源、装配 Session 并注册 Mapper。

## 配置与启动

- `single-ds.properties`：单数据源，默认使用此配置。
- `multi-ds.properties`：三个命名数据源，分别演示 Mapper 和 Session 注入。

先修改配置中的 MySQL 地址、数据库名和账号，再在本目录执行：

```bash
mvn package
java -jar target/example-hasor-1.0.0.jar
```

使用多数据源配置：

```bash
java -jar target/example-hasor-1.0.0.jar multi-ds.properties
```

IDE 中运行 `DemoApplication.main()` 即可启动。应用通过 `Boot` 加载配置和模块，并在退出时关闭容器。

## 运行测试

```bash
mvn test
```

两个测试都使用 Hasor Boot 加载 `src/test/resources` 下的配置，覆盖 Mapper 注入与 XML 查询：

- `Demo1ApplicationTests`：使用 `single-ds-test.properties`，演示单数据源。
- `Demo2ApplicationTests`：使用 `multi-ds-test.properties`，演示多数据源。

测试配置使用 H2 内存数据库，不访问应用配置中的 MySQL。
