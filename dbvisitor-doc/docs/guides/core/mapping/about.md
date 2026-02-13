---
id: about
sidebar_position: 1
hide_table_of_contents: true
title: 对象映射
description: dbVisitor 只保留对象映射，剔除关系映射（一对多、多对多等），学习曲线平稳。
---

# 对象映射

dbVisitor 只做 **对象映射**（Object Mapping），不做关系映射（一对一、一对多、多对多等）。Java 对象与数据库表/视图直接对应，配合 [构造器 API](../../api/lambda_api) 可屏蔽数据库方言差异。

```java title='快速示例'
@Table("users")
public class User {
    @Column(name = "id", primary = true, keyType = KeyType.Auto)
    private Long id;
    @Column("name")
    private String name;
}
```

## 映射配置

- [注解方式](./basic)，使用 `@Table` 和 `@Column` 注解声明映射。
- [文件方式](../file/entity_map)，通过 Mapper 文件的 `<entity>` 标签配置映射。
- [主键生成器](./keytype)，支持序列、UUID、自增主键回填等策略。
- [处理类型](./type)，枚举、JSON、特殊 JDBC Type 的类型映射。

## 小技巧

- [驼峰命名法](./camel_case)，自动映射 Java 驼峰属性名到数据库下划线列名。
- [名称敏感性](./delimited)，处理列名中的大小写敏感或数据库关键字。
- [写入策略](./write_policy)，控制属性值是否参与 INSERT / UPDATE 操作。
- [语句模版](./template)，应对构造器 API 中的特殊 SQL 场景（如 MySQL Point 类型）。
