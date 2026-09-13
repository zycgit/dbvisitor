---
id: run-command
sidebar_position: 6
title: Database Commands
---

`runCommand` passes a document argument to SDK `runCommand` and returns one command response.

```text
db.runCommand({ping: 1})
```

You can also bind an `org.bson.Document` with `?`. If the response contains a cursor, the driver does not automatically issue getMore. Use find for document queries and aggregate for pipelines. This does not execute JavaScript programs.
