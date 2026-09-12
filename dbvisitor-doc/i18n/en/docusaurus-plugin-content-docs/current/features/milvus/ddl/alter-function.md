---
id: alter-function
sidebar_position: 17
title: ALTER TABLE … FUNCTION
---

:::info[Note]
SDK methods: `addCollectionFunction`, `alterCollectionFunction`, `dropCollectionFunction`.
:::

### Online function management {#alter-functions}

When the server provides the corresponding APIs, submit these commands against an existing collection:

```sql
ALTER TABLE online_docs ADD FUNCTION embed USING TEXTEMBEDDING (body) INTO (dense)
    DESCRIPTION 'Document embedding'
    WITH (provider='openai',model_name='text-embedding-3-small');
ALTER TABLE online_docs ALTER FUNCTION embed USING TEXTEMBEDDING (body) INTO (dense)
    DESCRIPTION 'Updated description'
    WITH (provider='openai',model_name='text-embedding-3-small');
ALTER TABLE online_docs DROP FUNCTION embed;
```

online_docs must already contain suitable input/output fields; TextEmbedding also requires the model service configuration described above. ADD/ALTER reuse CREATE TABLE's BM25/TEXTEMBEDDING definition. ALTER submits a complete replacement, not a parameter merge. The function name identifies the function being replaced; renaming is not exposed. Operations use the connection's current database without implicitly adding fields, loading collections, rebuilding indexes or backfilling existing data. Schema constraints, required load state and handling of existing data follow server rules.

Each SQL command maps to one official addCollectionFunction, alterCollectionFunction or dropCollectionFunction request. Success returns update count 0 and no ResultSet. Names cannot be bound with `?`; descriptions and WITH values can. Failures surface as SQLException and stop subsequent statements, without a DROP/CREATE fallback.

**Version boundary: Milvus 2.6.2 returns UNIMPLEMENTED for all three online APIs.** Network errors and other execution failures do not guarantee rollback. See the official [add function API](https://milvus.io/api-reference/java/v2.6.x/v2/Collections/addCollectionFunction.md) and [alter function API](https://milvus.io/api-reference/java/v2.6.x/v2/Collections/alterCollectionFunction.md).
