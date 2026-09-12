---
id: about
sidebar_position: 1
title: 5.7 对象映射
description: dbVisitor 只做对象映射（Object Mapping），通过 @Table/@Column 注解将 Java 对象映射到数据库表。
---

# 5.7 对象映射

dbVisitor 通过 `@Table` 和 `@Column` 将 Java 实体映射到数据库：一个实体通常对应一行数据，属性对应列。属性也可以是完整对象，整体保存在一个字段中；这不等于关联表或一对多关系映射。

## 最小示例

```java
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

@Table("users")
public class User {
    @Column(primary = true)
    private Integer id;

    @Column("user_name")
    private String name;

    // 省略 getter/setter
}
```

这里 `User` 对应 `users` 表，`id` 对应主键列，`name` 对应 `user_name` 列。表需预先存在，声明映射不等于执行建表。

`BaseMapper` 和 `LambdaTemplate` 的实体模式会使用这些信息生成增删改查语句。直接使用 `JdbcTemplate` 执行原生命令时不强制定义实体。

## 按使用场景阅读

| 需要做什么 | 阅读内容 |
| --- | --- |
| 指定表名、列名，或忽略属性 | [映射表](./table) |
| 不在 Java 类上添加映射注解 | [文件方式映射](../file/entity_map) |
| 对应 `user_name` 与 `userName` 等命名 | [驼峰命名法](./camel_case) |
| 处理大小写或关键字名称 | [名称敏感性](./name_sensitivity) |
| 控制属性是否参与写入 | [写入策略](./write_policy) |
| 配置枚举、抽象类型和特殊类型转换 | [类型映射和处理](./type_mapping) |
| 将对象、Map 或 List 保存为一个 JSON 字段 | [JSON 字段映射](./json-field.md) |
| 自定义列值使用的 SQL 表达式 | [语句模板](./statement_template) |
| 配置自增、序列或 UUID 主键 | [主键生成器](./key_generator) |
