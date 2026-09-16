---
id: jdbc
sidebar_position: 1
title: 1. Programmatic API
hide_table_of_contents: true
---

import CapabilityTable from '@site/src/components/CapabilityTable';
import matrix from '@site/src/data/capabilities/tables/jdbc.json';

For general usage, see [5.1 Programmatic API](../../guides/core/jdbc/about); shared capabilities are covered in [Parameters and Rules](./parameters.md) and [Receiving Results](./results.md). Status links in the table explain data-source-specific behavior.

<CapabilityTable matrix={matrix} showCounts={false} />

- Updates: execute insert, update, and delete commands and read affected counts; both SQL and database-native commands are accepted.
- Queries: beans, maps, single values, lists, and counts, including column-name matching and repeated queries.
- Key-value queries: `queryForPairs` reads the first two columns as Map keys and values.
- Batch operations: multiple writes, empty and larger batches, and error handling; JDBC batch execution and atomicity are not guaranteed.
- Multiple results: `multipleExecute` and `call` collect command result sets, including naming and mapping. Reading results through `call` does not imply database stored-procedure support.
- Stored procedures and functions: bind parameters and read scalars, records, tables, or cursors; status links describe the available invocation forms.
