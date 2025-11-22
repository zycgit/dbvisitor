---
id: args_rule
sidebar_position: 1
hide_table_of_contents: true
title: 7.1 参数处理规则
description: 此类规则主要特点为定义或者处理数据。比如：计算MD5、生成 UUID、加密/解密等。
---

# 参数处理规则

此类规则主要特点为定义或者处理数据。比如：计算MD5、生成 UUID、加密/解密等。

| 规则                        | 描述                                                           |
|---------------------------|--------------------------------------------------------------|
| [`@{md5, argExpr}`](#md5) | 对 `argExpr` 进行 OGNL 求值，产生的结果进一步进行 MD5 计算。将 DM5 值作为最终 SQL 参数。 |
| [`@{uuid32}`](#uuid)      | 产生一个 32 字符长度的 `UUID`，并加入到 SQL 参数中。                           |
| [`@{uuid36}`](#uuid)      | 产生一个 36 字符长度的 `UUID`，并加入到 SQL 参数中。                           |
| [`@{pairs}`](#pairs)      | 用于遍历 Map/List/Array 等集合类型，并使用固定模版生成查询条件。                     |

## MD5 规则 {#md5}

```sql title='根据账号和密码查询用户(密码已经过MD5加密)'
select * from users where account = :loginName and password = @{md5, loginPassword}
```

## UUID 规则 {#uuid}

```sql title='新增用户自动生成 32 长度的 UUID 作为 UID'
insert into users (id,uid,name,time) values (:id, @{uuid32}, :name, now());
```

## PAIRS 规则 {#pairs}

规则用法：`@{pairs, <参数>, <模版>}`，在模版中可以使用以下变量：
- `:k` 表示集合的 Key，对于 Map 类型，`:k` 表示 Key 值。对于集合类型，`:k` 表示索引值，和 `:i` 相同。
- `:v` 表示集合的 Value，对于 List/Array 类型，`:v` 表示元素值。
- `:i` 表示当前遍历的索引，从 0 开始。

```java title='一个 Map 数据'
Map<String,String> hashData = new HashMap<>();
hashData.put("field1", "value1");
hashData.put("field2", "value2");
```

```redis title='将 Map 集合存储到 Redis HASH 结构'
HSET myKey1 @{pairs, :arg0, :k :v}
```
- 规则解释
  - **:arg0**, 表示第一个参数
  - **:k**, 表示 Map 集合的 Key
  - **:v**, 表示 Map 集合的 Value
- 生成的语句：
  - `HSET ? ? ? ? ?`
  - `HSET myKey1 field1 value1 field2 value2`
