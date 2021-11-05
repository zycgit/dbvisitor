# JDBC 框架

&emsp;&emsp;HasorDB 是一款基于 jdbc 的数据库访问框架，相当于 MyBatis、SpringJDBC 功能整合但又不依赖它们。

* Project Home: [https://www.hasor.net](https://www.hasor.net)
* [![QQ群:193943114](https://img.shields.io/badge/QQ%E7%BE%A4-193943114-orange)](https://qm.qq.com/cgi-bin/qm/qr?k=0ZqU8WlKVENanH6ajgpbVua7WJUMOKQ9&jump_from=webapi)
  [![Gitter](https://badges.gitter.im/hasor/hasor-dataql.svg)](https://gitter.im/hasor/hasor-dataql?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
  [![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
  [![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.hasor/hasor-core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.hasor/hasor-core)
  [![Build Status](https://travis-ci.org/zycgit/hasor.svg?branch=master)](https://travis-ci.org/zycgit/hasor)

## 功能

- 多种方式
    - 基于 Xml 配置 SQL（高度兼容 MyBatis）
    - 基于 Lambda 的 CURD（类似于 MyBatis Plus）
    - 基于 JdbcTemplate 接口方式（高度兼容 Spring JDBC）
    - 基于 @Insert、@Update、@Delete、@Query、@Callable 注解（类似 JPA）

- 事务控制
    - 支持嵌套事务
    - 5个隔离级别
    - 7个传播特性
    - TransactionTemplate 模板事务

- 功能特性
    - 分页查询
    - 支持三种 insert duplicate key 策略（INTO、UPDATE、IGNORE）
    - 多种数据库方言
    - 全面支持 JDBC 4.2 各种数据类型
    - 全面支持 Java8 中的各种时间类型
    - 支持多数据源（不支持分布式事务）

----------

## 方言能力表

----------

## 样例

```java
JdbcTemplate jdbcTemplate=new JdbcTemplate();
```

## 源码说明

- docker-compose.yml（MySQL、PG、Oracle、MSSQL）
