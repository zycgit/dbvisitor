---
id: about
sidebar_position: 1
title: 5.2 Mapper API
description: Mapper API 通过 Java 接口组织 DAO，适合把 SQL 调用收束到业务接口中。
---

# 5.2 Mapper API

Mapper API 用 Java 接口组织数据访问层；SQL 可以写在方法注解里，也可以放在 Mapper XML 文件中。

## 先选写法

| 你的目标 | 推荐方式 | 入口 |
| --- | --- | --- |
| SQL 较短，放在接口方法旁边最清楚 | 方法注解 | [方法注解](#method-annotations) |
| SQL 较长、动态片段多、需要集中维护 | Mapper 文件 | [调用文件 Mapper](./file_statement) |
| 单表 CRUD、按主键操作、样本查询 | BaseMapper | [BaseMapper](#base-mapper) |
| 条件组合复杂，但仍希望挂在 Mapper 接口下 | 构造器调用 | [调用构造器 API](./lambda_builder) |

:::tip
单表 CRUD 优先使用 [BaseMapper](#base-mapper)。Mapper API 负责将接口方法绑定到注解 SQL、XML SQL、BaseMapper 或构造器能力。
:::

## 最小示例

```java title='UserMapper.java'
@SimpleMapper
@RefMapper("/mapper/userMapper.xml")
public interface UserMapper {
    // 方法注解：SQL 直接写在接口方法上
    @Query("select * from users where email = #{email}")
    User selectByEmail(@Param("email") String email);

    // Mapper 文件：SQL 写在 userMapper.xml 中，id 对应方法名
    List<User> listByCondition(@Param("name") String name,
                               @Param("age") Integer age);
}
```

```xml title='mapper/userMapper.xml'
<mapper namespace="net.example.mapper.UserMapper">
    <select id="listByCondition">
        select * from users
        where 1 = 1
        @{and, name like concat('%', #{name}, '%')}
        @{and, age = #{age}}
    </select>
</mapper>
```

```java title='调用 Mapper'
Session session = config.newSession(dataSource);
UserMapper mapper = session.createMapper(UserMapper.class);

User user = mapper.selectByEmail("alice@example.com");
List<User> users = mapper.listByCondition("alice", 18);
```

:::tip
Session 的获取方式取决于项目架构，详见 [框架整合](../../yourproject/buildtools#integration)。
:::

## 和其它核心 API 的关系

| 能力 | 在 Mapper API 中怎么体现 |
| --- | --- |
| [JdbcTemplate](../jdbc/about) | Mapper 方法最终仍然是一次 SQL 执行，只是省去了手写模板调用。 |
| [参数传递](../../args/about) | Mapper 方法参数通过 `@Param`、Bean、Map 等方式绑定到 SQL。 |
| [结果接收](../../result/about) | Mapper 方法返回值决定结果如何接收，例如实体、列表、分页结果或影响行数。 |
| [Mapper 文件](../file/about) | `@RefMapper` 把接口方法映射到 XML 中的 SQL 语句。 |
| [BaseMapper](#base-mapper) | 接口可以继承 BaseMapper 获得通用 CRUD，也可以继续声明注解或 XML 方法。 |
| [构造器 API](../lambda/about) | 继承 BaseMapper 后，可以在接口默认方法中调用 `query()`、`update()` 等构造器。 |

## 方法注解 {#method-annotations}

方法注解把 SQL 写在 Mapper 接口方法上，适合 SQL 较短、语义和方法名强绑定的场景。调用方只依赖 Java 接口，不需要直接接触 `JdbcTemplate` 或 `Session` 的执行方法。

| 你要做什么 | 注解 | 说明 |
| --- | --- | --- |
| 查询并返回结果 | [@Query](./annotation_query) | 返回实体、集合、分页结果等。 |
| 插入数据 | [@Insert](./annotation_insert) | 可配合 generated keys 或 `@SelectKeySql` 回填主键。 |
| 更新数据 | [@Update](./annotation_update) | 返回影响行数。 |
| 删除数据 | [@Delete](./annotation_delete) | 返回影响行数。 |
| 执行任意 SQL | [@Execute](./annotation_execute) | 适合 DDL、多语句执行、多结果集等（不等同于 JDBC Batch）。 |
| 调用存储过程 | [@Call](./annotation_call) | 使用 CallableStatement 调用过程或函数。 |
| 复用 SQL 片段 | [@Segment](./annotation_segment) | 定义可被规则引用的片段。 |

```java title='方法注解示例'
@SimpleMapper
public interface UserMapper {
    @Query("select * from users where id = #{id}")
    User selectById(@Param("id") long id);

    @Update("update users set name = #{name} where id = #{id}")
    int updateName(@Param("id") long id, @Param("name") String name);
}
```

注解 SQL 支持 [规则](../../rules/about)，可以写条件拼接、IN 查询、SET 片段等动态逻辑。方法参数可以使用 `@Param` 命名，也可以传入 Bean、Map；方法返回值决定结果接收方式。详细能力分别见 [参数传递](../../args/about) 和 [结果接收](../../result/about)。

如果 SQL 很长、动态片段很多，或者需要集中维护 `resultMap`、`entity` 映射和动态 SQL 标签，请改用 [Mapper 文件](./file_statement)。

## BaseMapper {#base-mapper}

`BaseMapper<T>` 基于 [对象映射](../mapping/about) 自动生成单表 CRUD SQL，适合不想手写常规增删改查语句的场景。它通常作为 Mapper 接口的父接口使用，也可以通过 `session.createBaseMapper(User.class)` 直接创建。

:::info[前置条件]
BaseMapper 依赖 [对象映射](../mapping/about) 生成 SQL。没有实体映射时，请使用 [JdbcTemplate](../jdbc/about) 或 [自由 Map 模式](../map_query/freedom)。
:::

| 你要做什么 | 推荐方法 |
| --- | --- |
| 根据主键查询、删除、更新 | `selectById`、`deleteById`、`update` |
| 新增一条或多条数据 | `insert` |
| 按样本对象查询 | `listBySample`、`countBySample` |
| 单表分页查询 | `pageBySample` |
| 条件变复杂 | 从 `mapper.query()` 或 `mapper.update()` 切换到 [调用构造器 API](./lambda_builder) |

```java title='继承 BaseMapper'
@SimpleMapper
public interface UserMapper extends BaseMapper<User> {
}
```

```java title='常见 CRUD'
UserMapper mapper = session.createMapper(UserMapper.class);

int rows = mapper.insert(user);
User loaded = mapper.selectById(1L);
int updated = mapper.update(user);       // 根据主键，只更新非 null 字段
int replaced = mapper.replace(user);     // 根据主键替换整行，包括 null 字段
int deleted = mapper.deleteById(1L);
```

```java title='样本查询和分页'
User sample = new User();
sample.setStatus("ACTIVE");

List<User> users = mapper.listBySample(sample);

Page page = PageObject.of(0, 20);
PageResult<User> result = mapper.pageBySample(sample, page);
```

```java title='分页排序'
Map<String, OrderType> orderBy = new HashMap<>();
orderBy.put("id", OrderType.DESC);

Map<String, OrderNullsStrategy> nulls = new HashMap<>();
nulls.put("name", OrderNullsStrategy.FIRST);

PageResult<User> result = mapper.pageBySample(sample, page, orderBy, nulls);
```

`update` 只更新非空字段；`replace` 表示整行替换；`upsert` 表示主键不存在时新增、存在时更新。插入后的主键回填请看 [@Insert 自增主键回填](./annotation_insert#generated-keys)。

## 深入阅读

- [方法注解](#method-annotations) — 使用 `@Query`、`@Insert`、`@Update`、`@Delete` 等在接口方法上声明 SQL。
- [调用文件 Mapper](./file_statement) — Mapper 方法如何调用 XML 文件中的 SQL。
- [调用构造器 API](./lambda_builder) — 在 Mapper 接口中复用 LambdaTemplate/构造器能力。
- [@Insert 自增主键回填](./annotation_insert#generated-keys) — 插入后的自增主键回填。
