---
id: segment
sidebar_position: 8
hide_table_of_contents: true
title: "@Segment"
description: Segment 注解用来标记在接口方法的参数上，用于定义一个 SQL 片段。
---
import TagRed from '@site/src/components/tags/TagRed';
import TagGray from '@site/src/components/tags/TagGray';

# @Segment 注解

用来标记在接口方法上，定义一个可复用的 SQL 片段。方法名即为片段名称，在其他 SQL 中通过 `@{macro,片段名}` 规则引用。

```java title='示例：用 SQL 片段复用列列表'
@SimpleMapper
public interface UserMapper {
    // 方法名 user_do_allColumns 即为 SQL 片段名称
    @Segment("user_uuid, user_name, login_name, login_password, email, seq, register_time")
    void user_do_allColumns();

    // 也支持字符串数组，会以空格连接
    @Segment({"user_uuid, user_name, login_name,",
              "login_password, email, seq, register_time"})
    void user_do_allColumns2();

    // 通过 @{macro,user_do_allColumns} 引用上面定义的 SQL 片段
    @Insert({                                                               //
            "insert into user_info (@{macro,user_do_allColumns})",          //
            "values (#{userUuid}, #{name}, #{loginName}, #{loginPassword}, #{email}, #{seq}, #{registerTime})" })
    int createUser(UserInfo tbUser);
}
```

:::tip[提示]
`@Segment` 标注的方法返回类型通常为 `void`，该方法不会被实际调用，仅作为 SQL 片段的载体。
:::

## 属性清单

| 属性名 | 描述 |
|---|---|
| value | <TagRed/> 定义的 SQL 片段内容（支持字符串数组，以空格连接）。 |
