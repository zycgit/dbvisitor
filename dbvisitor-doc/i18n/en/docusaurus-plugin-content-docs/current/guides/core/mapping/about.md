---
id: about
sidebar_position: 1
title: 5.7 Object Mapping
description: dbVisitor only does Object Mapping, mapping Java objects to database tables via @Table/@Column annotations.
---

# 5.7 Object Mapping

dbVisitor maps Java entities to a database with @Table and @Column: an entity usually represents one row and its properties represent columns. A property can also be an entire object stored in one column; this is not a related-table or one-to-many relationship mapping.

## Minimal Example

```java
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

@Table("users")
public class User {
    @Column(primary = true)
    private Integer id;

    @Column("user_name")
    private String name;

    // Getters and setters omitted
}
```

User maps to the users table, id to the primary-key column and name to user_name. The table must already exist; declaring a mapping does not create it.

BaseMapper and LambdaTemplate entity mode use these mappings to generate CRUD statements. Executing native commands directly with JdbcTemplate does not require an entity.

## Find Your Use Case

| What you need | Read |
| --- | --- |
| Specify table and column names, or ignore properties | [Map a Table](./table) |
| Define mappings without annotating Java classes | [File-Based Entity Mapping](../file/entity_map) |
| Match names such as user_name and userName | [Camel Case](./camel_case) |
| Handle case-sensitive or keyword names | [Name Sensitivity](./name_sensitivity) |
| Control which properties participate in writes | [Write Policy](./write_policy) |
| Configure enums, abstract types and special conversions | [Type Mapping and Handlers](./type_mapping) |
| Store an object, Map or List as one JSON field | [JSON Field Mapping](./json-field.md) |
| Customize SQL expressions for column values | [Statement Templates](./statement_template) |
| Configure auto-increment, sequence or UUID keys | [Key Generators](./key_generator) |
