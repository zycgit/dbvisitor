---
id: about
sidebar_position: 1
hide_table_of_contents: true
title: 对象映射
description: 相较于完整的 ORM 概念 dbVisitor 剔除了关系映射只保留了对象映射，并基于对象映射通过构造器 API简化数据库操作。
---

相较于完整的 ORM 概念 dbVisitor 剔除了关系映射只保留了对象映射，并基于对象映射通过 [构造器 API](../../api/lambda_api) 简化数据库操作。

:::tip[dbVisitor 的对象映射具有如下特点：]
- 学习曲线平稳，无需掌握复杂的概念。
- 利用 SQL 方言在使用 [构造器 API](../../api/lambda_api) 操作数据库时无需考虑数据库的差异性。
- 不支持关系映射如：一对一、一对多、多对一、多对多。
:::

## 使用指引

映射表
- [注解方式](./basic)，使用 @Table 和 @Column 注解将类型映射到具体表或视图。
- [文件方式](../file/entity_map)，在 Mapper 文件中通过 &lt;entity&gt; 标签进行映射。
- [主键生成器](./keytype)，用来生成主键数据，如使用数据库序列、UUID 等，也可以在写入数据成功后获取表的自增主键。
- [处理类型](./type)，在列映射中处理不同的数据类型，包括枚举、JSON、特殊 JDBC Type 等。

小技巧
- [驼峰命名法](./camel_case)，使用 @Table 和 @Column 注解将类型映射到具体表或视图。
- [名称敏感性](./delimited)，使用特定属性处理名称拼写中的敏感性，或者处理名称为关键字情况关键字。
- [写入策略](./write_policy)，在使用 构造器 API 操作数据库时，决定属性值是否参与 INSERT 和 UPDATE 操作。
- [语句模版](./template)，可以处理在使用构造器 API 时一些特定情况 SQL，例如处理 MySQL 的 point 类型。
