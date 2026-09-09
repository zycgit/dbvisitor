---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: ClickHouse
description: ClickHouse 在 dbVisitor 中的方言能力、批量写入、主键回填和使用差异。
---

# ClickHouse

ClickHouse 是分析型数据库，表引擎、分区键、排序键和写入模型与传统 OLTP 数据库不同。dbVisitor 提供 ClickHouse 方言用于常规查询、分页和 INSERT。

## 快速了解差异

| 关注点 | ClickHouse 行为 |
|--------|----------------|
| 主键生成 | 通常由应用侧生成，不依赖数据库自增 |
| 分页 | `LIMIT ?` / `LIMIT offset, count` |
| 写入冲突 | 不支持 Ignore/Update 策略 |
| 批量写入 | 支持 JDBC batch（无回填时） |
| 存储过程 | 不支持 |
| 序列 | 不支持 |

## 主键生成

ClickHouse 不使用数据库自增主键模型。业务标识一般由应用侧生成（UUID、雪花 ID 等）或通过 ClickHouse 表达式生成。不推荐依赖 JDBC generated keys。

## 对象映射

```java
@Table("user_info")
public class UserInfo {
    @Column(value = "id", primary = true, keyType = KeyType.UUID32)
    private String id;
    @Column("name")
    private String name;
}
```

使用 `KeyType.UUID32`/`KeyType.UUID36` 在应用侧生成主键。

## 批量写入建议

- 无主键回填时可用 JDBC batch 提升性能
- ClickHouse 推荐批量写入而非逐条插入
- 写入可见性取决于表引擎和设置。启用 `async_insert` 且关闭 `wait_for_async_insert` 时，响应可在数据落入表前返回；这不是所有 INSERT 的默认行为。见[异步写入说明](https://clickhouse.com/docs/optimize/asynchronous-inserts)。

## 专题

- [主键与返回键](./generated-keys)：ClickHouse 下推荐使用应用侧生成 ID，与 JDBC generated keys 的边界。
- [方言细节](./dialect-details)：分页、写入策略限制、batch 行为的底层实现。

## 与通用文档的关系

通用 API 用法见 [核心API](../../guides/overview)。以下内容补充 ClickHouse 下的具体差异和推荐写法。
