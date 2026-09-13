/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.mapper;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.api.mapper.annotation.AnnotationMapperGeneratedKeysCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import net.hasor.dbvisitor.test.realdb.redis.api.mapper.RedisNativeMapperSupport.Generated;
import org.junit.After;
import org.junit.Before;

public class RedisAnnotationMapperGeneratedKeysTest extends AnnotationMapperGeneratedKeysCase {
    private final RedisKeyFixture fixture = new RedisKeyFixture();

    @Override
    protected DataSourceProfile profile() {
        return RedisProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        try {
            fixture.open();
        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            throw new SQLException(e);
        }
    }

    @Override
    @Before
    public void createAnnotationMapper() throws Exception {
        fixture.open();
    }

    @Override
    protected Object keyRecord(Object id, String name, int age, String email) {
        return fixture.record(id, name);
    }

    @Override
    protected Object keyValue(Object record) {
        return ((Generated) record).getId();
    }

    @Override
    protected int writeKeyRecord(KeyWrite operation, Object record) throws Exception {
        return fixture.write(operation.name(), (Generated) record);
    }

    @Override
    protected String readKeyName(Object id) throws Exception {
        return fixture.readName(id);
    }

    @After
    public void closeFixture() throws SQLException {
        fixture.close();
    }
}
