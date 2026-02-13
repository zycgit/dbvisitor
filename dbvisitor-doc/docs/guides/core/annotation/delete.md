---
id: delete
sidebar_position: 5
hide_table_of_contents: true
title: "@Delete"
description: Delete 注解用来标记在接口方法上，它接受一个字符串参数或字符串数组表示执行一个 DELETE 语句。
---
import TagRed from '@site/src/components/tags/TagRed';
import TagGray from '@site/src/components/tags/TagGray';

# @Delete 注解

用来标记在接口方法上，执行一个 DELETE 语句。

:::info
如果传入的是一个字符串数组，它们会被连接起来，中间用一个空格隔开。<br/>
通过字符串数组可以更清晰地管理多行 SQL。
:::

```java title='示例：根据 id 字段删除用户'
@SimpleMapper
public interface UserMapper {
    @Delete({"delete from users ",    // 1. 定义 SQL 语句
             "where id = #{userId}"}) //
    int deleteUserById(               // 2. userId 参数
            @Param("userId") int userId
    );
}
```

## 属性清单

| 属性名 | 描述 |
|---|---|
| value | <TagRed/> 将要被执行的 DELETE 语句。 |
| statementType | <TagGray/> JDBC 执行使用何种方式。默认值为 `Prepared`<br/> - `Statement` 对应 `java.sql.Statement`<br/> - `Prepared` 对应 `java.sql.PreparedStatement`<br/> - `Callable` 对应 `java.sql.CallableStatement` |
| timeout | <TagGray/> 设置执行超时时间（秒），会调用 `Statement.setQueryTimeout(int)`。默认值 `-1` 表示不设置 |
