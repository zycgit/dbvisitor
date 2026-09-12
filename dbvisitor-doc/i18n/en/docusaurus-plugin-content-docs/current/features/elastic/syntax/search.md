---
id: search
sidebar_position: 1
title: Search Operations
---


| Command | Description | Example |
|---|---|---|
| `GET .../_search` | Execute search query | `GET /my_index/_search { "query": { "match_all": {} } }` |
| `POST .../_search` | Execute search query | `POST /my_index/_search { "query": { "term": { "user": "kimchy" } } }` |
| `GET .../_count` | Count documents | `GET /my_index/_count` |
| `GET .../_msearch` | Batch search | `POST /my_index/_msearch [{}, {"query":{"match_all":{}}}]` |
| `GET .../_mget` | Batch get documents | `GET /my_index/_mget {"ids":["1","2"]}` |
| `GET .../_explain` | Get explanation info | `GET /my_index/_explain/1 {"query":{"match_all":{}}}` |
| `GET .../_source` | Get document source data | `GET /my_index/_source/1` |
