---
id: builder
sidebar_position: 3
title: 3. Builder API
hide_table_of_contents: true
---

import CapabilityTable from '@site/src/components/CapabilityTable';
import matrix from '@site/src/data/capabilities/tables/builder.json';

General usage: [5.3 Builder API](../../guides/core/lambda/about). Status links explain differences for each data source.

<CapabilityTable matrix={matrix} showCounts={false} />

- Data Writes: inserts, updates, and deletes, including multiple operations, empty-condition protection, affected counts, and readback.
- Write Conflicts: default inserts, Ignore, and Update strategies.
- Query: field selection, entity and Map results, scalars, calculated columns, counts, and distinct queries.
- Map Query Mode: mapped Map uses Java property names; freedom Map uses database column names. Both include reads and writes.
- Pagination: pages, totals, navigation, and iteration in pages.
- Where Builder: comparisons, ranges, collections, LIKE, NULL, empty strings, and condition groups.
- Predicate Values: value binding in condition methods and apply, including special characters and boundary values.
- Group By: grouped queries and aggregate results.
- Order By: ascending and descending order, multi-column priority, and null ordering.
