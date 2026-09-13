---
id: requests
sidebar_position: 1
title: Request Format and Parameters
---

A command contains an HTTP method, a path and an optional JSON body. Use REST requests, not Elasticsearch SQL.

```text
POST /user_info/_search {"query":{"term":{"uid": ?}}}
PUT /user_info/_doc/{?}?refresh=wait_for {"name": ?}
```

- Use `?` for JSON values and `{?}` for path and URL parameter values. Bind in occurrence order.
- Quote JSON field names. Do not add quotes around bound string values yourself.
- Separate requests with semicolons. These are multiple statements, not JDBC batch.

`GET /_cat/...` adds `format=json`; an explicit format must also be json. Generic REST supports GET, POST, PUT and DELETE. HEAD returns the HTTP status code in the `STATUS` column.

See [Command Results](results.md) for return formats.
