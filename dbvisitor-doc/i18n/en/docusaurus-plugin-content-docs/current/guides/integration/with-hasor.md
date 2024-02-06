---
id: with-hasor
sidebar_position: 4
title: Hasor 整合
description: dbVisitor ORM 工具和 Hasor 整合使用。
---
# 与 Hasor 集成

通过 `dbvisitor-hasor` 工具包可以更加便捷的在 Hasor 上使用 dbVisitor ORM 工具。

## 什么是 Hasor
Hasor 是一个类似 Spring 的项目，提供 IoC/Aop 和 Web 开发，它比 Spring 更加小巧，比 Guice 更加丰富。

- Hasor 项目地址：https://gitee.com/clougence/hasor

## dbvisitor-hasor 特性

- 自动给配置数据源
- 提供注解化事务控制
- 自动注入 Mapper 接口
- 支持多数据源

## 配置方法

首先引入如下依赖包：

```xml
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>dbvisitor-hasor</artifactId>
    <version>5.4.1</version>
</dependency>
```

使用 dbVisitor 可以不依赖数据库连接池，但有数据库连接池是大多数项目的标配。这里选用 HikariCP

```xml
<dependency>
    <groupId>com.zaxxer</groupId>
    <artifactId>HikariCP</artifactId>
    <version>4.0.3</version>
</dependency>
```

配置文件，如下使用了 HikariCP 后的配置文件

```properties
dbvisitor.jdbc-ds=com.zaxxer.hikari.HikariDataSource
dbvisitor.jdbc-ds.jdbc-url=jdbc:mysql://127.0.0.1:13306/devtester?allowMultiQueries=true
dbvisitor.jdbc-ds.username=root
dbvisitor.jdbc-ds.password=123456
dbvisitor.jdbc-ds.minimum-idle=5
dbvisitor.jdbc-ds.maximum-pool-size=12
dbvisitor.jdbc-ds.max-lifetime=1200000
dbvisitor.jdbc-ds.auto-commit=true
dbvisitor.jdbc-ds.connection-timeout=20000
dbvisitor.mapper-locations=classpath:dbvisitor/mapper/*.xml
dbvisitor.mapper-packages=net.hasor.dbvisitor.test.dao
```

一个 Service 类

```java
public class ServiceTest {
    @Inject
    private TestService userService;
    ...
}
```

```java
// 初始化 Hasor 并加载 DbVisitorModule 插件
AppContext appContext = Hasor.create().mainSettingWith("jdbc.properties").build(binder -> {
    binder.installModule(new DbVisitorModule());
});

// 尽情享受
ServiceTest service = appContext.getInstance(ServiceTest.class);
...
```

这里提供地址可以获取 Demo 工程

