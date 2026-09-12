---
id: keys
sidebar_position: 2
title: Keys Command Set
---


| Command | Return | Rows | Result |
|---|---|---|---|
| [COPY](https://redis.io/docs/latest/commands/COPY/) | Value | -- | 1 if successful; 0 if failed |
| [DEL](https://redis.io/docs/latest/commands/del/) | Value | -- | Integer > 0 if one or more keys removed; 0 if none of the specified keys exist |
| [UNLINK](https://redis.io/docs/latest/commands/unlink/) | ResultSet | 1 | RESULT field, LONG type |
| [DUMP](https://redis.io/docs/latest/commands/dump/) | ResultSet | 1 | VALUE field, BYTES type |
| [EXISTS](https://redis.io/docs/latest/commands/exists/) | ResultSet | 1 | RESULT field, LONG type |
| [EXPIRE](https://redis.io/docs/latest/commands/expire/) | Value | -- | 1 if timeout was set; 0 otherwise. |
| [EXPIREAT](https://redis.io/docs/latest/commands/expireat/) | Value | -- | 1 if timeout was set; 0 otherwise. |
| [EXPIRETIME](https://redis.io/docs/latest/commands/expiretime/) | ResultSet | 1 | RESULT field, LONG type |
| [PEXPIRE](https://redis.io/docs/latest/commands/pexpire/) | Value | -- | 1 if timeout was set; 0 otherwise. |
| [PEXPIREAT](https://redis.io/docs/latest/commands/pexpireat/) | Value | -- | 1 if timeout was set; 0 otherwise. |
| [PEXPIRETIME](https://redis.io/docs/latest/commands/pexpiretime/) | ResultSet | 1 | RESULT field, LONG type |
| [KEYS](https://redis.io/docs/latest/commands/keys/) | ResultSet | multiple | KEY field, STRING type (Note: the driver uses the scan command instead of the keys command) |
| [OBJECT ENCODING](https://redis.io/docs/latest/commands/object-encoding/) | ResultSet | 1 | RESULT field, STRING type |
| [OBJECT FREQ](https://redis.io/docs/latest/commands/object-freq/) | ResultSet | 1 | RESULT field, LONG type |
| [OBJECT IDLETIME](https://redis.io/docs/latest/commands/object-idletime/) | ResultSet | 1 | RESULT field, LONG type |
| [OBJECT REFCOUNT](https://redis.io/docs/latest/commands/object-refcount/) | ResultSet | 1 | RESULT field, LONG type |
| [PERSIST](https://redis.io/docs/latest/commands/persist/) | Value | -- | 1 when the key's expiry is removed; 0 when the key does not exist or has no expiry |
| [TTL](https://redis.io/docs/latest/commands/ttl/) | ResultSet | 1 | RESULT field, LONG type |
| [PTTL](https://redis.io/docs/latest/commands/pttl/) | ResultSet | 1 | RESULT field, LONG type |
| [RANDOMKEY](https://redis.io/docs/latest/commands/randomkey/) | ResultSet | 1 | KEY field, STRING type |
| [RENAME](https://redis.io/docs/latest/commands/rename/) | Value | -- | 1 on success; an existing destination is overwritten, and a missing source causes an error |
| [RENAMENX](https://redis.io/docs/latest/commands/renamenx/) | Value | -- | 1 if key was renamed; 0 if target key already exists. |
| [SCAN](https://redis.io/docs/latest/commands/scan/) | ResultSet | multiple | CURSOR field, STRING type<br/>KEY field, STRING type |
| [TOUCH](https://redis.io/docs/latest/commands/touch/) | Value | -- | Number of keys that were TOUCHed. |
| [TYPE](https://redis.io/docs/latest/commands/type/) | ResultSet | 1 | RESULT field, STRING type |
