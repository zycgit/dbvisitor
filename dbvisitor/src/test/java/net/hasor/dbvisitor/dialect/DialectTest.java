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
import net.hasor.test.AbstractDbTest;
import org.junit.Test;

/***
 * 方言
 * @version 2014-1-13
 * @author 赵永春 (zyc@hasor.net)
 */
public class DialectTest extends AbstractDbTest {
    private final BoundSql queryBoundSql = new BoundSql() {
        @Override
        public String getSqlString() {
            return "select * from tb_user where age > 12 and sex = ?";
        }

        @Override
        public Object[] getArgs() {
            return new Object[] { 'F' };
        }
    };

    @Test
    public void dialect_default_1() {
        DefaultSqlDialect dialect = (DefaultSqlDialect) SqlDialectRegister.findOrCreate("");
        String buildTableName1 = dialect.tableName(true, null, "", "tb_user");
        String buildTableName2 = dialect.tableName(true, null, "abc", "tb_user");
        String buildCondition = dialect.fmtName(true, "userUUID");

        assert buildTableName1.equals("tb_user");
        assert buildTableName2.equals("abc.tb_user");
        assert buildCondition.equals("userUUID");

        BoundSql boundSql = dialect.countSql(this.queryBoundSql);
        assert boundSql.getSqlString().equals("SELECT COUNT(*) FROM (select * from tb_user where age > 12 and sex = ?) as TEMP_T");

        try {
            dialect.pageSql(this.queryBoundSql, 1, 3);
            assert false;
        } catch (Exception e) {
            assert true;
        }
    }

}