- [Example 工程](https://gitee.com/zycgit/dbvisitor/tree/main/dbvisitor-example/hasor/)

## 数据源配置

| 属性名                             | 描述                                                                                                                  |
|---------------------------------|---------------------------------------------------------------------------------------------------------------------|
| `dbvisitor.jdbc-ds`             | 可选，具体的数据源 DataSource 类型，例如 `com.zaxxer.hikari.HikariDataSource`，如果不指定则会使用工具内置的 `DefaultDataSource` 类 (它不支持连接池)      |
| `dbvisitor.jdbc-ds.xxx`         | 可选，通过 xxx 配置数据源的属性                                                                                                  |
| `dbvisitor.multiple-datasource` | 可选，多数据源情况下，的数据源标识符                                                                                                  |

工具内置了一个用于测试的，简单 `DataSource` 实现。它不支持连接池，这意味着每次都会创建一个新的链接。配置方式如下：

```properties
dbvisitor.jdbc-ds.jdbc-url=jdbc:mysql://127.0.0.1:13306/devtester?allowMultiQueries=true
dbvisitor.jdbc-ds.driver-class-name=com.mysql.cj.jdbc.Driver
dbvisitor.jdbc-ds.username=root
dbvisitor.jdbc-ds.password=123456
dbvisitor.mapper-locations=classpath:dbvisitor/mapper/*.xml
dbvisitor.mapper-packages=net.hasor.dbvisitor.test.dao
```

配置了三个数据源(one/two/three)分别链接到三个不同数据库，每个数据源都使用了内置的简单 DataSource 实现

```properties
dbvisitor.multiple-datasource=one,two,three
# -- one
dbvisitor.one.jdbc-ds=com.zaxxer.hikari.HikariDataSource
dbvisitor.one.jdbc-ds.jdbc-url=jdbc:mysql://127.0.0.1:13306/devtester?allowMultiQueries=true
dbvisitor.one.jdbc-ds.username=root
dbvisitor.one.jdbc-ds.password=123456
dbvisitor.one.jdbc-ds.minimum-idle=5
dbvisitor.one.jdbc-ds.maximum-pool-size=12
dbvisitor.one.jdbc-ds.max-lifetime=1200000
dbvisitor.one.jdbc-ds.auto-commit=true
dbvisitor.one.jdbc-ds.connection-timeout=20000
dbvisitor.one.mapper-locations=classpath:dbvisitor/mapper/role/*.xml
dbvisitor.one.mapper-packages=net.hasor.dbvisitor.test.dao.role
# -- two
dbvisitor.two.jdbc-ds=com.zaxxer.hikari.HikariDataSource
dbvisitor.two.jdbc-ds.jdbc-url=jdbc:mysql://127.0.0.1:13306/devtester?allowMultiQueries=true
dbvisitor.two.jdbc-ds.username=root
dbvisitor.two.jdbc-ds.password=123456
dbvisitor.two.jdbc-ds.minimum-idle=5
dbvisitor.two.jdbc-ds.maximum-pool-size=12
dbvisitor.two.jdbc-ds.max-lifetime=1200000
dbvisitor.two.jdbc-ds.auto-commit=true
dbvisitor.two.jdbc-ds.connection-timeout=20000
dbvisitor.two.mapper-locations=classpath:dbvisitor/mapper/user/*.xml
dbvisitor.two.mapper-packages=net.hasor.dbvisitor.test.dao.user
# -- three
dbvisitor.three.jdbc-ds=com.zaxxer.hikari.HikariDataSource
dbvisitor.three.jdbc-ds.jdbc-url=jdbc:mysql://127.0.0.1:13306/devtester?allowMultiQueries=true
dbvisitor.three.jdbc-ds.username=root
dbvisitor.three.jdbc-ds.password=123456
dbvisitor.three.jdbc-ds.minimum-idle=5
dbvisitor.three.jdbc-ds.maximum-pool-size=12
dbvisitor.three.jdbc-ds.max-lifetime=1200000
dbvisitor.three.jdbc-ds.auto-commit=true
dbvisitor.three.jdbc-ds.connection-timeout=20000
dbvisitor.three.mapper-disabled=true
```

## dbVisitor 配置项说明

| 属性名                             | 描述                                                                                                                                |
|---------------------------------|-----------------------------------------------------------------------------------------------------------------------------------|
| `dbvisitor.mapper-locations`    | 可选，要扫描加载的 Mapper 映射文件所在路径，默认值为 `dbvisitor/mapper/*.xml`                                                                           |
| `dbvisitor.mapper-packages`     | 可选，要扫描加载的 Mapper 接口定义所在的包名，如果有多个包使用 `,` 号分割                                                                                       |
| `dbvisitor.named-type-registry` | 可选，如需自定义 `TypeHandlerRegistry` 则在这里指定                                                                                             |
| `dbvisitor.named-rule-Registry` | 可选，如需自定义 `RuleRegistry` 则在这里指定                                                                                                    |
| `dbvisitor.mapper-disabled`     | 可选，使用 `true/false` 表示是否禁用 `dbvisitor.mapper-packages` 扫描到的 Mapper 接口。<br/>默认值为 false 当与某些框架合用同一个 mapper 文件时如果遇到冲突可考虑设置为 true      |
| `dbvisitor.marker-annotation`   | 可选，当 `dbvisitor.mapper-packages` 扫描到的 Mapper 接口身上标有 某个特定类型的注解时才会被认作 Mapper 接口类。默认为：`net.hasor.dbvisitor.dal.repository.DalMapper` |
| `dbvisitor.marker-interface`    | 可选，当 `dbvisitor.mapper-packages` 扫描到的 Mapper 接口实现了某个特定接口时才会被认作 Mapper 接口类。默认为：`net.hasor.dbvisitor.dal.mapper.Mapper`             |
| `dbvisitor.mapper-scope`        | 可选，Mapper Bean 所处的 Hasor 作用域，默认作用域是 `javax.inject.Singleton`                                                                      |
| `dbvisitor.auto-mapping`        | 可选，是否将类型下的所有字段都自动和数据库中的列进行映射匹配，true 表示自动。false 表示必须通过 @Column 注解声明                                                                |                                                                                                                         
| `dbvisitor.camel-case`          | 可选，表名和属性名，根据驼峰规则转换为带有下划线的表名和列名                                                                                                    |                                                                                                                        
| `dbvisitor.case-insensitive`    | 可选，强制在生成 表名/列名/索引名 时候增加标识符限定，例如：通过设置该属性来解决列名为关键字的问题。默认是 false 不设置。                                                                |                                                                                                                               
| `dbvisitor.use-delimited`       | 可选，是否对表名列名敏感，默认 true 不敏感                                                                                                          |                                                                                                                               
| `dbvisitor.dialect`             | 可选，默认使用的数据库方言                                                                                                                     |

补充说明
- 对于 `@DalMapper` 注解只可以用于注释另一个注释，因此可以使用 `@RefMapper` 或 `@SimpleMapper` 来代替。细节请参阅 **[注解化 Mapper](../dal/anno-mapper.mdx)**
- `dbvisitor.marker-annotation`、`dbvisitor.marker-interface` 两个属性配置满足其一即可
- 一个 Mapper 接口可以继承下面两个接口其一
    - net.hasor.dbvisitor.dal.mapper.Mapper （标记性接口）
    - net.hasor.dbvisitor.dal.mapper.BaseMapper （有通用方法）
