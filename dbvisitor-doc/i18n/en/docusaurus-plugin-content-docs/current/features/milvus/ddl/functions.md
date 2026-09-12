---
id: functions
slug: /features/milvus/sql/functions
sidebar_position: 16
title: FUNCTION Definitions
---

:::info[Note]
SDK methods: `createCollection`.
:::

## Syntax

```text
FUNCTION function_name USING { BM25 | TEXTEMBEDDING }
    (input_field [, ...]) INTO (output_field [, ...])
    [DESCRIPTION description]
    [WITH (option = value [, ...])];

```

FUNCTION is a definition clause inside CREATE TABLE or ALTER TABLE, not a standalone statement. Function type determines valid input/output fields and their counts. ANALYZE is a standalone statement invoking an analyzer and returning token information; it neither creates functions nor writes entities or searches vectors.

## BM25 / TextEmbedding schema functions {#functions}

Declare functions after fields. Text input is non-nullable VARCHAR; omit server-generated output fields from INSERT/UPSERT.

```sql
CREATE TABLE docs (
    id INT64 PRIMARY KEY,
    body VARCHAR(2000) WITH (enable_analyzer=true, analyzer_params='{"type":"standard"}'),
    dense FLOAT_VECTOR(2), sparse SPARSE_FLOAT_VECTOR,
    FUNCTION bm25_fn USING BM25 (body) INTO (sparse)
);
CREATE INDEX bm25_idx ON docs(sparse) USING SPARSE_INVERTED_INDEX
    WITH (metric_type=BM25,bm25_k1=1.2,bm25_b=0.75);
SELECT id,score FROM docs ORDER BY sparse <?> 'hybrid search' LIMIT 10;

CREATE TABLE embedded_docs (
    id INT64 PRIMARY KEY, body VARCHAR(2000), dense FLOAT_VECTOR(1536),
    FUNCTION embed USING TEXTEMBEDDING (body) INTO (dense)
        WITH (provider='openai',model_name='text-embedding-3-small')
);
SELECT id,score FROM embedded_docs ORDER BY dense <=> 'search text' LIMIT 10;
```

BM25 requires enable_analyzer=true and Sparse output. TextEmbedding uses dense float output. Field WITH accepts enable_analyzer/enable_match booleans and analyzer_params as a JSON object string. Function WITH scalar values become SDK string parameters. TextEmbedding requires suitable server/provider/model/network/credential configuration and matching dimensions; configure the external model service before running the example. Configure secrets on the server instead of exposing them through SQL/SHOW CREATE. Current schema functions are BM25/TEXTEMBEDDING, not every future FunctionType.

Function definitions accept `INTO (...) DESCRIPTION 'text' WITH (...)`. DESCRIPTION also accepts a `?` bound to a non-null string. It maps to the dedicated SDK description field and is preserved by SHOW CREATE TABLE; WITH (description=...) remains a function parameter. Omitting DESCRIPTION uses an empty string.

In description literals, write a single quote as `''` and a backslash as `\\`; `\n`, `\r` and `\t` represent newline, carriage return and tab. PreparedStatement values are passed directly without SQL escaping. SHOW CREATE emits a description literal that can be parsed again.

<span id="alter-functions" />

<span id="analyze" />
