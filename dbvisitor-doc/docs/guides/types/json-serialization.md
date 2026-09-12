---
id: json-serialization
sidebar_position: 5
title: 8.5 JSON 序列化处理器
description: 配置 JSON 序列化库、字段级处理器和 SQL 参数处理器。
---

<span id="json-序列化处理器" />

# 8.5 JSON 序列化处理器

`JsonTypeHandler` 负责在 Java 值和 JSON 文本之间转换。本页介绍处理器实现、JSON 库以及无法使用类型注解时的显式配置。业务对象映射的默认推荐方式和完整实体读写示例见 [JSON 字段映射](../core/mapping/json-field.md)。

## 选择配置方式

| 使用场景 | 配置方式 |
| --- | --- |
| 可以修改的业务对象 | 推荐在对象类型上使用 `@BindTypeHandler(JsonTypeHandler.class)` |
| `Map`、`List` 等无法添加类型注解的属性 | 在 `@Column` 上配置 `typeHandler`，必要时指定 `specialJavaType` |
| Mapper SQL 中的参数 | 在 `#{...}` 参数中指定 `typeHandler` |
| 只读写原始 JSON 文本 | 使用 `String`，不配置 JSON 处理器 |

## JSON 库

使用 JSON 处理器前，项目中至少要有一个受支持的 JSON 库。`JsonTypeHandler` 按以下顺序选择首个可用实现：

1. Jackson
2. Gson
3. Fastjson
4. Fastjson2

可使用的依赖包括 `com.fasterxml.jackson.core:jackson-databind`、`com.google.code.gson:gson`、`com.alibaba:fastjson` 或 `com.alibaba.fastjson2:fastjson2`。依赖版本由应用统一管理。

## Map 和 List 字段

`Map`、`List` 无法添加 `@BindTypeHandler`，因此在实体属性上指定处理器。`specialJavaType` 用于明确反序列化所需的具体实现类：

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

完整实体映射及读写行为见 [JSON 字段映射](../core/mapping/json-field.md)。

## 在 Mapper SQL 中使用

使用 `#{...}` 参数表达式时，可以为单个参数指定处理器：

```text title='Mapper SQL'
UPDATE users
SET more_info = #{arg1, typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler}
WHERE id = #{arg0}
```

这里需要使用处理器的全限定类名，因为它位于 Mapper 语句配置中，不是 Java 源码的 `import` 语境。

## 数据库字段选择

JSON 处理器负责值转换，不负责改变数据库字段类型。文本列长度必须足够容纳序列化结果；使用数据库原生 JSON 类型时，还要遵循该数据库的参数绑定和查询语法。

具体字段类型及限制见 [MySQL](../../features/mysql/types.md)、[PostgreSQL](../../features/postgresql/types.md)、[Oracle](../../features/oracle/types.md)、[SQL Server](../../features/mssql/types.md)、[DB2](../../features/db2/types.md)、[H2](../../features/h2/types.md)和 [ClickHouse](../../features/clickhouse/types.md)。

## 内置实现

### JSON

| 类型处理器 | 说明 |
| --- | --- |
| `JsonTypeHandler` | 自动检测，并按 Jackson、Gson、Fastjson、Fastjson2 的顺序选择可用实现 |
| `JsonUseForJacksonTypeHandler` | 固定使用 Jackson |
| `JsonUseForGsonTypeHandler` | 固定使用 Gson |
| `JsonUseForFastjsonTypeHandler` | 固定使用 Fastjson |
| `JsonUseForFastjson2TypeHandler` | 固定使用 Fastjson2 |

### BSON

| 类型处理器 | 说明 |
| --- | --- |
| `BsonTypeHandler` | 使用 MongoDB BSON 库序列化和反序列化对象 |
| `BsonListTypeHandler` | 使用 BSON 处理 `List`、`Set` 等集合类型，并识别字段泛型 |
