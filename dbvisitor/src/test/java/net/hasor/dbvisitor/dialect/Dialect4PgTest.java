/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.dbvisitor.dialect;
import java.util.Arrays;
import java.util.Collections;
import net.hasor.dbvisitor.dialect.provider.PostgreSqlDialect;
import net.hasor.dbvisitor.jdbc.JdbcHelper;
import net.hasor.dbvisitor.lambda.DuplicateKeyStrategy;
import net.hasor.dbvisitor.lambda.GeneratedKeyStrategy;
import org.junit.Test;

/***
 * PgSQL 方言
 * @version 2014-1-13
 * @author 赵永春 (zyc@hasor.net)
 */
public class Dialect4PgTest extends AbstractDialectTest {

    @Override
    protected PostgreSqlDialect findDialect() {
        return (PostgreSqlDialect) SqlDialectRegister.findOrCreate(JdbcHelper.POSTGRESQL);
    }

    @Test
    public void dialect_postgresql_1() {
        PostgreSqlDialect dialect = findDialect();
        String buildTableName1 = dialect.tableName(true, null, "", "tb_user");
        String buildTableName2 = dialect.tableName(true, null, "abc", "tb_user");
        String buildCondition = dialect.fmtName(true, "userUUID");

        assert buildTableName1.equals("\"tb_user\"");
        assert buildTableName2.equals("\"abc\".\"tb_user\"");
        assert buildCondition.equals("\"userUUID\"");

        BoundSql countSql = dialect.countSql(this.queryBoundSql);
        assert countSql.getSqlString().equals("SELECT COUNT(*) FROM (select * from tb_user where age > 12 and sex = ?) as TEMP_T");
        assert countSql.getArgs().length == 1;

        BoundSql pageSql = dialect.pageSql(this.queryBoundSql, 1, 3);
        assert pageSql.getSqlString().equals("select * from tb_user where age > 12 and sex = ? LIMIT ? OFFSET ?");
        assert pageSql.getArgs().length == 3;
        assert pageSql.getArgs()[0].equals('F');
        assert pageSql.getArgs()[1].equals(3L);
        assert pageSql.getArgs()[2].equals(1L);
    }

    @Test
    public void dialect_insert_returning_generated_keys_1() throws Exception {
        PostgreSqlDialect dialect = findDialect();
        SqlCommandBuilder builder = dialect.newBuilder();
        builder.setTable(null, "public", "user_info");
        builder.addInsert("name", null, "?");
        builder.addInsert("age", null, "?");

        BoundSql boundSql = builder.buildInsert(true, Collections.singletonList("id"), 3, Collections.singletonList("id"), DuplicateKeyStrategy.Into, GeneratedKeyStrategy.MultiValuesResultSet);

        assert boundSql.getSqlString().equals("INSERT INTO \"public\".\"user_info\" (\"name\", \"age\") VALUES (?, ?), (?, ?), (?, ?) RETURNING \"id\"");
        assert dialect.generatedKeyStrategy(Collections.singletonList("id"), Arrays.asList("name", "age"), Collections.singletonList("id"), DuplicateKeyStrategy.Into) == GeneratedKeyStrategy.MultiValuesResultSet;
    }

    @Test
    public void dialect_insert_on_conflict_update_1() throws Exception {
        PostgreSqlDialect dialect = findDialect();
        SqlCommandBuilder builder = dialect.newBuilder();
        builder.setTable(null, "public", "user_info");
        builder.addInsert("id", null, "?");
        builder.addInsert("name", null, "?");
        builder.addInsert("age", null, "?");

        BoundSql boundSql = builder.buildInsert(true, Collections.singletonList("id"), 1, Collections.emptyList(), DuplicateKeyStrategy.Update, GeneratedKeyStrategy.OneByOne);

        assert boundSql.getSqlString().equals("INSERT INTO \"public\".\"user_info\" (\"id\", \"name\", \"age\") VALUES (?, ?, ?) ON CONFLICT (\"id\") DO UPDATE SET (\"name\", \"age\") = (EXCLUDED.\"name\", EXCLUDED.\"age\")");
    }

    @Test
    public void dialect_insert_on_conflict_update_returning_1() throws Exception {
        PostgreSqlDialect dialect = findDialect();
        SqlCommandBuilder builder = dialect.newBuilder();
        builder.setTable(null, "public", "user_info");
        builder.addInsert("id", null, "?");
        builder.addInsert("name", null, "?");
        builder.addInsert("age", null, "?");

        BoundSql boundSql = builder.buildInsert(true, Collections.singletonList("id"), 2, Collections.singletonList("id"), DuplicateKeyStrategy.Update, GeneratedKeyStrategy.MultiValuesResultSet);

        assert boundSql.getSqlString().equals("INSERT INTO \"public\".\"user_info\" (\"id\", \"name\", \"age\") VALUES (?, ?, ?), (?, ?, ?) ON CONFLICT (\"id\") DO UPDATE SET (\"name\", \"age\") = (EXCLUDED.\"name\", EXCLUDED.\"age\") RETURNING \"id\"");
        assert dialect.generatedKeyStrategy(Collections.singletonList("id"), Arrays.asList("id", "name", "age"), Collections.singletonList("id"), DuplicateKeyStrategy.Update) == GeneratedKeyStrategy.MultiValuesResultSet;
    }

    @Test
    public void dialect_insert_on_conflict_ignore_multi_values_1() throws Exception {
        PostgreSqlDialect dialect = findDialect();
        SqlCommandBuilder builder = dialect.newBuilder();
        builder.setTable(null, "public", "user_info");
        builder.addInsert("id", null, "?");
        builder.addInsert("name", null, "?");

        BoundSql boundSql = builder.buildInsert(true, Collections.singletonList("id"), 2, Collections.emptyList(), DuplicateKeyStrategy.Ignore, GeneratedKeyStrategy.MultiValuesResultSet);

        assert boundSql.getSqlString().equals("INSERT INTO \"public\".\"user_info\" (\"id\", \"name\") VALUES (?, ?), (?, ?) ON CONFLICT DO NOTHING");
    }
}
