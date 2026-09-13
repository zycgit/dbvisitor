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
import net.hasor.dbvisitor.test.nxn.capability.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class RedisAnnotationMapperCommandErrorTest extends RedisNativeMapperSupport {
    private RedisCoverageMapper coverage() throws Exception {
        return session.createMapper(RedisCoverageMapper.class);
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
}
