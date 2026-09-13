---
id: commands
sidebar_position: 1
title: 命令格式与参数
---

使用 MongoDB 风格的方法调用，不需要改写为 SQL。

```text
use test;
db.user_info.find({name: ?});
user_info.find({name: ?});
test.user_info.find({name: ?});
```

`db` 表示当前数据库；省略数据库前缀也使用当前数据库。URL 未选择数据库时，先执行 `use test`，或写出 `test.user_info`。

## 参数与 ObjectId

命令值使用 `?`，Mapper 中使用 `#{name}`；不要将参数拼成命令字符串。ObjectId 字段按以下格式查询：

```text
test.user_info.find({_id: ObjectId(?)})
```

绑定十六进制字符串，不能用普通字符串条件代替 ObjectId。多个命令用分号分隔，不是 JavaScript 程序，也不是 JDBC batch。
