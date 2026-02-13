---
id: json-serialization
sidebar_position: 5
title: 8.5 JSON 序列化处理器
description: dbVisitor 内置支持 Jackson、Gson、Fastjson、Fastjson2 四种 JSON 序列化方案，以及 BSON 序列化。
---

# JSON 序列化处理器

JSON 序列化处理器位于 `net.hasor.dbvisitor.types.handler.json` 包中。通过序列化处理器可以将 Java 对象以 JSON 格式存储在数据库的文本字段中。

```java title='在对象映射中使用'
public class User {
    @Column(typeHandler = JsonTypeHandler.class)
    private UserExtInfo moreInfo;

    // getters and setters omitted
}
```

```text title='在 SQL 语句中使用'
update users
set more_info = #{arg1, typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler}
where id = #{arg0}
```

:::info
使用 JSON 序列化处理器前，需在项目中引入对应的 JSON 库依赖。
:::

## 内置实现 {#json}

**JSON 序列化**

| 类型处理器 | 说明 |
|---|---|
| `JsonTypeHandler` | 自动检测，按 Jackson → Gson → Fastjson → Fastjson2 顺序寻找可用的 JSON 库 |
| `JsonUseForJacksonTypeHandler` | 使用 [Jackson](https://github.com/FasterXML/jackson-core) |
| `JsonUseForGsonTypeHandler` | 使用 [Gson](https://github.com/google/gson) |
| `JsonUseForFastjsonTypeHandler` | 使用 [Fastjson](https://github.com/alibaba/fastjson) |
| `JsonUseForFastjson2TypeHandler` | 使用 [Fastjson2](https://github.com/alibaba/fastjson2) |

**BSON 序列化**

| 类型处理器 | 说明 |
|---|---|
| `BsonTypeHandler` | 使用 MongoDB BSON 库进行序列化/反序列化 |
| `BsonListTypeHandler` | 基于 BSON 处理 `List`、`Set` 等集合类型字段，可自动识别字段泛型 |
