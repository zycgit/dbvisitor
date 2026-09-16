/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic7.material;

import java.sql.SQLException;
import java.util.Date;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.test.contract.api.mapper.xml.XmlMapperResultHandlerCase;
import org.junit.After;
import org.junit.Before;

public abstract class ElasticXmlResultHandlerSupport extends XmlMapperResultHandlerCase {
    private final ElasticMatrixFixture fixture = new ElasticMatrixFixture();

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = fixture.open(profile().env());
        for (int i = 1; i <= 3; i++) {
            jdbcTemplate.executeUpdate("PUT /" + fixture.index() + "/_doc/" + (baseId() + i) + " {\"id\": ?,\"name\": ?,\"age\": ?,\"email\": ?,\"create_time\": ?}", new Object[] { baseId() + i, "ResHdl" + i, 20 + i * 5, "hdl" + i + "@nxn.test", new Date(timestamp()) });
        }
    }

    @Override
    protected String mapperResource() {
        return "/mapper/elastic/ResultHandlerMatrix.xml";
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
