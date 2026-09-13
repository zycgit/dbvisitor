---
id: indexes
sidebar_position: 1
title: 索引与映射
---

索引配置通过 REST 请求管理，不由实体注解自动创建。

## 创建索引与字段

```text
PUT /user_info {"mappings":{"properties":{"uid":{"type":"keyword"},"name":{"type":"keyword"}}}}
GET /user_info/_mapping
PUT /user_info/_mapping {"properties":{"age":{"type":"integer"}}}
```

以上为 ES 7 无类型 Mapping。ES 6 使用对应版本的类型路径与 Mapping 结构。

## 设置、别名与刷新

```text
GET /user_info/_settings
PUT /user_info/_settings {"index":{"number_of_replicas":1}}
POST /_aliases {"actions":[{"add":{"index":"user_info","alias":"users"}}]}
POST /user_info/_refresh
```

## 关闭、打开与删除

```text
POST /user_info/_close
POST /user_info/_open
DELETE /user_info
```

删除索引会删除文档。重建到新索引时使用：

```text
POST /_reindex {"source":{"index":"user_info"},"dest":{"index":"user_info_copy"}}
```
