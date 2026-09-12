---
id: release
sidebar_position: 3
title: RELEASE
---

:::info[说明]
对应 SDK 方法：`releaseCollection`、`releasePartitions`。
:::

```text
RELEASE TABLE table_name [PARTITION partition_name];
/*+ sync=false */ RELEASE TABLE table_name;
```

释放集合或分区占用的内存，不删除实体。默认等待 NotLoad 状态；`sync=false` 只提交请求，不等待完成。成功返回更新计数 0。
