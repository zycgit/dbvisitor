---
id: upsert
sidebar_position: 2
title: UPSERT
---

:::info[说明]
对应 SDK 方法：`upsert`。
:::

## 语法

```text
UPSERT INTO collection_name
    [PARTITION partition_name]
    [(field_name [, ...])]
    VALUES { (value [, ...]) [, ...] | ? };
```

INSERT 调用原生 insert；UPSERT 调用原生 upsert，不通过预查询判断主键是否存在。两者都不是关系数据库的唯一键冲突检查。默认 UPSERT 按完整实体覆盖；部分更新须显式启用 partial_update。

INSERT/UPSERT 支持多个 VALUES 元组（须显式列名），或 VALUES ? 绑定 Iterable/Iterator，每行为 Map（可省略列名）、List/Object[]（须列名）。不支持 INSERT SELECT。返回 SDK 实际确认的 long 计数；请求 RETURN_GENERATED_KEYS 时另提供标准主键游标。


## Upsert (插入或覆盖) {#upsert}

```sql
-- 插入或覆盖到默认分区
UPSERT INTO table_name (id, vector, age) VALUES (1, [0.1, 0.2], 10);

-- 插入或覆盖到指定分区
UPSERT INTO table_name PARTITION partition_name (id, vector) VALUES (2, [0.3, 0.4]);

-- 原生部分更新：已有主键只修改 age，新主键按服务端插入规则处理
/*+ partial_update=true */ UPSERT INTO table_name (id, age) VALUES (?, ?);
```

`partial_update` 是 UPSERT 的布尔 hint，可用 `?` 绑定，映射 SDK `UpsertReq.partialUpdate`。省略或 false 保留完整覆盖模式；true 使用 Milvus 2.6.2+ 的原生部分更新，保留已有实体中未提供的字段。新主键仍必须满足 schema 的必填字段、默认值和函数输入要求；不会由驱动先查询完整实体。该 hint 不适用于 INSERT，非布尔值会报错。跨多页不保证原子性或回滚，失败不会自动重放不确定的写入。

INSERT/UPSERT 和 UPDATE SET 都按 schema 转换 JSON、向量及 Array。JSON 字符串、Map、List、JsonElement 或数值数组按 JSON 编码，不执行 FloatVector 降精度转换；这不是任意输入无损往返的保证，具体格式仍受 SDK/服务端校验约束。

多行绑定和主键返回见 [INSERT](insert.md)。
