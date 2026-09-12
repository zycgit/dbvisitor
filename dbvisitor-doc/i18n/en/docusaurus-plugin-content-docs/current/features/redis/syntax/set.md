---
id: set
sidebar_position: 5
title: Set Command Set
---


| Command | Return | Rows | Result |
|---|---|---|---|
| [SADD](https://redis.io/docs/latest/commands/sadd/) | Value | -- | Number of elements added to the set, not including elements already present. |
| [SCARD](https://redis.io/docs/latest/commands/scard/) | ResultSet | 1 | RESULT field, LONG type |
| [SDIFF](https://redis.io/docs/latest/commands/sdiff/) | ResultSet | multiple | ELEMENT field, STRING type |
| [SDIFFSTORE](https://redis.io/docs/latest/commands/sdiffstore/) | Value | -- | Number of elements in the resulting set. |
| [SINTER](https://redis.io/docs/latest/commands/sinter/) | ResultSet | multiple | ELEMENT field, STRING type |
| [SINTERCARD](https://redis.io/docs/latest/commands/sintercard/) | ResultSet | 1 | RESULT field, LONG type |
| [SINTERSTORE](https://redis.io/docs/latest/commands/sinterstore/) | Value | -- | Number of elements in the resulting set. |
| [SISMEMBER](https://redis.io/docs/latest/commands/sismember/) | ResultSet | 1 | RESULT field, LONG type |
| [SMISMEMBER](https://redis.io/docs/latest/commands/smismember/) | ResultSet | multiple | RESULT field, BOOLEAN type |
| [SMEMBERS](https://redis.io/docs/latest/commands/smembers/) | ResultSet | multiple | ELEMENT field, STRING type |
| [SMOVE](https://redis.io/docs/latest/commands/smove/) | Value | -- | 1 if element was moved; 0 otherwise |
| [SPOP](https://redis.io/docs/latest/commands/spop/) | ResultSet | multiple | ELEMENT field, STRING type |
| [SRANDMEMBER](https://redis.io/docs/latest/commands/srandmember/) | ResultSet | multiple | ELEMENT field, STRING type |
| [SREM](https://redis.io/docs/latest/commands/srem/) | Value | -- | Number of members removed from the set, not including non-existing members. |
| [SSCAN](https://redis.io/docs/latest/commands/sscan/) | ResultSet | multiple | CURSOR field, STRING type<br/>ELEMENT field, STRING type |
| [SUNION](https://redis.io/docs/latest/commands/sunion/) | ResultSet | multiple | ELEMENT field, STRING type |
| [SUNIONSTORE](https://redis.io/docs/latest/commands/sunionstore/) | Value | -- | Number of elements in the resulting set. |
