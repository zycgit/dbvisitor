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

用来标记在接口方法上，执行存储过程调用。

:::info
如果传入的是一个字符串数组，它们会被连接起来，中间用一个空格隔开。<br/>
`@Call` 隐含使用 `CallableStatement`，因此没有 `statementType` 属性。
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

UserMapper mapper = ...;
Map<String, Object> res1 = mapper.callSelectUser1(23);
// res1.get("abc"); 获取 “abc” 输入输出参数

Map<String, Object> res2 = mapper.callSelectUser2();
// res2.get("abc"); 获取 “abc” 输出参数
```

- 使用 `@Call` 调用存储过程时，返回值类型应为 **Map&lt;String,Object&gt;**。SQL 写法详见 [存储过程调用章节](../jdbc/procedure)。
  - 存储过程的入参：通过方法参数传递
  - 存储过程的出参：通过返回值 Map 接收
  - 存储过程产生的结果集：通过返回值 Map 接收

## 属性清单

| 属性名 | 描述 |
|---|---|
| value | <TagRed/> 存储过程调用语句。 |
| timeout | <TagGray/> 设置执行超时时间（秒），会调用 `Statement.setQueryTimeout(int)`。默认值 `-1` 表示不设置 |
| bindOut | <TagGray/> 对输出参数进行过滤，只接收指定名称的参数。不指定则接收所有输出参数。 |
