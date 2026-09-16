---
id: results
sidebar_position: 9
title: 9. Receiving Results
hide_table_of_contents: true
---

import CapabilityTable from '@site/src/components/CapabilityTable';
import matrix from '@site/src/data/capabilities/tables/results.json';

General usage: [9. Receiving Results](../../guides/result/about). Status links explain differences for each data source.

<CapabilityTable matrix={matrix} showCounts={false} />

- API entry points: Programmatic API, Mapper API (method annotations), Builder API, and Mapper files.
- Partial support: follow the status link to see which entry points are available. An unavailable query entry point does not mean a handler cannot process the result.
- Row mapping: use `RowMapper` to map each row to an object.
- Row callbacks: use `RowCallbackHandler` to consume rows individually.
- Result extraction: use `ResultSetExtractor` to process a result set; [built-in extractors](../../guides/result/for_extractor#inner) also provide filtering, pairs, and other operations.
