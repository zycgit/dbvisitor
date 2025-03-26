---
id: insert
sidebar_position: 1
hide_table_of_contents: true
title: 新增操作
description: 使用 dbVisitor 构造器新增数据时允许使用实体 Bean 或者使用 Map 作为新数据的容器。
---

使用 dbVisitor 构造器新增数据时允许使用实体 Bean 或者使用 Map 作为新数据的容器。

```java title='使用 Bean 作为数据容器'
User user = new User();
user.setId(20);
user.setName("new name");
user.setAge(88);
user.setCreateTime(new Date());

WrapperAdapter adapter = ...
int result = adapter.insertByEntity(User.class)
                    .applyEntity(user);
                    .executeSumResult();
// 返回 result 为 1
```

```java title='使用 Map 作为数据容器'
Map<String, Object> user = new HashMap<>();
newValue.put("id", 20);
newValue.put("name", "new name");
newValue.put("age", 88);
newValue.put("create_time", new Date());

WrapperAdapter adapter = ...
int result = adapter.insertByEntity(User.class)
                    .applyMap(user);
                    .executeSumResult();
// 返回 result 为 1
```

## 批量化 {#batch}

当有大量数据需要插入时可以选择使用批量化写入。

```java
User user1 = new User();
...
User user2 = new User();
...
User user3 = new User();
...

WrapperAdapter adapter = ...
int result = adapter.insertByEntity(User.class)
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
- 需要 dbVisitor 的数据库方言能够支持，[了解数据库支持性](../../yourproject/support)。
- 需要通过 onDuplicateStrategy 方法指定冲突处理策略。

dbVisitor 中对于这种情况可以配置冲突策略，这样就可以避免在写入数据时多余的代码逻辑。可选的冲突策略有三个：
- 报错(Into)，使用常规的 INSERT INTO 写入数据。
- 替换(Update)，使用 Merge 或者 ON CONFLICT 等数据库特定语言来实现数据写入冲突是自动更新。
- 忽略(Ignore)，使用 Ignore 或者其它数据库提供的专用语句来实现写入错误时忽略报错。

### 默认策略(INTO)

默认策略下将会使用普通的 `insert into` 语句进行数据插入，当遇到数据冲突通常数据库会报错。

```java title='默认策略可以不指定，也可以明确设置'
WrapperAdapter adapter = ...
int result = adapter.insertByEntity(User.class)
                    .applyEntity(user);
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
WrapperAdapter adapter = ...
int result = adapter.insertByEntity(User.class)
                    .applyEntity(user);
                    .onDuplicateStrategy(DuplicateKeyStrategy.Update) // 冲突更新
                    .executeSumResult();
```

### 忽略策略(IGNORE)

替换策略的实现是根据具体数据库方言实现决定，如：

- 对于 MySQL 将会使用 `INSERT IGNORE` 语句。
- 对于 Oracle 将会使用 `MERGE INTO ... WHEN NOT MATCHED THEN ...` 语句。
- 对于 达梦数据库将会使用数据库 HINT `IGNORE_ROW_ON_DUPKEY_INDEX` 根据主键列进行忽略。

```java title='使用方式'
WrapperAdapter adapter = ...
int result = adapter.insertByEntity(User.class)
                    .applyEntity(user);
                    .onDuplicateStrategy(DuplicateKeyStrategy.Ignore) // 冲突忽略
                    .executeSumResult();
```
