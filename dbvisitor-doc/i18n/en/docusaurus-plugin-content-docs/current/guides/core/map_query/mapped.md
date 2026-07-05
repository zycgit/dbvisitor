---
id: mapped
sidebar_position: 2
title: Mapped Map Mode
description: Use Map as the row carrier while reusing existing object mapping.
---

# Mapped Map Mode

Mapped Map Mode is converted from an Entity builder and still uses object mapping on the entity class. The caller passes a Map, but Map keys are interpreted as Java property names.

## Suitable For

- The project already has entity classes and `@Table` / `@Column` mappings.
- Column conversion, field filtering, TypeHandler, and write policies should be reused.
- Data flows through the application as Maps, while the database structure still matches an entity class.
- Query results should be `List<Map<String, Object>>` rather than entity objects.

## Not Suitable For

- There is no entity class or object mapping should not be maintained. Use [Freedom Map Mode](./freedom).
- Field names come from database column names and should not be converted through object mapping.
- The table structure is fully runtime-driven and cannot be covered by entity mapping.

## Mode Relationship

```text title='Mapped Map Mode'
Entity builder
lambda.query(User.class)
        |
        | asMap()
        v
Map builder
Map key = User property name
        |
        | Object mapping conversion
        v
SQL column = database column name
```

The core value of Mapped Map Mode is that the row carrier becomes Map while field interpretation remains object-mapping based.

## Entrance

```java title='Switch from Entity builder to Map'
LambdaTemplate lambda = new LambdaTemplate(dataSource);

MapQuery query = lambda.query(User.class).asMap();
MapInsert insert = lambda.insert(User.class).asMap();
MapUpdate update = lambda.update(User.class).asMap();
MapDelete delete = lambda.delete(User.class).asMap();
```

`asMap()` changes only the row carrier. Table name, column names, primary key, TypeHandler, and write policies still come from the object mapping of `User.class`.

## Query By Mapped Properties

If `loginName` maps to `login_name`, the condition still uses `loginName`.

```java title='Query by mapped property'
List<Map<String, Object>> users = lambda.query(User.class)
        .asMap()
        .eq("loginName", "alice")
        .select("id", "loginName", "email")
        .queryForList();
```

SQL shape:

```sql
SELECT id, login_name, email FROM users WHERE login_name = ?
```

## Query Results

Mapped Map Mode returns `Map<String, Object>`. Map keys follow mapped property names so application code can keep Java property naming.

```java title='Read query result'
Map<String, Object> user = lambda.query(User.class)
        .asMap()
        .eq("id", 1001)
        .queryForObject();

Object loginName = user.get("loginName");
Object email = user.get("email");
```

## Insert And Update

For insert and update, keys that do not exist in object mapping are not appended directly to SQL.

```java title='Filter insert fields by mapping'
Map<String, Object> data = new HashMap<>();
data.put("id", 1001);
data.put("loginName", "alice");
data.put("email", "alice@example.com");
data.put("unknownField", "ignored");

int rows = lambda.insert(User.class)
        .asMap()
        .applyMap(data)
        .executeSumResult();
```

```java title='Generate SET columns from mapping'
Map<String, Object> setValues = new HashMap<>();
setValues.put("loginName", "alice_new");
setValues.put("email", null);
setValues.put("unknownField", "ignored");

int rows = lambda.update(User.class)
        .asMap()
        .eq("id", 1001)
        .updateToSample(setValues)
        .doUpdate();
```

See [Map Insert, Update, And Delete](./write) for more write and update differences.

## Boundaries

- Map keys are Java property names, not database column names.
- Unmapped Map keys are filtered.
- Field values follow TypeHandler, write policy, and other object-mapping rules.
- If there is nothing to update, `there nothing to update.` is raised.
- UPDATE without WHERE is blocked unless `allowEmptyWhere()` is explicit.

## Further Reading

- [Freedom Map Mode](./freedom), Map entrance without object mapping.
- [Map Insert, Update, And Delete](./write), choosing `applyMap`, `updateToSample`, and `updateRow`.
- [Object Mapping](../mapping/about), mapping capabilities reused by Mapped Map Mode.
