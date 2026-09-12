---
id: insert
slug: /features/milvus/sql/insert
sidebar_position: 1
title: INSERT
---

:::info[说明]
对应 SDK 方法：`insert`。
:::

## 语法

```text
INSERT INTO collection_name
    [PARTITION partition_name]
    [(field_name [, ...])]
    VALUES { (value [, ...]) [, ...] | ? };
```

INSERT 调用原生 insert；UPSERT 调用原生 upsert，不通过预查询判断主键是否存在。两者都不是关系数据库的唯一键冲突检查。默认 UPSERT 按完整实体覆盖；部分更新须显式启用 partial_update。

INSERT/UPSERT 支持多个 VALUES 元组（须显式列名），或 VALUES ? 绑定 Iterable/Iterator，每行为 Map（可省略列名）、List/Object[]（须列名）。不支持 INSERT SELECT。返回 SDK 实际确认的 long 计数；请求 RETURN_GENERATED_KEYS 时另提供标准主键游标。


## 插入数据

```sql
-- 插入到默认分区
INSERT INTO table_name (id, vector, age) VALUES (1, [0.1, 0.2], 10);

-- 插入到指定分区
INSERT INTO table_name PARTITION partition_name (id, vector) VALUES (2, [0.3, 0.4]);
```


## 多行写入与 Generated Keys {#generated-keys}

```sql
INSERT INTO docs (id,body,dense) VALUES (1,'first',[1,2]),(2,'second',[3,4]);
UPSERT INTO docs (id,body,dense) VALUES (1,'changed',[1,2]),(3,'third',[3,4]);
INSERT INTO docs (id,body,dense) VALUES ?;
UPSERT INTO docs VALUES ?;
```

VALUES ? 可绑定 Iterable/Iterator：带列名时每行 List/Object[]，Map 的键须匹配列名；省略列名时每行必须为 Map。省略 schema 默认值/nullable/function 输出字段时交给 SDK 处理。fetchSize 控制每次发送的实体数，0 沿用 SDK 默认，超大值截到 SDK 单页上限；不限制总量。驱动不关闭调用方提供的 Iterator/流。

INSERT/UPSERT 返回实际 SDK long 计数。使用 prepareStatement(sql, Statement.RETURN_GENERATED_KEYS) 或 Statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS) 请求主键；getGeneratedKeys 返回 SDK ID，列名为主键名。默认/NO_GENERATED_KEYS 为空游标；maxRows 不限制写入或主键。

不对普通 INSERT/UPSERT 自动重试。分页失败包含 phase、confirmedPages、confirmedRows、currentPageRows；确认过的页不会回滚，未确认的页可能已经写入。主键仅在成功响应时交付；请求全量主键会使用 O(主键数) 内存。

<span id="upsert" />
