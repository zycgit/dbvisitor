---
slug: mongodb-mybatis-style
title: Using dbVisitor to Work with MongoDB in a MyBatis Style
authors: [ZhaoYongChun]
tags: [mongodb, mybatis, orm, jdbc, dbvisitor]
---

In Java, MongoDB is typically accessed via the official `mongo-java-driver` or Spring’s `spring-data-mongodb`. They are powerful, but for teams accustomed to RDBMS and MyBatis, switching to MongoDB often means learning a completely different API and mindset.

In mixed architectures running both MySQL and MongoDB, the data-access layer can feel split: MyBatis Mappers and XML on one side, `MongoTemplate` or Repository on the other. That divergence increases learning cost and makes common features (like pagination) hard to unify.

This post shows how **dbVisitor** lets you operate MongoDB in a “MyBatis-like” way to unify the architecture.

<!--truncate-->

## 1. Pain points of the traditional approach

In mixed stacks you often face:

* **API divergence**: SQL/JDBC for RDBMS vs BSON/proprietary protocols for MongoDB.
* **Pagination differences**: MyBatis with PageHelper/RowBounds vs manual `skip`/`limit` or Spring Data `Pageable` in MongoDB.
* **High maintenance cost**: Two separate code paths and abstractions increase complexity and error risk.

## 2. How dbVisitor helps

dbVisitor provides a JDBC driver layer (`dbvisitor-driver`) plus an adapter (`jdbc-mongo`) so MongoDB can be accessed via standard JDBC. You operate MongoDB like MySQL.

It also offers MyBatis-style ORM: Mapper interfaces, XML mappings, annotations, and Lambda expressions.

### 2.1 Object mapping (ORM)

Define a Java object and map it with annotations—similar to MyBatis-Plus or JPA:

```java
@Table("user_info")
public class UserInfo {
    // 映射 _id 字段，并自动处理 ObjectId
    @Column(value = "_id", primary = true, keyType = KeyType.Auto, whereValueTemplate = "ObjectId(?)")
    private String id;

    @Column("name")
    private String name;

    @Column("age")
    private Integer age;

    // 省略 getter/setter
}
```

### 2.2 Mapper interfaces (annotations)

Define Mapper interfaces and annotate MongoDB commands.

```java
@SimpleMapper
public interface UserInfoMapper {
    // 插入数据
    @Insert("test.user_info.insert(#{info})")
    int saveUser(@Param("info") UserInfo info);

    // 根据 ID 查询
    @Query("test.user_info.find({_id: ObjectId(#{id})})")
    UserInfo loadById(@Param("id") String id);

    // 删除数据
    @Delete("test.user_info.remove({_id: ObjectId(#{id})})")
    int deleteUser(@Param("id") String id);
}
```

### 2.3 Base Mapper

If you do not want to write any commands, extend `BaseMapper`; dbVisitor generates basic CRUD automatically.

```java
@SimpleMapper
public interface UserInfoBaseMapper extends BaseMapper<UserInfo> {
    // 自动拥有 insert, update, delete, selectById, listBySample 等方法
}
```

### 2.4 Lambda style

dbVisitor also provides a MyBatis-Plus–style Lambda API with full type safety.

```java
LambdaTemplate lambda = new LambdaTemplate(connection);

// 查询 name = "mali" 的用户
UserInfo user = lambda.query(UserInfo.class)
    .eq(UserInfo::getName, "mali")
    .queryForObject();

// 更新操作
lambda.update(UserInfo.class)
    .eq(UserInfo::getId, user.getId())
    .updateTo(UserInfo::getAge, 27)
    .doUpdate();
```

### 2.5 Manage mappers with XML (MyBatis style)

For complex queries or centralized SQL, define Mappers in XML—almost identical to MyBatis.

**Mapper 接口：**

```java
@RefMapper("mapper/user-mapper.xml")
public interface UserInfoXmlMapper {
    int saveUser(@Param("info") UserInfo info);
    List<UserInfo> listByUserName(@Param("userName") String userName, Page page);
}
```

**XML 文件 (user-mapper.xml)：**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//dbvisitor.net//DTD Mapper 1.0//EN"
        "https://www.dbvisitor.net/schema/dbvisitor-mapper.dtd">
<mapper namespace="com.example.mapper.UserInfoXmlMapper">
    <resultMap id="userResultMap" type="com.example.entity.UserInfo">
        <result column="_id" property="id"/>
        <result column="name" property="name"/>
        <result column="age" property="age"/>
    </resultMap>

    <insert id="saveUser">
        test.user_info.insert({
            name: #{info.name},
            age: #{info.age}
        })
    </insert>

    <!-- 支持自动分页 -->
    <select id="listByUserName" resultMap="userResultMap">
        test.user_info.find({name: #{userName}})
    </select>
</mapper>
```

## 3. Unified pagination

With dbVisitor, pagination is identical for MySQL and MongoDB. Just pass a `Page` object.

```java
// 创建分页对象
Page page = new PageObject();
page.setPageSize(10);
page.setPageNumber(0); // 第一页

// 执行查询，dbVisitor 会自动拦截并重写为分页查询
// 对于 MongoDB，会自动转换为 .skip(0).limit(10)
List<UserInfo> list = mapper.listByUserName("mali", page);

// 获取总记录数（如果需要）
long total = page.getTotalCount();

// 翻页
page.nextPage();
list = mapper.listByUserName("mali", page);
```

## 4. Summary

dbVisitor lets you use one API and one mental model (Mapper/XML/Lambda) for both relational databases and MongoDB. That lowers development and maintenance costs and keeps the data-access layer consistent.

If you want a unified RDBMS/NoSQL experience, dbVisitor is worth trying.
