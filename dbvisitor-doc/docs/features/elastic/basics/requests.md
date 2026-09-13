---
id: requests
sidebar_position: 1
title: 请求格式与参数
---

命令由 HTTP 方法、路径和可选 JSON 请求体组成。使用 REST 请求，不使用 Elasticsearch SQL。

```text
POST /user_info/_search {"query":{"term":{"uid": ?}}}
PUT /user_info/_doc/{?}?refresh=wait_for {"name": ?}
```

- JSON 值使用 `?`，路径和 URL 参数值使用 `{?}`，按出现顺序绑定。
- JSON 字段名须加引号；字符串参数不需要自行拼接引号。
- 多条请求可用分号分隔；它们是多语句，不是 JDBC batch。

`GET /_cat/...` 自动追加 `format=json`；显式指定时也必须使用 json。通用 REST 支持 GET、POST、PUT、DELETE；HEAD 将 HTTP 状态码放在 `STATUS` 列中。

命令的返回形式见[命令返回结果](results.md)。
