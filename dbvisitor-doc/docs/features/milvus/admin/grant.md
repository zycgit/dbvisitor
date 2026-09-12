---
id: grant
sidebar_position: 10
title: GRANT / REVOKE
---

:::info[说明]
对应 SDK 方法：`grantRole`、`revokeRole`、`grantPrivilege`、`revokePrivilege`、`grantPrivilegeV2`、`revokePrivilegeV2`。
:::

## 授权与撤销

```sql
GRANT ROLE role_name TO username;
REVOKE ROLE role_name FROM username;

GRANT Search ON Collection table_name TO ROLE role_name;
REVOKE Search ON Collection table_name FROM ROLE role_name;
GRANT Query ON Collection * TO ROLE role_name;
```

权限名和 GRANT/REVOKE 的对象类型按 SDK 原样传递，示例的对象类型是 Collection，不是 TABLE；应使用服务端认可的大小写和权限名。SHOW GRANTS 则使用本适配器的 ON TABLE/USER/GLOBAL 过滤语法。

不带 ON 返回 SDK 查询范围内的授权记录，不表示跨所有数据库汇总；ON TABLE / USER 按对象类型和名称精确过滤，ON GLOBAL 只返回 Global 类型的记录。这里查询授权记录，不展开全局或通配授权来计算某个对象的有效权限。ROLE 列从 SDK 角色详情取得，不依赖单条授权记录是否带有角色名。

执行授权操作时应提供有权执行该操作的连接身份。Milvus 2.6.2 即使关闭认证检查，GRANT 仍需从连接认证信息取得授权者；匿名连接可能返回授权信息缺失错误。驱动不会自动补入管理员凭据。


## 显式数据库范围的授权 {#scoped-privileges}

```sql
GRANT PRIVILEGE Search ON DATABASE app_db TABLE books TO ROLE reader;
REVOKE PRIVILEGE Search ON DATABASE app_db TABLE books FROM ROLE reader;
-- 明确授权 app_db 下的所有集合；权限组由服务端解释
GRANT PRIVILEGE CollectionReadOnly ON DATABASE app_db TABLE * TO ROLE reader;
REVOKE PRIVILEGE CollectionReadOnly ON DATABASE app_db TABLE * FROM ROLE reader;
```

带 `PRIVILEGE ... ON DATABASE ... TABLE ...` 的形式分别调用 SDK `grantPrivilegeV2`、`revokePrivilegeV2`，与前面的旧版 `GRANT Search ON Collection ...` 命令区分。数据库、集合范围都必须提供，不使用连接当前数据库作为隐式替代，也不自动补 `*`；数据库和集合位置均可显式写 `*`，实际权限范围及权限组名称由服务端校验。名称与权限均为 SQL 标识符，不是可用 `?` 绑定的值。

执行后连接的当前数据库不变，成功返回更新计数 0（SDK 确认，不是授权数量）。用 `SHOW ROLE reader ON DATABASE app_db` 检查指定数据库中的角色授权，也可显式用 `ON DATABASE *` 查询 SDK 的通配数据库范围；省略 ON DATABASE 时仍使用连接数据库。无需也不支持通过 JDBC `setCatalog()` 切换当前连接的数据库。结果是授权记录，不是鉴权效果证明。服务端不支持 V2 或拒绝操作时抛出 SQLException，不回退到缺少显式数据库范围的旧接口。
