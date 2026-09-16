---
id: transactions
slug: /features/milvus/transactions
sidebar_position: 100
title: Transaction Support
---

JDBC Milvus does not provide database commit or rollback. Transaction APIs and propagation settings cannot make multiple writes all-or-nothing.

If a paged UPDATE finishes two pages and fails on the third, the first two are not rolled back. See [UPDATE](../write/update.md) for failure progress and retries.

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

This JDBC path does not support transaction isolation settings. Milvus consistency levels control query visibility, not JDBC transaction isolation. Strong consistency does not add rollback for multiple writes.

See [Database Transactions](../../../guides/transaction/about.md) for general usage. “Limited” in the comparison table refers to these call or transaction-effect restrictions, not to missing framework APIs.
