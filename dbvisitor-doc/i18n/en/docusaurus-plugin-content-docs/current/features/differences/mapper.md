---
id: mapper
sidebar_position: 2
title: 2. Mapper API
hide_table_of_contents: true
---

import CapabilityTable from '@site/src/components/CapabilityTable';
import matrix from '@site/src/data/capabilities/tables/mapper.json';

For general usage, see [5.2 Mapper API](../../guides/core/mapper/about); shared capabilities are covered in [Parameters and Rules](./parameters.md) and [Receiving Results](./results.md). Status links in the table explain data-source-specific behavior.

<CapabilityTable matrix={matrix} showCounts={false} />

- Method annotations: queries, writes, `@Execute`, multiline command assembly, and error handling.
- Mapper reads and writes: BaseMapper CRUD, replacement, upsert, sample queries, and writes using Map arguments.
- Key strategies: assigned and default keys, generated-key retrieval, `selectKey`, and composite keys.
- Pagination: method annotations, BaseMapper, and file-mapper calls.
- Execution options: statement mode, timeout, fetch settings, and result-set types, including scrolling.
- Calling builders: obtain and use a builder through a Mapper or Session.
- Referencing file mappers: query, write, and read return values by statement ID.
- Session management: create mappers, reuse sessions, and access data across APIs.
