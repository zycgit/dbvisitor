---
id: about
sidebar_position: 1
title: 5.4 Mapper 文件
description: 通过 XML 文件定义 SQL，支持动态 SQL、对象/结果集映射、分页查询等。
---

# 5.4 Mapper 文件

使用 `@RefMapper` 注解将 Mapper 接口与 XML 文件关联，将全部或部分方法的 SQL 定义迁移到文件中。接口上未关联到文件的方法仍然可以使用 [方法注解](../mapper/about)（`@Query` / `@Insert` 等）。

## 先选写法

| 你的目标 | 推荐方式 |
| --- | --- |
| 常见动态条件、IN 查询、SET 片段 | 优先使用 [规则](../../rules/about)，写法更接近原生 SQL。 |
| 迁移已有 MyBatis 风格 SQL | 使用 [动态 SQL 标签](./dynamic_sql)。 |
| 配置查询结果到 DTO 的映射 | 使用 [resultMap](./result_map)。 |
| 配置表到实体类的映射 | 使用 [entity](./entity_map) 或 [对象映射注解](../mapping/about)。 |

```java title='Mapper 接口'
@RefMapper("/mapper/userMapper.xml")
public interface UserMapper {
    // 注解方式定义的 SQL
    @Insert("insert into users (id, name) values (#{id}, #{name})")
    int saveUser(User user);

    // 映射到 XML 文件中的 SQL
    List<User> listUsers(@Param("searchId") long searchId);
}
```

```xml title='userMapper.xml'
<mapper namespace="net.example.mapper.UserMapper">
    <select id="listUsers">
        select * from users where id > #{searchId}
    </select>
</mapper>
```

```java title='使用 Mapper'
Session session = config.newSession(dataSource);
UserMapper mapper = session.createMapper(UserMapper.class);
List<User> result = mapper.listUsers(2);
```

:::tip[提示]
Session 的获取方式取决于项目架构，详见 **[框架整合](../../yourproject/buildtools#integration)**。
:::

## 使用指引

- [文档结构](./document)，Mapper 文件的基本结构与文档验证。
- [文档标签](./statements)，`<select>`、`<insert>`、`<update>`、`<delete>` 等 SQL 标签（可查 [类型别名](./type_alias)）。
- [动态 SQL](./dynamic_sql)，`<if>`、`<choose>`、`<foreach>` 等标签实现动态拼接。
- [规则](../../rules/about)，通过规则赋予 SQL 更强大的动态能力。
- [映射表](./entity_map)，在 XML 中配置 Java 对象与数据库表的映射。
- [映射结果集](./result_map)，配置查询结果集到 Java 对象的映射。
- [自动映射](./auto_mapping)，简化 `<resultMap>` 或 `<entity>` 配置。
- [分页查询](./paging)，通过分页对象进行分页查询。
