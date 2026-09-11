/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.dialect;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.hasor.cobble.CollectionUtils;
import net.hasor.dbvisitor.dialect.provider.DmDialect;
import net.hasor.dbvisitor.jdbc.JdbcHelper;
import net.hasor.dbvisitor.lambda.DuplicateKeyStrategy;
import net.hasor.dbvisitor.lambda.GeneratedKeyStrategy;
import org.junit.Test;

/***
 * 达梦方言
 * @version 2014-1-13
 * @author 赵永春 (zyc@hasor.net)
 */
public class Dialect4dmTest extends AbstractDialectTest {

    @Override
    protected DmDialect findDialect() {
        return (DmDialect) SqlDialectRegister.findOrCreate(JdbcHelper.DM);
    }

    @Test
    public void dialect_dm_1() {
        DmDialect dialect = findDialect();
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
        assert pageSql.getSqlString().equals("select * from tb_user where age > 12 and sex = ? LIMIT ?, ?");
        assert pageSql.getArgs().length == 3;
        assert pageSql.getArgs()[0].equals('F');
        assert pageSql.getArgs()[1].equals(1L);
        assert pageSql.getArgs()[2].equals(3L);

        BoundSql pageSql2 = dialect.pageSql(this.queryBoundSql, 0, 3);
        assert pageSql2.getSqlString().equals("select * from tb_user where age > 12 and sex = ? LIMIT ?");
        assert pageSql2.getArgs().length == 2;
        assert pageSql2.getArgs()[0].equals('F');
        assert pageSql2.getArgs()[1].equals(3L);

        assert dialect.selectSeq(false, null, null, "user_seq").equals("SELECT user_seq.NEXTVAL");
        assert dialect.selectSeq(true, null, "app", "user_seq").equals("SELECT \"app\".\"user_seq\".NEXTVAL");
    }

    @Test
    public void dialect_dm_insert_strategy() {
        DmDialect dialect = findDialect();

        assert dialect.supportDuplicateStrategy(Collections.singletonList("id"), CollectionUtils.asList("id", "name"), Collections.emptyList(), DuplicateKeyStrategy.Update);
        assert !dialect.supportDuplicateStrategy(Collections.singletonList("id"), Collections.singletonList("id"), Collections.emptyList(), DuplicateKeyStrategy.Update);

        String table = "examination";
        List<String> keys = CollectionUtils.asList("student", "course");
        List<String> columns = CollectionUtils.asList("student", "course", "score", "passed", "teacher");
        Map<String, String> terms = CollectionUtils.asMap("passed", "to_char(?)");

        String sql = dialect.insertSql(DuplicateKeyStrategy.Update, GeneratedKeyStrategy.OneByOne, false, null, null, table, keys, columns, Collections.emptyList(), 1, terms);
        assert sql.equals("MERGE INTO examination TMP USING (SELECT ? student, ? course, ? score, to_char(?) passed, ? teacher FROM dual) SRC ON (TMP.student = SRC.student AND TMP.course = SRC.course) WHEN MATCHED THEN UPDATE SET score = SRC.score, passed = SRC.passed, teacher = SRC.teacher WHEN NOT MATCHED THEN INSERT (student, course, score, passed, teacher) VALUES ( SRC.student, SRC.course, SRC.score, SRC.passed, SRC.teacher)");
    }
}
