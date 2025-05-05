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
import net.hasor.dbvisitor.dialect.provider.SqlServerDialect;
import net.hasor.dbvisitor.jdbc.JdbcHelper;
import org.junit.Test;

/***
 * PgSQL 方言
 * @version 2014-1-13
 * @author 赵永春 (zyc@hasor.net)
 */
public class Dialect4SqlServerTest extends AbstractDialectTest {

    @Override
    protected SqlServerDialect findDialect() {
        return (SqlServerDialect) SqlDialectRegister.findOrCreate(JdbcHelper.SQL_SERVER);
    }

    @Test
    @Override
    public void dialect_tableName_1() {
        SqlDialect dialect = findDialect();
        String l = dialect.leftQualifier();
        String r = dialect.rightQualifier();

        assert dialect.tableName(false, "a", "", "c").equals("a.dbo.c");
        assert dialect.tableName(false, "a", null, "c").equals("a.dbo.c");
        assert dialect.tableName(false, "", "b", "c").equals("b.c");
        assert dialect.tableName(false, null, "b", "c").equals("b.c");

        assert dialect.tableName(false, null, "sch", "null").equals("sch." + l + "null" + r);
        assert dialect.tableName(false, null, "sch", "1table").equals("sch." + l + "1table" + r);
        assert dialect.tableName(false, null, "sch", "tab;").equals("sch." + l + "tab;" + r);
        assert dialect.tableName(false, null, "sch", "tab;le").equals("sch." + l + "tab;le" + r);
    }

    @Test
    public void dialect_sqlserver2012_1() {
        SqlServerDialect dialect = findDialect();
        String buildTableName1 = dialect.tableName(true, null, "", "tb_user");
        String buildTableName2 = dialect.tableName(true, null, "abc", "tb_user");
        String buildTableName3 = dialect.tableName(true, "aac", "", "tb_user");
        String buildCondition = dialect.fmtName(true, "userUUID");

        assert buildTableName1.equals("[tb_user]");
        assert buildTableName2.equals("[abc].[tb_user]");
        assert buildTableName3.equals("[aac].dbo.[tb_user]");
        assert buildCondition.equals("[userUUID]");

        BoundSql countSql = dialect.countSql(this.queryBoundSql);
        assert countSql.getSqlString().equals("SELECT COUNT(*) FROM (select * from tb_user where age > 12 and sex = ?) as TEMP_T");
        assert countSql.getArgs().length == 1;

        BoundSql pageSql = dialect.pageSql(this.queryBoundSql, 1, 3);
        assert pageSql.getSqlString().equals("WITH selectTemp AS (SELECT TOP 100 PERCENT  ROW_NUMBER() OVER (ORDER BY CURRENT_TIMESTAMP) as __row_number__,  * from tb_user where age > 12 and sex = ?) SELECT * FROM selectTemp WHERE __row_number__ BETWEEN 2 AND 4 ORDER BY __row_number__");
        assert pageSql.getArgs().length == 3;
        assert pageSql.getArgs()[0].equals('F');
        assert pageSql.getArgs()[1].equals(2L);
        assert pageSql.getArgs()[2].equals(4L);

        BoundSql countSql2 = dialect.countSql(this.queryBoundSql2);
        assert countSql2.getSqlString().equals("SELECT COUNT(*) FROM (select * from tb_user where age > 12 and sex = ? order by a desc) as TEMP_T");
        assert countSql2.getArgs().length == 1;
        BoundSql pageSql2 = dialect.pageSql(this.queryBoundSql2, 1, 3);
        assert pageSql2.getSqlString().equals("WITH selectTemp AS (SELECT TOP 100 PERCENT  ROW_NUMBER() OVER (order by a desc) as __row_number__,  * from tb_user where age > 12 and sex = ? order by a desc) SELECT * FROM selectTemp WHERE __row_number__ BETWEEN 2 AND 4 ORDER BY __row_number__");
        assert pageSql2.getArgs().length == 3;
        assert pageSql2.getArgs()[0].equals('F');
        assert pageSql2.getArgs()[1].equals(2L);
        assert pageSql2.getArgs()[2].equals(4L);
    }
}