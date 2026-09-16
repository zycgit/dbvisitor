---
id: mapper-files
sidebar_position: 4
title: 4. Mapper Files
hide_table_of_contents: true
---

import CapabilityTable from '@site/src/components/CapabilityTable';
import matrix from '@site/src/data/capabilities/tables/mapper-files.json';

General usage: [5.4 Mapper Files](../../guides/core/file/about). Status links explain differences for each data source.Shared parameter capabilities are listed under [Parameters and Rules](./parameters.md).

<CapabilityTable matrix={matrix} showCounts={false} />

- Command Execution: load files, resolve statements, and execute native commands, including repeated calls and no-match results.
- Execution Options: `statementType`, `fetchSize`, `timeout`, `resultSetType`, and scrollable results.
- Key Strategies: assigned keys, generated keys, and values filled through `selectKey`.
- Stored Procedure Calls: input, output, INOUT, and cursor parameters.
- sql tag: reuse command fragments through `<sql>` and `<include>`.
- Dynamic SQL: generate commands using conditional and loop tags.
- Mapping Result Sets: use `resultType` and `resultMap` for entities, maps, and scalars. Custom receiving interfaces are covered under [Receiving Results](./results.md).
- Paging Queries: retrieve page data and totals.
- Call File Mapper: a Java interface uses `@RefMapper` to call file commands, dynamic content, and result mappings.
