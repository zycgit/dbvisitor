---
id: sql_build
sidebar_position: 5
title: 仅生成 SQL
description: dbVisitor ORM 单表模式是围绕 LambdaTemplate 工具类展开，它继承自 JdbcTemplate 具备后者的所有能力。
---

# 仅生成 SQL

本节是 **[条件构造器](../crud/where-build.md)** 的一个补充，这是 dbVisitor 一个最具特色的功能。

允许仅仅生成 SQL 但不执行它，因此在使用这个功能的时候可以不需要指定任何数据库

```java title='查询所有年龄为 32 的用户'
LambdaTemplate lambdaTemplate = new LambdaTemplate();
EntityQueryOperation<TestUser> query = lambdaTemplate.lambdaQuery(TestUser.class);
BoundSql boundSql = query.eq(TestUser::getAge, 32).getBoundSql();

String sqlString = boundSql.getSqlString();
Object[] sqlArgs = boundSql.getArgs();
```
