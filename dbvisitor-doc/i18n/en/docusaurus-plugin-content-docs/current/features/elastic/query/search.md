---
id: search
sidebar_position: 1
title: Search and Count
---

:::info[Note]
REST endpoints: `_search` and `_count`.
:::

## Search

```text
POST /user_info/_search {"query":{"term":{"uid":"1001"}},"size":10}
```

Map `uid` as keyword. Results contain `_ID` and `_DOC`, with document fields expanded by default. Read a specific field by column name; see [Reading Results](../dbvisitor/results.mdx).

## Count

```text
POST /user_info/_count {"query":{"term":{"uid":"1001"}}}
```

Use `_count` for the total, not the number of rows in a search page. See [Pagination](../dbvisitor/pagination.mdx) for dbVisitor Page usage.

## Aggregations

Single-value aggregations return one row, using aggregation names as column names:

```text
POST /user_info/_search {"aggs":{"max_age":{"max":{"field":"age"}}}}
```

Use `composite` to group by fields. This example returns `age` and `cnt` columns:

```text
POST /user_info/_search {
  "aggs": {
    "rows": {
      "composite": {"sources": [{"age": {"terms": {"field": "age", "missing_bucket": true}}}]},
      "aggs": {"cnt": {"filter": {"match_all": {}}}}
    }
  }
}
```

Remove the nested `aggs` to read distinct field combinations; list additional fields in `sources`. The driver fetches subsequent pages through `after_key` as needed. JDBC `fetchSize` controls the number of buckets per page.

For an exact distinct count, add `"meta":{"dbvisitor":{"mode":"count","column":"distinct_count"}}` inside `rows`. This traverses every distinct bucket and excludes combinations containing null; it is not a low-cost approximate count.

Search results do not expose the complete REST response, such as `_score` or `highlight`.

## Scripted Columns

Names declared in `script_fields` can be read as result columns. This example returns an `age_twice` column. A projection containing only scripted columns does not require index-mapping access:

```text
POST /user_info/_search {
  "_source": false,
  "script_fields": {
    "age_twice": {"script": {"source": "doc['age'].value * params.factor", "params": {"factor": 2}}}
  }
}
```

Bind dynamic values in `params`, rather than concatenating them into `source`. Single values return scalars, multiple values return a `List`, and empty values return `null`.
