---
id: compaction
sidebar_position: 10
title: SHOW COMPACTION
---

:::info[Note]
SDK methods: `getCompactionState`, `getCompactionPlans`.
:::

```sql
SHOW COMPACTION ?;
SHOW COMPACTION PLANS ?;
```

`SHOW COMPACTION ?` accepts a task ID bound with `setLong` and returns one row containing `COMPACTION_ID`, `STATE`, `EXECUTING_PLANS`, `COMPLETED_PLANS`, and `TIMEOUT_PLANS`. STATE is the SDK state name; the last three columns are BIGINT plan counts, not percentages or affected-row counts.

`SHOW COMPACTION PLANS ?` returns one row containing `COMPACTION_ID`, `STATE`, and `PLANS`. PLANS is a VARCHAR JSON array; each plan contains a `sources` list of source segment IDs and a `target` segment ID. An empty plan list still returns the state row with `[]`. Separate SHOW calls are independent snapshots and can differ as the task progresses. Missing-task or server errors are surfaced as SQLException. SQL exposes only the state and failure information provided by the SDK.
