---
id: name-sensitivity
sidebar_position: 61
title: 名称敏感性
---

## 表名与列名 {#database-names}

SQL Server 的名称大小写规则由排序规则决定。在不区分大小写的数据库中，`user_info` 与 `User_Info` 不能表示两张不同的表；需要区分时应使用不同名称。

`@Table(useDelimited = true)` 会让构造器 API 引用表名、列名，例如 `[user_info]`，但不会改变大小写规则。

## 结果列大小写 {#result-column-case}

`@Table(caseInsensitive = false)` 控制返回列名与实体属性的匹配，不改变数据库查找表或字段的规则。配置示例见[名称敏感性](../../guides/core/mapping/name_sensitivity.md#result-column-case)。
