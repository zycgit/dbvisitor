---
id: mongo
sidebar_position: 2
hide_table_of_contents: true
title: MongoDB 特异性
description: MongoDB 数据源使用 dbVisitor 的能力范围与限制。
---

# MongoDB 数据源特异性

dbVisitor 通过 [JDBC-Mongo](../../drivers/mongo/about) 驱动，基于 JDBC 协议访问 MongoDB 数据源。

**支持的能力：**
- 使用 MongoDB 命令操作数据（[支持的命令列表](../../drivers/mongo/commands)）
- [JdbcTemplate](#exec-command)、[构造器 API](#exec-lambda)、[BaseMapper](#exec-mapper)、[方法注解](#exec-annotation)、[Mapper 文件](#exec-file)
- 对象映射、结果集映射、[规则](../../rules/about)、[参数传递](../../args/about)、[ResultSetExtractor/RowMapper](../../result/about)
- Mapper File 中的[动态 SQL](../../core/file/dynamic) 标签、`Statement.RETURN_GENERATED_KEYS`（自动获取 `_id`）

**不支持：** executeBatch、存储过程

## 概念类比

不同 MongoDB 命令的执行结果分为三种：
- **更新数** — 类比 INSERT/UPDATE/DELETE，用 `executeUpdate` 获取
- **单行/多行结果** — 类比 SELECT 结果集，第一列为 `_ID`，第二列为 `_JSON`（均为字符串）

---

## 命令方式（JdbcTemplate）{#exec-command}

使用 JdbcTemplate 可以直接执行 MongoDB 命令，在此之前请确保已经正确配置好 MongoDB 数据源，具体请参考 [MongoDB 驱动使用指南](../../drivers/mongo/usecase)。

:::tip[提示]
更多使用方式请参考 [JdbcTemplate 类](../../core/jdbc/about#guide)，在使用过程中下面两个特性由于驱动原因无法支持：
- 批量化
- 存储过程
:::

```java title='创建 JdbcTemplate'
JdbcTemplate jdbc = new JdbcTemplate(dataSource);
// 或者
JdbcTemplate jdbc = new JdbcTemplate(connection);
```

```java title='插入数据'
// 直接命令方式
jdbc.execute("test.user_info.insert({name: 'mali', age: 26})");
// 参数化命令方式
jdbc.execute("test.user_info.insert(?)", new Object[] { "{name: 'mali', age: 26}" });
```

```java title='查询数据'
// 查询所有
List<Map<String, Object>> list = jdbc.queryForList("test.user_info.find()");
// 条件查询
Map<String, Object> mali = jdbc.queryForMap("test.user_info.find({name: 'mali'})");
String json = (String) mali.get("_JSON");
```

```java title='更新数据'
jdbc.execute("test.user_info.update({name: 'mali'}, {$set: {age: 27}})");
```

```java title='删除数据'
jdbc.execute("test.user_info.remove({name: 'mali'})");
```

---

## 构造器 API（LambdaTemplate）{#exec-lambda}

构造器 API 提供了一种类型安全、流式调用的方式操作 MongoDB，用法与 RDBMS 一致，详细请参考 [构造器 API](../../core/lambda/about)。

```java title='初始化'
LambdaTemplate lambda = new LambdaTemplate(dataSource);
```

```java title='新增'
UserInfo user = new UserInfo();
user.setUid("1001");
user.setName("test_user");

int result = lambda.insert(UserInfo.class)
                   .applyEntity(user)
                   .executeSumResult();
```

```java title='查询'
// 根据条件查询
UserInfo user = lambda.query(UserInfo.class)
                      .eq(UserInfo::getUid, "1001")
                      .queryForObject();

// 列表查询
List<UserInfo> users = lambda.query(UserInfo.class)
                             .like(UserInfo::getName, "test%")
                             .queryForList();
```

```java title='更新'
int result = lambda.update(UserInfo.class)
                   .eq(UserInfo::getUid, "1001")
                   .updateTo(UserInfo::getName, "New Name")
                   .doUpdate();
```

```java title='删除'
int result = lambda.delete(UserInfo.class)
                   .eq(UserInfo::getUid, "1001")
                   .doDelete();
```

---

## BaseMapper 接口 {#exec-mapper}

`BaseMapper` 提供了通用 CRUD 方法，用法与 RDBMS 一致，详细请参考 [BaseMapper](../../core/mapper/about)。

```java title='定义 Mapper 接口'
@SimpleMapper
public interface UserInfoMapper extends BaseMapper<UserInfo> {
    // 可以在此添加自定义方法
}
```

```java title='使用 Mapper 进行 CRUD'
Session session = ...;
UserInfoMapper mapper = session.createMapper(UserInfoMapper.class);

// 新增
mapper.insert(user);
// 查询
UserInfo loadedUser = mapper.selectById(user.getUid());
// 更新
loadedUser.setName("updated_name");
mapper.update(loadedUser);
// 删除
mapper.deleteById(user.getUid());
```

---

## 注解方式 {#exec-annotation}

:::tip[提示]
对于 [核心 API 提供的注解](../../core/annotation/about) 方式除了 `@Call` 注解不支持之外，其它所有注解都可以在 MongoDB 数据源上正常使用。
:::

```java title='1. 定义对象'
@Table("user_info")
public class UserInfo {
    @Column(value = "uid", primary = true)
    private String uid;
    @Column("name")
    private String name;
    ... // 省略 getter/setter 方法
}
```

```java title='2. 定义 Mapper 接口'
@SimpleMapper()
public interface UserInfoMapper {
    @Insert("test.user_info.insert(#{info, typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler})")
    int saveUser(@Param("info") UserInfo info);

    @Query("test.user_info.find({uid: #{uid}})")
    UserInfo loadUser(@Param("uid") String uid);

    @Delete("test.user_info.remove({uid: #{uid}})")
    int deleteUser(@Param("uid") String uid);
}
```

```java title='3. 创建并使用 Mapper'
Configuration config = new Configuration();
Session session = config.newSession(dataSource);

UserInfoMapper mapper = session.createMapper(UserInfoMapper.class);
```

### 映射 ObjectId

MongoDB 的 `_id` 字段通常是 `ObjectId` 类型，而在 Java 对象中我们通常使用 `String` 类型。

```java title='定义对象（ObjectId 映射）'
@Table("user_info")
public class UserInfo {
    @Column(value = "_id", primary = true, keyType = KeyType.Auto, whereValueTemplate = "ObjectId(?)")
    private String id;

    @Column("name")
    private String name;
    ...
}
```

```java title='Mapper 定义（使用 ObjectId 查询）'
@SimpleMapper()
public interface UserInfoMapper {
    @Query("test.user_info.find({_id: ObjectId(#{id})})")
    UserInfo loadById(@Param("id") String id);
}
```

### 分页查询

在 Mapper 方法中添加 `Page` 参数即可实现分页查询。

```java title='Mapper 定义'
@SimpleMapper()
public interface UserInfoMapper {
    @Query("test.user_info.find({name: #{name}})")
    List<UserInfo> queryByName(@Param("name") String name, Page page);
}
```

```java title='调用分页'
Page page = new PageObject();
page.setPageSize(10);

List<UserInfo> list = mapper.queryByName("mali", page);
```

---

## 文件方式（Mapper File）{#exec-file}

```java title='1. 定义对象'
@Table("user_info")
public class UserInfo {
    @Column(value = "uid", primary = true)
    private String uid;
    @Column("name")
    private String name;
    ... // 省略 getter/setter 方法
}
```

```xml title='2. 定义 Mapper 文件'
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//dbvisitor.net//DTD Mapper 1.0//EN"
"https://www.dbvisitor.net/schema/dbvisitor-mapper.dtd">
<mapper namespace="net.test.dto.UserInfoMapper">
    <insert id="saveUser">
        test.user_info.insert(#{info, typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler})
    </insert>

    <select id="loadUser" resultType="net.test.dto.UserInfo">
        test.user_info.find({uid: #{uid}})
    </select>

    <delete id="deleteUser">
        test.user_info.remove({uid: #{uid}})
    </delete>
</mapper>
```

```java title='3. 定义 Mapper 接口'
@RefMapper("/path/to/mapper.xml")
public interface UserInfoMapper {
    int saveUser(@Param("info") UserInfo info);

    UserInfo loadUser(@Param("uid") String uid);

    int deleteUser(@Param("uid") String uid);
}
```
