/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis;

import java.util.Properties;
import net.hasor.dbvisitor.adapter.redis.JedisKeys;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.realdb.redis.dto1.UserInfo1;
import net.hasor.dbvisitor.test.realdb.redis.dto1.UserInfo1Mapper;
import net.hasor.dbvisitor.test.realdb.redis.dto2.UserInfo2;
import net.hasor.dbvisitor.test.realdb.redis.dto2.UserInfo2Mapper;
import net.hasor.dbvisitor.test.realdb.redis.dto3.UserInfo3;
import net.hasor.dbvisitor.test.realdb.redis.dto3.UserInfo3Mapper;
import net.hasor.dbvisitor.test.realdb.redis.dto3.UserInfo4Mapper;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import static org.junit.Assert.*;

public class RedisMapperTest {
    @org.junit.BeforeClass
    public static void assumeDataSource() {
        OneApiDataSourceManager.assumeCurrentDataSource("redis");
    }

    @Test
    @Capability(CapabilityId.ADAPTER_REDIS_MAPPER_ANNOTATION_CRUD)
    public void using_mapper_api_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(OneApiDataSourceManager.getConnection("redis"))) {
            UserInfo1Mapper infoMapper = s.createMapper(UserInfo1Mapper.class);

            UserInfo1 user = new UserInfo1();
            user.setUid("1111");
            user.setName("username");
            user.setLoginName("login_123");
            user.setLoginPassword("password");

            // insert
            int saveStatus = infoMapper.saveUser(user);
            assertEquals(1, saveStatus);

            // load
            UserInfo1 info = infoMapper.loadUser("1111");
            assertNotSame(user, info);
            assertEquals("1111", info.getUid());
            assertEquals("username", info.getName());
            assertEquals("login_123", info.getLoginName());
            assertEquals("password", info.getLoginPassword());

            // delete
            assertNotNull(s.getConnection().unwrap(Jedis.class).get("user_1111"));
            assertNull(infoMapper.loadUser("missing-user"));

            // SET overwrites the same key; no SQL UPDATE syntax is required.
            user.setName("updated");
            assertEquals(1, infoMapper.updateUser(user));
            assertEquals("updated", infoMapper.loadUser("1111").getName());
            assertEquals("login_123", infoMapper.loadUser("1111").getLoginName());
            user.setName("updated-again");
            user.setLoginName("new-login");
            assertEquals(1, infoMapper.updateUser(user));
            assertEquals("updated-again", infoMapper.loadUser("1111").getName());
            assertEquals("new-login", infoMapper.loadUser("1111").getLoginName());
            assertEquals("password", infoMapper.loadUser("1111").getLoginPassword());

            int delStatus = infoMapper.deleteUser("1111");
            assertEquals(1, delStatus);
            assertNull(s.getConnection().unwrap(Jedis.class).get("user_1111"));
            assertNull(infoMapper.loadUser("1111"));
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_REDIS_MAPPER_ANNOTATION_JSON_HANDLER)
    public void using_mapper_api_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(OneApiDataSourceManager.getConnection("redis"))) {
            UserInfo2Mapper infoMapper = s.createMapper(UserInfo2Mapper.class);

            UserInfo2 user = new UserInfo2();
            user.setUid("2222");
            user.setName("username");
            user.setLoginName("login_123");
            user.setLoginPassword("password");

            // insert
            int saveStatus = infoMapper.saveUser(user);
            assertEquals(1, saveStatus);

            // load
            UserInfo2 info = infoMapper.loadUser("2222");
            assertNotSame(user, info);
            assertEquals("2222", info.getUid());
            assertEquals("username", info.getName());
            assertEquals("login_123", info.getLoginName());
            assertEquals("password", info.getLoginPassword());

            // delete
            assertNotNull(s.getConnection().unwrap(Jedis.class).get("user_2222"));
            int delStatus = infoMapper.deleteUser("2222");
            assertEquals(1, delStatus);
            assertNull(s.getConnection().unwrap(Jedis.class).get("user_2222"));
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_REDIS_MAPPER_XML_JSON_HANDLER)
    public void using_mapper_file_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        Properties extraProps = new Properties();
        extraProps.setProperty(JedisKeys.SEPARATOR_CHAR, ";");
        try (Session s = config.newSession(OneApiDataSourceManager.getConnection("redis", extraProps))) {
            UserInfo3Mapper infoMapper = s.createMapper(UserInfo3Mapper.class);

            UserInfo3 user = new UserInfo3();
            user.setUid("3333");
            user.setName("username");
            user.setLoginName("login_123");
            user.setLoginPassword("password");

            // insert
            int saveStatus = infoMapper.saveUser(user);
            assertEquals(1, saveStatus);

            // load1
            UserInfo3 info1 = infoMapper.loadUser1("3333");
            assertEquals("3333", info1.getUid());
            assertEquals("username", info1.getName());
            assertEquals("login_123", info1.getLoginName());
            assertEquals("password", info1.getLoginPassword());

            // load2
            UserInfo1 info2 = infoMapper.loadUser2("3333");
            assertEquals("3333", info2.getUid());
            assertEquals("username", info2.getName());
            assertEquals("login_123", info2.getLoginName());
            assertEquals("password", info2.getLoginPassword());

            // delete
            assertNotNull(s.getConnection().unwrap(Jedis.class).get("user_3333"));
            int delStatus = infoMapper.deleteUser("3333");
            assertEquals(1, delStatus);
            assertNull(s.getConnection().unwrap(Jedis.class).get("user_3333"));
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_REDIS_MAPPER_XML_CRUD)
    public void using_mapper_file_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        Properties extraProps = new Properties();
        extraProps.setProperty(JedisKeys.SEPARATOR_CHAR, ";");
        try (Session s = config.newSession(OneApiDataSourceManager.getConnection("redis", extraProps))) {
            UserInfo4Mapper infoMapper = s.createMapper(UserInfo4Mapper.class);

            UserInfo1 user = new UserInfo1();
            user.setUid("4444");
            user.setName("username");
            user.setLoginName("login_123");
            user.setLoginPassword("password");

            // insert
            int saveStatus = infoMapper.saveUser(user);
            assertEquals(1, saveStatus);

            // load1
            UserInfo1 info1 = infoMapper.loadUser("4444");
            assertEquals("4444", info1.getUid());
            assertEquals("username", info1.getName());
            assertEquals("login_123", info1.getLoginName());
            assertEquals("password", info1.getLoginPassword());

            // delete
            assertNotNull(s.getConnection().unwrap(Jedis.class).get("user_4444"));
            assertNull(infoMapper.loadUser("missing-user"));

            // The XML update uses the same native overwrite operation.
            user.setName("xml-updated");
            assertEquals(1, infoMapper.updateUser(user));
            assertEquals("xml-updated", infoMapper.loadUser("4444").getName());
            assertEquals("login_123", infoMapper.loadUser("4444").getLoginName());

            int delStatus = infoMapper.deleteUser("4444");
            assertEquals(1, delStatus);
            assertNull(s.getConnection().unwrap(Jedis.class).get("user_4444"));
            assertNull(infoMapper.loadUser("4444"));
        }
    }
}
