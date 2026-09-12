---
id: results
sidebar_position: 8
title: 返回结果与原生能力边界
---


| 请求 | JDBC 返回及限制 |
| --- | --- |
| `_search` | 读取 hits.hits；`_ID` 为命中 ID，`_DOC` 仅为 `_source` JSON，不是完整响应。 |
| `_count` | 返回独立计数结果；不从 `_search` 命中页的行数推算总数。 |
| `_msearch` | 每个子查询使用一个结果集，不等同于 JDBC batch。 |
| 通用 REST | JSON 对象按顶层字段映射，数组按元素映射；不是任意接口专有返回协议的实现。 |
| 文档插入 | 返回更新计数及可选生成键；重复使用指定文档 ID 的 index 请求是覆盖，不是关系型唯一键冲突。 |

搜索路径不提供顶层 aggregations、hits.total、`_scroll_id`，也不保留命中的 `_score`、sort、highlight、fields 等元数据。`size: 0` 的聚合搜索因此不能通过 JDBC 读出聚合桶；`_DOC` 也无法补回这些信息。需要这些信息或 scroll、search_after 游标时使用官方客户端。

普通全文搜索和 keyword 精确过滤仍可使用 Query DSL；相关分析器、索引 Mapping、索引别名和模板由 Elasticsearch 管理，dbVisitor 实体映射不会创建它们。通用 REST 写请求可能返回 ResultSet，不应仅凭 HTTP 方法选择 executeUpdate；不确定返回类型时使用 execute。

### 写入可见性与计数

文档写入确认不代表下一次搜索立即可见。文档 index/update/delete 接口可使用相应 refresh 参数；`_update_by_query`、`_delete_by_query` 不应照搬文档接口的 `refresh=wait_for`。连接参数 indexRefresh 会给相关写请求追加 `refresh=true`，显式参数优先。这不是事务提交，也不使多个请求具备原子性。

当前适配器对 UPDATE/DELETE 类请求，在没有显式 refresh 参数且 indexRefresh 未启用时返回 `Statement.SUCCESS_NO_INFO`，不能把它当作负数影响行数。启用计数读取时，单文档更新使用 result，按条件操作使用 updated/deleted 字段。显式写 refresh=false 也会进入计数读取分支，但不意味着搜索已可见。

按条件更新/删除可能部分成功或发生版本冲突。驱动返回的数量不是完整任务报告；不要使用异步任务参数并期望这里自动返回任务 ID、轮询完成或提供回滚。
