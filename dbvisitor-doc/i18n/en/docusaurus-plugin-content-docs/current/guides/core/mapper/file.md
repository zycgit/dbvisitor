---
id: file
sidebar_position: 4
hide_table_of_contents: true
title: Call File Mapper
description: Execute SQL located in Mapper files through the executeStatement and queryStatement methods of the BaseMapper interface.
---

# Call File Mapper

You can execute SQL located in Mapper files through the `executeStatement` and `queryStatement` methods of the `BaseMapper` interface.

You can choose the appropriate way to obtain a Session according to your project architecture. For details, please refer to: **[Framework Integration](../../yourproject/buildtools#integration)**

```xml title='For example: Mapper file is as follows'
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//dbvisitor.net//DTD Mapper 1.0//EN"
        "https://www.dbvisitor.net/schema/dbvisitor-mapper.dtd">
<mapper namespace="user">
    <select id="listUsers" resultMap="user_resultMap">
        select * from users
    </select>
</mapper>
```

```java title='Example: Execute SQL in Mapper'
// user represents namespace
// listUsers represents Statement Id in Mapper File
List<User> users = session.queryStatement("user.listUsers");
```

:::info[For detailed configuration of File Mapper, please refer to:]
- Please refer to [File Mapper](../file/about) for details.
:::

## Pagination Query {#page}

Perform pagination queries by passing the Page parameter through the overloaded methods `queryStatement` and `pageStatement` of the Session object.

```java
PageObject page = new PageObject();
page.setPageSize(20);

List<User> users = session.queryStatement("user.listUsers", null, page);
```

```java
PageObject page = new PageObject();
page.setPageSize(20);

PageResult<User> users = session.pageStatement("user.listUsers", null, page);
```

- `PageResult` pagination results will also contain **Original Pagination Info**, **Total Records**, **Total Pages**.
