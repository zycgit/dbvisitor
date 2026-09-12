---
id: defaults
sidebar_position: 3
title: 字段约束
---

## PRIMARY KEY、AUTO_ID 与 NULL

字段默认 NOT NULL，显式声明 NULL 开启可空。主键不能为 NULL。PRIMARY KEY 指定主键，AUTO_ID 启用服务端生成主键。冲突的约束会被拒绝；`COMMENT 'text'` 设置字段说明。

## DEFAULT 默认值

支持非主键标量字段的 `DEFAULT`：`BOOL`、`INT8/INT16/INT32/INT64`、`FLOAT/DOUBLE`、`VARCHAR`。数值允许正负号；整数默认值必须精确落在字段范围内，浮点值必须有限，字符串不能超过声明的 UTF-8 字节长度。非法值、重复 DEFAULT，以及主键、JSON、Array、向量字段的 DEFAULT 会在发送建表请求前被拒绝。

普通 INSERT/完整 UPSERT 省略字段或绑定 null 时，按 SDK 规则由 Milvus 应用 DEFAULT；无 DEFAULT 的 nullable 字段保存 NULL，非 nullable 字段由 SDK/服务端校验。SHOW CREATE 保留默认值、nullable、Array 元素/容量、分析器和函数。向量 nullable 需 2.6.18+，不支持向量 IS NULL/IS NOT NULL；标量 nullable/Array 维持 2.6.2 基线。
