---
id: defaults
sidebar_position: 3
title: Field Constraints
---

## PRIMARY KEY, AUTO_ID and NULL

Fields default to NOT NULL. Declare NULL to enable nullable; primary keys cannot be nullable. PRIMARY KEY identifies the key, and AUTO_ID enables server-generated keys. Conflicting constraints are rejected. COMMENT 'text' supplies the field description.

## Default Values

`DEFAULT` is supported on non-primary scalar fields: `BOOL`, `INT8/INT16/INT32/INT64`, `FLOAT/DOUBLE`, and `VARCHAR`. Numeric defaults may have a sign. Integer defaults must fit exactly, floating defaults must be finite, and strings must fit the declared UTF-8 byte length. Invalid or duplicate defaults, and defaults on primary keys, JSON, Array, or vector fields, are rejected before CREATE is sent.

For ordinary INSERT or full UPSERT with omitted/null-bound fields, Milvus applies DEFAULT according to SDK rules. Nullable fields without defaults store NULL; the SDK/server validates non-nullable fields. SHOW CREATE preserves defaults, nullable, Array element/capacity, analyzers and functions. Nullable vectors require 2.6.18+ and do not support IS NULL/IS NOT NULL predicates; scalar nullable/Array retain the 2.6.2 baseline.
