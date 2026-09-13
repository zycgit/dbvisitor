---
id: login-state
sidebar_position: 2
title: Login State
---

Use a login token to locate expiring user state and delete it on logout.

## Data Structure

Key `demo:login:{token}` stores user-state JSON in a String with an 1800-second lifetime.

```json
{"userId":"u1001","displayName":"mali"}
```

## Mapping

Store login state as one JSON value, without passwords.

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

## Calling the Mapper

Run inside the [Session created above](about.md#create-a-session):

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

`saved`, `renewed` and `removed` are `1`; `current.userId` is `u1001`, and `missing` is `null` after logout. renew sets a fresh 3600-second lifetime rather than adding 3600 seconds to the remaining lifetime.

## Notes

- The login flow generates and protects the token. This example covers state storage, not authentication.
- A null load result means missing or expired state. renew returning 0 means the key is absent and must not be treated as a valid login.
