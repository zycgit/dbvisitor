---
id: parameters
sidebar_position: 7
title: 7. Parameters and Rules
hide_table_of_contents: true
---

import CapabilityTable from '@site/src/components/CapabilityTable';
import matrix from '@site/src/data/capabilities/tables/parameters.json';

General usage: [Parameter Passing](../../guides/args/about) and [SQL Rules](../../guides/rules/about). This table consolidates shared parameter capabilities across APIs. Status links explain datasource-specific differences.

<CapabilityTable matrix={matrix} showCounts={false} />

- Positional parameters: bind values by position in programmatic calls and method annotations, including NULL.
- Named parameters: bind named values from Maps, Beans, and nested properties in programmatic calls, method annotations, and Mapper files.
- Text substitution: use only trusted identifiers or fragments; bind user-provided values.
- Mixed sources: combine Array, Bean, and Map parameter sources.
- Explicit types: specify JDBC types and TypeHandlers through SqlArg.
- Binding and reuse: PreparedStatement binding, reuse, NULL and empty strings.
- General rules: select native commands conditionally and bind parameters only in the active branch.
- SQL fragment rules: generate SQL AND, OR, IN, and SET fragments; not a translation into non-SQL commands.
