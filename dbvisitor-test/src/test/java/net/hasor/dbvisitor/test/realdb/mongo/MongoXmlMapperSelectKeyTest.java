/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mongo;

import java.sql.SQLException;
import java.util.List;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.test.contract.api.mapper.xml.XmlMapperSelectKeyCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MongoProfile;
import net.hasor.dbvisitor.test.realdb.mongo.material.MongoKeyFixture;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class MongoXmlMapperSelectKeyTest extends XmlMapperSelectKeyCase {
    private final MongoKeyFixture fixture = new MongoKeyFixture();

    @Override
    protected DataSourceProfile profile() {
        return MongoProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = this.fixture.open();
    }

    @Override
    @Before
    public void createXmlMapperSession() throws Exception {
        Configuration configuration = newConfiguration();
        configuration.loadMapper(mapperResource());
        this.session = this.fixture.session(configuration);
        this.fixture.reserve("XmlKeyGenBefore");
    }

    @Override
    protected String mapperResource() {
        return "/realdb/mongo/KeyMapper.xml";
    }

    @Override
    protected boolean numericGeneratedKeys() {
        return false;
    }

    @Override
    protected String readKeyName(Object id) throws Exception {
        assertGeneratedKey(id);
        List<String> rows = this.session.queryStatement("xmltest.KeyGenerationMapper.selectById", mapOf("id", id.toString()));
        assertEquals(1, rows.size());
        return rows.get(0);
    }

    @Override
    protected void assertGeneratedKey(Object id) {
        assertNotNull(id);
        assertTrue(id instanceof String || id instanceof ObjectId);
        assertTrue(ObjectId.isValid(id.toString()));
    }

    @Override
    protected void assertKeyProgression(Object previous, Object current) {
        if (previous != null) {
            assertNotEquals(previous, current);
        }
    }

    @After
    public void closeFixture() throws Exception {
        this.fixture.close();
    }
}
