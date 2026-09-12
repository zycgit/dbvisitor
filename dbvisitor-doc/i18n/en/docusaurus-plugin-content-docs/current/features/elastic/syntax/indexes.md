---
id: indexes
sidebar_position: 3
title: Index Management
---


| Command | Description | Example |
|---|---|---|
| `PUT /index` | Create index | `PUT /new_index` |
| `DELETE /index` | Delete index | `DELETE /new_index` |
| `POST .../_open` | Open index | `POST /my_index/_open` |
| `POST .../_close` | Close index | `POST /my_index/_close` |
| `PUT .../_mapping` | Set Mapping | `PUT /my_index/_mapping { "properties": ... }` |
| `PUT .../_settings` | Set Settings | `PUT /my_index/_settings { "index": ... }` |
| `POST /_aliases` | Alias management | `POST /_aliases { "actions": ... }` |
| `POST /_reindex` | Reindex | `POST /_reindex { "source": ..., "dest": ... }` |
| `POST .../_refresh` | Refresh index | `POST /my_index/_refresh` |
