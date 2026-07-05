---
id: about
sidebar_position: 1
title: 5.2 Mapper API
description: Mapper API organizes DAO layers through Java interfaces, ideal for binding SQL calls to business interfaces.
---

# 5.2 Mapper API

Mapper API organizes data access layers using Java interfaces. SQL can be written in method annotations or placed in Mapper XML files.

## Choose a Style First

| Your Goal | Recommended Style | Entry |
| --- | --- | --- |
| SQL is short, clearest next to the method | Method annotations | [Method Annotations](#method-annotations) |
| SQL is long, has many dynamic fragments, needs centralized maintenance | Mapper files | [Call File Mapper](./file_statement) |
| Single-table CRUD, primary-key operations, sample queries | BaseMapper | [BaseMapper](#base-mapper) |
| Conditions are complex but should stay under a Mapper interface | Fluent calls | [Call Fluent API](./lambda_builder) |

:::tip
For single-table CRUD, prefer [BaseMapper](#base-mapper). Mapper API binds interface methods to annotation SQL, XML SQL, BaseMapper, or Fluent capabilities.
:::

## Minimal Example

```java title='UserMapper.java'
@SimpleMapper
@RefMapper("/mapper/userMapper.xml")
public interface UserMapper {
    // Method annotation: SQL written directly on the interface method
    @Query("select * from users where email = #{email}")
    User selectByEmail(@Param("email") String email);

    // Mapper file: SQL written in userMapper.xml, id matches method name
    List<User> listByCondition(@Param("name") String name,
                               @Param("age") Integer age);
}
```

```xml title='mapper/userMapper.xml'
<mapper namespace="net.example.mapper.UserMapper">
    <select id="listByCondition">
        select * from users
        where 1 = 1
        @{and, name is not null, "name like concat('%', #{name}, '%')"}
        @{and, age is not null, "age = #{age}"}
    </select>
</mapper>
```

```java title='Using the Mapper'
Session session = config.newSession(dataSource);
UserMapper mapper = session.createMapper(UserMapper.class);

User user = mapper.selectByEmail("alice@example.com");
List<User> users = mapper.listByCondition("alice", 18);
```

:::tip
How to obtain a Session depends on your project architecture. See [Framework Integration](../../yourproject/buildtools#integration).
:::

## Relationship to Other Core APIs

| Capability | How It Appears in Mapper API |
| --- | --- |
| [JdbcTemplate](../jdbc/about) | A Mapper method ultimately executes SQL; you just skip writing template calls manually. |
| [Parameter Passing](../../args/about) | Mapper method parameters bind to SQL via `@Param`, Bean, Map, etc. |
| [Result Reception](../../result/about) | The Mapper method return type determines how results are received — entity, list, page result, or affected rows. |
| [Mapper File](../file/about) | `@RefMapper` maps interface methods to SQL statements in XML. |
| [BaseMapper](#base-mapper) | An interface can extend BaseMapper for common CRUD and still declare annotation or XML methods. |
| [Fluent API](../lambda/about) | After extending BaseMapper, default methods can call `query()`, `update()`, and other Fluent builders. |

## Method Annotations {#method-annotations}

Method annotations place SQL on Mapper interface methods. They work best when SQL is short and semantically bound to the method name. Callers depend only on the Java interface and never touch `JdbcTemplate` or `Session` execution methods directly.

| What You Want to Do | Annotation | Notes |
| --- | --- | --- |
| Query and return results | [@Query](./annotation_query) | Returns entity, collection, page result, etc. |
| Insert data | [@Insert](./annotation_insert) | Can work with generated keys or `@SelectKeySql` for primary key write-back. |
| Update data | [@Update](./annotation_update) | Returns affected rows. |
| Delete data | [@Delete](./annotation_delete) | Returns affected rows. |
| Execute arbitrary SQL | [@Execute](./annotation_execute) | Suitable for DDL, batch execution, multiple result sets, etc. |
| Call a stored procedure | [@Call](./annotation_call) | Uses CallableStatement to call procedures or functions. |
| Reuse SQL fragments | [@Segment](./annotation_segment) | Defines fragments that can be referenced by rules. |

```java title='Method Annotation Example'
@SimpleMapper
public interface UserMapper {
    @Query("select * from users where id = #{id}")
    User selectById(@Param("id") long id);

    @Update("update users set name = #{name} where id = #{id}")
    int updateName(@Param("id") long id, @Param("name") String name);
}
```

Annotation SQL supports [rules](../../rules/about), including conditional concatenation, IN queries, SET fragments, and other dynamic logic. Method parameters can be named with `@Param`, or passed as Bean/Map. The method return type determines result reception. See [Parameter Passing](../../args/about) and [Result Reception](../../result/about) for details.

If SQL is long, has many dynamic fragments, or needs centralized `resultMap`, `entity` mapping, and dynamic SQL tags, use [Mapper files](./file_statement) instead.

## BaseMapper {#base-mapper}

`BaseMapper<T>` auto-generates single-table CRUD SQL from [Object Mapping](../mapping/about). It is ideal when you don't want to hand-write common CRUD statements. It is usually used as a parent interface for Mapper interfaces, and can also be created directly via `session.createBaseMapper(User.class)`.

:::info[Prerequisite]
BaseMapper depends on [Object Mapping](../mapping/about) to generate SQL. Without entity mapping, use [JdbcTemplate](../jdbc/about) or [Freedom Map Mode](../map_query/freedom).
:::

| What You Want to Do | Recommended Method |
| --- | --- |
| Query, delete, or update by primary key | `selectById`, `deleteById`, `update` |
| Insert one or multiple rows | `insert` |
| Query by sample object | `listBySample`, `countBySample` |
| Single-table pagination query | `pageBySample` |
| Conditions become complex | Switch from `mapper.query()` or `mapper.update()` to [Call Fluent API](./lambda_builder) |

```java title='Extending BaseMapper'
@SimpleMapper
public interface UserMapper extends BaseMapper<User> {
}
```

```java title='Common CRUD'
UserMapper mapper = session.createMapper(UserMapper.class);

int rows = mapper.insert(user);
User loaded = mapper.selectById(1L);
int updated = mapper.update(user);       // Updates non-null fields by primary key
int replaced = mapper.replace(user);     // Replaces entire row by primary key, including null fields
int deleted = mapper.deleteById(1L);
```

```java title='Sample Query and Pagination'
User sample = new User();
sample.setStatus("ACTIVE");

List<User> users = mapper.listBySample(sample);

Page page = PageObject.of(0, 20);
PageResult<User> result = mapper.pageBySample(sample, page);
```

```java title='Pagination with Sorting'
Map<String, OrderType> orderBy = new HashMap<>();
orderBy.put("id", OrderType.DESC);

Map<String, OrderNullsStrategy> nulls = new HashMap<>();
nulls.put("name", OrderNullsStrategy.FIRST);

PageResult<User> result = mapper.pageBySample(sample, page, orderBy, nulls);
```

`update` only writes non-null fields; `replace` means whole-row replacement; `upsert` means insert if the primary key does not exist, update if it does. For primary key write-back after insert, see [@Insert Generated Keys](./annotation_insert#generated-keys).

## Further Reading

- [Method Annotations](#method-annotations) — Using `@Query`, `@Insert`, `@Update`, `@Delete`, etc. to declare SQL on interface methods.
- [Call File Mapper](./file_statement) — How Mapper methods call SQL in XML files.
- [Call Fluent API](./lambda_builder) — Reusing LambdaTemplate/Fluent capabilities within Mapper interfaces.
- [@Insert Generated Keys](./annotation_insert#generated-keys) — Writing back auto-generated primary keys after INSERT.
