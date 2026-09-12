---
id: limits
sidebar_position: 7
title: 限制与注意事项
---

- `_cat` 查询会自动追加 `format=json` 参数（若手动指定则必须为 json）。
- 仅支持 REST 风格命令语法，不支持 Elasticsearch SQL。
