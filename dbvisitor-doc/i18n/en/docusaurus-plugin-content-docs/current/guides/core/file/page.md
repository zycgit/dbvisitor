---
id: page
sidebar_position: 6
hide_table_of_contents: true
title: 分页查询
description: 将 Mapper 接口和 Mapper File 建立联系后如何进行分页查询？
---

使用 @RefMapper 将 Mapper 接口和 Mapper File 建立联系后，可以通过增加分页对象参数来开启分页查询。

```java title='带有分页参数的 Mapper 方法'
@RefMapper("/mapper/userMapper.xml")
public interface UserMapper {
    List<User> listUsers1(@Param("searchId") long searchId, Page page);

    PageResult<User> listUsers2(@Param("searchId") long searchId, Page page);
}
```

```java title='分页查询：返回分页集合'
Page pageInfo = PageObject.of(0, 20);   // 第 0 页每页 20条，页码从 0 开始
//or pageInfo = PageObject.of(1, 20, 1);// 第 1 页每页 20条，页码从 1 开始

UserMapper userMapper = ...
List<User> result = userMapper.listUsers1(123, pageInfo);
```

```java title='分页查询：返回分页对象'
Page pageInfo = PageObject.of(0, 20);   // 第 0 页每页 20条，页码从 0 开始
//or pageInfo = PageObject.of(1, 20, 1);// 第 1 页每页 20条，页码从 1 开始

UserMapper userMapper = ...
PageResult<User> result = userMapper.listUsers2(123, pageInfo);
```

- PageResult 分页结果中还会包含 **原始分页信息**、**总记录数**、**总页数**。
