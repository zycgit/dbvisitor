---
id: json-serialization
sidebar_position: 5
title: 8.5 JSON 序列化处理器
description: dbVisitor 内置支持了 4 个常见 json 序列化服务提供程序可供选择。
---

JSON 类型处理器位于 `net.hasor.dbvisitor.types.handler.json` 包中，使用时需要引入对应的依赖。通过序列化处理器可以将 Java 对象序列化为 JSON 格式的字符串存储在数据库中。 
- 例如，以 JSON 为代表的序列化格式最为普遍。

```java title='在对象映射中使用序列化处理器'
public class User {
    @Column(typeHandler = net.hasor.dbvisitor.types.handler.json.JsonTypeHandler)
    private UserExtInfo moreInfo;

    // getters and setters omitted
}
```

```text title='在 SQL 语句中使用序列化处理器'
update
    users
set 
    more_info = #{arg1, typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler}
where
    id = #{arg0}
```

## JSON 序列化器{#json}

dbVisitor 内置支持了 4 个常见 json 序列化服务提供程序可供选择，可用于将对象以 JSON 格式进行序列化和反序列化的类型处理器。具体如下：

| 类型处理器                          | 作用                                                                                       |
|--------------------------------|------------------------------------------------------------------------------------------|
| JsonTypeHandler                | 根据用户依赖环境以 Jackson、Gson、Fastjson、Fastjson2 为顺序依次尝试寻找可用的 JSON 服务提供者。                       |
| JsonUseForFastjsonTypeHandler  | 使用 Fastjson 作为 JSON 序列化/反序列化 服务提供者。<br/> - 项目地址：https://github.com/alibaba/fastjson      |
| JsonUseForFastjson2TypeHandler | 使用 Fastjson2 作为 JSON 序列化/反序列化 服务提供者。<br/> - 项目地址：https://github.com/alibaba/fastjson2    |
| JsonUseForGsonTypeHandler      | 使用 Gson 作为 JSON 序列化/反序列化 服务提供者。<br/> - 项目地址：https://github.com/google/gson               |
| JsonUseForJacksonTypeHandler   | 使用 Jackson 作为 JSON 序列化/反序列化 服务提供者。<br/> - 项目地址：https://github.com/FasterXML/jackson-core |
