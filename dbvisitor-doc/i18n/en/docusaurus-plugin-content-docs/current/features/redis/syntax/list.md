---
id: list
sidebar_position: 3
title: List Command Set
---


| Command | Return | Rows | Result |
|---|---|---|---|
| [LMOVE](https://redis.io/docs/latest/commands/lmove/) | ResultSet | 1 | ELEMENT field, STRING type |
| [BLMOVE](https://redis.io/docs/latest/commands/blmove/) | ResultSet | 1 | ELEMENT field, STRING type |
| [LMPOP](https://redis.io/docs/latest/commands/lmpop/) | ResultSet | multiple | KEY field, STRING type (returns Key from Key,ValueList structure)<br/>ELEMENT field, STRING type |
| [BLMPOP](https://redis.io/docs/latest/commands/blmpop/) | ResultSet | multiple | KEY field, STRING type (returns Key from Key,ValueList structure)<br/>ELEMENT field, STRING type |
| [LPOP](https://redis.io/docs/latest/commands/lpop/) | ResultSet | multiple | ELEMENT field, STRING type |
| [RPOP](https://redis.io/docs/latest/commands/rpop/) | ResultSet | multiple | ELEMENT field, STRING type |
| [BLPOP](https://redis.io/docs/latest/commands/blpop/) | ResultSet | multiple | ELEMENT field, STRING type |
| [BRPOP](https://redis.io/docs/latest/commands/brpop/) | ResultSet | multiple | ELEMENT field, STRING type |
| [RPOPLPUSH](https://redis.io/docs/latest/commands/rpoplpush/) | ResultSet | 1 | ELEMENT field, STRING type |
| [BRPOPLPUSH](https://redis.io/docs/latest/commands/brpoplpush/) | ResultSet | 1 | ELEMENT field, STRING type |
| [LINDEX](https://redis.io/docs/latest/commands/lindex/) | ResultSet | 1 | ELEMENT field, STRING type |
| [LINSERT](https://redis.io/docs/latest/commands/linsert/) | Value | -- | Length of the list after insert; returns 0 when key doesn't exist; returns -1 when pivot not found. |
| [LLEN](https://redis.io/docs/latest/commands/llen/) | ResultSet | 1 | RESULT field, LONG type |
| [LPOS](https://redis.io/docs/latest/commands/lpos/) | ResultSet | multiple | RESULT field, LONG type |
| [LPUSH](https://redis.io/docs/latest/commands/lpush/) | Value | -- | Length of the list after the PUSH operation. |
| [LPUSHX](https://redis.io/docs/latest/commands/lpushx/) | Value | -- | Length of the list after the PUSH operation. |
| [RPUSH](https://redis.io/docs/latest/commands/rpush/) | Value | -- | Length of the list after the PUSH operation. |
| [RPUSHX](https://redis.io/docs/latest/commands/rpushx/) | Value | -- | Length of the list after the PUSH operation. |
| [LRANGE](https://redis.io/docs/latest/commands/lrange/) | ResultSet | multiple | ELEMENT field, STRING type |
| [LREM](https://redis.io/docs/latest/commands/lrem/) | Value | -- | Number of removed elements. |
| [LSET](https://redis.io/docs/latest/commands/lset/) | Value | -- | 1 if successful, 0 otherwise. (Success when status is "OK") |
| [LTRIM](https://redis.io/docs/latest/commands/ltrim/) | Value | -- | 1 if successful, 0 otherwise. (Success when status is "OK") |
