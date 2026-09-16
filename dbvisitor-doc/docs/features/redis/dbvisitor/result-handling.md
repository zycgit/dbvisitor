---
id: result-handling
slug: /features/redis/result-handling
sidebar_position: 91
title: 结果接收
---

Redis 命令返回结果集后，可以使用 dbVisitor 的三个结果处理接口。不同 API 的接入情况如下。

## API 支持 {#api-support}

| API 入口 | RowMapper | RowCallbackHandler | ResultSetExtractor |
| --- | --- | --- | --- |
| 编程式 API | <span className="support-status support-status--yes">支持</span> | <span className="support-status support-status--yes">支持</span> | <span className="support-status support-status--yes">支持</span> |
| Mapper API（方法注解） | <span className="support-status support-status--yes">支持</span> | <span className="support-status support-status--yes">支持</span> | <span className="support-status support-status--yes">支持</span> |
| 构造器 API | <span className="support-status support-status--no">不支持</span> | <span className="support-status support-status--no">不支持</span> | <span className="support-status support-status--no">不支持</span> |
| Mapper 文件 | <span className="support-status support-status--yes">支持</span> | <span className="support-status support-status--yes">支持</span> | <span className="support-status support-status--yes">支持</span> |

构造器 API 不会将查询条件转换为 Redis 命令，因此不能从该入口发起查询。这不是三个结果处理接口的限制；改用原生命令即可处理结果。

## 编程式 API

例如，`ZRANGE scores 0 -1 WITHSCORES` 返回 `ELEMENT`、`SCORE` 两列。

```java
// RowMapper：每行转换为一个字符串。
List<String> members = jdbcTemplate.queryForList(
        "ZRANGE ? 0 -1 WITHSCORES", new Object[] { "scores" },
        (rs, rowNum) -> rs.getString("ELEMENT"));
```

```java
// RowCallbackHandler：逐行处理，不收集返回列表。
jdbcTemplate.query("ZRANGE ? 0 -1 WITHSCORES", new Object[] { "scores" },
        (rs, rowNum) -> System.out.println(rs.getString("ELEMENT")));

// ResultSetExtractor：将整个结果集整理为成员与分数的映射。
Map<String, Double> scores = jdbcTemplate.query(
        "ZRANGE ? 0 -1 WITHSCORES", new Object[] { "scores" }, rs -> {
            Map<String, Double> result = new LinkedHashMap<>();
            while (rs.next()) {
                result.put(rs.getString("ELEMENT"), rs.getDouble("SCORE"));
            }
            return result;
        });
```

各命令的列名见[命令与结果集](./results.mdx)。

## Mapper API 与 Mapper 文件

方法注解使用 `@Query` 的 `resultRowMapper`、`resultRowCallback`、`resultSetExtractor` 属性指定处理器类；Mapper 文件的 `<select>` 使用同名属性。

```xml
<select id="members"
        resultRowMapper="net.hasor.dbvisitor.jdbc.mapper.ColumnMapRowMapper">
    ZRANGE #{key} 0 -1 WITHSCORES
</select>
```

方法注解与 Mapper 文件中都填写 Redis 原生命令，不需要转换成 SQL。处理器配置及实现方式见[结果接收](../../../guides/result/about.md)。
