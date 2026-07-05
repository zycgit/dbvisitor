---
id: about
sidebar_position: 1
title: 5.7 对象映射
description: dbVisitor 只做对象映射（Object Mapping），通过 @Table/@Column 注解将 Java 对象映射到数据库表。
---

# 5.7 对象映射

dbVisitor 只做 **对象映射**（Object Mapping），不做关系映射（一对多、多对多等）。Java 对象与数据库表直接对应，配合 [构造器 API](../../api/lambda) 屏蔽数据库方言差异。

## 什么时候需要

| 使用方式 | 是否必须 | 说明 |
|---------|:-----:|------|
| JdbcTemplate | 不需要 | 可以直接执行 SQL，也可以按需把结果映射到 Bean |
| Mapper 方法注解 | 不强制 | SQL 由注解提供；返回 Bean 时可用结果映射 |
| Mapper 文件 resultMap | 按需 | 查询结果复杂、JOIN、别名、函数列时建议配置 |
| BaseMapper | 必须 | 需要表、列、主键信息才能生成 CRUD SQL |
| LambdaTemplate Entity/Map 模式 | 必须 | 需要映射信息生成 SQL |
| Map 查询模式（自由 Map） | 不需要 | 直接使用表名、列名和 Map |

## 最小映射

```java
@Table("users")
public class User {
    @Column(name = "id", primary = true, keyType = KeyType.Auto)
    private Long id;

    @Column("name")
    private String name;

    @Column("age")
    private Integer age;

    @Column(value = "create_time", insert = false, update = false)
    private Date createTime;
}
```

## 映射方式

有两种方式定义对象映射，可以混合使用：

| 方式 | 说明 | 入口 |
|------|------|------|
| 注解方式 | `@Table` + `@Column` 直接在实体类上声明，最常用 | [注解方式](./table) |
| 文件方式 | Mapper XML 中 `<entity>` 标签描述映射，避免代码侵入 | [文件方式](../file/entity_map) |

## 核心配置

### 主键生成

`keyType` 决定主键生成方式。不同数据库的支持情况不同：

| keyType | 说明 | 适用场景 |
|---------|------|---------|
| `KeyType.Auto` | 数据库自增/IDENTITY | MySQL AUTO_INCREMENT、PG SERIAL |
| `KeyType.Sequence` | 数据库序列 | Oracle、PG sequence（搭配 `@KeySeq`） |
| `KeyType.UUID32` | 应用侧生成 32 位 UUID | 无自增主键的数据库 |
| `KeyType.UUID36` | 应用侧生成 36 位 UUID | 同上 |

```java
// 自增主键
@Column(value = "id", primary = true, keyType = KeyType.Auto)
private Long id;

// 序列主键
@KeySeq("user_info_seq")
@Column(value = "id", primary = true, keyType = KeyType.Sequence)
private Long id;

// 应用侧 UUID
@Column(value = "id", primary = true, keyType = KeyType.UUID32)
private String id;
```

详细说明见 [主键生成器](./key_generator) 和 [数据源特性](../../../features/overview)。

### 类型映射

`@Column` 的 `typeHandler` 属性可以指定特定类型处理器：

```java
// JSON 序列化
@Column(value = "extra_info", typeHandler = JsonTypeHandler.class)
private Map<String, Object> extraInfo;

// 枚举映射
@Column("status")
private OrderStatus status; // 自动使用 EnumTypeHandler
```

详细说明见 [类型映射](./type_mapping)。

### 写入策略

控制属性在 INSERT/UPDATE 时的行为：

```java
@Column(value = "create_time", insert = true, update = false)
private Date createTime; // 只在 INSERT 时写入

@Column(value = "update_time", insert = true, update = true)
private Date updateTime; // INSERT 和 UPDATE 都写入
```

详细说明见 [写入策略](./write_policy)。

## 常见配置

### 驼峰命名

如果列名遵循下划线命名（如 `user_name`），Java 属性使用驼峰命名（`userName`），可以开启自动映射：

```java
@Table(value = "user_info", autoMapping = true)
public class UserInfo { ... }
```

详细说明见 [驼峰命名](./camel_case)。

### 名称敏感性

当列名大小写敏感或是数据库关键字时：

```java
@Column(value = "\"order\"", nameSensitivity = true)
private Integer order; // 列名是关键字，需要转义
```

详细说明见 [名称敏感性](./name_sensitivity)。

### 语句模版

控制构造器 API 生成的 SQL 中列值的表达方式。例如 MySQL `POINT` 类型：

```java
@Column(value = "location", whereValueTemplate = "ST_GeomFromText(?)")
private String location;
```

详细说明见 [语句模版](./statement_template)。

## 深入阅读

- [注解方式](./table)：`@Table`、`@Column`、`@Primary`、`@KeySeq` 完整用法
- [主键生成器](./key_generator)：序列、UUID、自增回填策略
- [类型映射](./type_mapping)：枚举、JSON、特殊 JDBC 类型
- [写入策略](./write_policy)：控制 INSERT/UPDATE 列参与行为
- [驼峰命名](./camel_case)：自动映射 `user_name` → `userName`
- [名称敏感性](./name_sensitivity)：大小写敏感、关键字转义
- [语句模版](./statement_template)：自定义生成 SQL 中的列值表达
