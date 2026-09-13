---
id: results
sidebar_position: 3
title: Write Results
---

Write success and immediate search visibility are separate concerns.

- Use `refresh=wait_for` when a single-document write must be visible to a following search.
- For update/delete by query, use `refresh=true`, not `wait_for`.
- The connection option `indexRefresh=true` appends `refresh=true` to applicable writes; explicit request options take precedence.

UPDATE/DELETE requests without refresh and with indexRefresh disabled return `Statement.SUCCESS_NO_INFO`: success without a definite count. With an explicit refresh option, single-document counts use result, and by-query requests use updated/deleted.

`refresh=false` also enables count extraction but does not ensure immediate search visibility.
