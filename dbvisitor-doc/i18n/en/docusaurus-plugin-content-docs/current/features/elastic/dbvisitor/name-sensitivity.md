---
id: name-sensitivity
slug: /features/elastic/name-sensitivity
sidebar_position: 61
title: Name Sensitivity
---

## Index and Field Names {#database-names}

Index names must be lowercase, such as `user_info`; `User_Info` cannot name a separate index. Field names retain their case. Map properties to the actual field names:

```java
@Table("user_info")
public class User {
    @Column("Name")
    private String name;
    // Getters and setters omitted
}
```

This property accesses `Name`, not `name`.

## Result Column Case {#result-column-case}

With a free Map query and `Options.of().caseInsensitive(false)`, read the returned `Name` key with the same casing. This controls result lookup, not Elasticsearch field names. Entity configuration is described under [Name Sensitivity](../../../guides/core/mapping/name_sensitivity.md#result-column-case).
