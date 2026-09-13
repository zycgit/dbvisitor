---
id: about
slug: /features/redis/commands/server
sidebar_position: 0
hide_table_of_contents: true
title: 服务器命令
---

选择命令查看语法、参数、返回结果与示例。执行方式见[数据读写](../dbvisitor/usage.mdx)，参数占位符见[命令格式与参数](../basics/commands.md)。

| 命令 | 用途 |
| --- | --- |
| [EVAL](eval.md) | 执行服务端 Lua 脚本。 |
| [MOVE](move.md) | 将键移到另一个逻辑数据库。 |
| [WAIT](wait.md) | 等待此前写入得到指定数量副本的确认。 |
| [WAITAOF](waitaof.md) | 等待此前写入持久化到本机或副本的 AOF。 |
| [PING](ping.md) | 检查连接，可回显指定文本。 |
| [ECHO](echo.md) | 回显指定文本。 |
| [SELECT](select.md) | 切换当前连接的逻辑数据库。 |
| [INFO](info.md) | 读取服务器信息，按指标展开为结果行。 |
