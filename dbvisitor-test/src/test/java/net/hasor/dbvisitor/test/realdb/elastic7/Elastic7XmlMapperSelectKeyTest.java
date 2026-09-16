/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic7;

import java.sql.SQLException;
import java.util.Collections;
import net.hasor.dbvisitor.test.contract.api.mapper.xml.XmlMapperSelectKeyCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Elastic7Profile;
import org.junit.After;
import org.junit.Before;
import static org.junit.Assert.*;

public class Elastic7XmlMapperSelectKeyTest extends XmlMapperSelectKeyCase {
    private final Elastic7KeyMapperFixture fixture = new Elastic7KeyMapperFixture();

    @Override
    protected DataSourceProfile profile() {
        return Elastic7Profile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = fixture.open(profile().env());
        fixture.prepareReservation("XmlKeyGenBefore");
    }

    @Override
    @Before
    public void createXmlMapperSession() throws Exception {
        this.jdbcTemplate = fixture.open(profile().env());
        this.session = fixture.session();
        session.getConfiguration().loadMapper(mapperResource());
    }

    @Override
    protected String mapperResource() {
        return "/mapper/elastic/KeyGenerationMatrix.xml";
    }

    @Override
    protected boolean numericGeneratedKeys() {
        return false;
    }

    @Override
    protected void assertGeneratedKey(Object id) {
        assertTrue(id instanceof String);
        assertFalse(((String) id).isEmpty());
    }

    @Override
    protected void assertKeyProgression(Object previous, Object current) {
        if (previous != null) {
            assertNotEquals(previous, current);
        }
    }

    @Override
    protected String readKeyName(Object id) throws Exception {
        return session.<String>queryStatement("xmltest.KeyGenerationMapper.selectById", Collections.singletonMap("id", id)).get(0);
    }

    @After
    public void closeKeysFixture() throws Exception {
        fixture.close();
    }
}
