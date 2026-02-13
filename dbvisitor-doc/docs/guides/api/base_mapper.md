---
id: base_mapper
sidebar_position: 3
hide_table_of_contents: true
title: 4.3 通用 Mapper
description: 使用通用 BaseMapper 接口可以让您的程序在数据访问层变得更加精炼。
---

# BaseMapper 接口

通用 BaseMapper 接口提供了一组 **预设的数据库操作方法**，借助 [对象映射](../core/mapping/about) 信息即可完成 CRUD，**无需编写任何 SQL 语句**。

:::tip[特点]
- **零 SQL 开发**：只需定义实体映射，即获得 `insert`、`update`、`delete`、`selectById` 等全套 CRUD 方法。
- 内置 **样本查询** `listBySample` ：通过传入一个实体对象，非空属性自动作为等值查询条件。
- 内置 **分页查询** `pageBySample` ：自动计算总记录数、总页数。
- 内置 **批量操作**：`selectByIds` 批量主键查询、`insert(List)` 批量插入。
- 可通过 `mapper.query()` 切换到 [构造器 API](./lambda_api) 执行复杂条件查询。
- 可与 [声明式 API](./declarative_api) 联合使用，在同一接口中混用预设方法和自定义 SQL。
:::

## 声明实体类

```java title='使用 @Table 和 @Column 注解建立映射'
@Table("users")
public class User {
    @Column(name = "id", primary = true, keyType = KeyTypeEnum.Auto)
    private Long id;
    @Column("name")
    private String name;
    @Column("age")
    private Integer age;
    @Column("email")
    private String email;
    ...
}
```

## 创建 BaseMapper

```java title='通过 Session 创建'
Configuration config = new Configuration();
Session session = config.newSession(dataSource);
BaseMapper<User> mapper = session.createBaseMapper(User.class);
```

## 增删改查

```java title='基础 CRUD 操作'
// 插入 1 条数据
User user = ...
int result = mapper.insert(user);

// 插入多条数据
int result = mapper.insert(Arrays.asList(user1, user2));

// 根据主键查询
User loaded = mapper.selectById(1);

// 批量主键查询
List<User> users = mapper.selectByIds(Arrays.asList(1, 2, 3));

// 根据主键更新
user.setAge(30);
int result = mapper.update(user);

// 根据主键删除
int result = mapper.deleteById(1);
```

## 样本查询与分页

```java title='通过样本对象进行条件查询和分页'
// 样本查询：非空属性作为等值条件
User sample = new User();
sample.setAge(30);
List<User> result = mapper.listBySample(sample);

// 统计查询
int count = mapper.countBySample(sample);

// 分页查询
Page pageInfo = mapper.pageInitBySample(sample, 0, 20); // 页码从 0 开始
PageResult<User> result = mapper.pageBySample(sample, pageInfo);
```

## 切换到构造器 API

```java title='需要复杂条件时，可直接获取构造器'
List<User> result = mapper.query()
                          .like(User::getName, "A%")
                          .ge(User::getAge, 20)
                          .queryForList();
```

:::info[有关接口的详细信息，请参阅：]
- [BaseMapper 接口](../core/mapper/about)
- [对象映射](../core/mapping/about)
:::
