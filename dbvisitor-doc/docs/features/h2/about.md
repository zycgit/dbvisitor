---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: H2
description: H2 在 dbVisitor 中的方言能力、主键回填、IDENTITY、序列和测试场景差异。
---

# H2

H2 常用于单元测试、集成测试和轻量级嵌入式场景。dbVisitor 为 H2 提供分页、序列、普通 INSERT、MERGE 冲突处理和 JDBC generated keys 回填能力。

## 快速了解差异

| 关注点 | H2 行为 |
|--------|--------|
| 主键生成 | `IDENTITY` 列 + JDBC generated keys |
| 分页 | `LIMIT ? OFFSET ?` |
| 写入冲突 | `MERGE INTO ...` |
| 批量写入 | 支持 JDBC batch（无回填时） |
| 存储过程 | 支持（Java 存储过程） |
| 序列 | 支持 `values next value for seq` |

## 主键回填

`IDENTITY` 列通过 JDBC generated keys 回填。无回填需求时使用 JDBC batch；有回填需求时逐条执行保证每条记录对应回填。

## 写入冲突策略

| 策略 | H2 实现 |
|------|--------|
| Ignore | `MERGE INTO ...`（需主键） |
| Update | `MERGE INTO ...`（需主键） |

## 测试场景建议

- H2 可使用内存数据库 URL，例如 `jdbc:h2:mem:demo;DB_CLOSE_DELAY=-1`，无需部署独立数据库服务。
- H2 的兼容模式只覆盖部分语法和行为；`jdbc:h2:` URL 默认识别为 H2 方言，不会因兼容模式自动改成 MySQL/PostgreSQL 方言。
- 在实际部署的数据库上验证分页、序列、类型和事务行为。

## 专题

- [自增主键回填](./generated-keys)：`IDENTITY`、sequence、`KeyType.Auto`、`KeyType.Sequence` 和 Mapper 配置。
- [方言细节](./dialect-details)：分页 SQL、MERGE 语法、序列的底层实现。

## 与通用文档的关系

通用 API 用法见 [核心API](../../guides/overview)。以下内容补充 H2 下的具体差异和推荐写法。
