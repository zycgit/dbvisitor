/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.feature.mapping;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.feature.mapping.AnnotationTypeHandlerContractTest;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class RedisAnnotationTypeHandlerContractTest extends AnnotationTypeHandlerContractTest {
    @Override
    protected DataSourceProfile profile() {
        return RedisProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() {
        OneApiDataSourceManager.assumeCurrentDataSource("redis");
    }

    @Override
    @Before
    public void createLambdaTemplate() {
        // Metadata checks do not need an SQL-generating LambdaTemplate.
    }

    @Override
    @Test
    @Ignore("Redis has no relational INSERT/SELECT field mapping for the Lambda CRUD contract")
    public void columnAnnotation_shouldApplyCustomTypeHandlerOnInsert() throws SQLException {
        super.columnAnnotation_shouldApplyCustomTypeHandlerOnInsert();
    }

    @Override
    @Test
    @Ignore("Redis has no relational INSERT/SELECT field mapping for the Lambda CRUD contract")
    public void columnAnnotation_shouldUseJdbcTypeDuringRoundTrip() throws SQLException {
        super.columnAnnotation_shouldUseJdbcTypeDuringRoundTrip();
    }

    @Override
    @Test
    @Ignore("Redis has no relational INSERT/SELECT field mapping for the Lambda CRUD contract")
    public void columnAnnotation_shouldUseSpecialJavaTypeDuringRoundTrip() throws SQLException {
        super.columnAnnotation_shouldUseSpecialJavaTypeDuringRoundTrip();
    }
}
