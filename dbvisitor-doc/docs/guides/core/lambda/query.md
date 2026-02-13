---
id: query
sidebar_position: 4
hide_table_of_contents: true
title: 查询操作
description: 在 dbVisitor 中使用 LambdaTemplate 查询数据可以有多种方式获取返回结果。
---

# 查询操作

在 dbVisitor 中使用 LambdaTemplate 查询数据可以有多种方式获取返回结果，具体如下：
- [查询列表](./query#list)，执行查询并结果将被映射到一个对象列表。
- [查询对象](./query#object)，执行查询并结果将被映射到一个对象。
- [查询总数](./query#count)，通过 dbVisitor 所支持的数据库方言将查询语句转化为 COUNT 查询。
- [处理查询结果](./query#process)，通过使用 RowCallbackHandler、ResultSetExtractor 处理查询结果。
- [分页查询](./query#page)，使用分页查询机制进行分页查询。

:::info[提示]
查询操作中涉及查询条件相关内容请参考 **[条件构造器](./where-builder)**。
:::

## 查询列表 {#list}

执行查询，结果将被映射到一个对象列表。

```java title='结果集映射到：实体类型'
LambdaTemplate lambda = ...

List<User> result = null;
result = lambda.query(User.class)
               .le(User::getId, 100)   // 匹配 ID 小于等于 100
               .queryForList();        // 将结果集映射实体类型
```

```java title='结果集映射到：特定类型'
LambdaTemplate lambda = ...

List<UserVO> result = null;
result = lambda.query(User.class)
               .le(User::getId, 100)        // 匹配 ID 小于等于 100
               .queryForList(UserVO.class); // 结果集映射到指定类型
```

- User 实体的查询结果将其映射到其它类型时，映射规则将会依据新类型而定。详细用法参考 [对象映射](../mapping/about) 相关内容。

```java title='结果集映射到：Map'
LambdaTemplate lambda = ...

List<Map<String,Object>> result = null;
result = lambda.query(User.class)
               .le(User::getId, 100)    // 匹配 ID 小于等于 100
               .queryForMapList();      // 结果集使用 Map 结构
```

- Map 的 Keys 将会和 User 实体属性名对应。

```java
LambdaTemplate lambda = ...
RowMapper<UserVO> rowMapper = new BeanMappingRowMapper(UserVO.class);

List<UserVO> result = null;
result = lambda.query(User.class)
               .le(User::getId, 100)    // 匹配 ID 小于等于 100
               .queryForList(rowMapper);// 使用 RowMapper 处理结果集
```

- 使用 [RowMapper](../../result/for_mapper) 接口实现自定义 `ResultSet` 在读取每一行时的映射处理。

## 查询单个对象 {#object}

执行查询并结果将被映射到一个对象，如果查询结果存在多个匹配那么会引发异常。

```java title='结果集映射到：实体类型'
LambdaTemplate lambda = ...

User result = null;
result = lambda.query(User.class)
               .eq(User::getId, 100)   // 匹配 ID 等于 100
               .queryForObject();      // 将结果映射实体类型
```

```java title='结果集映射到：特定类型'
LambdaTemplate lambda = ...

UserVO result = null;
result = lambda.query(User.class)
               .eq(User::getId, 100)          // 匹配 ID 等于 100
               .queryForObject(UserVO.class); // 结果集映射到指定类型
```

- User 实体的查询结果将其映射到其它类型时，映射规则将会依据新类型而定。详细用法参考 [对象映射](../mapping/about) 相关内容。

```java title='结果集映射到：Map'
LambdaTemplate lambda = ...

Map<String,Object> result = null;
result = lambda.query(User.class)
               .eq(User::getId, 100) // 匹配 ID 等于 100
               .queryForMap();       // 结果集使用 Map 结构
```

- Map 的 Keys 将会和 User 实体属性名对应。

```java
LambdaTemplate lambda = ...
RowMapper<UserVO> rowMapper = new BeanMappingRowMapper(UserVO.class);

UserVO result = null;
result = lambda.query(User.class)
               .le(User::getId, 100)    // 匹配 ID 小于等于 100
               .queryForObject(rowMapper);// 使用 RowMapper 处理结果集
```

- 使用 [RowMapper](../../result/for_mapper) 接口实现自定义 `ResultSet` 在读取每一行时的映射处理。

## 查询总数 {#count}

通过 dbVisitor 所支持的数据库方言将查询语句转化为 COUNT 查询语句。例如：

```sql
-- 原始查询语句
select id, name from users where id <= 100;
-- 转化为 count 查询
select count(*) from (select id, name from users where id <= 100;) as TEMP_T;
```

```java
LambdaTemplate lambda = ...
RowMapper<UserVO> rowMapper = new BeanMappingRowMapper(UserVO.class);

int count = null;
count = lambda.query(User.class)
              .le(User::getId, 100)  // 匹配 ID 小于等于 100
              .queryForCount();      // 查询总数，使用 int 类型
            //.queryForLargeCount(); // 查询总数，使用 long 类型
```

- 使用 queryForCount 或是 queryForLargeCount 根据具体需要来决定。

## 处理查询结果 {#process}

通过 [RowCallbackHandler](../../result/row_callback) 专注每一行的数据处理，而非获取结果集。

```java
RowCallbackHandler handler = new RowCallbackHandler() {
    @Override
    public void processRow(ResultSet rs, int rowNum) throws SQLException {
        ...
    }
};


LambdaTemplate lambda = ...

lambda.query(User.class)
      .eq(User::getId, 100)  // 匹配 ID 等于 100
      .query(handler);       // 通过 RowCallbackHandler 处理结果集
```

使用 [ResultSetExtractor](../../result/for_extractor) 自定义 `java.sql.ResultSet` 整个结果集的处理。

```java
ResultSetExtractor extractor = new ResultSetExtractor<Map<Integer, String>>() {
    public Map<Integer, String> extractData(ResultSet rs) throws SQLException {
        Map<Integer, String> hashMap = new HashMap<>();

        while (rs.next()) {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            hashMap.put(id, name);
        }

        return hashMap;
    }
};


LambdaTemplate lambda = ...

Map<Integer, String> result = null;
result = lambda.query(User.class)
               .eq(User::getId, 100)  // 匹配 ID 等于 100
               .query(extractor);     // 通过 ResultSetExtractor 处理结果集
```

## 分页查询 {#page}

:::info[提示]
分页查询需要依赖数据库方言的支持，在 **[数据库支持性](../../api/differences/about#dialect)** 中已列出 dbVisitor 所支持的数据库。
:::

dbVisitor 内置了分页查询机制，使用方便且无需任何配置。具体工作方式为：

```java title='使用：分页参数'
LambdaTemplate lambda = ...

List<User> result = null;
result = lambda.query(User.class)
               .le(User::getId, 100) // 匹配 ID 小于等于 100
               .initPage(10, 1)      // 每页 10 条，查询第 2 页(起始页码为 0)
               .queryForList();      // 分页查询
```

```java title='使用：分页对象'
LambdaTemplate lambda = ...

// 分页对象
Page page = new PageObject();
page.setPageNumberOffset(1); // 起始页码设置为 1
page.setPageSize(10);        // 每页 10 条
page.setCurrentPage(2);      // 查询第 2 页

// 分页查询
List<User> result = null;
result = lambda.query(User.class)
               .le(User::getId, 100) // 匹配 ID 小于等于 100
               .usePage(page)        // 分页信息
               .queryForList();      // 分页查询
```

- 分页对象提供了诸多方法可用，详细请参考 [分页对象](../../api/page_object) 了解更多内容。
- 本页前面 [查询列表](./query#list) 和 [处理查询结果](./query#process) 内容中所提到的结果集获取方式，可以和分页相互配合使用。
