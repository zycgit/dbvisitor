---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: Oracle
description: Oracle 在 dbVisitor 中的方言能力、主键回填、IDENTITY、序列和 MERGE 冲突策略。
---

# Oracle

Oracle 可以使用 dbVisitor 的 JDBC、Mapper、Lambda、BaseMapper、事务、函数、过程和序列等全部通用能力。

## 快速了解差异

| 关注点 | Oracle 行为 |
|--------|------------|
| 主键生成 | `IDENTITY`（12c+）或 sequence；推荐 `selectKey` 先取值再 INSERT |
| 分页 | `ROWNUM` 嵌套查询 |
| 写入冲突 | `MERGE INTO ... WHEN MATCHED ... WHEN NOT MATCHED ...` |
| 批量写入 | 支持，但需要主键回填时退回逐条执行 |
| 存储过程 | 支持 |
| 序列 | 支持 `seq.NEXTVAL` |

## 主键回填

Oracle 12c+ 的 `IDENTITY` 列通过 JDBC generated keys 回填。**关键配置**：必须显式指定 `keyColumn`，否则 Oracle JDBC 驱动只返回 `ROWID` 而非业务主键列。

推荐方式：sequence 场景下，用 `selectKey` 在 INSERT 前先取序列值，再带入 INSERT：

```xml
<insert id="insertUser">
    <selectKey keyProperty="id" keyColumn="id" order="BEFORE">
        SELECT user_info_seq.NEXTVAL AS id FROM dual
    </selectKey>
    INSERT INTO user_info (id, name, age) VALUES (#{id}, #{name}, #{age})
</insert>
```

Oracle 内置方言未实现 `SeqSqlDialect`，不能直接使用 `KeyType.Sequence` + `@KeySeq`。可采用上面的 `selectKey`，或在应用中先读取序列值，再以普通主键字段插入。

## RETURNING INTO

Oracle 的 `RETURNING ... INTO` 是 OUT 参数模型，不等同于 PostgreSQL `RETURNING`。**不要**在 Mapper XML 中用 `generatedKeySource="resultSet"` 配合 Oracle `RETURNING INTO`。

## 写入冲突策略

| 策略 | Oracle 实现 |
|------|------------|
| Ignore | `MERGE INTO ... WHEN NOT MATCHED THEN INSERT`（需主键） |
| Update | `MERGE INTO ... WHEN MATCHED THEN UPDATE ... WHEN NOT MATCHED THEN INSERT`（需主键） |

## 专题

- [自增主键回填](./generated-keys)：`IDENTITY`、sequence、`keyColumn`、`selectKey`、`RETURNING INTO` 的详细配置。
- [方言细节](./dialect-details)：ROWNUM 分页、MERGE 语法、全部 OneByOne 策略的底层实现。

## 与通用文档的关系

通用 API 用法见 [核心API](../../guides/overview)。以下内容补充 Oracle 下的具体差异和推荐写法。
