---
id: about
sidebar_position: 1
hide_table_of_contents: true
title: Mapper File
description: Introduction to the usage of Mapper files in dbVisitor.
---

# Mapper File

Using `@RefMapper` instead of `@SimpleMapper` annotation allows associating a Mapper interface with an XML file and mapping all or part of the methods in the interface to the Mapper file.

```java title='1. Link Mapper Interface and Mapper File via @RefMapper Annotation'
@RefMapper("/mapper/userMapper.xml")
public interface UserMapper {
    // Conventional Declarative API
    @Insert({ "insert into users (",
            "    id, name, age, create_time",
            ") values (",
            "    #{id}, #{name}, #{age}, #{createTime})",
            ")" })
    int saveUser(User user);

    // Map query operation to XML file to implement more complex queries
    List<User> listUsers(@Param("searchId") long searchId, List<String> prefixList);
}
```

```xml title='2. Define all or part of Mapper interface methods in the file'
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//dbvisitor.net//DTD Mapper 1.0//EN"
        "https://www.dbvisitor.net/schema/dbvisitor-mapper.dtd">
    <!-- ➊ The namespace content content should be consistent with the Mapper interface name -->
<mapper namespace="net.example.mapper.UserMapper">
    <!-- ➋ The ID attribute of the select tag should match the name of the method to be mapped. Result type definition can be omitted as it is determined by the associated interface method -->
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

```java title='3. Create Mapper'
// 1. Create Configuration
Configuration config = new Configuration();

// 2. Create Session
Session session = config.newSession(dataSource);
// OR
Session session = config.newSession(connection);

// 3. Create Mapper
UserMapper mapper = session.createMapper(UserMapper.class);
List<User> result = mapper.listUsers(2, prefixList);
```

In actual usage, the way to obtain a Session may vary depending on your project architecture. The above code demonstrates a primitive way to create a Session.
You can choose the appropriate way to obtain a Session according to your project architecture. For details, please refer to: **[Framework Integration](../../yourproject/buildtools#integration)**

Relevant Classes
- net.hasor.dbvisitor.mapper.RefMapper
- net.hasor.dbvisitor.mapper.SimpleMapper
- net.hasor.dbvisitor.mapper.Param
- net.hasor.dbvisitor.session.Configuration
- net.hasor.dbvisitor.session.Session

## User Guide

- [Document Structure](./document), understand the basic concepts of Mapper files, such as document structure and how to validate documents.
- [Document Tags](./sql_element), XML tag elements used to configure SQL statements.
  - Check available [Type Aliases](./sort_type_name) for the resultType attribute of the &lt;select&gt; tag.
- [Dynamic SQL](./dynamic), allows dynamic configuration of SQL through tags like if, choose, foreach, etc.
- [Rules](../../rules/about), endow SQL with more powerful features through rules.
- [Mapping Tables](./entity_map), used to describe how to map Java objects to database tables.
- [Mapping Result Sets](./result_map), used to describe how to fetch data from query result sets.
- [Auto Mapping](./automapping), simplify &lt;resultMap&gt; or &lt;entity&gt; tag configuration through auto mapping.
- [Pagination Query](./page), perform pagination queries via pagination objects after establishing a relationship between Mapper Interface and Mapper XML file.
