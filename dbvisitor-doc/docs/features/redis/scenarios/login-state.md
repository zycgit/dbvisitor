---
id: login-state
sidebar_position: 2
title: 登录状态
---

用登录令牌定位用户状态，设置有效期，并在退出登录时删除。

## 数据结构

键 `demo:login:{token}`，String 值保存用户信息 JSON，存活时间 1800 秒。

```json
{"userId":"u1001","displayName":"mali"}
```

## 数据映射

登录状态整体保存为 JSON，不保存密码。

```java title="LoginState.java"
package com.example.redis;

import net.hasor.dbvisitor.types.BindTypeHandler;
import net.hasor.dbvisitor.types.handler.json.JsonTypeHandler;

@BindTypeHandler(JsonTypeHandler.class)
public class LoginState {
    private String userId;
    private String displayName;

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
```

## Mapper

```java title="LoginStateMapper.java"
package com.example.redis;

import net.hasor.dbvisitor.mapper.Delete;
import net.hasor.dbvisitor.mapper.Insert;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.Query;
import net.hasor.dbvisitor.mapper.SimpleMapper;
import net.hasor.dbvisitor.mapper.Update;

@SimpleMapper
public interface LoginStateMapper {
    @Insert("SET #{key} #{state} EX #{ttlSeconds}")
    int save(@Param("key") String key, @Param("state") LoginState state,
             @Param("ttlSeconds") int ttlSeconds);

    @Query("GET #{key}")
    LoginState load(@Param("key") String key);

    @Update("EXPIRE #{key} #{ttlSeconds}")
    int renew(@Param("key") String key, @Param("ttlSeconds") int ttlSeconds);

    @Delete("DEL #{key}")
    int logout(@Param("key") String key);
}
```

## 调用示例

在[已创建的 Session](about.md#创建-session) 中执行：

```java
LoginStateMapper mapper = session.createMapper(LoginStateMapper.class);
String token = UUID.randomUUID().toString();
String key = "demo:login:" + token;

LoginState state = new LoginState();
state.setUserId("u1001");
state.setDisplayName("mali");

int saved = mapper.save(key, state, 1800);
LoginState current = mapper.load(key);
int renewed = mapper.renew(key, 3600);
int removed = mapper.logout(key);
LoginState missing = mapper.load(key);
```

`saved`、`renewed`、`removed` 均为 `1`；`current.userId` 为 `u1001`，退出后 `missing` 为 `null`。renew 从调用时重新计算 3600 秒，不是在原剩余时间上加 3600 秒。

## 注意事项

- 令牌由登录流程生成并妥善保管；此例只展示状态存储，不代替身份验证。
- load 返回 null 表示状态已失效或不存在；renew 返回 0 表示键已不存在，不应继续视为登录有效。
