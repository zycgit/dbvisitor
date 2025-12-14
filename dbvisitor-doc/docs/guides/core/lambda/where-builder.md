---
id: where-builder
sidebar_position: 5
title: 条件构造器
description: 条件构造器可以用于 query、update、delete 三类操作中，用于生成 WHERE 语句后面的条件部分。
---

条件构造器可以用于 `query`、`update`、`delete` 三类操作中，用于生成 WHERE 语句后面的条件部分。
在本问将会介绍如下内容：
- 条件样本
- 条件组
- 关系式：[与关系](./where-builder#and)、[或关系](./where-builder#or)、[非关系](./where-builder#not)

```java title='使用案例'
LambdaTemplate lambda = ...
List<User> result = lambda.query(User.class)
                          .eq(User::getAge, 32)
                          .queryForList();
```

```sql title='对应的 SQL'
SELECT * FROM users WHERE age = ?
```

具体可用的方法如下表所示：

| 方法                       | SQL                                 | 描述                                                                 |
|:-------------------------|:------------------------------------|:-------------------------------------------------------------------|
| eq                       | `=`                                 | 列 等于 值                                                             |
| ne                       | `<>`                                | 列 不等于 值                                                            |
| gt                       | `>`                                 | 列 大于 值                                                             |
| ge                       | `>=`                                | 列 大于等于 值                                                           |
| lt                       | `<`                                 | 列 小于 值                                                             |
| le                       | `<=`                                | 列 小于等于 值                                                           |
| like                     | `LIKE concat('%', ? ,'%')`          | 列 模糊匹配                                                             |
| likeLeft                 | `LIKE concat('%', ?)`               | 列 左半边模糊匹配                                                          |
| likeRight                | `LIKE concat(?, '%')`               | 列 右半边模糊匹配                                                          |
| notLike                  | `NOT LIKE concat('%', ? ,'%')`      | 列 模糊匹配（取反）                                                         |
| notLikeLeft              | `NOT LIKE concat('%', ?)`           | 列 左半边模糊匹配（取反）                                                      |
| notLikeRight             | `NOT LIKE concat(?, '%')`           | 列 右半边模糊匹配（取反）                                                      |
| isNull                   | `IS NULL`                           | 列 为空 值                                                             |
| isNotNull                | `IS NOT NULL`                       | 列 非空 值                                                             |
| in                       | `IN (? ,?)`                         | 列 值位于 **集合内**                                                      |
| notIn                    | `NOT IN (? ,?)`                     | 列 值位于 **集合外**                                                      |
| rangeBetween             | `BETWEEN ? AND ?`                   | 使用 BETWEEN ... AND ... 进行范围查找                                      |
| rangeOpenOpen            | `? <  col AND col <  ?`             | 列 值位于 **开区间内**<br/> - 开区间指的是：区间边界的两个值不包括在内：`(a,b)`                 |
| rangeClosedClosed        | `? <= col AND col <= ?`             | 列 值位于 **闭区间内**<br/> - 闭区间指的是：区间边界的两个值包括在内：`[a,b]`                  |
| rangeOpenClosed          | `? <  col AND col <= ?`             | 列 值位于 **半开半闭区间内**<br/> - 半开半闭区间指的是：左边条件处于开区间状态，右边条件处于闭区间状态：`(a,b]` |
| rangeClosedOpen          | `? <= col AND col <  ?`             | 列 值位于 **半闭半开区间内**<br/> - 半闭半开区间指的是：左边条件处于闭区间状态，右边条件处于开区间状态：`[a,b)` |
| rangeNotBetween          | `NOT BETWEEN ? AND ?`               | 使用 NOT BETWEEN ... AND ... 进行范围查找                                  |
| rangeNotOpenOpen         | `not (? <  col AND col <  ?)`       | 列 值位于 **开区间外**<br/> - 开区间指的是：区间边界的两个值不包括在内：`(a,b)`                 |
| rangeNotClosedClosed     | `not (? <= col AND col <= ?)`       | 列 值位于 **闭区间外**<br/> - 闭区间指的是：区间边界的两个值包括在内：`[a,b]`                  |
| rangeNotOpenClosed       | `not (? <  col AND col <= ?)`       | 列 值位于 **半开半闭区间外**<br/> - 半开半闭区间指的是：左边条件处于开区间状态，右边条件处于闭区间状态：`(a,b]` |
| rangeNotClosedOpen       | `not (? <= col AND col <  ?)`       | 列 值位于 **半闭半开区间外**<br/> - 半闭半开区间指的是：左边条件处于闭区间状态，右边条件处于开区间状态：`[a,b)` |
| eqBySample、eqBySampleMap | 详情查看：[条件样本](./where-builder#sample) | 参照样本生成查询条件。                                                        |
| nested(...)              | 详情查看：[条件组](./where-builder#nested)  | 用于在生成语句中通过增加括号来定义条件组。                                              |
| and、and(...)             | 详情查看：[与关系](./where-builder#and)     | 表示前后两个条件之间是 **与关系**，或者定义条件组和前面条件的关系为 **与关系**。                      |
| or、or(...)               | 详情查看：[或关系](./where-builder#or)      | 表示前后两个条件之间是 **或关系**，或者定义条件组和前面条件的关系为 **或关系**。                      |
| not、not(...)             | 详情查看：[非关系](./where-builder#not)     | 表示后面跟随的匹配条件取反，或者定义一个条件组，匹配整个条件组的反。                                 |


## 条件样本 {#sample}

条件样本方法中 sample 样本参数不为空的属性会以 and 方式拼起来，并作为一组条件。
- 类似：`('col1 = ?' and 'col2 = ?' and col3 = ?)`

在一个有多个等值条件的查询中，通过 API 多次调用 eq 方法来拼接查询虽然可以满足需要，但是借助条件样本会简化这一操作。
- 例如：查询需求 “name、mail、uid 三个属性在有值时才会被设置为查询属性”，使用下面两种方式查询是等价的：

```java title='连续 eq 拼接条件'
LambdaTemplate lambda = ...
User u = ...

List<User> result = null;
result = lambda.query(User.class)
               // name 参数不为空时，设置为条件
               .eq(StringUtils.isNotBlank(c.getName()), User::getName, c.getName())
               // email 参数不为空时，设置为条件
               .eq(StringUtils.isNotBlank(c.getEmail()), User::getEmail, c.getEmail())
               // uid 参数不为空时，设置为条件
               .eq(StringUtils.isNotBlank(c.getUID()), User::getUID, c.getUID())
               .queryForList();
```

```java title='使用 Bean 作为样本对象'
LambdaTemplate lambda = ...
User u = ...

List<User> result = null;
result = lambda.query(User.class)
               .eqBySample(u)
               .queryForList();
```

```java title='使用 Map 作为样本对象'
LambdaTemplate lambda = ...
Map<String,Object> u = ...

List<User> result = null;
result = lambda.query(User.class)
               .eqBySampleMap(u)
               .queryForList();
```

:::info[使用提示]
- 在使用条件样本时需要注意以下几点：
- eqBySample、eqBySampleMap 方法只会寻找样本中不为空的属性作为条件，因此：
  - 如果要想匹配 NULL 值，需要额外使用 isNull 来特别指定。
  - 不建议在样本对象中使用 byte、sort、int、long、float、double、char 基本类型属性。
- 如果多次调用 eqBySample 设置样本条件，那么先后两次样本中重叠部分会被覆盖。
:::

## 条件组 {#nested}

用于生成带有括号的条件组语句使用如下方式：

```java
LambdaTemplate lambda = ...

List<User> result = null;
result = lambda.query(User.class)
               .nested(qc -> {
                   qc.eq(User::getName, "log1");
                   qc.and();
                   qc.eq(User::getEmail, "log1@xx.com");
               })
               .or()
               .nested(qc -> {
                   qc.eq(User::getName, "log2");
                   qc.and();
                   qc.eq(User::getEmail, "log2@xx.com");
               })
               .queryForList();
```

```sql title='生成的 SQL'
SELECT * FROM users WHERE ( name = ? AND email = ? ) OR ( name = ? AND email = ? )
```

:::info[小贴士]
- 使用 `nested(...).or().nested(...)` 可简写为 `nested(...).or(...)`
- 使用 `nested(...).and().nested(...)` 可简写为 `nested(...).nested(...)` 或 `nested(...).and(...)`
- 使用 `nested(...).not().nested(...)` 可简写为 `nested(...).not(...)`
- 括号的层级可以有多层。
:::

## 与关系 {#and}

表达式之间的关系默认采用于关系，因此默认情况下可以不用明确指定表达式之间的关系。例如：

```java
LambdaTemplate lambda = ...

List<User> result = null;
result = lambda.query(User.class)
               .eq(User::getName, "123")
             //.and() /* 默认关系可写，可不写 */
               .eq(User::getAge, 12)
               .queryForList();
```

```sql title='生成的 SQL'
SELECT * FROM users WHERE name = ? AND age = ?
```

用于连接不同条件组之间的与关系可以使用如下方法：

```java
LambdaTemplate lambda = ...

List<User> result = null;
result = lambda.query(User.class)
               .nested(qc -> {
                   qc.eq(User::getId, 1)
                     .or()
                     .eq(User::getId, 2);
               }).and(qc -> {
                   qc.eq(User::getName, "user-1")
                     .or()
                     .eq(User::getName, "user-2");
               }).queryForList();
```

```sql title='生成的 SQL'
SELECT * FROM users WHERE (id = ? OR id = ?) AND (name = ? OR name = ?);
```

## 或关系 {#or}

表示两个条件之间的或关系使用如下方式：

```java
LambdaTemplate lambda = ...

List<User> result = null;
result = lambda.query(User.class)
               .eq(User::getName, "123") // 条件 1
               .or()                     // 或关系
               .eq(User::getAge, 12)     // 条件 2
               .queryForList();
```

```sql title='对应的 SQL'
SELECT * FROM users WHERE name = ? OR age = ?
```

用于连接不同条件组之间的或关系可以使用如下方法：

```java
LambdaTemplate lambda = ...

List<User> result = null;
result = lambda.query(User.class)
               .nested(qc -> {
                   qc.eq(User::getName, "user-1")
                     .eq(User::getStatus, 2);
               }).or(qc -> {
                   qc.eq(User::getName, "user-2")
                     .eq(User::getStatus, 2);
               }).queryForList();
```

```sql title='生成的 SQL'
SELECT * FROM users WHERE ( name = ? AND status = ? ) OR ( name = ? AND status = ? )
```

## 非关系 {#not}

对一个匹配条件取反，可以使用下面方法：

```java
LambdaTemplate lambda = ...

List<User> result = null;
result = lambda.query(User.class)
               .not()   // 对后面的 eq 等值判断取反向操作
               .eq(User::getName, "123")
               .queryForList();
```

```sql title='对应的 SQL'
SELECT * FROM users WHERE not name = ?
```

:::info[提示]
通常条件方法都有其对应的取反方法，使用这些预先定义的方法将会更加直观，如：
- eq 对应 ne
- isNull 对应 isNotNull
:::

非关系的运算的设计主要是为了一些特定场景中使用，例如对整个条件组取反。

```java
LambdaTemplate lambda = ...

List<User> result = null;
result = lambda.query(User.class)
               .not(qc -> {
                   qc.eq(User::getId, 1)
                     .or()
                     .eq(User::getId, 2);
               }).queryForList();
```

```sql title='生成的 SQL'
SELECT * FROM users WHERE NOT (id = ? OR id = ?);
```
