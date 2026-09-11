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
import java.util.List;
import java.util.Locale;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.EntityQuery;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapping.Table;
import net.hasor.dbvisitor.test.contract.api.lambda.LambdaIteratorContractTest;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.After;
import org.junit.Before;

/** Same entity-iterator assertions, with an isolated collection and native vector ordering. */
public class MilvusLambdaIteratorContractTest extends LambdaIteratorContractTest {
    private final MilvusDatabaseFixture database = new MilvusDatabaseFixture();

    @Override
    protected DataSourceProfile profile() {
        return MilvusProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = new JdbcTemplate(this.database.open());
        this.jdbcTemplate.execute("""
                CREATE TABLE user_info (
                    id INT64 PRIMARY KEY, name VARCHAR(128), age INT32,
                    email VARCHAR(128), create_time VARCHAR(128), v FLOAT_VECTOR(2)
                ) WITH (consistency_level=Strong)
                """);
        this.jdbcTemplate.execute("CREATE INDEX iterator_v ON user_info(v) USING FLAT WITH (metric_type=L2)");
        this.jdbcTemplate.execute("LOAD TABLE user_info");
        this.lambdaTemplate = new LambdaTemplate(this.jdbcTemplate);
    }

    @Override
    protected EntityQuery<IteratorUser> queryUsers(String namePattern) throws SQLException {
        // Each case owns its collection, replacing the shared-table prefix used to isolate relational fixtures.
        return this.lambdaTemplate.query(IteratorUser.class);
    }

    @Override
    protected EntityQuery<IteratorUser> orderedQuery(String namePattern, Integer minimumAge) throws SQLException {
        EntityQuery<IteratorUser> query = queryUsers(namePattern);
        if (minimumAge != null) {
            query.gt(UserInfo::getAge, minimumAge);
        }
        return query.orderByL2(IteratorUser::getV, new float[] { 0, 0 });
    }

    @Override
    protected void insertUser(int id, String name, Integer age) throws SQLException {
        this.jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time, v) VALUES (?, ?, ?, ?, ?, ?)",
                new Object[] { id, name, age, name.toLowerCase(Locale.ROOT) + "@nxn.test", new Date(), new float[] { id, 0 } });
    }

    @After
    public void cleanupFixture() throws SQLException {
        try {
            if (this.jdbcTemplate != null) {
                this.jdbcTemplate.execute("DROP TABLE IF EXISTS user_info");
            }
        } finally {
            this.database.close();
        }
    }

    @Table("user_info")
    public static class IteratorUser extends UserInfo {
        private List<Float> v;

        public List<Float> getV() {
            return this.v;
        }

        public void setV(List<Float> v) {
            this.v = v;
        }
    }
}
