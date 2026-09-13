---
id: commands
sidebar_position: 1
title: Command Format and Parameters
---

Use MongoDB-style method calls without rewriting them as SQL.

```text
use test;
db.user_info.find({name: ?});
user_info.find({name: ?});
test.user_info.find({name: ?});
```

`db` refers to the current database. Omitting the database prefix also uses the current database. If none is selected in the URL, execute `use test` first or use `test.user_info` explicitly.

## Parameters and ObjectId

Use `?` for command values and `#{name}` in Mappers. Do not concatenate parameters into command text. Query an ObjectId field as follows:

```text
test.user_info.find({_id: ObjectId(?)})
```

Bind the hexadecimal string; a plain string condition does not match an ObjectId. Separate commands with semicolons. This is neither a JavaScript program nor JDBC batch.
