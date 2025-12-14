---
id: call
sidebar_position: 7
hide_table_of_contents: true
title: "@Call"
description: Call 注解用来标记在接口方法上，它接受一个字符串参数或字符串数组表示执行存储过程调用。
---
import TagRed from '@site/src/components/tags/TagRed';
import TagGray from '@site/src/components/tags/TagGray';

# @Call 注解

用来标记在接口方法上，它接受一个字符串参数或字符串数组表示执行存储过程调用。

:::info
如果传入的是一个字符串数组，它们会被连接起来，并且中间用一个空格隔开。<br/>
通过字符串数组以合理阅读方式来管理SQL。
:::

```java title='示例'
@SimpleMapper
public interface UserMapper {
    // 使用输入输出参数
    @Call("{call proc_select_user(#{abc, mode=inout, jdbcType=decimal})}")
    Map<String, Object> callSelectUser1(@Param("abc") int argAbc);

    // 仅输出参数
    @Call("{call proc_select_user(#{abc, mode=out, jdbcType=decimal})}")
    Map<String, Object> callSelectUser2();
}

UserMapper mapper = ...
Map<String, Object> res1 = mapper.callSelectUser1(23);
// res1.get("abc"); 获取 “abc” 输入输出参数

Map<String, Object> res2 = mapper.callSelectUser2();
// res2.get("abc"); 获取 “abc” 输出参数
```

- 使用 Call 注释调用存储过程时要求方法的返回值类型必须为 **Map&lt;String,Object&gt;** 类型。SQL 写法可以在 [存储过程调用章节](../jdbc/procedure) 中了解。
  - 存储过程的入参：通过参数传递
  - 存储过程的出参：通过返回值接收
  - 存储过程产生的结果集：通过返回值接收

## 属性清单

| 属性名           | 描述                                                                                                         |
|---------------|------------------------------------------------------------------------------------------------------------|
| value         | <TagRed/> 将要被执行的查询语句。                                                                                      |
| timeout       | <TagGray/> 通过设置一个大于 `0` 时会被设置到 `java.sql.Statement.setQueryTimeout(int)` 可以让查询在执行时候设置一个超时时间，单位是秒，默认值为 `-1` |
| bindOut       | <TagGray/> 对输出参数进行过滤，用来确定哪些结果参数会被接收，如果不指定将会接收所有参数。                                                         |
