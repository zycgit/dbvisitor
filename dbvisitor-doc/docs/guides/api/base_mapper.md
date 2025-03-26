---
id: base_mapper
sidebar_position: 3
hide_table_of_contents: true
title: 4.3 通用 Mapper
description: 使用通用 BaseMapper 接口可以让您的程序在数据访问层变得更加精炼。
---

# BaseMapper 接口

通用 BaseMapper 接口提供了一组常见的数据库操作方法利用对象映射信息完成对数据库的操作。通用 Mapper 可以让您的程序在数据访问层变得更加精炼。

:::tip[特点]
- 只需要建立数据库表的 [映射关系](../core/mapping/about)，便可以利用 [预设的接口](../core/mapper/about) 进行数据库操作。
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

```java title='2. 操作数据库'
// 插入 1 条数据
User user = ...
int result = mapper.insert(user);

// 插入多条数据
User user1 = ...
User user2 = ...
int result = mapper.insert(Arrays.asList(user1, user2));

// 根据对象 ID 更新数据
User user = ...
int result = mapper.update(user);

// 根据对象 ID 删除数据
int result = mapper.delete(1);

// 根据样本执行查询
User sample = ...
List<User> result = mapper.listBySample(sample);

// 根据样本执行分页查询
User sample = ...
Page pageInfo = PageObject.of(0, 20);   // 第 0 页每页 20条，页码从 0 开始
//or pageInfo = PageObject.of(1, 20, 1);// 第 1 页每页 20条，页码从 1 开始
PageResult<User> result = mapper.pageBySample(sample, pageInfo);
```

:::info[有关接口的详细信息，请参阅：]
- [BaseMapper 接口](../core/mapper/about)
- [对象映射](../core/mapping/about)
:::
