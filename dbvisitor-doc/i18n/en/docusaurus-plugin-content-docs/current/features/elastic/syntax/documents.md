---
id: documents
sidebar_position: 2
title: Document Operations
---


| Command | Description | Example |
|---|---|---|
| `PUT .../_doc/...` | Create or update document | `PUT /my_index/_doc/1 { "user": "kimchy" }` |
| `POST .../_doc/...` | Create document | `POST /my_index/_doc/ { "user": "kimchy" }` |
| `POST .../_create/...` | Create document (fails if exists) | `POST /my_index/_create/1 { "user": "kimchy" }` |
| `POST .../_update/...` | Update document | `POST /my_index/_update/1 { "doc": { "age": 20 } }` |
| `POST .../_update_by_query` | Update by query | `POST /my_index/_update_by_query { "script": ... }` |
| `DELETE ...` | Delete document | `DELETE /my_index/_doc/1` |
| `POST .../_delete_by_query` | Delete by query | `POST /my_index/_delete_by_query { "query": ... }` |
