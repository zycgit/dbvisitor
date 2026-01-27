---
id: file
sidebar_position: 4
hide_table_of_contents: true
title: 调用文件 Mapper
description: 通过 BaseMapper 接口的 executeStatement、queryStatement 方法可以执行位于 Mapper 文件中的 SQL。
---

# 调用文件 Mapper

通过 BaseMapper 接口的 executeStatement、queryStatement 方法可以执行位于 Mapper 文件中的 SQL。

你可以根据您的项目架构选择合适的方式获取 Session，详细信息请参考：**[框架整合](../../yourproject/buildtools#integration)**

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

```java title='示例：执行 Mapper 中的 SQL'
// user 表示 namespace
// listUsers 表示 Mapper File 中的语句 Id
List<User> users = session.queryStatement("user.listUsers");
```

:::info[有关文件 Mapper 的详细配置，请参阅：]
- 详细请参考 [文件 Mapper](../file/about)。
:::

## 分页查询 {#page}

通过 Session 对象的 queryStatement、pageStatement 重载方法，在传递 Page 参数的方式进行分页查询。

```java
PageObject page = new PageObject();
page.setPageSize(20);

List<User> users = session.queryStatement("user.listUsers", null, page);
```

```java
PageObject page = new PageObject();
page.setPageSize(20);

PageResult<User> users = session.pageStatement("user.listUsers", null, page);
```

- PageResult 分页结果中还会包含 **原始分页信息**、**总记录数**、**总页数**。