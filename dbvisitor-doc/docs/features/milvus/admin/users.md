---
id: users
sidebar_position: 8
title: 用户语句
---

:::info[说明]
对应 SDK 方法：`createUser`、`updatePassword`、`updateUser`、`dropUser`。
:::

## 用户管理

```text
CREATE USER [IF NOT EXISTS] username PASSWORD 'password' [WITH (description='...')];
ALTER USER username PASSWORD 'new-password' REPLACE 'old-password';
DROP USER [IF EXISTS] username;
```

CREATE USER 的密码以及 ALTER USER 的新旧密码均可写 `?`，用 `PreparedStatement.setString()` 绑定；用户名仍是 SQL 标识符，不能作为值参数绑定。ALTER USER 按 SQL 顺序绑定新密码、旧密码，直接调用 SDK `updatePassword`，成功返回更新计数 0。服务端决定密码约束、权限和旧密码校验，驱动不实现本地认证，也不在修改成功后自动重放修改请求。

```java
try (PreparedStatement stmt = conn.prepareStatement(
        "ALTER USER app_user PASSWORD ? REPLACE ?")) {
    stmt.setString(1, newPassword);
    stmt.setString(2, oldPassword);
    stmt.executeUpdate();
}
```

ALTER USER 可追加 `WITH (reset_connection=false, description='...')`，值均支持参数绑定。`reset_connection` 为布尔值，默认保持 SDK 的 false；true 会让 SDK 以被修改用户和新密码重建当前客户端连接，清除原 token，可能切换当前连接身份，不能把它当作无副作用的刷新开关。`description` 为字符串，直接传给 SDK，支持情况由服务端版本决定；省略时保持 SDK 默认值。密码修改和随后重连不是原子操作：重连失败不代表密码未修改。连接池中其他连接不会被自动更新。


## 用户和角色说明 {#principal-descriptions}

创建用户或角色时也可指定 `WITH (description=?)`，分别传给 SDK `CreateUserReq.description`、`CreateRoleReq.description`。说明必须是非 NULL 字符串，可以为空；省略时保持 SDK 的空字符串默认值。创建用户按密码、说明的 SQL 顺序绑定参数。`IF NOT EXISTS` 命中已有对象时不修改密码或说明；参数仍会被读取和校验，不影响同次执行的后续语句。

```sql
CREATE USER app_user PASSWORD ? WITH (description=?);
CREATE ROLE reader WITH (description=?);
```

**Milvus 2.6.2 接受这些创建请求，但 SHOW USER / SHOW ROLE 读到的说明为空**，直接使用官方 SDK 也如此。创建成功不等于说明已持久化；驱动不会缓存输入来填充 DESCRIPTION。需要说明存储时，请确认部署版本的原生支持。

```sql
ALTER USER app_user WITH (description='应用查询账号');
ALTER ROLE reader WITH (description='只读访问角色');
ALTER USER app_user WITH (description=?);
```

这两个独立命令分别调用 SDK `updateUser`、`alterRole`，不调用密码修改、角色重建或授权接口。WITH 只接受 `description`，其值必须是非 NULL 字符串；空字符串表示清空说明。可以用 `PreparedStatement.setString()` 绑定说明内容，名称仍为 SQL 标识符。SDK 调用成功时返回更新计数 0，不表示修改了 0 个用户或角色。

**Milvus 2.6.2 不支持这两个说明更新操作**：用户说明更新会被服务端按密码修改校验并返回密码长度错误，角色说明更新返回 `UNIMPLEMENTED`。驱动原样传播失败，不通过重设密码或重建角色模拟支持。
