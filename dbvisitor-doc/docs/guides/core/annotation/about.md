---
id: about
sidebar_position: 1
hide_table_of_contents: true
title: 方法注解
description: 通过在 Mapper 接口方法上标记注解实现 SQL 与调用逻辑分离，简化数据库访问。
---

# 方法注解

通过在 Mapper 接口方法上标记注解，将 **SQL 与调用逻辑分离**，无需编写 JDBC 模板代码。

```java title='定义 Mapper 接口'
@SimpleMapper
public interface UserMapper {
    @Query("select * from users where id > ?")
    List<User> listUsers(long searchId);
}
```

```java title='使用 Mapper'
Session session = config.newSession(dataSource);
UserMapper mapper = session.createMapper(UserMapper.class);
List<User> result = mapper.listUsers(2);
```

:::tip[提示]
Session 的获取方式取决于项目架构，详见 **[框架整合](../../yourproject/buildtools#integration)**。
:::

## 注解清单

| 注解 | 用途 | 示例场景 |
|------|------|--------|
| [@Query](./query) | 执行查询语句并返回结果集 | SELECT 查询、分页查询 |
| [@Insert](./insert) | 执行 INSERT 语句 | 新增记录、获取自增主键 |
| [@Update](./update) | 执行 UPDATE 语句 | 修改记录 |
| [@Delete](./delete) | 执行 DELETE 语句 | 删除记录 |
| [@Execute](./execute) | 执行任意语句（DML/DDL） | 建表、批量操作、多结果集 |
| [@Call](./call) | 调用存储过程 | 存储过程/函数调用 |
| [@Segment](./segment) | 定义可复用的 SQL 片段 | 通过 `@{macro,...}` 规则引用 |

## 通用说明

- 所有接受 SQL 的注解（`value` 属性）均支持 **字符串数组**，数组元素会以空格连接，方便多行书写
- SQL 中可以使用 [规则](../../rules/about) 赋予动态能力（条件拼接、IN 查询等）
- `statementType` 属性的可选值为 `Statement`、`Prepared`（默认）、`Callable`
