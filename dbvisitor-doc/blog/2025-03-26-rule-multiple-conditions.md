---
slug: rule_multiple_conditions
title: 使用规则处理复杂条件
description: 详解如何使用 dbVisitor 的 AND/OR 规则定义包含括号和组合逻辑的复杂 SQL 条件。
authors: [ZhaoYongChun]
tags: [Rule, DynamicSQL]
language: zh-cn
---

在日常开发中，我们最常使用的动态 SQL 规则是简单的单字段条件，例如 `@{and, name = :name}`。它的逻辑非常清晰：当参数 `name` 不为空时，追加 `AND name = ?`；否则忽略。

但现实世界的业务逻辑往往更复杂。例如，我们需要实现一个组合筛选功能，允许用户通过 **"(年龄与性别匹配) 或者 (姓名与ID匹配)"** 这样的复合逻辑来查询数据。

本文将介绍如何利用 dbVisitor 规则的高级特性，一行代码搞定这种复杂的嵌套逻辑。

<!-- truncate -->

## 场景挑战

假设我们有如下查询需求：
> 查询用户，满足以下任意一组条件即可：
> 1. `age` 等于指定值 **且** `sex` 为 1
> 2. `name` 等于指定值 **且** `id` 在指定列表中

对应的 SQL 逻辑结构为：
```sql
WHERE (age = ? AND sex = '1') OR (name = ? AND id IN (?, ?, ?))
```

### 传统痛点

如果不使用 dbVisitor 的高级规则，在其他框架（如 MyBatis XML）中实现这个逻辑会非常痛苦：

*   **括号管理麻烦**：你需要小心翼翼地控制 `(` 和 `)` 的生成，防止某些参数为空时留下空的括号 `()` 导致 SQL 报错。
*   **前缀处理繁琐**：你需要处理 `OR` 关键字的拼接。如果第一组条件为空，第二组条件前面不能有 `OR`；如果前面已经有 `WHERE` 条件，这里又需要补 `AND`。

写出来的 XML 可能会像天书一样：
```xml
<!-- 繁琐的 XML 实现（反例） -->
<trim prefix="AND (" suffix=")" prefixOverrides="OR">
    <if test="age != null">
        (age = #{age} AND sex = '1')
    </if>
    <if test="name != null">
        OR (name = #{name} AND id IN 
        <foreach ...>...</foreach>
        )
    </if>
</trim>
```

## dbVisitor 的优雅解法

dbVisitor 的设计哲学是 **"让 SQL 回归 SQL"**。它的 `@{and, ...}` 规则不仅支持简单的 `key = value`，更支持写入完整的、包含括号和逻辑运算符的 SQL 片段。

### 代码示例

我们只需要在一个 `@{and}` 规则中写下完整的逻辑即可：

```xml
/* SQL 模板 */
select * from user_info 
where status = 'ENABLE'
@{and, (age = :age and sex = '1') or (name = :name and id in (:ids)) }
```

### 运行机制

当执行这段 SQL 时，dbVisitor 引擎会执行以下判断：

1.  **参数扫描**：引擎会扫描规则表达式 `(age = :age ...)` 中引用的所有参数（`:age`, `:name`, `:ids`）。
2.  **动态决策**：
    *   **情况 A：所有关键参数均为空**。如果 `:age`, `:name`, `:ids` 全部为 `null`，整个 `@{and, ...}` 规则**将被完全忽略**。SQL 退化为 `select * from user_info where status = 'ENABLE'`。
    *   **情况 B：存在有效参数**。只要其中有任意一个参数不为空（且符合启用条件），整个表达式就会被作为一个整体追加到 SQL 中。
3.  **自动修饰**：dbVisitor 会自动处理 `WHERE` 后的连接词。如果这是第一个条件，它会自动填充 `AND`（如果前面已有 `status='ENABLE'`）。

### 生成结果

假设传入参数 `age = 18`, `name = "Tom"`, `ids = [1, 2, 3]`，生成的最终 SQL 为：

```sql
select * from user_info 
where status = 'ENABLE'
  AND ( (age = ? and sex = '1') or (name = ? and id in (?, ?, ?)) )
```

## 方案优势

1.  **极高的可读性**：你写的规则就是标准的 SQL 语法，包含了括号和 `OR` 逻辑，任何懂 SQL 的人都能一眼看懂，无需脑补 XML 标签的嵌套逻辑。
2.  **零胶水代码**：不需要 `<trim>`, `<if>`, `<choose>` 等繁杂的标签来处理 SQL 语法片段的拼接。
3.  **安全性**：尽管允许写复杂表达式，但所有的变量（`:age` 等）依然通过 JDBC 预编译（PreparedStatement）处理，**完全防止 SQL 注入**。

## 总结

dbVisitor 的规则引擎不仅仅是简单的非空判断。通过支持在规则中嵌套复杂的 SQL 表达式，它让我们能够以最直观、最接近原生 SQL 的方式来处理复杂的动态查询逻辑。

下次遇到复杂的 `OR` 组合查询时，不妨试试直接把逻辑写进 `@{and, ...}` 里吧！
