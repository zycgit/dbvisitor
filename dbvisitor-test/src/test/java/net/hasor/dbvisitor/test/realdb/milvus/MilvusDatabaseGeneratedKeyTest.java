/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.SQLException;
import java.util.Date;
import net.hasor.dbvisitor.test.contract.feature.keygen.NumericKeyModel;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.contract.feature.keygen.DatabaseGeneratedKeyCase;
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeyAutoLongUser;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.After;
import org.junit.Before;

/** AutoID uses a 64-bit key; preserve the common write/backfill assertions with a Long fixture. */
public class MilvusDatabaseGeneratedKeyTest extends DatabaseGeneratedKeyCase {
    private final MilvusCapabilityFixture fixture = new MilvusCapabilityFixture();

    @Override
    protected DataSourceProfile profile() {
        return MilvusProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = this.fixture.open();
        this.lambdaTemplate = new LambdaTemplate(this.jdbcTemplate);
    }

    @Override
    protected void ensureAutoLongTable() throws SQLException {
        this.fixture.userTable("user_keygen_auto_long", "id INT64 PRIMARY KEY AUTO_ID");
    }

    @Override
    protected void ensureAutoTable() throws SQLException {
        ensureAutoLongTable();
    }

    @Override
    protected NumericKeyModel<?> autoKeyModel() {
        return new NumericKeyModel<>(KeyAutoLongUser.class, this::nativeUser, KeyAutoLongUser::getId, KeyAutoLongUser::setId);
    }

    @Override
    protected void allowExplicitAutoId() throws SQLException {
        this.jdbcTemplate.execute("ALTER TABLE user_keygen_auto_long SET PROPERTIES (allow_insert_auto_id=true)");
    }

    @Override
    protected long readAutoKey(long id) throws SQLException {
        return this.jdbcTemplate.queryForObject("SELECT id FROM user_keygen_auto_long WHERE id = ?", new Object[] { id }, Long.class);
    }

    private KeyAutoLongUser nativeUser(String name, int age) {
        KeyAutoLongUser user = new KeyAutoLongUser();
        user.setName(name);
        user.setAge(age);
        user.setCreateTime(new Date());
        return user;
    }

    @After
    public void cleanupFixture() throws SQLException {
        this.fixture.close();
    }
}
