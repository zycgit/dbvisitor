---
id: index
slug: /features/mongo/commands
sidebar_position: 0
title: Command Syntax
---

`jdbc-mongo` parses raw Command instructions and converts them to underlying API calls. Below is the list of supported commands.

:::tip[Command Enhancement]
Regarding the `db.` prefix enhancement for raw MongoDB commands:
- You can omit the `db.` prefix and use the collection name directly, e.g., `coll1.find()` queries the `coll1` collection in the current database.
- You can use `<databaseName>.<collectionName>.` to specify the exact collection, e.g., `myDb.coll1.find()` queries the `coll1` collection in the `myDb` database.
- You can use `use databaseName` to switch the current database.
:::


<span id="collection" />

- [Collection Operations](./collections.md)

<span id="database" />

- [Database Management](./databases.md)

<span id="index" />

- [Index Management](./indexes.md)

<span id="user" />

- [User Management](./users.md)

<span id="other" />

- [Other Commands](./other.md)

- [Hint Support](./hints.md)

- [Limitations](./limits.md)

<span id="native-operations" />

- [Native Operations and Results](./operations.md)
