---
id: json-serialization
sidebar_position: 5
title: 8.5 JSON Serialization Handler
description: Configure JSON libraries, property-level handlers and SQL parameter handlers.
---

<span id="json-serialization-handler" />

# 8.5 JSON Serialization Handler

`JsonTypeHandler` converts between Java values and JSON text. This page covers handler implementations, JSON libraries and explicit configuration for types that cannot carry a type annotation. For the recommended business-object mapping and a complete entity read/write example, see [JSON Field Mapping](../core/mapping/json-field.md).

## Choose a Configuration

| Use case | Configuration |
| --- | --- |
| Business object whose source you control | Prefer `@BindTypeHandler(JsonTypeHandler.class)` on the object type |
| `Map`, `List` or another property type you cannot annotate | Configure `typeHandler` on `@Column` and specify `specialJavaType` when needed |
| Parameter in Mapper SQL | Specify `typeHandler` in the `#{...}` parameter |
| Raw JSON text only | Use `String` without a JSON handler |

## JSON Libraries

At least one supported JSON library must be available before using a JSON handler. `JsonTypeHandler` selects the first available implementation in this order:

1. Jackson
2. Gson
3. Fastjson
4. Fastjson2

Available dependencies include `com.fasterxml.jackson.core:jackson-databind`, `com.google.code.gson:gson`, `com.alibaba:fastjson` and `com.alibaba.fastjson2:fastjson2`. Let the application manage dependency versions consistently.

## Map and List Properties

`Map` and `List` cannot carry `@BindTypeHandler`, so specify the handler on the entity property. `specialJavaType` selects the concrete implementation used for deserialization:

```java
import java.util.LinkedHashMap;
import java.util.Map;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.types.handler.json.JsonTypeHandler;

@Column(value = "preferences",
        typeHandler = JsonTypeHandler.class,
        specialJavaType = LinkedHashMap.class)
private Map<String, Object> preferences;
```

See [JSON Field Mapping](../core/mapping/json-field.md) for the complete entity mapping and read/write behavior.

## Use in Mapper SQL

A `#{...}` parameter expression can specify a handler for one parameter:

```text title='Mapper SQL'
UPDATE users
SET more_info = #{arg1, typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler}
WHERE id = #{arg0}
```

The fully qualified class name is required here because this is Mapper statement configuration, not Java source code with an `import`.

## Choose a Database Field

The JSON handler converts values; it does not change the database field type. A text column must be large enough for the serialized value. Native JSON types also require the database's parameter-binding and query syntax.

For field types and limits, see [MySQL](../../features/mysql/types.md), [PostgreSQL](../../features/postgresql/types.md), [Oracle](../../features/oracle/types.md), [SQL Server](../../features/mssql/types.md), [DB2](../../features/db2/types.md), [H2](../../features/h2/types.md) and [ClickHouse](../../features/clickhouse/types.md).

## Built-in Implementations

### JSON

| Handler | Description |
| --- | --- |
| `JsonTypeHandler` | Auto-detects and selects the first available implementation in Jackson, Gson, Fastjson, Fastjson2 order |
| `JsonUseForJacksonTypeHandler` | Always uses Jackson |
| `JsonUseForGsonTypeHandler` | Always uses Gson |
| `JsonUseForFastjsonTypeHandler` | Always uses Fastjson |
| `JsonUseForFastjson2TypeHandler` | Always uses Fastjson2 |

### BSON

| Handler | Description |
| --- | --- |
| `BsonTypeHandler` | Uses the MongoDB BSON library to serialize and deserialize objects |
| `BsonListTypeHandler` | Uses BSON for `List`, `Set` and other collection types and recognizes field generics |
