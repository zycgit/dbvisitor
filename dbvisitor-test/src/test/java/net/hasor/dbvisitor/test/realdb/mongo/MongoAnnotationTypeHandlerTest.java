/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mongo;

import java.sql.SQLException;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.contract.feature.type.AnnotationTypeHandlerCase;
import net.hasor.dbvisitor.test.contract.material.model.annotation.JdbcTypeUser;
import net.hasor.dbvisitor.test.contract.material.model.annotation.ReadTypeHandlerUser;
import net.hasor.dbvisitor.test.contract.material.model.annotation.SpecialJavaTypeUser;
import net.hasor.dbvisitor.test.contract.material.model.annotation.TypeHandlerUser;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MongoProfile;
import net.hasor.dbvisitor.test.realdb.mongo.material.MongoEntityFixture;
import org.junit.After;
import org.junit.Before;

public class MongoAnnotationTypeHandlerTest extends AnnotationTypeHandlerCase {
    private final MongoEntityFixture fixture = new MongoEntityFixture();

    @Override
    protected DataSourceProfile profile() {
        return MongoProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        if (this.jdbcTemplate == null) {
            this.jdbcTemplate = this.fixture.open();
            this.fixture.mapEntity(TypeHandlerUser.class);
            this.fixture.mapEntity(ReadTypeHandlerUser.class);
            this.fixture.mapEntity(JdbcTypeUser.class);
            this.fixture.mapEntity(SpecialJavaTypeUser.class);
        }
    }

    @Override
    protected LambdaTemplate mappingLambdaTemplate() throws SQLException {
        setup();
        return this.fixture.lambda();
    }

    @After
    public void closeFixture() throws SQLException {
        this.fixture.close();
    }
}
