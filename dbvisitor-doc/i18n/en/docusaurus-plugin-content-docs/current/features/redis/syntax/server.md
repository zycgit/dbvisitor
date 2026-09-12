---
id: server
sidebar_position: 4
title: Server Command Set
---


| Command | Return | Rows | Result |
|---|---|---|---|
| [MOVE](https://redis.io/docs/latest/commands/move/) | Value | -- | 1 if key was moved; 0 otherwise |
| [WAIT](https://redis.io/docs/latest/commands/wait/) | ResultSet | 1 | REPLICAS field, LONG type |
| [WAITAOF](https://redis.io/docs/latest/commands/waitaof/) | ResultSet | 1 | LOCAL field, LONG type<br/>REPLICAS field, LONG type |
| [PING](https://redis.io/docs/latest/commands/ping/) | ResultSet | 1 | RESULT field, STRING type |
| [ECHO](https://redis.io/docs/latest/commands/echo/) | ResultSet | 1 | RESULT field, STRING type |
| [SELECT](https://redis.io/docs/latest/commands/select/) | Value | -- | Returns 1 on success; throws exception otherwise |
