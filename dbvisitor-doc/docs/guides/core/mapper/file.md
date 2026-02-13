---
id: file
sidebar_position: 4
hide_table_of_contents: true
title: 调用文件 Mapper
description: 通过 BaseMapper 接口的 executeStatement、queryStatement 方法可以执行位于 Mapper 文件中的 SQL。
---

# 调用文件 Mapper

通过 BaseMapper 接口的 `executeStatement`、`queryStatement` 方法可以执行位于 Mapper 文件中的 SQL。

:::tip[提示]
Session 的获取方式取决于项目架构，详见 **[框架整合](../../yourproject/buildtools#integration)**。
:::

```xml title='例如：Mapper 文件如下'
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//dbvisitor.net//DTD Mapper 1.0//EN"
        "https://www.dbvisitor.net/schema/dbvisitor-mapper.dtd">
<mapper namespace="user">
    <select id="listUsers" resultMap="user_resultMap">
        select * from users
    </select>
</mapper>
```

```java title='通过 BaseMapper 执行'
BaseMapper<User> mapper = session.createBaseMapper(User.class);
// "user.listUsers" 中 user 是 namespace，listUsers 是语句 Id
List<User> users = mapper.queryStatement("user.listUsers", null);
```

```java title='通过 Session 执行（效果相同，但异常类型不同）'
List<User> users = session.queryStatement("user.listUsers", null);
```

:::info[有关文件 Mapper 的详细配置，请参阅：]
- 详细请参考 [文件 Mapper](../file/about)。
:::

## 分页查询 {#page}

通过传递 `Page` 参数进行分页查询。BaseMapper 和 Session 都支持带分页的 `queryStatement`。

```java title='分页查询（返回 List）'
PageObject page = new PageObject();
page.setPageSize(20);

// BaseMapper 方式
BaseMapper<User> mapper = session.createBaseMapper(User.class);
List<User> users = mapper.queryStatement("user.listUsers", null, page);
```

```java title='分页查询（返回 PageResult，仅 Session 支持）'
PageObject page = new PageObject();
page.setPageSize(20);

PageResult<User> users = session.pageStatement("user.listUsers", null, page);
```

- `PageResult` 分页结果中还会包含 **原始分页信息**、**总记录数**、**总页数**。