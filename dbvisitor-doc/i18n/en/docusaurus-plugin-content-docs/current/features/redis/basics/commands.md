---
id: commands
sidebar_position: 1
title: Command Format and Parameters
---

Use Redis command names and arguments without converting them to SQL. Separate multiple commands with newlines.

```text
SET user:1001:name mali
GET user:1001:name
DEL user:1001:name
```

Bind dynamic values with `?`; text containing spaces remains one argument:

```java
jdbc.executeUpdate("SET ? ?", new Object[] {"user:1001:name", "Alice Smith"});
String name = jdbc.queryForString("GET ?", "user:1001:name");
```

Mappers use `#{key}` and `#{value}`; see [Reading and Writing Data](../dbvisitor/usage.mdx). Write command names and options such as NX and EX directly in the command.

See [Redis](../about.md) for the command list. In syntax templates, `[]` marks optional items, `|` alternatives and `...` repetition; do not include these symbols in commands. Keep optional arguments in the order shown.

Examples use `demo:` keys. Run them in a test database to avoid business-key collisions; repeating examples can overwrite or accumulate demonstration data.
