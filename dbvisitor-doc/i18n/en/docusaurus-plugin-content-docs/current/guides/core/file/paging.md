---
id: paging
sidebar_position: 7
title: Paging Queries
description: How to paginate once a mapper interface is linked to a mapper XML file.
---

# Paging Queries

After linking a mapper interface to a mapper XML via `@RefMapper`, add a paging object argument to enable pagination.

```java title='Mapper methods with paging arguments'
@RefMapper("/mapper/userMapper.xml")
public interface UserMapper {
    List<User> listUsers1(@Param("searchId") long searchId, Page page);

    PageResult<User> listUsers2(@Param("searchId") long searchId, Page page);
}
```

```java title='Paging: return a list'
Page pageInfo = PageObject.of(0, 20);   // page 0, size 20 (0-based)
//or pageInfo = PageObject.of(1, 20, 1);// page 1, size 20 (1-based)

UserMapper userMapper = ...
List<User> result = userMapper.listUsers1(123, pageInfo);
```

```java title='Paging: return a PageResult'
Page pageInfo = PageObject.of(0, 20);   // page 0, size 20 (0-based)
//or pageInfo = PageObject.of(1, 20, 1);// page 1, size 20 (1-based)

UserMapper userMapper = ...
PageResult<User> result = userMapper.listUsers2(123, pageInfo);
```

- `PageResult` also contains the **original paging info**, **total rows**, and **total pages**.
