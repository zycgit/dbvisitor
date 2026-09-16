---
id: mapping-keys
sidebar_position: 6
title: 6. Object Mapping
hide_table_of_contents: true
---

import CapabilityTable from '@site/src/components/CapabilityTable';
import matrix from '@site/src/data/capabilities/tables/mapping-keys.json';

General usage: [5.7 Object Mapping](../../guides/core/mapping/about). Status links explain differences for each data source.

<CapabilityTable matrix={matrix} showCounts={false} />

- Table Mapping: default and explicit mappings, including ignored properties.
- Name Sensitivity: database rules for table/column name casing and `useDelimited` quoting.
- Result Column Case: use `caseInsensitive` to control how returned column names match properties.
- Write Policy: INSERT/UPDATE participation, NULL handling, and selected fields.
- JSON Field Mapping: map entity properties to one field; direct JSON value conversion is covered under [Type Handlers](./types#json).
- Key strategies: assigned, auto-increment, UUID, sequence, and custom generation; status links explain each datasource’s strategy boundaries.

Empty-string and array round trips are listed under [Type Handlers](./types).
