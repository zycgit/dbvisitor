---
id: transactions
slug: /features/clickhouse/transactions
sidebar_position: 100
title: Transaction Support
---

ClickHouse JDBC transaction behavior depends on the driver version, connection settings, and server capabilities. Framework transaction APIs cannot guarantee commit or rollback on a connection without real transactions.

If one INSERT succeeds and the next fails, do not rely on rollback to undo the first. Some JDBC compatibility modes accept transaction calls but only log them when there is no underlying transaction; see the [ClickHouse JDBC compatibility implementation](https://github.com/ClickHouse/clickhouse-java/blob/v0.6.3/clickhouse-jdbc/src/main/java/com/clickhouse/jdbc/internal/JdbcTransaction.java).

## Transaction annotations {#annotations}

Annotations declare call boundaries; they do not establish database rollback support. A successful method call alone does not prove that a transaction took effect.

## Transaction templates {#templates}

Templates organize callback execution, but an exception from the callback does not guarantee that completed writes are undone.

## Programmatic transactions {#programmatic}

Explicit begin, commit, and rollback calls still depend on the JDBC implementation. A driver may reject them or provide compatibility behavior only; the absence of an exception does not prove that rollback succeeded.

## Transactions across APIs {#shared-transactions}

Sharing a data source among JdbcTemplate, Mapper, and the Builder API does not make their calls atomic. Sharing a transaction context also requires actual database transaction support.

## Transaction propagation {#propagation}

With no active transaction, `SUPPORTS` and `NEVER` can execute commands. The framework provides propagation rules, but joining, suspending, and resuming transactions is distinct from committing or rolling back data. Rollback under `NESTED` also depends on savepoints.

## Isolation levels {#isolation}

The current connection does not provide effective transaction isolation settings. Accepting or returning a JDBC isolation value does not establish its guarantees; waiting for mutations is not transaction isolation.

See [Database Transactions](../../guides/transaction/about.md) for general usage. “Limited” in the comparison table refers to these call or transaction-effect restrictions, not to missing framework APIs.
