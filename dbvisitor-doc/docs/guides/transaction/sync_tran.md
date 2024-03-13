---
id: sync_tran
sidebar_position: 1
title: 资源同步
description: dbVisitor ORM 在同一个 DataSource 上同时使用多个事务就需要涉及到 DataSource 资源同步。
---

# 资源同步

如果在同一个 `DataSource` 上同时使用多个事务就需要涉及到 DataSource 资源同步问题。举个简单的例子：

```java {5,11}
DataSource dataSource = DsUtils.dsMySql();
Connection conn = dataSource.getConnection();
conn.setAutoCommit(false);

JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
// do something conn

conn.commit();

// do something conn
JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);

conn.commit();
conn.close();
```

再比如在同一个 `DataSource` 上开启两个相互独立的事务：

```java {5,11}
DataSource dataSource = DsUtils.dsMySql();
Connection tranA = dataSource.getConnection();

tranA.setAutoCommit(false);
JdbcTemplate jdbcTemplate = new JdbcTemplate(tranA);
// do something with tranA

Connection tranB = dataSource.getConnection();
tranB.setAutoCommit(false);

JdbcTemplate jdbcTemplate = new JdbcTemplate(tranB);
// do something with tranB

tranB.commit();
tranA.commit();
```

使用上面这种方式需要在整个调用链上传递 `Connection` 以确保不同的业务处理逻辑用到相同的数据库连接。
若 `Connection` 维护不当就会造成链接泄漏，而这种泄漏通常比较难以发现和定位的。

## 本地同步 {#sync}

dbVisitor 内置了资源管理器，可以用来同步上述这种对 `Connection` 的依赖，但同时又不需要将其作为参数传递，例如：上面两个例子可以换成如下：

例1：

```java {5,11}
DataSource dataSource = DsUtils.dsMySql();
TransactionManager manager = DataSourceManager.getManager(dataSource);

manager.begin();
JdbcTemplate jdbcTemplate1 = new JdbcTemplate(dataSource);
// do something conn

manager.commit();

manager.begin();
JdbcTemplate jdbcTemplate2 = new JdbcTemplate(dataSource);
// do something conn

manager.commit();
```

例2：

```java {5,9}
DataSource dataSource = DsUtils.dsMySql();
TransactionManager manager = DataSourceManager.getManager(dataSource);

manager.begin(); // tranA
JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
// do something with tranA

manager.begin(); // tranB
JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
// do something with tranB

manager.commit(); // tranB
manager.commit(); // tranA
```

## 高级 API

首先的方式是使用 dbVisitor 数据库操作 API，比如 `DalSession`、`JdbcTemplate`、`LambdaTemplate`、`@Transactional`

这些 API 在执行数据库操作时会自动处理资源的创建、重用以及清理。我们无需关心这些具体过程。

## 低级 API {#low-api}

低级别 API 的特点是缺少了那些自动化的管理工作，比如在使用数据库连接时需要主动释放它。

```java
Connection conn = DataSourceManager.getConnection(dataSource);
// do something
conn.close();
```

连接复用是在低级别 API 上提供支持的能力，它让 `DataSource` 与当前线程形成绑定，并提供一个连接复用的 `Connection` 对象。

```java
Connection conn1 = DataSourceManager.getConnection(dataSource); // new connection
Connection conn2 = DataSourceManager.getConnection(dataSource); // ref ++
Connection conn3 = DataSourceManager.getConnection(dataSource); // ref ++

// do something

conn3.close(); // ref --
conn2.close(); // ref --
conn1.close(); // real close connection
```

:::tip
上面 `conn1`、`conn2`、`conn3` 实际使用的是同一个 `Connection` 对象。
:::
