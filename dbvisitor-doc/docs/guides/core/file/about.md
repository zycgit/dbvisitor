---
id: about
sidebar_position: 1
hide_table_of_contents: true
title: Mapper 文件
description: dbVisitor 中 Mapper 文件的用法介绍。
---

# Mapper 文件

使用 @RefMapper 替代 @SimpleMapper 注解可以将 Mapper 接口和一个 XML 文件建立关联，并将接口中所有或者一部分方法映射到 Mapper 文件中。

```java title='1. 通过 @RefMapper 注解将 Mapper 接口和 Mapper 文件建立联系'
@RefMapper("/mapper/userMapper.xml")
public interface UserMapper {
    // 常规的声明式 API
    @Insert({ "insert into users (",
            "    id, name, age, create_time",
            ") values (",
            "    #{id}, #{name}, #{age}, #{createTime})",
            ")" })
    int saveUser(User user);

    // 将查询操作映射到 XML 文件中已实现更加复杂的查询
    List<User> listUsers(@Param("searchId") long searchId, List<String> prefixList);
}
```

```xml title='2. 全部或者部分 Mapper 接口中的方法在文件中定义'
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//dbvisitor.net//DTD Mapper 1.0//EN"
        "https://www.dbvisitor.net/schema/dbvisitor-mapper.dtd">
    <!-- ➊ namespace 中的内容和 Mapper 接口名保持一致 -->
<mapper namespace="net.example.mapper.UserMapper">
    <!-- ➋ select 标签的 ID 属性和需要映射的方法名称保持一致，查询结果类型可以无需定义由关联的接口方法决定 -->
    <select id="listUsers">
        select <include refid="userColumns"/>
        from   users
        where  owner_uid = #{searchId} and
               <if test="prefixList != null and prefixList.size() > 0">
                   <foreach collection="prefixList" item="item" index="index" separator="or" open="and (" close=")">
                       #{item} like concat(res_path,'%')
                   </foreach>
               </if>
    </select>
</mapper>
```

```java title='3. 创建 Mapper'
// 1，创建 Configuration
Configuration config = new Configuration();

// 2，创建 Session
Session session = config.newSession(dataSource);
或者
Session session = config.newSession(connection);

// 3，创建 Mapper
UserMapper mapper = session.createMapper(UserMapper.class);
List<User> result = mapper.listUsers(2, prefixList);
```

## 使用指引

- [文档结构](./document)，了解 Mapper 文件的基本概念，如文档结构以及如何对文档进行验证。
- [SQL 标签](./sql_element)，用来配置 SQL 语句的 XML 标签元素。
  - 查阅 &lt;select&gt; 标签的 resultType 属性可用的 [类型别名](./sort_type_name)。
- [动态 SQL](./dynamic)，允许通过 if、choose、foreach 等标签对 SQL进行动态配置。
- [规则](../../rules/about)，通过规则赋予 SQL 更加强大的特性。
- [映射表](./entity_map)，用于描述如何将 Java 对象映射到数据库表。
- [映射结果集](./result_map)，用于描述如何从查询结果集中获取数据。
- [自动映射](./automapping)，通过自动映射简化 &lt;resultMap&gt; 或 &lt;entity&gt; 标签配置。
- [分页查询](./page)，Mapper 接口在和 Mapper XML 文件建立关系后通过分页对象进行分页查询。
