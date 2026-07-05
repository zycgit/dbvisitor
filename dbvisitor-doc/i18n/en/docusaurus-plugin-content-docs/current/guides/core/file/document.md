---
id: document
sidebar_position: 2
title: Document Structure
description: Learn the XML-based mapper file structure and available elements.
---
import TagRed from '@site/src/components/tags/TagRed';
import TagGray from '@site/src/components/tags/TagGray';

# Document Structure

Mapper files are stored as XML. The basic structure is:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//dbvisitor.net//DTD Mapper 1.0//EN"
                        "https://www.dbvisitor.net/schema/dbvisitor-mapper.dtd">
<mapper namespace="...">
    ...
</mapper>
```

## Attributes

| Property                  | Description                                                                                                                |
|---------------------------|----------------------------------------------------------------------------------------------------------------------------|
| namespace                 | <TagRed/> Usually the fully qualified mapper interface name; each method maps to a SQL operation in this file.            |
| caseInsensitive           | <TagGray/> Case-insensitive matching for column/property names. Default `true`. Handy when drivers return uppercase cols. |
| mapUnderscoreToCamelCase  | <TagGray/> Convert camelCase properties to snake_case column names (e.g., `createTime` → `create_time`). Default `false`. |
| autoMapping               | <TagGray/> Enable **auto-mapping**. Default `true`.                                                                        |
| useDelimited              | <TagGray/> (v5.3.4+) Force quoting/delimiters on table/column names when generating SQL. Default `false`.                  |

## Tags

Top-level XML elements available under the root:

- [&lt;entity&gt;](./entity_map): map a database table to a type; each type maps once.
- [&lt;resultMap&gt;](./result_map): describe how to load data from result sets.
- [&lt;select&gt;](./statements#select): configure SELECT statements.
- [&lt;update&gt; / &lt;delete&gt;](./statements#update_delete): configure UPDATE and DELETE.
- [&lt;insert&gt;](./statements#insert): configure INSERT.
- [&lt;execute&gt;](./statements#execute): execute arbitrary SQL.
- [&lt;sql&gt;](./statements#sql): reusable SQL fragments in the same mapper file.

## Validate the Document

Use XML DTD or XML Schema to validate mapper XML.

```xml title='Example: validate with DTD'
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//dbvisitor.net//DTD Mapper 1.0//EN"
                        "https://www.dbvisitor.net/schema/dbvisitor-mapper.dtd">
<mapper namespace="...">
    ...
</mapper>
```

```xml title='Example: validate with XML Schema'
<?xml version="1.0" encoding="UTF-8"?>
<mapper xmlns="https://www.dbvisitor.net/schema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="https://www.dbvisitor.net/schema https://www.dbvisitor.net/schema/dbvisitor-mapper.xsd"
        namespace="...">
    ...
</mapper>
```
