---
id: json-field
sidebar_position: 6.5
title: JSON Field Mapping
description: Map a Java object, Map or List to one database JSON field.
---

# JSON Field Mapping

JSON field mapping stores a Java object, `Map` or `List` as one database field value and restores it when the entity is read. It does not split inner properties into columns or create one-to-one or one-to-many relationships.

## Choose a Configuration

| Java property type | Recommended configuration |
| --- | --- |
| Business class whose source you control | Annotate the class with `@BindTypeHandler(JsonTypeHandler.class)` |
| `Map`, `List` or another type you cannot annotate | Configure `typeHandler` on the entity property's `@Column` |
| Raw JSON text only | Use a `String` property without a JSON handler |

A business class's storage convention is often reused by multiple properties or query entry points, so the type-level `@BindTypeHandler` annotation is the default recommendation. Use a property-level handler when the type itself cannot carry the annotation.

## Map a Business Object

The following stores `UserExtInfo` in the `user_details.more_info` field. The CREATE TABLE statement uses MySQL:

```sql
CREATE TABLE user_details (
    id INTEGER PRIMARY KEY,
    more_info VARCHAR(2000)
);
```

Declare the JSON handler on the business object type. This class is not a table entity and needs neither `@Table` nor `@Column`:

```java
import net.hasor.dbvisitor.types.BindTypeHandler;
import net.hasor.dbvisitor.types.handler.json.JsonTypeHandler;

@BindTypeHandler(JsonTypeHandler.class)
public class UserExtInfo {
    private String city;
    private String theme;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }
}
```

The enclosing entity only maps `moreInfo` to `more_info` and does not repeat the handler:

```java
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

@Table("user_details")
public class UserDetails {
    @Column(primary = true)
    private Integer id;

    @Column("more_info")
    private UserExtInfo moreInfo;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public UserExtInfo getMoreInfo() {
        return moreInfo;
    }

    public void setMoreInfo(UserExtInfo moreInfo) {
        this.moreInfo = moreInfo;
    }
}
```

## Write and Read

The `lambda` instance below is a [LambdaTemplate](../lambda/about.md) configured for your database:

```java
UserExtInfo info = new UserExtInfo();
info.setCity("Hangzhou");
info.setTheme("dark");

UserDetails user = new UserDetails();
user.setId(1);
user.setMoreInfo(info);
lambda.insert(UserDetails.class).applyEntity(user).executeSumResult();

UserDetails loaded = lambda.query(UserDetails.class)
        .eq(UserDetails::getId, 1)
        .queryForObject();

String city = loaded.getMoreInfo().getCity();
```

After insertion, `more_info` contains JSON such as `{"city":"Hangzhou","theme":"dark"}`. On query, `moreInfo` is restored as `UserExtInfo` and `city` is `"Hangzhou"`. JSON property order does not affect the meaning.

## Map and List Properties

`Map` and `List` cannot carry `@BindTypeHandler` directly, so configure the handler on the entity property. When the property uses an interface type, `specialJavaType` can select the concrete deserialization type:

```java
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.types.handler.json.JsonTypeHandler;

@Column(value = "preferences",
        typeHandler = JsonTypeHandler.class,
        specialJavaType = LinkedHashMap.class)
private Map<String, Object> preferences;

@Column(value = "tags",
        typeHandler = JsonTypeHandler.class,
        specialJavaType = LinkedList.class)
private List<String> tags;
```

## Usage Boundaries

- `@Table` applies only to the enclosing entity; inner JSON properties do not become database columns.
- Inserting or updating a JSON property handles the whole field value; inner-property changes are not merged automatically.
- Changing an in-memory object does not write it back automatically; execute an update explicitly.
- Lambda conditions do not become JSON-path queries. Use database-specific JSON query syntax or separate columns to filter inner properties.
- The text column must hold the serialized value. The `2000` in `VARCHAR(2000)` is an example length, not a mapping trigger.

For JSON libraries, handler implementations and SQL parameter configuration, see [8.5 JSON Serialization Handler](../../types/json-serialization.md).
