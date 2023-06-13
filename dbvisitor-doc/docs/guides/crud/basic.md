---
sidebar_position: 1
title: 基础操作
description: 使用 dbVisitor ORM 框架的 LambdaTemplate 类进行基础 CRUD 操作
---

# 基础操作

dbVisitor 单表模式是围绕 `LambdaTemplate` 工具类展开，它继承自 `JdbcTemplate` 具备后者的所有能力。

自使用单表模式时需要定义 DTO 对象，下面从最简单的 `CRUD` 开始。

```sql title='假定表结构'
create table `test_user` (
    `id`          int(11),
    `name`        varchar(255),
    `age`         int,
    `create_time` datetime,
    primary key (`id`)
);
```

```java title='对应的 DTO'
@Table(mapUnderscoreToCamelCase = true)
public class TestUser {
    private Integer id;
    private String  name;
    private Integer age;
    private Date    createTime;

    // getters and setters omitted
}
```

创建 `LambdaTemplate` 也只需要简单的 new 出来即可

```java
LambdaTemplate lambdaTemplate = new LambdaTemplate(dataSource);
```

或者

```java
LambdaTemplate lambdaTemplate = new LambdaTemplate(connection);
```

## 新增数据

使用 LambdaTemplate 类插入新数据，新数据使用 **[对象映射](../objects/class-as-table.md)** 方式承载。

```java {8}
TestUser testUser = new TestUser();
testUser.setId(20);
testUser.setName("new name");
testUser.setAge(88);
testUser.setCreateTime(new Date());

InsertOperation<TestUser> insert = lambdaTemplate.lambdaInsert(TestUser.class);
int result = insert.applyEntity(testUser).executeSumResult();
```

```text title='执行结果为'
1
```

## 新增数据(Map)

使用 LambdaTemplate 类插入新数据，新数据使用 Map 承载，Map 的 `key` 值为 `列名`。

```java {8}
Map<String, Object> newValue = new HashMap<>();
newValue.put("id", 20);
newValue.put("name", "new name");
newValue.put("age", 88);
newValue.put("create_time", new Date());

LambdaInsert<TestUser> insert = lambdaTemplate.lambdaInsert(TestUser.class);
int result = insert.applyMap(newValue).executeSumResult();
```

```text title='执行结果为'
1
```

## 批量新增数据

和新增一条数据时一样，这次使用 `applyEntity` 的集合重载方法。

```java {16-17,19}
TestUser data1 = new TestUser();
data1.setId(20);
data1.setName("new name");
data1.setAge(88);
data1.setCreateTime(new Date());

TestUser data2 = new TestUser();
data2.setId(30);
data2.setName("new name2");
data2.setAge(77);
data2.setCreateTime(new Date());

LambdaInsert<TestUser> insert = lambdaTemplate.lambdaInsert(TestUser.class);

List<TestUser> dataList = new ArrayList<>();
dataList.add(data1);
dataList.add(data2);

int result = insert.applyEntity(dataList).executeSumResult();
```

也可以通过反复 `applyEntity` 统一执行 `executeSumResult` 以达到批量新增的目的

```java {3-4}
LambdaInsert<TestUser> insert = lambdaTemplate.lambdaInsert(TestUser.class);

insert = insert.applyEntity(data1);
insert = insert.applyEntity(data2);

int result = insert.executeSumResult();
```

```text title='执行结果为'
2
```

## 批量新增数据(Map)

只需要将 `applyEntity` 方法更换为 `applyMap` 即可使用 `Map` 作为新增数据的容器

```java {16-17,19}
Map<String, Object> data1 = new HashMap<>();
data1.put("id", 20);
data1.put("name", "new name");
data1.put("age", 88);
data1.put("create_time", new Date());

Map<String, Object> data2 = new HashMap<>();
data2.put("id", 30);
data2.put("name", "new name2");
data2.put("age", 77);
data2.put("create_time", new Date());

LambdaInsert<TestUser> insert = lambdaTemplate.lambdaInsert(TestUser.class);

List<Map<String, Object>> dataList = new ArrayList<>();
dataList.add(data1);
dataList.add(data2);

int result = insert.applyMap(dataList).executeSumResult();
```

也可以通过反复 `applyEntity` 统一执行 `executeSumResult` 以达到批量新增的目的

```java {3,4}
LambdaInsert<TestUser> insert = lambdaTemplate.lambdaInsert(TestUser.class);

insert = insert.applyMap(data1);
insert = insert.applyMap(data2);

int result = insert.executeSumResult();
```

```text title='执行结果为'
2
```

## 条件更新(使用样本)

更新 ID 为 1 的数据，将其 `name` 和 `age` 设置为最新值。

```java {6-7}
TestUser testUser = new TestUser();
testUser.setName("new name");
testUser.setAge(88);

EntityUpdateOperation<TestUser> update = lambdaTemplate.lambdaUpdate(TestUser.class);
int result = update.eq(TestUser::getId, 1)
                   .updateBySample(testUser)
                   .doUpdate();
```

:::tip
`updateBySample` 方法的参数为样本数据，`updateBySample` 方法会把样本数据中不为空的属性拼接为 `set` 的属性值
:::

```text title='执行结果为'
1
```

## 条件更新(使用Map)

或者使用 Map 承载数据，使用 Map 的时 `key` 值为 `属性名`。

```java {6-7}
Map<String, Object> newValue = new HashMap<>();
newValue.put("name", "new name");
newValue.put("age", 88);

EntityUpdateOperation<TestUser> update = lambdaTemplate.lambdaUpdate(TestUser.class);
int result = update.eq(TestUser::getId, 1)
                   .updateTo(newValue)
                   .doUpdate();
```

```text title='执行结果为'
1
```

## 删除数据

删除 ID 为 1 的数据

```java {3}
EntityDeleteOperation<TestUser> update = lambdaTemplate.lambdaDelete(TestUser.class);
int result = update.eq(TestUser::getId, 1).doDelete();
```

```text title='执行结果为'
1
```
