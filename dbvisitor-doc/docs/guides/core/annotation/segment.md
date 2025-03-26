---
id: segment
sidebar_position: 8
hide_table_of_contents: true
title: Segment 注解
description: Segment 注解用来标记在接口方法的参数上，用于定义一个 SQL 片段。
---
import TagRed from '@site/src/components/tags/TagRed';
import TagGray from '@site/src/components/tags/TagGray';

# @Segment 注解
## 注解说明

用来标记在接口方法的参数上，用于定义一个 SQL 片段。

```java title='示例：创建 users 分表'
@SimpleMapper
public interface UserMapper {
    // 方法名为 SQL 片段名称
    @Segment("user_uuid, user_name, login_name, login_password, email, seq, register_time")
    void user_do_allColumns();

    // 如果是一个字符串数组，它们会被连接起来，并且中间用一个空格隔开。
    @Insert({                                                               //
            "insert into user_info (@{macro,user_do_allColumns})",          // 利用 macro 宏规则引用 SQL 片段
            "values (#{userUuid}, #{name}, #{loginName}, #{loginPassword}, #{email}, #{seq}, #{registerTime})" })
    int createUser(UserInfo tbUser);
}
```

- 在 createUser 方法中利用 macro 宏规则引用 user_do_allColumns 方法定义的 SQL 片段。

## 属性清单

| 属性名              | 描述                   |
|------------------|----------------------|
| value            | <TagRed/> 定义的 SQL片段。 |
