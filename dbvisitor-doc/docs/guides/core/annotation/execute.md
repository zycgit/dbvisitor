---
id: execute
sidebar_position: 6
hide_table_of_contents: true
title: "@Execute"
description: Execute 注解用来标记在接口方法上，它接受一个字符串参数或字符串数组表示执行任意的 SQL 语句。
---
import TagRed from '@site/src/components/tags/TagRed';
import TagGray from '@site/src/components/tags/TagGray';

# @Execute 注解

用来标记在接口方法上，执行任意的 SQL 语句（DML/DDL 均可）。

:::info
如果传入的是一个字符串数组，它们会被连接起来，中间用一个空格隔开。<br/>
通过字符串数组可以更清晰地管理多行 SQL。
:::

```java title='示例：创建 users 分表'
@SimpleMapper
public interface UserMapper {
    @Execute({"create table users_${part} (",// 1. 定义 SQL 语句
              "   id   int primary key,",    //
              "   name varchar(50)",         //
              ")"})
    int newPartTable(                      
            @Param("part") int partId        // 2. partId 参数
    );
}
```

- `${part}` 采用的是字符串替换方式传递参数（注意 SQL 注入风险）

## 属性清单

| 属性名 | 描述 |
|---|---|
| value | <TagRed/> 将要被执行的 SQL 语句。 |
| statementType | <TagGray/> JDBC 执行使用何种方式。默认值为 `Prepared`<br/> - `Statement` 对应 `java.sql.Statement`<br/> - `Prepared` 对应 `java.sql.PreparedStatement`<br/> - `Callable` 对应 `java.sql.CallableStatement` |
| timeout | <TagGray/> 设置执行超时时间（秒），会调用 `Statement.setQueryTimeout(int)`。默认值 `-1` 表示不设置 |
| bindOut | <TagGray/> 绑定输出参数名称，用于接收存储过程的输出参数或多结果集。<br/>使用该参数时返回值类型必须为 **Map&lt;String,Object&gt;** |
