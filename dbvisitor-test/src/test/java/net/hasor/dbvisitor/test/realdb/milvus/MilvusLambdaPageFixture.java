/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.EntityQuery;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.realdb.milvus.MilvusLambdaResultFixture.ResultUser;

/** Deterministic distance order for the shared pagination assertions. */
final class MilvusLambdaPageFixture implements AutoCloseable {
    private final MilvusLambdaResultFixture fixture = new MilvusLambdaResultFixture();
    private       Connection                connection;

    Connection open() throws SQLException {
        this.connection = this.fixture.open();
        return this.connection;
    }

    void insert(int id, String name, Integer age) throws SQLException {
        new JdbcTemplate(this.connection).executeUpdate("INSERT INTO user_info (id, name, age, email, create_time, v) VALUES (?, ?, ?, ?, ?, ?)", new Object[] { id, name, age, name + "@nxn.test", new Date(), new float[] { (id - 720000) / 1000F, 0 } });
    }

    EntityQuery<? extends UserInfo> query(LambdaTemplate lambda) throws SQLException {
        return lambda.query(ResultUser.class);
    }

    EntityQuery<? extends UserInfo> query(LambdaTemplate lambda, String prefix) throws SQLException {
        if (!prefix.matches("[A-Za-z0-9-]+")) {
            throw new IllegalArgumentException("Expected a fixed pagination fixture prefix");
        }
        return query(lambda).apply("name LIKE '" + prefix + "%'");
    }

    @SuppressWarnings("unchecked")
    EntityQuery<? extends UserInfo> order(EntityQuery<? extends UserInfo> query) {
        return ((EntityQuery<ResultUser>) query).orderByL2(ResultUser::getV, new float[] { 0, 0 });
    }

    @Override
    public void close() throws SQLException {
        this.fixture.close();
    }
}
