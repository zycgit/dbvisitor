---
id: types
sidebar_position: 8
title: 8. Type Handlers
hide_table_of_contents: true
---

import CapabilityTable from '@site/src/components/CapabilityTable';
import matrix from '@site/src/data/capabilities/tables/types.json';

General usage: [8. Type Handlers](../../guides/types/about). Status links explain differences for each data source.

<CapabilityTable matrix={matrix} showCounts={false} />

- Basic types: numbers, booleans, characters, and their NULL values.
- Time types: dates, times, time zones, date parts, precision, and NULL values.
- Custom handlers: use a field handler for entity writes and reads.
- JSON serialization: Map, List, Set, Bean, and NULL conversion.
- Array types: binding, reading, updating, and NULL round trips.

See [Vector Operations](./vectors) for vector type mapping and search support.
