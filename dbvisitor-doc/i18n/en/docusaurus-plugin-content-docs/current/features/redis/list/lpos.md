---
id: lpos
sidebar_position: 14
title: LPOS
---

:::info[Note]
Official reference: [LPOS](https://redis.io/docs/latest/commands/lpos/).
:::

Find the positions of an element.

## Syntax

```text
LPOS key element [RANK rank] [COUNT count] [MAXLEN len]
```

`RANK` selects the matching occurrence to start from; negative ranks search from the tail. `COUNT` limits returned positions, with zero meaning all. `MAXLEN` limits the search length.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | multiple | RESULT field, LONG type |

:::caution[Caution]
Currently `COUNT` takes effect only with `RANK` or `MAXLEN`, as in `RANK 1 COUNT 2`. The single-result path without an effective COUNT can fail when the element is missing; do not interpret this as position zero.
:::

## Example

```text
RPUSH demo:queue first second first
LPOS demo:queue first RANK 1 COUNT 2
```
