/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic7;

import java.sql.SQLException;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.contract.feature.mapping.AnnotationTypeHandlerCase;
import net.hasor.dbvisitor.test.contract.material.model.annotation.JdbcTypeUser;
import net.hasor.dbvisitor.test.contract.material.model.annotation.SpecialJavaTypeUser;
import net.hasor.dbvisitor.test.contract.material.model.annotation.TypeHandlerUser;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Elastic7Profile;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.realdb.elastic7.material.ElasticMatrixFixture;
import org.junit.After;
import org.junit.Before;

public class Elastic7AnnotationTypeHandlerTest extends AnnotationTypeHandlerCase {
    private final ElasticMatrixFixture fixture = new ElasticMatrixFixture();

    @Override
    protected DataSourceProfile profile() {
        return Elastic7Profile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        OneApiDataSourceManager.assumeCurrentDataSource(profile().env());
    }

    @Override
    protected LambdaTemplate mappingLambdaTemplate() throws SQLException {
        jdbcTemplate = fixture.open(profile().env());
        fixture.registry().loadEntityAsTable(TypeHandlerUser.class, fixture.index());
        fixture.registry().loadEntityAsTable(JdbcTypeUser.class, fixture.index());
        fixture.registry().loadEntityAsTable(SpecialJavaTypeUser.class, fixture.index());
        return fixture.lambdaTemplate();
    }

    @After
    public void closeFixture() throws SQLException {
        fixture.close();
    }
}
