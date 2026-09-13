---
id: set
sidebar_position: 1
title: SET
---

:::info[说明]
官方文档：[SET](https://redis.io/docs/latest/commands/set/)。
:::

写入字符串，可按键是否存在决定是否覆盖。

## 语法

```text
SET key value [NX | XX] [GET] [EX seconds | PX milliseconds | EXAT unix-seconds | PXAT unix-milliseconds | KEEPTTL]
```

`NX` 仅新增，`XX` 仅覆盖；`GET` 返回旧值。`EX/PX` 是存活秒数/毫秒数，`EXAT/PXAT` 是 Unix 时间戳，`KEEPTTL` 保留原过期时间。选项按语法中的顺序书写。

过期参数支持 64 位整数，可使用字面值或绑定 Java `long`；`PXAT` 可直接传入 Unix 毫秒时间戳。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 更新计数/结果集 | --/1 | 值：当不使用 GET 时，返回 0（表示没有设置） 或 1（表示设置成功）<br/>结果集：当使用 GET 时，VALUE 字段，STRING 类型 |

key 或 value 参数绑定 `byte[]` 时，使用二进制 SET；带 `GET` 时，`VALUE` 为 `byte[]`，可用 `getBytes()` 读取。

## 示例

```text
SET demo:message hello EX 60
SET demo:message world XX GET KEEPTTL
```
