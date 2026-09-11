/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.dialect;
import net.hasor.dbvisitor.dialect.provider.InformixDialect;
import net.hasor.dbvisitor.jdbc.JdbcHelper;
import org.junit.Test;

/***
 * Informix 方言
 * @version 2014-1-13
 * @author 赵永春 (zyc@hasor.net)
 */
public class Dialect4InformixTest extends AbstractDialectTest {

    @Override
    protected InformixDialect findDialect() {
        return (InformixDialect) SqlDialectRegister.findOrCreate(JdbcHelper.INFORMIX);
    }

    @Test
    public void dialect_informix_1() {
        InformixDialect dialect = this.findDialect();
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
        assert pageSql.getSqlString().equals("SELECT  SKIP ?  FIRST ?  * FROM ( select * from tb_user where age > 12 and sex = ? ) TEMP_T");
        assert pageSql.getArgs().length == 3;
        assert pageSql.getArgs()[0].equals(1L);
        assert pageSql.getArgs()[1].equals(3L);
        assert pageSql.getArgs()[2].equals('F');
    }
}
