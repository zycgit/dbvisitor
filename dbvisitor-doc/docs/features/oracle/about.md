---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: Oracle
description: Oracle 在 dbVisitor 中的方言能力、主键回填、IDENTITY、序列和 MERGE 冲突策略。
---

# Oracle

Oracle 可使用 JdbcTemplate、注解/XML Mapper、Lambda、BaseMapper、对象映射和 JDBC 事务。手写 SQL 保留 Oracle 语义；构造器通过 Oracle 方言生成 SQL，但并非每个通用接口都适用。

## API 支持与边界

| 能力 | 使用边界 |
| --- | --- |
| CRUD 与分页 | 支持；构造器分页生成 ROWNUM 嵌套查询，由数据库截取记录 |
| 实体与 Map 映射 | 支持；未引用的列名通常以大写返回，按名称映射不要假定原始 Map 键一定为小写 |
| 字符串 | Oracle 字符空字符串按 NULL 处理，不保证空串与 null 分别往返 |
| 主键生成 | Auto 使用 generated keys；Sequence 配合 @KeySeq 在插入前自动取值 |
| 批量插入 | Lambda/BaseMapper 的方言策略为逐条执行，包括无回填的插入；不等同于 Oracle JDBC 不支持批处理 |
| 事务 | 支持提交与回滚；隔离级别、保存点释放等操作受 Oracle JDBC 限制，不能直接套用所有数据库的事务选项 |
| 数组、游标和专有类型 | 使用 Oracle JDBC 对应的注册、读取和类型处理方式 |

## 快速了解差异

| 关注点 | Oracle 行为 |
|--------|------------|
| 主键生成 | `IDENTITY`（12c+）或 sequence |
| 分页 | `ROWNUM` 嵌套查询 |
| 写入冲突 | `MERGE INTO ... WHEN MATCHED ... WHEN NOT MATCHED ...` |
| 批量写入 | Lambda/BaseMapper 插入逐条执行；原生 JDBC Batch 是另一条调用路径 |
| 存储过程 | 支持 |
| 序列 | 支持 `seq.NEXTVAL` |

## 主键生成

Oracle 12c+ 的 `IDENTITY` 列通过 JDBC generated keys 回填。**关键配置**：必须显式指定 `keyColumn`，否则 Oracle JDBC 驱动只返回 `ROWID` 而非业务主键列。

Mapper 文件可以用 `selectKey` 在 INSERT 前获取序列值：

```xml
<insert id="insertUser">
    <selectKey keyProperty="id" keyColumn="id" order="BEFORE">
        SELECT user_info_seq.NEXTVAL AS id FROM dual
    </selectKey>
    INSERT INTO user_info (id, name, age) VALUES (#{id}, #{name}, #{age})
</insert>
```

构造器 API 支持 `KeyType.Sequence` + `@KeySeq` 自动获取并赋值，见[主键生成](./generated-keys.mdx)。

## 专题

- [数据回填](./backfill.mdx)：使用 `RETURNING INTO` 返回插入、更新或删除的字段值。
- [主键生成](./generated-keys)：`IDENTITY`、sequence、`keyColumn` 和 `selectKey`。
- [分页查询](./pagination.mdx)：分页原理、用法与注意事项。
- [多条写入一致性](./write.mdx)：中途失败的影响及事务处理。
- [插入冲突](./conflict.mdx)：Oracle MERGE 策略、用法与注意事项。

## 相关文档

通用用法见 [核心 API](../../guides/overview)。

- [类型支持](./types.md)：Java 值、存储方式及读回边界。
