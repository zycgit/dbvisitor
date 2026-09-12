---
id: server
sidebar_position: 4
title: Server 命令集
---


| 命令                                                        | 返回值 | 行数  | 结果                                       |
|-----------------------------------------------------------|-----|-----|------------------------------------------|
| [MOVE](https://redis.io/docs/latest/commands/move/)       | 值   | --  | 如果 key 被移动，则为 1；未被移动则为 0                 |
| [WAIT](https://redis.io/docs/latest/commands/wait/)       | 结果集 | 1   | REPLICAS 字段，LONG 类型                      |
| [WAITAOF](https://redis.io/docs/latest/commands/waitaof/) | 结果集 | 1   | LOCAL 字段，LONG 类型<br/>REPLICAS 字段，LONG 类型 |
| [PING](https://redis.io/docs/latest/commands/ping/)       | 结果集 | 1   | RESULT 字段，STRING 类型                      |
| [ECHO](https://redis.io/docs/latest/commands/echo/)       | 结果集 | 1   | RESULT 字段，STRING 类型                      |
| [SELECT](https://redis.io/docs/latest/commands/select/)   | 值   | --  | 操作成功返回 1，否则抛出异常                          |
