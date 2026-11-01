/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;
import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.api.mapper.xml.XmlMapperPaginationCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class MilvusXmlMapperPaginationTest extends XmlMapperPaginationCase {
    private final MilvusXmlCapabilityFixture fixture = new MilvusXmlCapabilityFixture();

    @Override
    protected DataSourceProfile profile() {
        return MilvusProfile.INSTANCE;
    }

    @Override
    @BeforeEach
    public void setup() throws SQLException {
        this.jdbcTemplate = this.fixture.open(baseId());
    }

    @Override
    @BeforeEach
    public void createXmlMapperSession() throws Exception {
        this.jdbcTemplate = this.fixture.open(baseId());
        this.session = this.fixture.session(newConfiguration());
    }

    @AfterEach
    public void cleanupFixture() throws Exception {
        try {
            this.fixture.close();
        } finally {
            if (this.session != null) {
                this.session.close();
            }
        }
    }
}
