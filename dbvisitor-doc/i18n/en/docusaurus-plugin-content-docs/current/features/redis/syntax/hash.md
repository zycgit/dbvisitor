---
id: hash
sidebar_position: 1
title: Hash Command Set
---


| Command | Return | Rows | Result |
|---|---|---|---|
| [HDEL](https://redis.io/docs/latest/commands/hdel/) | Value | -- | Number of fields deleted from the hash, not including non-existing fields. Returns 0 if the key does not exist. |
| [HEXISTS](https://redis.io/docs/latest/commands/hexists/) | ResultSet | 1 | RESULT field, BOOLEAN type |
| [HEXPIRE](https://redis.io/docs/latest/commands/hexpire/) | ResultSet | multiple | RESULT field, LONG type |
| [HEXPIREAT](https://redis.io/docs/latest/commands/hexpireat/) | ResultSet | multiple | RESULT field, LONG type |
| [HEXPIRETIME](https://redis.io/docs/latest/commands/hexpiretime/) | ResultSet | multiple | RESULT field, LONG type |
| [HPEXPIRE](https://redis.io/docs/latest/commands/hpexpire/) | ResultSet | multiple | RESULT field, LONG type |
| [HPEXPIREAT](https://redis.io/docs/latest/commands/hpexpireat/) | ResultSet | multiple | RESULT field, LONG type |
| [HPEXPIRETIME](https://redis.io/docs/latest/commands/hpexpiretime/) | ResultSet | multiple | RESULT field, LONG type |
| [HGET](https://redis.io/docs/latest/commands/hget/) | ResultSet | 1 | VALUE field, STRING type |
| [HGETALL](https://redis.io/docs/latest/commands/hgetall/) | ResultSet | multiple | FIELD field, STRING type<br/>VALUE field, STRING type |
| [HINCRBY](https://redis.io/docs/latest/commands/hincrby/) | ResultSet | 1 | VALUE field, LONG type |
| [HKEYS](https://redis.io/docs/latest/commands/hkeys/) | ResultSet | multiple | FIELD field, STRING type |
| [HLEN](https://redis.io/docs/latest/commands/hlen/) | ResultSet | 1 | RESULT field, LONG type |
| [HMGET](https://redis.io/docs/latest/commands/hmget/) | ResultSet | multiple | VALUE field, STRING type |
| [HSET](https://redis.io/docs/latest/commands/hset/) | Value | -- | Number of fields added. |
| [HMSET](https://redis.io/docs/latest/commands/hmset/) | Value | -- | Number of fields added. |
| [HSETNX](https://redis.io/docs/latest/commands/hsetnx/) | Value | -- | Returns 0 if the field already exists; returns 1 if a new field was created. |
| [HPERSIST](https://redis.io/docs/latest/commands/hpersist/) | ResultSet | multiple | RESULT field, LONG type |
| [HTTL](https://redis.io/docs/latest/commands/httl/) | ResultSet | multiple | RESULT field, LONG type |
| [HPTTL](https://redis.io/docs/latest/commands/hpttl/) | ResultSet | multiple | RESULT field, LONG type |
| [HRANDFIELD](https://redis.io/docs/latest/commands/hrandfield/) | ResultSet | multiple | FIELD field, STRING type<br/>VALUE field, STRING type (when using WITHVALUES) |
| [HSCAN](https://redis.io/docs/latest/commands/hscan/) | ResultSet | multiple | CURSOR field, STRING type<br/>FIELD field, STRING type<br/>VALUE field, STRING type (when not using NOVALUES) |
| [HSTRLEN](https://redis.io/docs/latest/commands/hstrlen/) | ResultSet | 1 | RESULT field, LONG type |
| [HVALS](https://redis.io/docs/latest/commands/hvals/) | ResultSet | multiple | VALUE field, STRING type |
