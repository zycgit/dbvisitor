---
id: analyze
sidebar_position: 1
title: ANALYZE
---

:::info[说明]
对应 SDK 方法：`runAnalyzer`。
:::

## 验证分词结果 {#analyze}

```sql
ANALYZE 'hello world' WITH (analyzer_params='{"tokenizer":"standard"}');
ANALYZE ['hello world', '', 'milvus'] WITH (with_detail=true, with_hash=true);
ANALYZE ? WITH (analyzer_params=?, with_detail=true);
-- 使用集合字段上下文
ANALYZE ? ON TABLE books(body) WITH (analyzer_names=?);
```

ANALYZE 调用官方 SDK `runAnalyzer`，不扫描集合，也不在 Java 中分词。输入可为一个字符串或非空字符串列表；`?` 可用 `setString` 绑定单个文本，或用 `setObject` 绑定字符串列表/`String[]`。列表元素不能是 NULL，但允许空字符串。输入与 WITH 选项按 SQL 顺序绑定，文本内容不会拼入表达式。

WITH 接受以下选项，不接受未知名称：

| 选项 | 值与行为 |
| --- | --- |
| `analyzer_params` | Map、JsonObject 或 JSON 对象字符串；原样作为分词器配置传入 SDK。 |
| `with_detail` | 布尔值，默认保留 SDK 的 false。 |
| `with_hash` | 布尔值，默认保留 SDK 的 false。 |
| `analyzer_names` | 非 NULL 字符串列表，默认保留 SDK 空列表；选择语义及与字段配置的组合由服务端校验。 |

所有选项值均支持 `?`。可选 `ON TABLE collection(field)` 设置 SDK 集合和字段上下文，数据库取自当前连接；省略时保留 SDK 的空集合/字段默认值。不创建、修改或加载集合。分词器名称、语言、字段上下文和选项组合由服务端校验，拒绝时返回 SQLException。

返回一个 JDBC ResultSet：每个 SDK 文本结果一行，TEXT_INDEX 为从 1 开始的 BIGINT 序号，TOKENS 为 VARCHAR JSON 数组。每个 token 保留 SDK 的 `token`、`startOffset`、`endOffset`、`position`、`positionLength`、`hash`；缺失的 SDK 字段可能省略，未启用的详情字段也可能返回默认 0，不应当作有效测量值。偏移量使用服务端语义，不转换为 Java 字符索引；SDK 的 32 位无符号 hash 使用 long 表示。无 token 的文本仍有一行 `[]`。`setMaxRows` 限制文本结果行数，不截断一段文本的 token 数组，也不限制服务端分析的输入量。
