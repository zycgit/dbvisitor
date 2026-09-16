---
id: name-sensitivity
slug: /features/elastic/name-sensitivity
sidebar_position: 61
title: 名称敏感性
---

## 索引名与字段名 {#database-names}

索引名必须使用小写，例如 `user_info`；不能用 `User_Info` 建立另一个索引。字段名保留大小写，实体属性应映射到实际字段名：

```java
@Table("user_info")
public class User {
    @Column("Name")
    private String name;
    // 省略 getter 和 setter
}
```

这里的属性访问 `Name` 字段，不是 `name` 字段。

## 结果列大小写 {#result-column-case}

使用自由 Map 查询并设置 `Options.of().caseInsensitive(false)` 时，应按返回的 `Name` 键读取。这控制的是结果查找，不会改变 Elasticsearch 的字段名。实体映射配置见[名称敏感性](../../../guides/core/mapping/name_sensitivity.md#result-column-case)。
