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
import net.hasor.dbvisitor.test.contract.api.mapper.xml.XmlMapperSqlFragmentCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Elastic7Profile;
import net.hasor.dbvisitor.test.realdb.elastic7.material.ElasticMatrixFixture;
import org.junit.After;
import org.junit.Before;

public class Elastic7XmlMapperSqlFragmentTest extends XmlMapperSqlFragmentCase {
    private final ElasticMatrixFixture fixture = new ElasticMatrixFixture();

    @Override
    protected DataSourceProfile profile() {
        return Elastic7Profile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = fixture.open(profile().env());
        for (int i = 1; i <= 5; i++) {
            jdbcTemplate.executeUpdate("PUT /" + fixture.index() + "/_doc/" + (baseId() + i) + " {\"id\": ?,\"name\": ?,\"age\": ?,\"email\": ?,\"create_time\": ?}", new Object[] { baseId() + i, "SqlFrag" + i, 20 + i * 5, "frag" + i + "@nxn.test", new Date(timestamp()) });
        }
    }

    @Override
    protected String mapperResource() {
        return "/mapper/elastic/SqlFragmentMatrix.xml";
    }

    @Override
    @Before
    public void createXmlMapperSession() throws Exception {
        this.jdbcTemplate = fixture.open(profile().env());
        this.session = fixture.session();
        Configuration configuration = session.getConfiguration();
        configuration.addMacro("esXmlPath", "POST /" + fixture.index());
        configuration.loadMapper(mapperResource());
    }

    @After
    public void closeXmlFixture() throws Exception {
        fixture.close();
    }
}
