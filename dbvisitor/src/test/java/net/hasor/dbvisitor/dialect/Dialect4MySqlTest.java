/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.dialect;
import java.util.Arrays;
import java.util.Collections;
import net.hasor.dbvisitor.dialect.provider.MySqlDialect;
import net.hasor.dbvisitor.jdbc.JdbcHelper;
import net.hasor.dbvisitor.lambda.DuplicateKeyStrategy;
import net.hasor.dbvisitor.lambda.GeneratedKeyStrategy;
import org.junit.Test;

/***
 * MySQL 方言
 * @version 2014-1-13
 * @author 赵永春 (zyc@hasor.net)
 */
public class Dialect4MySqlTest extends AbstractDialectTest {

    @Override
    protected MySqlDialect findDialect() {
        return (MySqlDialect) SqlDialectRegister.findOrCreate(JdbcHelper.MYSQL);
    }

    @Test
    public void dialect_mysql_1() {
        MySqlDialect dialect = findDialect();
        String buildTableName1 = dialect.tableName(true, null, "", "tb_user");
        String buildTableName2 = dialect.tableName(true, null, "abc", "tb_user");
        String buildCondition = dialect.fmtName(true, "userUUID");

        assert buildTableName1.equals("`tb_user`");
        assert buildTableName2.equals("`abc`.`tb_user`");
        assert buildCondition.equals("`userUUID`");

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
    }

    @Test
    public void dialect_mysql_insert_strategy() {
        MySqlDialect dialect = findDialect();

        assert dialect.generatedKeyStrategy(Collections.singletonList("id"), Arrays.asList("name", "age"), Collections.emptyList(), DuplicateKeyStrategy.Into) == GeneratedKeyStrategy.JdbcBatch;
        assert dialect.generatedKeyStrategy(Collections.singletonList("id"), Arrays.asList("name", "age"), Collections.singletonList("id"), DuplicateKeyStrategy.Into) == GeneratedKeyStrategy.JdbcBatchGeneratedKeys;
        assert dialect.generatedKeyStrategy(Collections.singletonList("id"), Arrays.asList("name", "age"), Collections.singletonList("id"), DuplicateKeyStrategy.Ignore) == GeneratedKeyStrategy.OneByOne;
        assert dialect.generatedKeyStrategy(Collections.singletonList("id"), Arrays.asList("name", "age"), Collections.singletonList("id"), DuplicateKeyStrategy.Update) == GeneratedKeyStrategy.OneByOne;
    }

    //    @Test
    //    public void dialect_mysql_1() {
    //        Insert<UserInfo2> lambdaInsert = new LambdaTemplate().lambdaInsert(UserInfo2.class);
    //        lambdaInsert.applyEntity(beanForData1());
    //        lambdaInsert.applyMap(mapForData2());
    //
    //        SqlDialect dialect = new MySqlDialect();
    //        BoundSql boundSql = lambdaInsert.getBoundSql(dialect);
    //
    //        assert boundSql instanceof BatchBoundSql;
    //        assert boundSql.getSqlString().equals("INSERT INTO user_info (userUUID, name, loginName, loginPassword, email, `index`, registerTime) VALUES (?, ?, ?, ?, ?, ?, ?)");
    //        assert boundSql.getArgs().length == 2;
    //        assert ((BatchBoundSql) boundSql).getArgs()[0].length == 7;
    //        assert ((BatchBoundSql) boundSql).getArgs()[1].length == 7;
    //
    //        assert ((BatchBoundSql) boundSql).getArgs()[0][0].equals(beanForData1().getUserUUID());
    //        assert ((BatchBoundSql) boundSql).getArgs()[0][1].equals(beanForData1().getName());
    //        assert ((BatchBoundSql) boundSql).getArgs()[0][2].equals(beanForData1().getLoginName());
    //        assert ((BatchBoundSql) boundSql).getArgs()[0][3].equals(beanForData1().getLoginPassword());
    //        assert ((BatchBoundSql) boundSql).getArgs()[0][4].equals(beanForData1().getEmail());
    //        assert ((BatchBoundSql) boundSql).getArgs()[0][5].equals(beanForData1().getIndex());
    //        assert ((BatchBoundSql) boundSql).getArgs()[0][6].equals(beanForData1().getRegisterTime());
    //
    //        assert ((BatchBoundSql) boundSql).getArgs()[1][0].equals(beanForData2().getUserUUID());
    //        assert ((BatchBoundSql) boundSql).getArgs()[1][1].equals(beanForData2().getName());
    //        assert ((BatchBoundSql) boundSql).getArgs()[1][2].equals(beanForData2().getLoginName());
    //        assert ((BatchBoundSql) boundSql).getArgs()[1][3].equals(beanForData2().getLoginPassword());
    //        assert ((BatchBoundSql) boundSql).getArgs()[1][4].equals(beanForData2().getEmail());
    //        assert ((BatchBoundSql) boundSql).getArgs()[1][5].equals(beanForData2().getIndex());
    //        assert ((BatchBoundSql) boundSql).getArgs()[1][6].equals(beanForData2().getRegisterTime());
    //    }
    //
    //    @Test
    //    public void dialect_mysql_2() {
    //        LambdaInsert<TB_User> lambdaInsert = new LambdaTemplate().lambdaInsert(TB_User.class);
    //        lambdaInsert.applyEntity(beanForData1());
    //
    //        SqlDialect dialect = new MySqlDialect();
    //        BoundSql boundSql1 = lambdaInsert.onDuplicateStrategy(DuplicateKeyStrategy.Into).getBoundSql(dialect);
    //        assert boundSql1.getSqlString().equals("INSERT INTO TB_User (userUUID, name, loginName, loginPassword, email, `index`, registerTime) VALUES (?, ?, ?, ?, ?, ?, ?)");
    //
    //        BoundSql boundSql2 = lambdaInsert.onDuplicateStrategy(DuplicateKeyStrategy.Ignore).getBoundSql(dialect);
    //        assert boundSql2.getSqlString().equals("INSERT IGNORE TB_User (userUUID, name, loginName, loginPassword, email, `index`, registerTime) VALUES (?, ?, ?, ?, ?, ?, ?)");
    //
    //        BoundSql boundSql3 = lambdaInsert.onDuplicateStrategy(DuplicateKeyStrategy.Update).getBoundSql(dialect);
    //        assert boundSql3.getSqlString().equals("INSERT INTO TB_User (userUUID, name, loginName, loginPassword, email, `index`, registerTime) VALUES (?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE");
    //    }

}
