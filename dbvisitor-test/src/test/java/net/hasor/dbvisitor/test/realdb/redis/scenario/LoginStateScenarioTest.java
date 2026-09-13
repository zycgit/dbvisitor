/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.scenario;

import net.hasor.dbvisitor.test.realdb.redis.scenario.mapper.LoginStateMapper;
import net.hasor.dbvisitor.test.realdb.redis.scenario.model.LoginState;
import org.junit.Test;
import static org.junit.Assert.*;

public class LoginStateScenarioTest extends RedisScenarioSupport {
    @Test
    public void storesRenewsAndRevokesLoginState() throws Exception {
        String key = key("login:opaque-token");
        LoginStateMapper mapper = this.session.createMapper(LoginStateMapper.class);
        LoginState state = new LoginState();
        state.setUserId("u1001");
        state.setDisplayName("mali");

        assertNull(mapper.load(key));
        assertEquals(1, mapper.save(key, state, 1800));
        assertEquals("u1001", mapper.load(key).getUserId());
        assertEquals("mali", mapper.load(key).getDisplayName());
        assertTtl(key, 1800);
        assertEquals(1, mapper.renew(key, 3600));
        assertTtl(key, 1800, 3600);
        assertEquals(1, mapper.logout(key));
        assertNull(mapper.load(key));
        assertEquals(0, mapper.renew(key, 3600));
    }
}
