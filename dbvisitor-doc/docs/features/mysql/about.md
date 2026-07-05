---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: MySQL
description: MySQL 在 dbVisitor 中的方言能力、类型映射、AUTO_INCREMENT、分页、重复键策略和存储过程差异。
---

# MySQL

MySQL 可以使用 dbVisitor 的 JDBC、Mapper、Lambda、BaseMapper、事务和对象映射等全部通用能力。

## 快速了解差异

| 关注点 | MySQL 行为 |
|--------|----------|
| 主键生成 | `AUTO_INCREMENT`，通过 JDBC generated keys 回填 |
| 分页 | `LIMIT ? OFFSET ?` |
| 写入冲突 | `INSERT IGNORE`（忽略）/ `ON DUPLICATE KEY UPDATE`（更新） |
| 批量写入 | 支持 JDBC batch |
| 存储过程 | 支持 |
| 序列 | 不支持（用 AUTO_INCREMENT 替代） |

## 主键回填

`AUTO_INCREMENT` 主键通过 JDBC generated keys 回填。Lambda / BaseMapper 按照以下策略选择执行方式：

- 无回填列：普通 JDBC batch
- `Into` + 有回填列：JDBC batch generated keys
- `Ignore` / `Update` + 有回填列：保守逐条执行

重复键策略由数据库唯一键或主键触发，不依赖传入的主键列列表。

## 写入冲突策略

| 策略 | MySQL 实现 |
|------|-----------|
| Ignore | `INSERT IGNORE INTO ...` |
| Update | `INSERT INTO ... ON DUPLICATE KEY UPDATE ...` |

## 类型映射要点

- `TINYINT(1)` 映射到 Java `Boolean`/`boolean`
- `DATETIME` / `TIMESTAMP` 映射到 `java.time.LocalDateTime` 或 `java.util.Date`
- `JSON` 类型通过 `JsonTypeHandler` 序列化
- 枚举建议使用 `VARCHAR` 配合 `EnumOfValue`/`EnumOfCode`

## 专题

- [自增主键回填](./generated-keys)：`AUTO_INCREMENT`、JDBC generated keys、批量回填和重复键策略的详细配置。
- [方言细节](./dialect-details)：分页 SQL、标识符引用、LIKE 语法、冲突策略底层实现。

## 与通用文档的关系

通用 API 用法见 [核心API](../../guides/overview)。以下内容补充 MySQL 下的具体差异和推荐写法。
