---
id: exec_mapper
sidebar_position: 4
hide_table_of_contents: true
title: BaseMapper 接口
description: 介绍如何使用 BaseMapper 接口简化 MongoDB 的 CRUD 操作。
---

# BaseMapper 接口

`BaseMapper` 是 dbVisitor 提供的一个通用 Mapper 接口，它内置了常用的 CRUD 方法。通过继承该接口，你可以直接获得对 MongoDB 集合的基本操作能力，而无需编写任何 SQL 或 Mongo 命令。

## 定义 Mapper

定义一个接口继承 `BaseMapper<T>`，其中 `T` 是你的实体类。并使用 `@SimpleMapper` 注解标记该接口。

```java title='定义 Mapper 接口'
@SimpleMapper
public interface UserInfoMapper extends BaseMapper<UserInfo> {
    // 可以在此添加自定义的 Mapper 方法
}
```

## 使用 Mapper

通过 `Session` 创建 Mapper 实例，然后直接调用内置方法。

```java title='使用 Mapper 进行 CRUD'
// 初始化 Session (通常由框架管理)
Session session = ...; 

UserInfoMapper mapper = session.createMapper(UserInfoMapper.class);

// 1. 新增
UserInfo user = new UserInfo();
user.setUid(UUID.randomUUID().toString());
user.setName("mapper_user");
mapper.insert(user);

// 2. 查询
UserInfo loadedUser = mapper.selectById(user.getUid());

// 3. 更新
loadedUser.setName("updated_name");
mapper.update(loadedUser); // 根据主键更新非空字段

// 4. 删除
mapper.deleteById(user.getUid());
```

## 支持的方法

`BaseMapper` 提供了丰富的方法，包括但不限于：

- `insert(T entity)`: 插入一条数据。
- `deleteById(Serializable id)`: 根据主键删除。
- `update(T entity)`: 根据主键更新实体（通常只更新非空字段）。
- `selectById(Serializable id)`: 根据主键查询。
- `listBySample(T sample)`: 根据样本对象查询列表。

:::tip
在使用 `BaseMapper` 时，请确保你的实体类正确配置了 `@Table` 和 `@Column` 注解，特别是主键的配置。
:::
