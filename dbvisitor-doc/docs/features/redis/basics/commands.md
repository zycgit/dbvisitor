---
id: commands
sidebar_position: 1
title: 命令格式与参数
---

使用 Redis 命令名和参数，不需要转换为 SQL。多条命令用换行分隔。

```text
SET user:1001:name mali
GET user:1001:name
DEL user:1001:name
```

动态值用 `?` 绑定，含空格的文本也作为一个参数传入：

```java
jdbc.executeUpdate("SET ? ?", new Object[] {"user:1001:name", "Alice Smith"});
String name = jdbc.queryForString("GET ?", "user:1001:name");
```

Mapper 使用 `#{key}`、`#{value}`，完整例子见[数据读写](../dbvisitor/usage.mdx)。命令名和 NX、EX 等选项直接写在命令中。

命令清单见 [Redis](../about.md)。语法中的 `[]` 表示可选项，`|` 表示择一，`...` 表示可重复；这些符号不写入实际命令。可选项按各页语法所列顺序填写。

示例使用 `demo:` 前缀的演示键。请在测试数据库运行，避免与业务键重名；重复运行可能覆盖或累加演示数据。
