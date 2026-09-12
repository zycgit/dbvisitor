---
id: grants
sidebar_position: 22
title: SHOW GRANTS
---

:::info[说明]
对应 SDK 方法：`describeRole`
:::

```text
SHOW GRANTS FOR ROLE role_name;
SHOW GRANTS FOR ROLE role_name ON GLOBAL;
SHOW GRANTS FOR ROLE role_name ON TABLE table_name;
SHOW GRANTS FOR ROLE role_name ON USER username;
```

返回角色在指定范围内的直接授权，不展开用户的全部有效权限。

返回列：DATABASE、ROLE、OBJECT、OBJECT_NAME、PRIVILEGE。
