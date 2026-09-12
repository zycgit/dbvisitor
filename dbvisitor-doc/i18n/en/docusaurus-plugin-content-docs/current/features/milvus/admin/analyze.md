---
id: analyze
sidebar_position: 1
title: ANALYZE
---

:::info[Note]
SDK methods: `runAnalyzer`.
:::

## Inspect Analyzer Results {#analyze}

```sql
ANALYZE 'hello world' WITH (analyzer_params='{"tokenizer":"standard"}');
ANALYZE ['hello world', '', 'milvus'] WITH (with_detail=true, with_hash=true);
ANALYZE ? WITH (analyzer_params=?, with_detail=true);
-- Use collection field context
ANALYZE ? ON TABLE books(body) WITH (analyzer_names=?);
```

ANALYZE calls official SDK `runAnalyzer`; it neither scans a collection nor tokenizes in Java. Input is a string or a nonempty list of strings. Bind a single text with `setString`, or a string list/`String[]` with `setObject`. List elements cannot be NULL, but empty strings are allowed. Inputs and WITH options bind in SQL order; text is never interpolated into an expression.

WITH accepts the following options and rejects unknown names:

| Option | Value and behavior |
| --- | --- |
| `analyzer_params` | Map, JsonObject, or JSON object string, passed to the SDK as analyzer configuration. |
| `with_detail` | Boolean; preserves the SDK default of false. |
| `with_hash` | Boolean; preserves the SDK default of false. |
| `analyzer_names` | List of non-NULL strings; preserves the SDK empty-list default. The server validates selection semantics and combinations with field configuration. |

Every option value supports `?`. Optional `ON TABLE collection(field)` sets the SDK collection and field context, using the connection database. Omitting it preserves the SDK's empty collection/field defaults. No collection is created, modified, or loaded. The server validates analyzer names, languages, field contexts, and option combinations; rejection is returned as SQLException.

One JDBC ResultSet is returned, with a row for each SDK text result: TEXT_INDEX is a 1-based BIGINT ordinal, and TOKENS is a VARCHAR JSON array. Tokens preserve SDK fields `token`, `startOffset`, `endOffset`, `position`, `positionLength`, and `hash`. Missing SDK fields may be omitted; disabled detail fields may contain default zero values, which are not meaningful measurements. Offsets retain server semantics rather than being converted to Java character indexes. The SDK's unsigned 32-bit hash is represented as a long. Texts without tokens still have a row containing `[]`. `setMaxRows` limits text-result rows, not the token array for a text or the number of inputs analyzed by the server.
