/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mongo;

import java.sql.SQLException;
import net.hasor.dbvisitor.mapper.Query;
import net.hasor.dbvisitor.mapper.SimpleMapper;
import net.hasor.dbvisitor.test.contract.api.mapper.annotation.AnnotationMapperCommandErrorCase;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MongoProfile;
import net.hasor.dbvisitor.test.realdb.mongo.material.MongoMapperFixture;
import org.junit.After;
import org.junit.Before;
import static org.junit.Assert.assertEquals;

public class MongoAnnotationMapperCommandErrorTest extends AnnotationMapperCommandErrorCase {
    private final MongoMapperFixture fixture = new MongoMapperFixture();
    private       BrokenMapper       brokenMapper;

    @SimpleMapper
    public interface BrokenMapper {
        @Query("@{macro, mongoSource}.find({id: })")
        UserInfo malformed();

        @Query("@{macro, mongoSource}.aggregate([{$invalidStage: {}}])")
        UserInfo invalidPipeline();

        @Query("@{macro, mongoSource}.find({}, {name: 1, age: 0})")
        UserInfo invalidProjection();

        @Query("@{macro, mongoSource}.count({})")
        int count();
    }

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
    public void createAnnotationMapper() throws Exception {
        this.brokenMapper = this.fixture.session(newConfiguration()).createMapper(BrokenMapper.class);
    }

    @Override
    protected void prepareInvalidCommands() throws Exception {
        this.jdbcTemplate.executeUpdate(this.fixture.source() + ".insert({name: 'still-usable', age: 1})");
    }

    @Override
    protected void executeInvalidCommand(int scenario) {
        switch (scenario) {
            case 0 -> this.brokenMapper.malformed();
            case 1 -> this.brokenMapper.invalidPipeline();
            case 2 -> this.brokenMapper.invalidProjection();
            default -> throw new IllegalArgumentException("Unknown failure scenario: " + scenario);
        }
    }

    @Override
    protected void verifyAfterCommandFailures() {
        assertEquals(1, this.brokenMapper.count());
    }

    @After
    public void closeFixture() throws Exception {
        this.fixture.close();
    }
}
