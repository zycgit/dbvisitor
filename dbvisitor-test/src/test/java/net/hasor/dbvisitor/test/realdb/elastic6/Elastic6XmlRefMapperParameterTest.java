/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic6;
import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.api.mapper.xml.XmlRefMapperParameterCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Elastic6Profile;
import net.hasor.dbvisitor.test.realdb.elastic7.Elastic7SessionMapperFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class Elastic6XmlRefMapperParameterTest extends XmlRefMapperParameterCase {
    private final Elastic7SessionMapperFixture fixture = new Elastic7SessionMapperFixture();

    @Override
    protected DataSourceProfile profile() {
        return Elastic6Profile.INSTANCE;
    }

    @Override
    @BeforeEach
    public void setup() throws SQLException {
        this.jdbcTemplate = fixture.open(profile().env());
        initData();
    }

    @Override
    protected void insertUser(Object[] values) throws SQLException {
        fixture.insert((Integer) values[0], (String) values[1], (Integer) values[2], (String) values[3]);
    }

    @Override
    @BeforeEach
    public void createRefMapper() throws Exception {
        this.jdbcTemplate = fixture.open(profile().env());
        this.dao = fixture.session().createMapper(Elastic7SessionMapperFixture.NativeRefDao.class);
    }

    @AfterEach
    public void closeRefFixture() throws Exception {
        fixture.close();
    }
}
