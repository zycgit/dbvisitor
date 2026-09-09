---
id: mapper
sidebar_position: 3
hide_table_of_contents: true
title: 4.3 Mapper API
description: Mapper API organizes DAO code with Java interfaces. SQL can come from Method Annotations, BaseMapper common CRUD, or Mapper files.
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# 4.3 Mapper API

Mapper API organizes the data access layer with Java interfaces. **It is not a single coding style, but a collective term for a family of APIs**: the same Mapper interface can mix Method Annotations, BaseMapper common CRUD, and Mapper files.

## Pick Your Approach

| Your situation | Recommended approach | Notes |
|---|---|---|
| SQL is short and clearest next to the interface | **Method Annotations** | `@Query` / `@Insert` / `@Update` / `@Delete` to declare SQL |
| Single-table CRUD, don't want to write SQL | **BaseMapper** | Extend `BaseMapper<T>` for zero-SQL CRUD |
| Complex condition combinations, but want to stay on the Mapper interface | **BaseMapper switch** | `mapper.query()` to enter the Fluent API |
| SQL is long or needs centralized maintenance | **Mapper File** | XML centralizes SQL management; interface methods reference statements in the file |

## Three Common Styles

### Method Annotations

```java
@SimpleMapper
public interface UserMapper {
    @Query("select * from users where id = #{id}")
    User selectById(@Param("id") long id);

    @Insert("insert into users (name, age) values (#{name}, #{age})")
    int insertUser(User user);
}
```

Best for short SQL. Detailed usage: [Method Annotations](../core/mapper/about)

### BaseMapper (Common CRUD)

```java
@SimpleMapper
public interface UserMapper extends BaseMapper<User> {
    // Inherits insert / update / delete / selectById / pageBySample and more directly
}
```

```java
UserMapper mapper = session.createMapper(UserMapper.class);

// Zero-SQL CRUD
mapper.insert(user);
User u = mapper.selectById(1L);
List<User> users = mapper.listBySample(sample);

// Switch to Fluent API when conditions get complex
List<User> result = mapper.query()
        .likeRight(User::getName, "A")
        .ge(User::getAge, 18)
        .queryForList();
```

Best for single-table CRUD scenarios. Detailed usage: [BaseMapper Common CRUD](../core/mapper/about#base-mapper)

### Mapper File

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
        @{and, status = :status}
              @{and, name like concat(:name, '%')}
    </select>
</mapper>
```

Best for long SQL with many dynamic fragments. Detailed usage: [File Mapper](./file_mapper)

## Minimal Example

Start by creating a `Session` from an existing `DataSource` or `Connection`, then use the `Session` to create Mapper interface instances.

<Tabs>
<TabItem value="datasource" label="With DataSource" default>

```java
DataSource dataSource = ...;

Configuration config = new Configuration();
Session session = config.newSession(dataSource);

// Annotation style
UserMapper mapper1 = session.createMapper(UserMapper.class);
User user = mapper1.selectById(1L);

// BaseMapper style
UserMapper mapper2 = session.createMapper(UserMapper.class); // UserMapper extends BaseMapper<User>
mapper2.insert(user);
```

</TabItem>
<TabItem value="connection" label="With Connection">

```java
Connection conn = ...;

Configuration config = new Configuration();
Session session = config.newSession(conn);

UserMapper mapper = session.createMapper(UserMapper.class);
User user = mapper.selectById(1L);
```

</TabItem>
</Tabs>

## Not Best For

- Just executing one-off SQL statements without needing a DAO interface → [Programmatic API](./jdbc)
- Queries rely mainly on chainable condition composition and you don't want to maintain SQL strings → [Fluent API](./lambda)

## Learn More

- [Mapper API Core](../core/mapper/about): complete guide to Method Annotations, BaseMapper, invoking the builder, and invoking File Mapper
- [Method Annotations](../core/mapper/about): `@Query`, `@Insert`, `@Update`, `@Delete`, `@Call` and other annotations
- [BaseMapper CRUD](../core/mapper/about#base-mapper): insert, update, delete, selectById, pageBySample
- [Parameter Passing](../args/about): how interface method parameters bind to SQL
- [File Mapper](./file_mapper): XML file structure, dynamic SQL, resultMap
