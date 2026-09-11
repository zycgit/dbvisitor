/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.dialect;
import net.hasor.dbvisitor.dialect.provider.HiveDialect;
import net.hasor.dbvisitor.jdbc.JdbcHelper;
import org.junit.Test;

/***
 * Hive 方言
 * @version 2014-1-13
 * @author 赵永春 (zyc@hasor.net)
 */
public class Dialect4HiveTest extends AbstractDialectTest {

    @Override
    protected HiveDialect findDialect() {
        return (HiveDialect) SqlDialectRegister.findOrCreate(JdbcHelper.HIVE);
    }

    @Test
    public void dialect_hive_1() {
        HiveDialect dialect = this.findDialect();
        String buildTableName1 = dialect.tableName(true, null, "", "tb_user");
        String buildTableName2 = dialect.tableName(true, null, "abc", "tb_user");
        String buildCondition = dialect.fmtName(true, "userUUID");

        assert buildTableName1.equals("\"tb_user\"");
        assert buildTableName2.equals("\"abc\".\"tb_user\"");
        assert buildCondition.equals("\"userUUID\"");

        try {
            dialect.countSql(this.queryBoundSql);
            assert false;
        } catch (Exception e) {
            assert true;
        }
        try {
            dialect.pageSql(this.queryBoundSql, 1, 3);
            assert false;
        } catch (Exception e) {
            assert true;
        }
    }
}
