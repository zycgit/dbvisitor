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