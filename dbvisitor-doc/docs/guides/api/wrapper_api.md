---
id: wrapper_api
sidebar_position: 4
hide_table_of_contents: true
title: 4.4 构造器 API
description: 构造器为创建和运行数据库查询提供了方便、流畅的界面。它可以用于执行应用程序中的大多数数据库操作，并与 dbVisitor 支持的所有数据库系统完美配合。
---

# 构造器 API

dbVisitor 的 [构造器](../core/wrapper/about) 为创建和运行数据库查询提供了方便、流畅的界面。它可以用于执行应用程序中的大多数数据库操作，并与 dbVisitor 支持的所有数据库系统完美配合。

:::tip[特点]
- 注重数据库访问的通用性，无需编写 SQL 即可访问所有数据库。
:::

```java title='1. 声明实体类'
@Table("users")
public class User {
    @Column(name = "id", primary = true, keyType = KeyTypeEnum.Auto)
    private Long id;
    @Column("name")
    private String name;
    ...
}
```

```java title='2. 创建构造器'
DataSource dataSource = ...
WrapperAdapter wrapper = new WrapperAdapter(dataSource);

或者

Connection conn = ...
WrapperAdapter wrapper = new WrapperAdapter(conn);
```

```java title='3. 插入数据'
WrapperAdapter adapter = ...

User user1 = new User();
user1.setName("安妮.贝隆");
...

InsertWrapper<User> wrapper = adapter.insertByEntity(User.class);
wrapper.applyEntity(user1);
int result = wrapper.executeSumResult();
// 返回 result 为 1
```

```java title='4. 插入多条数据'
WrapperAdapter adapter = ...

User user1 = new User();
...
User user2 = new User();
...

InsertWrapper<User> wrapper = adapter.insertByEntity(User.class);
wrapper.applyEntity(user1, user2);
int result = wrapper.executeSumResult();
// 返回 result 为 2
```

```java title='5. 更新数据'
WrapperAdapter adapter = ...

User user1 = new User();
user1.setName("安妮.贝隆");
...

EntityUpdateWrapper<User> wrapper = adapter.updateByEntity(User.class);
int result = wrapper.eq(User::getId, 1)
                    .updateTo(User::getName, "阿尔坎格罗·科莱里")
                    .doUpdate();
```

```java title='6. 删除数据'
WrapperAdapter adapter = ...

EntityDeleteWrapper<User> wrapper = adapter.deleteByEntity(User.class);
int result = wrapper.eq(User::getId, 1)
                    .doDelete();
```

```java title='7. 执行查询'
WrapperAdapter adapter = ...

EntityQueryWrapper<User> wrapper = adapter.queryByEntity(User.class);
List<User> result = wrapper.eq(User::getName, "阿尔坎格罗·科莱里")
                           .eq(User::getCountry, "意大利")
                           .queryForList();
```

:::info[有关构造器 API 的详细信息，请参阅：]
- [WrapperAdapter 类](../core/wrapper/about)
- [对象映射](../core/mapping/about)
:::
