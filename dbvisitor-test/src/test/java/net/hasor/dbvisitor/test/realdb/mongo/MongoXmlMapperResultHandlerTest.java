/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mongo;

import java.sql.SQLException;
import java.util.Date;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.test.contract.api.mapper.xml.XmlMapperResultHandlerCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MongoProfile;
import net.hasor.dbvisitor.test.realdb.mongo.material.MongoMapperFixture;
import org.junit.After;
import org.junit.Before;

public class MongoXmlMapperResultHandlerTest extends XmlMapperResultHandlerCase {
    private final MongoMapperFixture fixture = new MongoMapperFixture();

    @Override
    protected DataSourceProfile profile() {
        return MongoProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = this.fixture.open();
        for (int i = 1; i <= 3; i++) {
            this.jdbcTemplate.executeUpdate(this.fixture.source() + ".insert({id: ?, name: ?, age: ?, email: ?, create_time: ?})", new Object[] { baseId() + i, "ResHdl" + i, 20 + i * 5, "hdl" + i + "@nxn.test", new Date(timestamp()) });
        }
    }

    @Override
    protected String mapperResource() {
        return "/realdb/mongo/ResultHandlerMapper.xml";
    }

    @Override
    @Before
    public void createXmlMapperSession() throws Exception {
        Configuration configuration = newConfiguration();
        configuration.loadMapper(mapperResource());
        this.session = this.fixture.session(configuration);
    }

    @After
    public void closeFixture() throws Exception {
        this.fixture.close();
    }
}
