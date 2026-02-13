---
id: interface
sidebar_position: 6
title: 6.5 接口方式
hide_table_of_contents: true
description: dbVisitor 提供了 SqlArgSource、PreparedStatement 两种接口方式进行传参。
---

# 接口方式

dbVisitor 提供了两种接口方式进行传参。
- [SqlArgSource](./interface#source)，参数容器
- [PreparedStatement](./interface#pset)，JDBC 原生方式

## SqlArgSource {#source}

`SqlArgSource` 接口定义了参数获取的通用功能，通过该接口可以为带有参数的 SQL 提供参数值。

- **ArraySqlArgSource**，将数组作为参数数据源。
- **BeanSqlArgSource**，将 Bean 作为参数数据源。
- **MapSqlArgSource**，将 Map 作为参数数据源。

:::info
下面案例只是用于展示不同种类 SqlArgSource 的功能作用。
:::

```java title='例1：ArraySqlArgSource 使用位置参数'
Object[] array = new Object[] { 2, "Dave"};
SqlArgSource source = new ArraySqlArgSource(array);

jdbcTemplate.queryForList("select * from users where id > ? and name = ?", source);
```

```java title='例2：ArraySqlArgSource 使用名称化位置参数'
Object[] array = new Object[] { 2, "Dave"};
SqlArgSource source = new ArraySqlArgSource(array);

jdbcTemplate.queryForList("select * from users where id > :arg0 and name = :arg1", source);
```

```java title='例3：BeanSqlArgSource 使用 Bean 传递参数'
User user = new User(2, "Dave");
SqlArgSource source = new BeanSqlArgSource(user);

jdbcTemplate.queryForList("select * from users where id > :id and name = :name", source);
```

```java title='例4：MapSqlArgSource 使用 Map 传递参数'
Map<String, Object> map = Collections.singletonMap("id", 40);
SqlArgSource source = new MapSqlArgSource(map);

jdbcTemplate.queryForList("select * from users where id > :id", source);
```

## PreparedStatement {#pset}

通过 `PreparedStatementSetter` 接口可以直接使用 JDBC 底层 PreparedStatement 对象进行参数设置。

:::warning
当使用 `PreparedStatementSetter` 接口进行参数设置时，SQL 语句中将只能包含 “?” 问号以下几种参数传递写法均不支持。
- 名称参数：`:name` 或 `&name` 或 `#{...}`
- SQL 注入：`${...}`
- 规则用法：`@{...}`
:::

```java title='使用 PreparedStatement'
String sql = "select * from users where id > ?";
jdbcTemplate.queryForList(sql, new PreparedStatementSetter() {
    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
        ps.setInt(1, 2);
    }
});
```

## @Param 注解 {#param}

在声明式 API（[方法注解](../core/annotation/about)）中，通过 `@Param` 注解为方法参数指定名称，使其可以在 SQL 中通过名称引用。

```java title='例1：使用 @Param 命名参数'
@Query("select * from users where id > #{id} and name = #{name}")
List<User> findUsers(@Param("id") int id, @Param("name") String name);
```

### 参数展开规则

当方法只有一个 Bean 或 Map 类型参数且 **未标记** `@Param` 时，dbVisitor 会自动将其属性/键展开为顶层参数。

```java title='例2：Bean 属性自动展开（无 @Param）'
// Bean 属性直接作为 SQL 参数名使用
@Insert("insert into users (id, name, age) values (#{id}, #{name}, #{age})")
int insertUser(UserInfo user);
```

当标记了 `@Param` 时，需要通过 `参数名.属性名` 方式访问：

```java title='例3：@Param + Bean 嵌套访问'
@Insert("insert into users (id, name, email) values (#{user.id}, #{user.name}, #{email})")
int insertMixed(@Param("user") UserInfo user, @Param("email") String email);
```

:::info[参数展开总结]
- **单个 Bean/Map 无 @Param** → 属性/键自动展开为顶层参数
- **有 @Param** → 通过 `参数名.属性名` 嵌套访问
- **多个基本类型参数** → 必须使用 `@Param` 命名
:::
