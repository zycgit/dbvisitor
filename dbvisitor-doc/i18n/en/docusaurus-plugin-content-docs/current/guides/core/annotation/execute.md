---
id: execute
sidebar_position: 6
hide_table_of_contents: true
title: Execute 注解
description: Execute 注解用来标记在接口方法上，它接受一个字符串参数或字符串数组表示执行任意的 SQL 语句。
---
import TagRed from '@site/src/components/tags/TagRed';
import TagGray from '@site/src/components/tags/TagGray';

# @Execute 注解
## 注解说明

用来标记在接口方法上，它接受一个字符串参数或字符串数组表示执行任意的 SQL 语句。

:::info
如果传入的是一个字符串数组，它们会被连接起来，并且中间用一个空格隔开。<br/>
通过字符串数组以合理阅读方式来管理SQL。
:::

```java title='示例：创建 users 分表'
@SimpleMapper
public interface UserMapper {
    @Execute({"create table users_${part} (",// 1. 定义 SQL 语句
              "   id   int primary key,",    //
              "   name varchar(50)",         //
              ")"})
    int newPartTable(                      
            @Param("part") int partId        // 2. userId 参数
    );
}
```

- `${part}` 采用的是 SQL 注入方式传递参数

## 属性清单

| 属性名           | 描述                                                                                                                                                                                                |
|---------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| value         | <TagRed/> 将要被执行的查询语句。                                                                                                                                                                             |
| statementType | <TagGray/> 表示查询类型，表示 JDBC 查询使用何种方式。默认值为 `PREPARED`<br/> - `STATEMENT` 对应 `java.sql.Statement`<br/> - `PREPARED` 对应 `java.sql.PreparedStatement`<br/> - `CALLABLE` 对应 `java.sql.CallableStatement` |
| timeout       | <TagGray/> 通过设置一个大于 `0` 时会被设置到 `java.sql.Statement.setQueryTimeout(int)` 可以让查询在执行时候设置一个超时时间，单位是秒，默认值为 `-1`                                                                                        |
| bindOut       | <TagGray/> 对输出参数进行过滤，用来确定哪些结果参数会被接收，如果不指定将会接收所有参数。<br/>请注意：一旦使用了该参数那么接口返回值类型必须为 **Map&lt;String,Object&gt;** 类型。                                                                                  |
