package net.hasor.realdb.oracle;
import net.hasor.dbvisitor.lambda.InsertOperation;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.test.dto.UserInfo2;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

public class OraclePerformanceTest {
    private void reinit(Connection con) {
        LambdaTemplate lambdaTemplate = new LambdaTemplate(con);
        try {
            lambdaTemplate.execute("drop table user_info");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            lambdaTemplate.loadSQL("/dbvisitor_coverage/user_info_for_oracle.sql");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initData(Connection con, int count) throws SQLException {
        con.setAutoCommit(false);
        LambdaTemplate lambdaTemplate = new LambdaTemplate(con);
        InsertOperation<UserInfo2> lambdaInsert = lambdaTemplate.lambdaInsert(UserInfo2.class);
        for (int i = 0; i < count; i++) {
            UserInfo2 tbUser = new UserInfo2();
            tbUser.setUid("id_" + i);
            tbUser.setName(String.format("默认用户_%s", i));
            tbUser.setLoginName(String.format("acc_%s", i));
            tbUser.setPassword(String.format("pwd_%s", i));
            tbUser.setSeq(i);
            tbUser.setEmail(String.format("autoUser_%s@hasor.net", i));
            tbUser.setCreateTime(new Date());
            lambdaInsert.applyEntity(tbUser);
            //
            if (i % 500 == 0) {
                lambdaInsert.executeSumResult();
                System.out.println("write to db. " + i);
            }
        }
        lambdaInsert.executeSumResult();
    }

    @Test
    public void oracleInsertQuery_1() throws SQLException {
        long t = System.currentTimeMillis();
        try (Connection con = DsUtils.oracleConn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(con);

            reinit(con);
            initData(con, 2000);

            int tbUsersCount = lambdaTemplate.lambdaQuery(UserInfo2.class).queryForCount();
            System.out.println("query for list/map.");
            lambdaTemplate.lambdaQuery(UserInfo2.class).queryForMapList();
            assert tbUsersCount == 2000;
            System.out.println("cost: " + (System.currentTimeMillis() - t));
        }
    }

    @Test
    public void oracleInsertQuery_2() throws SQLException {
        long t = System.currentTimeMillis();
        try (Connection con = DsUtils.oracleConn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(con);
            //
            reinit(con);
            initData(con, 1000);
            //
            int tbUsersCount = lambdaTemplate.lambdaQuery(UserInfo2.class).queryForCount();
            System.out.println("query for list/map.");
            lambdaTemplate.lambdaQuery(UserInfo2.class).queryForMapList();
            assert tbUsersCount == 1000;
            System.out.println("cost: " + (System.currentTimeMillis() - t));
        }
    }
}
