/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.mapper;

import java.sql.SQLException;
import java.util.*;
import org.junit.Test;
import net.hasor.dbvisitor.test.nxn.capability.*;
import net.hasor.dbvisitor.test.realdb.redis.dto1.RedisParameterUser;
import static org.junit.Assert.*;

public class RedisMapperCoverageContractTest extends RedisNativeMapperSupport {

    private RedisCoverageMapper coverage() throws Exception {
        return session.createMapper(RedisCoverageMapper.class);
    }

    private RedisParameterUser user(int id, String name) {
        RedisParameterUser user = new RedisParameterUser();
        user.setId(id);
        user.setName(name);
        return user;
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_NATIVE_QUERY)
    public void nativeQuery_shouldReturnObjectListAndScalar() throws Exception {
        RedisCoverageMapper mapper = coverage();
        String key = key("users");
        assertEquals(1, mapper.appendBean(key, user(1, "first")));
        assertEquals(2, mapper.appendBean(key, user(2, "second")));
        List<RedisParameterUser> rows = mapper.beans(key);
        assertEquals(2, rows.size());
        assertEquals(Integer.valueOf(1), rows.get(0).getId());
        assertEquals("first", rows.get(0).getName());
        assertEquals(Integer.valueOf(2), rows.get(1).getId());
        assertEquals("second", rows.get(1).getName());
        assertEquals(2, mapper.count(key));
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_SCALAR)
    public void scalar_shouldMapIntegerTextCountAndDate() throws Exception {
        RedisCoverageMapper mapper = coverage();
        String age = key("age");
        String name = key("name");
        String date = key("date");
        String list = key("list");
        mapper().put(age, "23");
        mapper().put(name, "mali");
        java.sql.Date expected = java.sql.Date.valueOf("2024-03-15");
        session.jdbc().executeUpdate("SET ? ?", new Object[] { date, expected });
        session.jdbc().executeUpdate("RPUSH ? a b c", list);
        assertEquals(Integer.valueOf(23), mapper.integer(age));
        assertEquals("mali", mapper().get(name));
        assertEquals(3, mapper.count(list));
        assertEquals(expected, mapper.date(date));
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_NULL_VALUE)
    public void jsonBean_shouldPreserveNullProperties() throws Exception {
        RedisCoverageMapper mapper = coverage();
        String key = key("null-properties");
        RedisParameterUser value = user(1, "nullable");
        value.setAge(null);
        value.setEmail(null);
        assertEquals(1, mapper.putBean(key, value));
        RedisParameterUser actual = mapper.bean(key);
        assertNotNull(actual);
        assertEquals(Integer.valueOf(1), actual.getId());
        assertEquals("nullable", actual.getName());
        assertNull(actual.getAge());
        assertNull(actual.getEmail());
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_SPECIAL_TEXT)
    public void jsonBean_shouldPreserveQuotesAndUnicode() throws Exception {
        RedisCoverageMapper mapper = coverage();
        String quoted = key("quoted");
        String unicode = key("unicode");
        assertEquals(1, mapper.putBean(quoted, user(1, "O'Brien & Co.")));
        assertEquals(1, mapper.putBean(unicode, user(2, "测试用户 テスト")));
        assertEquals("O'Brien & Co.", mapper.bean(quoted).getName());
        assertEquals("测试用户 テスト", mapper.bean(unicode).getName());
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_DISTINCT_LIST)
    public void setResult_shouldMapDistinctScalarValues() throws Exception {
        String key = key("distinct");
        session.jdbc().executeUpdate("SADD ? 23 23 28", key);
        List<Integer> values = coverage().distinct(key);
        assertEquals(2, values.size());
        assertEquals(new HashSet<>(Arrays.asList(23, 28)), new HashSet<>(values));
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_NATIVE_COMMAND_ERRORS)
    public void commandErrors_shouldExposeSyntaxTypeAndNumericFailures() throws Exception {
        RedisCoverageMapper mapper = coverage();
        assertThrows(SQLException.class, mapper::invalidSyntax);
        String wrongType = key("wrong-type");
        mapper().put(wrongType, "text");
        SQLException typeError = assertThrows(SQLException.class, () -> mapper.hash(wrongType));
        assertTrue(typeError.getMessage().contains("WRONGTYPE"));
        SQLException numberError = assertThrows(SQLException.class, () -> mapper.increment(wrongType));
        assertTrue(numberError.getMessage().contains("integer"));
        assertEquals("text", mapper().get(wrongType));
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_KEYGEN_EXPLICIT_ID)
    public void explicitKey_shouldWriteCallerIdentifierWithoutGeneration() throws Exception {
        session.getConfiguration().loadMapper("/mapper/redis/CoverageMapper.xml");
        Generated value = new Generated();
        value.setKey(key("explicit"));
        value.setId(99L);
        value.setValue("explicit-value");
        assertEquals(1, ((Number) session.executeStatement("redis.Coverage.explicit", value)).intValue());
        assertEquals(Long.valueOf(99), value.getId());
        assertEquals(Arrays.asList("explicit-value"), session.queryStatement("redis.Coverage.explicitRead", value));
    }
}
