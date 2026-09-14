---
id: escape
sidebar_position: 8
title: 6.7 参数符号转义
description: 在 SQL 或原生命令中输出字面的问号、冒号和与号，同时保留位置参数与名称参数绑定。
---

命令本身含有 `?`、`:` 或 `&`，又不希望它们被识别为参数时，可以在前面加反斜杠。dbVisitor 解析后移除这个反斜杠，输出符号本身，不占用绑定参数。

| 要输出的符号 | 命令模板写法 | Java 字符串写法 |
| --- | --- | --- |
| `?` | `\?` | `"\\?"` |
| `:` | `\:` | `"\\:"` |
| `&` | `\&` | `"\\&"` |

## 原生命令中的问号

例如 Elasticsearch 请求地址中的 `?refresh=true` 是 URL 参数，不是 JDBC 绑定参数。使用位置参数时：

```java
jdbcTemplate.executeUpdate(
    "PUT /users/_doc/1\\?refresh=true {\"id\":?,\"name\":?}",
    new Object[] { 1, "Alice" });
```

驱动收到的命令如下，只有 JSON 中的两个 `?` 需要绑定值：

```text
PUT /users/_doc/1?refresh=true {"id":?,"name":?}
```

同样支持名称参数：

```java
jdbcTemplate.executeUpdate(
    "PUT /users/_doc/1\\?refresh=true {\"id\":#{id},\"name\":#{name}}",
    Map.of("id", 1, "name", "Alice"));
```

多个 URL 参数之间的 `&` 也可以转义，例如 Java 字符串中的 `"\\?op_type=create\\&refresh=true"`。

## 冒号与绑定参数相邻

冒号后紧贴 `?` 或 `#{...}` 时，不需要为冒号加转义或空格：

```text
{"id":?,"name":?}
{"id":#{id},"name":#{name}}
```

使用 `:name` 时，字段冒号与参数冒号之间用空格分隔：

```text
{"id": :id,"name": :name}
```

如果命令中的 `:name` 本来就是普通文本，则写为 `\:name`；普通文本 `&name` 同理写为 `\&name`。

## 注意事项

- 转义的是**命令模板中的符号**。传入参数的值无需这样处理，仍通过 `?`、`:name` 或 `#{...}` 绑定。
- Java 字符串需要写 `\\?`；Mapper XML 的普通文本中直接写 `\?`。XML 中的 `&` 还需写为 `&amp;`，例如 `\&amp;pretty`。
- 引号和 SQL 注释内的内容保持原样，不应用上述转义规则。PostgreSQL 的 `::` 类型转换也无需转义。
- 连续反斜杠紧接参数符号时，奇数个中的最后一个用于转义；偶数个全部保留，后面的符号仍作为参数解析。其他反斜杠保持原样。
