---
id: vectors
sidebar_position: 5
title: 5. Vector Operations
hide_table_of_contents: true
---

import CapabilityTable from '@site/src/components/CapabilityTable';
import matrix from '@site/src/data/capabilities/tables/vectors.json';

General usage: [5.6 Vector Query](../../guides/core/vector_query/about). Status links explain differences for each data source.

<CapabilityTable matrix={matrix} showCounts={false} />

- Vector Type Mapping: vector field mapping and reads/writes. See [8.9 Vector Type Handlers](../../guides/types/vector-handler) for configuration.
- Vector Search: nearest-neighbor ordering, range filters, and combined vector/scalar conditions. Vector storage support does not imply builder-based vector search.
