---
id: mapper
sidebar_position: 3
hide_table_of_contents: true
title: 4.3 Mapper API
description: Mapper API 通过 Java 接口组织 DAO。SQL 可以来自方法注解、BaseMapper 通用 CRUD 或 Mapper 文件。
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# 4.3 Mapper API

Mapper API 用 Java 接口组织数据访问层。**它不是单一的某种写法，而是一组 API 的统称**：同一个 Mapper 接口可以混合使用方法注解、BaseMapper 通用 CRUD 和 Mapper 文件。

## 先选写法

| 你的情况 | 推荐写法 | 说明 |
|---------|---------|------|
| SQL 短，放在接口旁边最清楚 | **方法注解** | `@Query`/`@Insert`/`@Update`/`@Delete` 声明 SQL |
| 单表 CRUD，不想写 SQL | **BaseMapper** | 继承 `BaseMapper<T>` 获得零 SQL 的增删改查 |
| 条件组合复杂，但希望挂在 Mapper 接口上 | **BaseMapper 切换** | `mapper.query()` 进入构造器 API |
| SQL 长或需要集中维护 | **Mapper 文件** | XML 集中管理 SQL，接口方法引用文件中的语句 |

## 三种常见写法

### 方法注解

```java
@SimpleMapper
public interface UserMapper {
    @Query("select * from users where id = #{id}")
    User selectById(@Param("id") long id);

    @Insert("insert into users (name, age) values (#{name}, #{age})")
    int insertUser(User user);
}
```

适合 SQL 短小的场景。详细用法：[方法注解](../core/mapper/about)

### BaseMapper（通用 CRUD）

```java
@SimpleMapper
public interface UserMapper extends BaseMapper<User> {
    // 继承后直接获得 insert / update / delete / selectById / pageBySample 等方法
}
```

```java
UserMapper mapper = session.createMapper(UserMapper.class);

// 零 SQL 的 CRUD
mapper.insert(user);
User u = mapper.selectById(1L);
List<User> users = mapper.listBySample(sample);

// 条件复杂时切换到构造器 API
List<User> result = mapper.query()
        .like(User::getName, "A%")
        .ge(User::getAge, 18)
        .queryForList();
```

适合单表 CRUD 为主的场景。详细用法：[BaseMapper 通用 CRUD](../core/mapper/about#base-mapper)

### Mapper 文件

```java
@RefMapper("/mapper/userMapper.xml")
public interface UserMapper {
    List<User> listUsers(@Param("status") String status, @Param("name") String name);
}
```

```xml title='userMapper.xml'
<mapper namespace="com.example.UserMapper">
    <select id="listUsers" resultType="com.example.User">
        select * from users
        where @{and, status = :status}
              @{and, name like concat(:name, '%')}
    </select>
</mapper>
```

适合 SQL 长、动态片段多的场景。详细用法：[文件 Mapper](./file_mapper)

## 最小示例

先从已有的 `DataSource` 或 `Connection` 创建 `Session`，再由 `Session` 创建 Mapper 接口实例。

<Tabs>
<TabItem value="datasource" label="已有 DataSource" default>

```java
DataSource dataSource = ...;

Configuration config = new Configuration();
Session session = config.newSession(dataSource);

// 注解方式
UserMapper mapper1 = session.createMapper(UserMapper.class);
User user = mapper1.selectById(1L);

// BaseMapper 方式
UserMapper mapper2 = session.createMapper(UserMapper.class); // UserMapper extends BaseMapper<User>
mapper2.insert(user);
```

</TabItem>
<TabItem value="connection" label="已有 Connection">

```java
Connection conn = ...;

Configuration config = new Configuration();
Session session = config.newSession(conn);

UserMapper mapper = session.createMapper(UserMapper.class);
User user = mapper.selectById(1L);
```

</TabItem>
</Tabs>

## 不适合场景

- 只是临时执行一两条 SQL，不需要 DAO 接口 → [编程式 API](./jdbc)
- 查询主要依赖链式条件组合，不想维护 SQL 字符串 → [构造器 API](./lambda)

## 深入阅读

- [Mapper API 核心](../core/mapper/about)：方法注解、BaseMapper、调用构造器、调用文件 Mapper 的完整说明
- [方法注解](../core/mapper/about)：`@Query`、`@Insert`、`@Update`、`@Delete`、`@Call` 等注解
- [BaseMapper CRUD](../core/mapper/about#base-mapper)：insert、update、delete、selectById、pageBySample
- [参数传递](../args/about)：接口方法参数如何绑定到 SQL
- [文件 Mapper](./file_mapper)：XML 文件结构、动态 SQL、resultMap
