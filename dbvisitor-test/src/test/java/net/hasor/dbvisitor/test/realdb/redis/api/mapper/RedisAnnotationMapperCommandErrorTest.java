/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.mapper;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.api.mapper.annotation.AnnotationMapperCommandErrorCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import org.junit.After;
import org.junit.Before;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RedisAnnotationMapperCommandErrorTest extends AnnotationMapperCommandErrorCase {
    private final RedisMapperFixture fixture = new RedisMapperFixture();

    @Override
    protected DataSourceProfile profile() {
        return RedisProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        fixture.open();
    }

    @After
    public void closeFixture() throws SQLException {
        fixture.close();
    }

    private RedisCoverageMapper nativeMapper;

    @Override
    public void createAnnotationMapper() throws Exception {
        fixture.open();
        nativeMapper = fixture.session().createMapper(RedisCoverageMapper.class);
    }

    @Override
    protected void prepareInvalidCommands() throws Exception {
        fixture.session().jdbc().executeUpdate("SET ? ?", new Object[] { fixture.key("wrong-type"), "text" });
    }

    @Override
    protected void executeInvalidCommand(int scenario) throws Exception {
        switch (scenario) {
            case 0 -> nativeMapper.invalidSyntax();
            case 1 -> nativeMapper.hash(fixture.key("wrong-type"));
            case 2 -> nativeMapper.increment(fixture.key("wrong-type"));
            default -> throw new IllegalArgumentException("Unknown failure scenario: " + scenario);
        }
    }

    @Override
    protected void verifyCommandFailure(int scenario, Exception error) {
        assertTrue(error instanceof SQLException);
        if (scenario == 1) {
            assertTrue(error.getMessage().contains("WRONGTYPE"));
        } else if (scenario == 2) {
            assertTrue(error.getMessage().contains("integer"));
        }
    }

    @Override
    protected void verifyAfterCommandFailures() throws Exception {
        assertEquals("text", fixture.session().jdbc().queryForString("GET ?", fixture.key("wrong-type")));
    }
}
