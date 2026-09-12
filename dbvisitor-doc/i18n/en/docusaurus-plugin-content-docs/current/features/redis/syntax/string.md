---
id: string
sidebar_position: 7
title: String Command Set
---


| Command | Return | Rows | Result |
|---|---|---|---|
| [SET](https://redis.io/docs/latest/commands/set/) | Value/ResultSet | --/1 | Value: When not using GET, returns 0 (not set) or 1 (set successfully)<br/>ResultSet: When using GET, VALUE field, STRING type |
| [GET](https://redis.io/docs/latest/commands/get/) | ResultSet | 1 | VALUE field, STRING type |
| [INCR](https://redis.io/docs/latest/commands/incr/) | ResultSet | 1 | VALUE field, LONG type |
| [INCRBY](https://redis.io/docs/latest/commands/incrby/) | ResultSet | 1 | VALUE field, LONG type |
| [DECR](https://redis.io/docs/latest/commands/decr/) | ResultSet | 1 | VALUE field, LONG type |
| [DECRBY](https://redis.io/docs/latest/commands/decrby/) | ResultSet | 1 | VALUE field, LONG type |
| [APPEND](https://redis.io/docs/latest/commands/append/) | ResultSet | 1 | RESULT field, LONG type |
| [GETDEL](https://redis.io/docs/latest/commands/getdel/) | ResultSet | 1 | VALUE field, STRING type |
| [GETEX](https://redis.io/docs/latest/commands/getex/) | ResultSet | 1 | VALUE field, STRING type |
| [GETRANGE](https://redis.io/docs/latest/commands/getrange/) | ResultSet | 1 | VALUE field, STRING type |
| [GETSET](https://redis.io/docs/latest/commands/getset/) | ResultSet | 1 | VALUE field, STRING type |
| [MGET](https://redis.io/docs/latest/commands/mget/) | ResultSet | multiple | KEY field, STRING type<br/>VALUE field, STRING type |
| [MSET](https://redis.io/docs/latest/commands/mset/) | Value | -- | Number of keys added. |
| [MSETNX](https://redis.io/docs/latest/commands/msetnx/) | Value | -- | Returns 0 if no keys were set (at least one key already exists); returns the number of keys if all keys were set. |
| [PSETEX](https://redis.io/docs/latest/commands/psetex/) | Value | -- | Returns 1 if successful; 0 otherwise. (Success when status is "OK") |
| [SETEX](https://redis.io/docs/latest/commands/setex/) | Value | -- | Returns 1 if successful; 0 otherwise. (Success when status is "OK") |
| [SETNX](https://redis.io/docs/latest/commands/setnx/) | Value | -- | 1 if key was set; 0 otherwise |
| [SETRANGE](https://redis.io/docs/latest/commands/setrange/) | Value | -- | Length of the string after modification. |
| [STRLEN](https://redis.io/docs/latest/commands/strlen/) | ResultSet | 1 | RESULT field, LONG type |
| [SUBSTR](https://redis.io/docs/latest/commands/substr/) | ResultSet | 1 | VALUE field, STRING type |
