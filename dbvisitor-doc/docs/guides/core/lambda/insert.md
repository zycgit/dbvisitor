---
id: insert
sidebar_position: 2
title: 新增操作
description: 使用 dbVisitor 构造器新增数据时允许使用实体 Bean 或者使用 Map 作为新数据的容器。
---

# 新增操作

使用 dbVisitor 构造器新增数据时允许使用实体 Bean 或者使用 Map 作为新数据的容器。

```java title='使用 Bean 作为数据容器'
User user = new User();
user.setId(20);
user.setName("new name");
user.setAge(88);
user.setCreateTime(new Date());

LambdaTemplate lambda = ...
int result = lambda.insert(User.class)
                   .applyEntity(user)
                   .executeSumResult();
// 返回 result 为 1
```

```java title='使用 Map 作为数据容器'
Map<String, Object> user = new HashMap<>();
user.put("id", 20);
user.put("name", "new name");
user.put("age", 88);
user.put("create_time", new Date());

LambdaTemplate lambda = ...
int result = lambda.insert(User.class)
                   .applyMap(user)
                   .executeSumResult();
// 返回 result 为 1
```

## 自增主键回填 {#generated-keys}

当实体映射中的主键由数据库生成时，Lambda 新增可以配合对象映射和数据库方言完成主键回填。不同数据库的主键返回方式差异较大，具体用法请阅读 [数据源特性](../../../features/overview)。Generated keys 的通用说明见 [Generated Keys](../mapper/annotation_insert#generated-keys)。

常规使用时只需要确认两件事：实体映射中主键列配置了生成策略，当前数据库方言支持对应的 generated keys 行为。

:::info[深入阅读]
下面的执行策略说明适合需要理解批量插入、数据库返回主键、自定义 KeyHolder 之间差异的场景。普通单行插入可以先跳过。
:::

Lambda Insert 会先区分两类后置主键处理：

- 数据库返回型：`onAfter=true` 且 `useGeneratedKeys=true`，例如自增主键、需要通过 `getGeneratedKeys()`、`RETURNING`、`OUTPUT INSERTED` 返回的列。
- 自定义后置型：`onAfter=true` 但 `useGeneratedKeys=false`，由用户自定义 `KeyHolder` 在插入后处理，不依赖数据库返回的 generated-key 结果集。

只有数据库返回型列会作为 `returnColumns` 交给数据库方言生成 SQL。对于 PostgreSQL 可能生成 `RETURNING`，对于 SQL Server 可能生成 `OUTPUT INSERTED`，其它数据库也可能使用 JDBC `getGeneratedKeys()`。

批量插入时，dbVisitor 会按如下规则选择执行方式：

- 没有主键回填需求时，优先使用普通 JDBC batch。
- 只有数据库返回型主键回填时，由数据库方言选择更合适的方式，例如 PostgreSQL `VALUES (...), (...) RETURNING id` 或 SQL Server `OUTPUT INSERTED.id`。
- 如果存在自定义后置 `KeyHolder`，则保守退回逐条执行，保证每一行插入后都能执行用户自定义的后置逻辑。

```text
Lambda Insert 主键回填策略

ColumnMapping
    |
    +-- onBefore=true
    |       |
    |       +-- INSERT 前生成值
    |           例如 UUID32、UUID36、Sequence、自定义 before KeyHolder
    |
    +-- onAfter=true
            |
            +-- useGeneratedKeys=true
            |       |
            |       +-- returnColumns
            |           |
            |           +-- 交给数据库方言选择返回方式
            |               |
            |               +-- PostgreSQL: RETURNING
            |               +-- SQL Server: OUTPUT INSERTED
            |               +-- JDBC: getGeneratedKeys()
            |
            +-- useGeneratedKeys=false
                    |
                    +-- customAfterProperties
                        |
                        +-- 批量插入退回 OneByOne
                            保证每行 INSERT 后执行自定义 afterApply

执行策略
    |
    +-- customAfterProperties 非空
    |       |
    |       +-- OneByOne
    |
    +-- returnColumns 非空
    |       |
    |       +-- 方言选择 MultiValuesResultSet / JdbcBatchGeneratedKeys / OneByOne
    |
    +-- returnColumns 为空
            |
            +-- 优先 JdbcBatch
```

因此，`KeyType.UUID32`、`KeyType.UUID36`、`KeyType.Sequence` 这类前置生成策略通常不会触发数据库返回列；`KeyType.Auto` 或 `useGeneratedKeys=true` 的 `KeyHolder` 才会参与 generated keys 回填。

## 批量化 {#batch}

当有大量数据需要插入时可以选择使用批量化写入。

```java
User user1 = new User();
...
User user2 = new User();
...
User user3 = new User();
...

LambdaTemplate lambda = ...
int result = lambda.insert(User.class)
                   .applyEntity(user1, user2, user3);               // 不定参方式
                 //.applyEntity(new User[]{user1, user2, user3});  // 使用数组
                 //.applyEntity(Arrays.asList(user1, user2, user3));// 使用 List
                   .executeSumResult();
// 返回 result 为 3
```

- 对于 Map 结构可以使用 applyMap 方法替代 applyEntity。
- 在通过 executeSumResult 方法正式写入数据之前，applyEntity、applyMap 两个方法可以被多次调用以适应不同批次数据的设置。


## 写入冲突 {#conflict}

向数据库插入重复数据通常并非有意而为之，而一旦出现主键冲突就会比较麻烦。一般的解决办法是先查询在选择更新或者是写入。

```java title='常规方法'
if (adapter.queryByEntity(User.class)
            .eq(User::getId,user.getId())
            .queryForCount() > 0) {
    // 更新
} else {
    // 写入
}
```

好消息是数据库本身对于写入冲突多半提供了更加高效的方式，比如：

- MySQL 数据库可以使用 `ON DUPLICATE KEY UPDATE` 字句修饰 INSERT。
- Oracle 数据库可以使用 `MERGE INTO ... WHEN MATCHED THEN ... WHEN NOT MATCHED THEN ...` 语句。

使用这些数据库特性需要有 2 个先决条件。
- 需要 dbVisitor 的数据库方言能够支持，[了解数据库支持性](../../../features/support#dialect)。
- 需要通过 onDuplicateStrategy 方法指定冲突处理策略。

dbVisitor 中对于这种情况可以配置冲突策略，这样就可以避免在写入数据时多余的代码逻辑。可选的冲突策略有三个：
- 报错(Into)，使用常规的 INSERT INTO 写入数据。
- 替换(Update)，使用 Merge 或者 ON CONFLICT 等数据库特定语言来实现数据写入冲突是自动更新。
- 忽略(Ignore)，使用 Ignore 或者其它数据库提供的专用语句来实现写入错误时忽略报错。

### 默认策略(INTO)

默认策略下将会使用普通的 `insert into` 语句进行数据插入，当遇到数据冲突通常数据库会报错。

```java title='默认策略可以不指定，也可以明确设置'
LambdaTemplate lambda = ...
int result = lambda.insert(User.class)
                   .applyEntity(user)
                   .onDuplicateStrategy(DuplicateKeyStrategy.Into) // 明确设置
                   .executeSumResult();
```

### 替换策略(UPDATE)

替换策略的实现是根据具体数据库方言实现决定，如：

- 对于 MySQL 将会使用 `ON DUPLICATE KEY UPDATE` 子句修饰 INSERT。
- 对于 Oracle 将会使用 `MERGE INTO ... WHEN MATCHED THEN ... WHEN NOT MATCHED THEN ...` 语句。
- 对于 PostgreSql 将会使用 `ON CONFLICT (...) DO UPDATE SET ...` 子句修饰 INSERT。

:::info[提示]
该策略是否支持需要以数据库方言为准，如果数据库方言不支持此类语句强行指定会报错。
:::

```java title='使用方式'
LambdaTemplate lambda = ...
int result = lambda.insert(User.class)
                   .applyEntity(user)
                   .onDuplicateStrategy(DuplicateKeyStrategy.Update) // 冲突更新
                   .executeSumResult();
```

### 忽略策略(IGNORE)

替换策略的实现是根据具体数据库方言实现决定，如：

- 对于 MySQL 将会使用 `INSERT IGNORE` 语句。
- 对于 Oracle 将会使用 `MERGE INTO ... WHEN NOT MATCHED THEN ...` 语句。
- 对于 达梦数据库将会使用数据库 HINT `IGNORE_ROW_ON_DUPKEY_INDEX` 根据主键列进行忽略。

```java title='使用方式'
LambdaTemplate lambda = ...
int result = lambda.insert(User.class)
                   .applyEntity(user)
                   .onDuplicateStrategy(DuplicateKeyStrategy.Ignore) // 冲突忽略
                   .executeSumResult();
```
