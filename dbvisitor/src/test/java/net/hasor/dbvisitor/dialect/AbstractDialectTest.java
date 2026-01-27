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
public abstract class AbstractDialectTest extends AbstractDbTest {
    protected final BoundSql queryBoundSql  = new BoundSql() {
        @Override
        public String getSqlString() {
            return "select * from tb_user where age > 12 and sex = ?";
        }

        @Override
        public Object[] getArgs() {
            return new Object[] { 'F' };
        }
    };
    protected final BoundSql queryBoundSql2 = new BoundSql() {
        @Override
        public String getSqlString() {
            return "select * from tb_user where age > 12 and sex = ? order by a desc";
        }

        @Override
        public Object[] getArgs() {
            return new Object[] { 'F' };
        }
    };

    protected abstract SqlDialect findDialect();

    @Test
    public void dialect_tableName_1() {
        SqlDialect dialect = findDialect();
        String l = dialect.leftQualifier();
        String r = dialect.rightQualifier();

        assert dialect.tableName(false, "a", "", "c").equals("a.c");
        assert dialect.tableName(false, "a", null, "c").equals("a.c");
        assert dialect.tableName(false, "", "b", "c").equals("b.c");
        assert dialect.tableName(false, null, "b", "c").equals("b.c");

        assert dialect.tableName(false, null, "sch", "null").equals("sch." + l + "null" + r);
        assert dialect.tableName(false, null, "sch", "1table").equals("sch." + l + "1table" + r);
        assert dialect.tableName(false, null, "sch", "tab;").equals("sch." + l + "tab;" + r);
        assert dialect.tableName(false, null, "sch", "tab;le").equals("sch." + l + "tab;le" + r);
    }
}
