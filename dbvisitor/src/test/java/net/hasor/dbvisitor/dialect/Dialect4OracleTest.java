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

import java.util.List;
import java.util.Map;
import net.hasor.cobble.CollectionUtils;
import net.hasor.dbvisitor.dialect.provider.OracleDialect;
import net.hasor.dbvisitor.jdbc.JdbcHelper;
import org.junit.Test;

/***
 * Oracle 方言
 * @version 2014-1-13
 * @author 赵永春 (zyc@hasor.net)
 */
public class Dialect4OracleTest extends AbstractDialectTest {

    @Override
    protected OracleDialect findDialect() {
        return (OracleDialect) SqlDialectRegister.findOrCreate(JdbcHelper.ORACLE);
    }

    @Test
    public void dialect_oracle_1() {
        OracleDialect dialect = this.findDialect();
        String buildTableName1 = dialect.tableName(true, null, "", "tb_user");
        String buildTableName2 = dialect.tableName(true, null, "abc", "tb_user");
        String buildCondition = dialect.fmtName(true, "userUUID");

        assert buildTableName1.equals("\"tb_user\"");
        assert buildTableName2.equals("\"abc\".\"tb_user\"");
        assert buildCondition.equals("\"userUUID\"");

        BoundSql countSql = dialect.countSql(this.queryBoundSql);
        assert countSql.getSqlString().equals("SELECT COUNT(*) FROM (select * from tb_user where age > 12 and sex = ?) TEMP_T");
        assert countSql.getArgs().length == 1;

        BoundSql pageSql = dialect.pageSql(this.queryBoundSql, 1, 3);
        assert pageSql.getSqlString().equals("SELECT * FROM ( SELECT TMP.*, ROWNUM ROW_ID FROM ( select * from tb_user where age > 12 and sex = ? ) TMP WHERE ROWNUM <= ? ) WHERE ROW_ID > ?");
        assert pageSql.getArgs().length == 3;
        assert pageSql.getArgs()[0].equals('F');
        assert pageSql.getArgs()[1].equals(4L);
        assert pageSql.getArgs()[2].equals(1L);
    }

    //    @Test
    //    public void dialect_oracle_1() {
    //        LambdaInsert<TbUser> lambdaInsert = new LambdaTemplate().lambdaInsert(TbUser.class);
    //        lambdaInsert.applyEntity(mappingBeanForData1());
    //        lambdaInsert.applyMap(mapForData2());
    //
    //        SqlDialect dialect = new OracleDialect();
    //        BoundSql boundSql1 = lambdaInsert.onDuplicateStrategy(DuplicateKeyStrategy.Into).getBoundSql(dialect);
    //        assert boundSql1.getSqlString().equals("INSERT INTO tb_user (userUUID, name, loginName, loginPassword, email, \"index\", registerTime) VALUES (?, ?, ?, ?, ?, ?, ?)");
    //
    //        BoundSql boundSql2 = lambdaInsert.onDuplicateStrategy(DuplicateKeyStrategy.Ignore).getBoundSql(dialect);
    //        assert boundSql2.getSqlString().equals("MERGE INTO tb_user TMP USING (SELECT ? userUUID, ? name, ? loginName, ? loginPassword, ? email, ? \"index\", ? registerTime FROM dual ) SRC ON (TMP.userUUID = SRC.userUUID) "//
    //                + "WHEN NOT MATCHED THEN INSERT (userUUID, name, loginName, loginPassword, email, \"index\", registerTime) VALUES( SRC.userUUID, SRC.name, SRC.loginName, SRC.loginPassword, SRC.email, SRC.\"index\", SRC.registerTime) ");
    //
    //        BoundSql boundSql3 = lambdaInsert.onDuplicateStrategy(DuplicateKeyStrategy.Update).getBoundSql(dialect);
    //        assert boundSql3.getSqlString().equals("MERGE INTO tb_user TMP USING (SELECT ? userUUID, ? name, ? loginName, ? loginPassword, ? email, ? \"index\", ? registerTime FROM dual ) SRC ON (TMP.userUUID = SRC.userUUID) " //
    //                + "WHEN MATCHED THEN UPDATE SET userUUID = SRC.userUUID, name = SRC.name, loginName = SRC.loginName, loginPassword = SRC.loginPassword, email = SRC.email, \"index\" = SRC.\"index\", registerTime = SRC.registerTime " //
    //                + "WHEN NOT MATCHED THEN INSERT (userUUID, name, loginName, loginPassword, email, \"index\", registerTime) VALUES( SRC.userUUID, SRC.name, SRC.loginName, SRC.loginPassword, SRC.email, SRC.\"index\", SRC.registerTime) ");
    //    }

    @Test
    public void dialect_oracle_2() {
        OracleDialect dialect = this.findDialect();

        String table = "examination";
        List<String> keys = CollectionUtils.asList("student", "course");
        List<String> columns = CollectionUtils.asList("student", "course", "score", "passed", "teacher");
        Map<String, String> terms = CollectionUtils.asMap("passed", "to_char(?)");

        String sql = dialect.insertReplace(false, null, null, table, keys, columns, terms);
        assert sql.equals("MERGE INTO examination TMP USING (SELECT ? student, ? course, ? score, to_char(?) passed, ? teacher FROM dual) SRC ON (TMP.student = SRC.student AND TMP.course = SRC.course) WHEN MATCHED THEN UPDATE SET score = SRC.score, passed = SRC.passed, teacher = SRC.teacher WHEN NOT MATCHED THEN INSERT (student, course, score, passed, teacher) VALUES ( SRC.student, SRC.course, SRC.score, SRC.passed, SRC.teacher)");
    }

}