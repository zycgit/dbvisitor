---
id: jdbc
slug: /features/oracle/programmatic
sidebar_position: 10
title: Programmatic API
---

## Multiple Results {#multiple-results}

`call` can collect the result set returned by a single SELECT. Do not join several SELECT statements with semicolons and pass them to `multipleExecute` to collect their results; execute the queries separately.

`RETURNING ... INTO` receives values through OUT parameters, not a SELECT result set. See [Data Backfill](backfill.mdx) for usage.
