---
id: exec_lambda
sidebar_position: 3
hide_table_of_contents: true
title: 构造器 API
description: 介绍如何使用 LambdaTemplate 进行类型安全的 MongoDB 操作。
---

# 构造器 API (LambdaTemplate)

构造器 API（也称为 Lambda API）提供了一种类型安全、流式调用的方式来操作数据库。对于 MongoDB，`LambdaTemplate` 提供了极大的灵活性，允许你通过 Java Lambda 表达式构建查询和更新条件。

## 初始化

你可以直接通过 JDBC `Connection` 创建 `LambdaTemplate` 实例。

```java
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import java.sql.Connection;

Connection conn = ...; // 获取 MongoDB 的 JDBC 连接
LambdaTemplate lambda = new LambdaTemplate(conn);
```

## CRUD 操作

### 新增 (Insert)

```java
UserInfo user = new UserInfo();
user.setUid("1001");
user.setName("test_user");

// 执行插入
int result = lambda.insert(UserInfo.class)
                   .applyEntity(user)
                   .executeSumResult();
```

### 查询 (Query)

使用 `eq`, `ne`, `gt`, `lt` 等方法构建查询条件。

```java
// 根据 ID 查询
UserInfo user = lambda.query(UserInfo.class)
                      .eq(UserInfo::getUid, "1001")
                      .queryForObject();

// 列表查询
List<UserInfo> users = lambda.query(UserInfo.class)
                             .like(UserInfo::getName, "test%")
                             .queryForList();
```

### 更新 (Update)

你可以指定更新条件和需要更新的字段。

```java
int result = lambda.update(UserInfo.class)
                   .eq(UserInfo::getUid, "1001")        // 条件：UID = 1001
                   .updateTo(UserInfo::getName, "New Name") // 更新 Name 字段
                   .doUpdate();
```

### 删除 (Delete)

```java
int result = lambda.delete(UserInfo.class)
                   .eq(UserInfo::getUid, "1001") // 条件：UID = 1001
                   .doDelete();
```

## 高级用法

`LambdaTemplate` 还支持更复杂的查询逻辑，如分组、排序、分页等，这些操作会被转换为对应的 MongoDB 命令。

```java
// 分页查询
List<UserInfo> pageList = lambda.query(UserInfo.class)
                                .eq(UserInfo::getStatus, "ACTIVE")
                                .orderBy(UserInfo::getCreateTime)
                                .usePage(0, 10) // 第一页，每页 10 条
                                .queryForList();
```

:::info
构造器 API 的优势在于它完全避免了硬编码的字符串（如字段名），利用 Java 的类型检查在编译期发现错误。
:::
