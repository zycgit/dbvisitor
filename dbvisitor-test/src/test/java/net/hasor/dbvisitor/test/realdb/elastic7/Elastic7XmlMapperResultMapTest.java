/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic7;

import java.sql.SQLException;
import java.util.Date;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.api.mapper.xml.XmlMapperResultMapCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Elastic7Profile;
import net.hasor.dbvisitor.test.realdb.elastic7.material.ElasticMatrixFixture;
import org.junit.After;
import org.junit.Before;

public class Elastic7XmlMapperResultMapTest extends XmlMapperResultMapCase {
    private final ElasticMatrixFixture fixture = new ElasticMatrixFixture();

    @Override
    protected DataSourceProfile profile() {
        return Elastic7Profile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = fixture.open(profile().env());
        for (int i = 1; i <= 3; i++) {
            int id = baseId() + i;
            String name = "RmCfg" + i;
            int age = 25 + i;
            jdbcTemplate.executeUpdate("PUT /" + fixture.index() + "/_doc/" + id + " "
                    + "{\"id\": ?,\"name\": ?,\"age\": ?,\"email\": ?,\"create_time\": ?,"
                    + "\"user_id\": ?,\"user_name\": ?,\"user_age\": ?}",
                    new Object[] { id, name, age, "rmcfg" + i + "@nxn.test", new Date(), id, name, age });
        }
    }

    @Override
    protected String mapperResource() {
        return "/mapper/elastic/ResultMapMatrix.xml";
    }

    @Override
    protected Session openSession(Configuration configuration) throws Exception {
        this.jdbcTemplate = fixture.open(profile().env());
        configuration.setMappingRegistry(fixture.registry());
        configuration.addMacro("esXmlPath", "POST /" + fixture.index());
        return configuration.newSession(fixture.connection());
    }

    @After
    public void closeXmlFixture() throws Exception {
        fixture.close();
    }
}
