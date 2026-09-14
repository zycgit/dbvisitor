---
id: search
sidebar_position: 1
title: 搜索与计数
---

:::info[说明]
对应 REST 接口：`_search`、`_count`。
:::

## 搜索

```text
POST /user_info/_search {"query":{"term":{"uid":"1001"}},"size":10}
```

`uid` 应为 keyword 字段。返回命中的 `_ID`、`_DOC`，默认还会展开文档字段。只需要某个字段时，按列名读取，见[结果读取](../dbvisitor/results.mdx)。

## 查询总数

```text
POST /user_info/_count {"query":{"term":{"uid":"1001"}}}
```

总数使用 `_count`，不是当前搜索页的行数。dbVisitor Page 的用法见[分页查询](../dbvisitor/pagination.mdx)。

## 聚合


单值聚合返回一行，聚合名称作为列名：

```text
POST /user_info/_search {"aggs":{"max_age":{"max":{"field":"age"}}}}
```

按字段分组使用 `composite`，下面返回 `age`、`cnt` 两列：

```text
POST /user_info/_search {
  "aggs": {
    "rows": {
      "composite": {"sources": [{"age": {"terms": {"field": "age", "missing_bucket": true}}}]},
      "aggs": {"cnt": {"filter": {"match_all": {}}}}
    }
  }
}
```

去掉子聚合 `aggs` 即可读取去重后的字段组合；多个字段在 `sources` 中逐一列出。驱动通过 `after_key` 按需取下一页，JDBC `fetchSize` 控制每页桶数。

需要精确统计唯一值数量时，在 `rows` 中添加 `"meta":{"dbvisitor":{"mode":"count","column":"distinct_count"}}`。此方式遍历所有唯一桶，忽略含空值的组合，不能当作低成本的近似计数。

搜索结果不包含完整 REST 响应中的 `_score`、`highlight` 等内容。

## 脚本计算列

`script_fields` 声明的名称可作为结果列读取。下面返回 `age_twice` 一列；只投影脚本列时，不需要读取索引 mapping：

```text
POST /user_info/_search {
  "_source": false,
  "script_fields": {
    "age_twice": {"script": {"source": "doc['age'].value * params.factor", "params": {"factor": 2}}}
  }
}
```

动态值放在 `params` 中绑定，不拼入 `source`。单值结果直接返回标量，多值返回 `List`，空值返回 `null`。
