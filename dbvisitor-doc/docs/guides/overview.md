---
id: overview
sidebar_position: 1
title: 介绍
description: dbVisitor 是一个轻量小巧的数据库 ORM 工具，提供对象映射、丰富的类型处理、动态SQL、存储过程、内置分页方言20+。支持嵌套事务、多数据源、条件构造器、INSERT 策略、多语句/多结果。并兼容 Spring 及 MyBatis 用法。
---
# 介绍

dbVisitor 是一个轻量小巧的数据库 ORM 工具，提供对象映射、丰富的类型处理、动态SQL、存储过程、 内置分页方言20+、
支持嵌套事务、多数据源、条件构造器、INSERT 策略、多语句/多结果。并兼容 Spring 及 MyBatis 用法。
它不依赖任何其它框架，因此可以很方便的和任意一个框架整合在一起使用。

## 功能特性

- 熟悉的方式
    - JdbcTemplate 接口方式（高度兼容 Spring JDBC）
    - Mapper 文件方式（高度兼容 MyBatis）
    - LambdaTemplate （高度接近 MyBatis Plus、jOOQ 和 BeetlSQL）
    - @Insert、@Update、@Delete、@Query、@Callable 注解（类似 JPA）

- 事务支持
    - 支持 5 个事务隔离级别、7 个事务传播行为（与 Spring tx 相同）
    - 提供 TransactionTemplate、TransactionManager 接口方式声明式事务控制能力（用法与 Spring 相同）

- 特色优势
    - 支持 分页查询 并且提供多种数据库方言（20+）
    - 支持 INSERT 策略（INTO、UPDATE、IGNORE）
    - 更加丰富的 TypeHandler（MyBatis 40+，dbVisitor 60+）
    - Mapper XML 支持多语句、多结果
    - 提供独特的规则机制，让动态 SQL 更加简单
    - 支持 存储过程
    - 支持 JDBC 4.2 和 Java8 中时间类型
    - 支持多数据源

## 同类工具

**Hibernate**
诞生于 2001 年由 Gavin King 发布第一个版本。它是 ORM 领域的标志性工具，在此之前 ORM 实践均是通过 EJB 来完成。
Hibernate 的价值在于它终结了由 EJB 所主导的 ORM 使用习惯，并开创了以 轻量化ORM 和 SpringJDBC 的新生态。同时它推动了 EJB3、和 JPA 规范的建立。

- https://hibernate.org/


**SpringJDBC**
从 Spring 框架推出就存在于 Spring 体系之内至今如此。它比 Hibernate 更加轻量和敏捷，它独特的通过编码的方式将 SQL 和程序结合在一起，使用起来十分轻巧。
除此之外 SpringJDBC 是第一个提出了 7 种事务传播行为。

- https://spring.io/


**MyBatis**
是一款非常棒的数据库访问框架，它虽然不具备 Hibernate 强大的 ORM 能力。但别具风格的 Mapper 文件，完美的解决了动态 SQL 编写和管理上的难题。
本质上来讲 MyBatis 是 SpringJDBC 和 Hibernate 之间的一个折中方案。对于研发管理更加友好。

围绕 MyBatis 涌现出了 MyBatisPlus、MyBatis-Spring 等家喻户晓的工具，前者基于 MyBatis 进行了更多扩展的封装、后者整合了 Spring 提供更加友好的开发体验。

- https://blog.mybatis.org/
