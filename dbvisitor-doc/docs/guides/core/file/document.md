---
id: document
sidebar_position: 2
hide_table_of_contents: true
title: 文档结构
description: 了解 Mapper 文件是以 XML 形式保存，它的基本结构和可用的 XML 元素。
---
import TagRed from '@site/src/components/tags/TagRed';
import TagGray from '@site/src/components/tags/TagGray';

# 文档结构

Mapper 文件是以 XML 形式保存，它的基本结构如下：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//dbvisitor.net//DTD Mapper 1.0//EN"
                        "https://www.dbvisitor.net/schema/dbvisitor-mapper.dtd">
<mapper namespace="...">
    ...
</mapper>
```

## 属性

| 属性名                      | 描述                                                                                                  |
|--------------------------|-----------------------------------------------------------------------------------------------------|
| namespace                | <TagRed/> 通常是配置一个接口类名，这个接口下的每个方法会对应到 mapper 文件中一个具体的 sql 操作上。                                       |
| caseInsensitive          | <TagGray/> 在处理映射列名和属性名时是否对大小写不敏感，默认是 `true` 不敏感。对于某些数据库查询结果始终返回大写，利用这个功能可以方便的映射到属性上。                |
| mapUnderscoreToCamelCase | <TagGray/> 用于决定属性名在映射到列名时，是否按照驼峰命名法转换为下划线命名法，例如：属性名 `createTime` 被转换为 `create_time`。默认是 `false` 不转换 |
| autoMapping              | <TagGray/> 用于决定是否进行 **自动映射**。默认是 `true` 自动映射。                                                       |
| useDelimited             | <TagGray/> (v5.3.4+)，用于决定在生成 SQL 语句时，表名/列名 是否强制使用限定符。默认：false 不使用。                                  |

## 标签

在根元素下可以使用的顶层 Xml 元素有如下几个：

- [&lt;entity&gt; 标签](./entity_map) 用于描述一个数据库表和类型的映射，每个类型只能映射一次。
- [&lt;resultMap&gt; 标签](./result_map) 用于描述如何从查询结果集中加载数据。
- [&lt;select&gt; 标签](./sql_element#select)，用于配置 SELECT 语句。
- [&lt;update&gt; 标签、&lt;delete&gt; 标签](./sql_element#update_delete)，用于配置 UPDATE 和 DELETE 语句。
- [&lt;insert&gt; 标签](./sql_element#insert)，用于配置 INSERT 语句。
- [&lt;execute&gt; 标签](./sql_element#execute)，可以用来执行任意的 SQL 语句。
- [&lt;sql&gt; 标签](./sql_element#sql)，代码片段，可在同一个 Mapper 文件内进行引用。

## 验证文档

使用 XML DTD 或者 XML Schema 可以验证 Mapper 文件的正确性。

```xml title='例：使用 DTD 验证'
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//dbvisitor.net//DTD Mapper 1.0//EN"
                        "https://www.dbvisitor.net/schema/dbvisitor-mapper.dtd">
<mapper namespace="...">
    ...
</mapper>
```

```xml title='例：使用 XML Schema 验证'
<?xml version="1.0" encoding="UTF-8"?>
<mapper xmlns="https://www.dbvisitor.net/schema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="https://www.dbvisitor.net/schema https://www.dbvisitor.net/schema/dbvisitor-mapper.xsd"
        namespace="...">
    ...
</mapper>
```
