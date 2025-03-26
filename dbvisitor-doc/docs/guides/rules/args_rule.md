---
id: args_rule
sidebar_position: 1
hide_table_of_contents: true
title: 7.1 参数处理规则
description: 此类规则主要特点为定义或者处理数据。比如：计算MD5、生成 UUID、加密/解密等。
---

# 参数处理规则

此类规则主要特点为定义或者处理数据。比如：计算MD5、生成 UUID、加密/解密等。

| 规则                | 描述                                                           |
|-------------------|--------------------------------------------------------------|
| `@{md5, argExpr}` | 对 `argExpr` 进行 OGNL 求值，产生的结果进一步进行 MD5 计算。将 DM5 值作为最终 SQL 参数。 |
| `@{uuid32}`       | 产生一个 32 字符长度的 `UUID`，并加入到 SQL 参数中。                           |
| `@{uuid36}`       | 产生一个 36 字符长度的 `UUID`，并加入到 SQL 参数中。                           |

## 案例

```sql title='根据账号和密码查询用户(密码已经过MD5加密)'
select * from users where account = :loginName and password = @{md5, loginPassword}
```

```sql title='新增用户自动生成 32 长度的 UUID 作为 UID'
insert into users (id,uid,name,time) values (:id, @{uuid32}, :name, now());
```