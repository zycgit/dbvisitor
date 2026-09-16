---
id: transactions
sidebar_position: 10
title: 10. Database Transactions
hide_table_of_contents: true
---

import CapabilityTable from '@site/src/components/CapabilityTable';
import matrix from '@site/src/data/capabilities/tables/transactions.json';

General usage: [10. Database Transactions](../../guides/transaction/about). Status links explain differences for each data source.

<CapabilityTable matrix={matrix} />

- Shared transactions: JdbcTemplate, Session, and the Builder API participate in the same transaction.
- Propagation: seven modes — `REQUIRED`, `REQUIRES_NEW`, `NESTED`, `SUPPORTS`, `NOT_SUPPORTED`, `MANDATORY`, and `NEVER`.
- Propagation counts: a mode counts as fully supported only when all its scenarios pass. Hover for each mode's status; follow the status link for partial-support limits.
- Limited: some calls work, but JDBC or database transaction capabilities impose restrictions. Follow the data source link for supported behavior and limits.
- Isolation levels: whether dbVisitor can apply transaction isolation settings for the data source, not whether every database accepts the same values. Follow the status link for accepted values and behavior.
