package net.hasor.realdb.mysql;
import net.hasor.dbvisitor.lambda.Insert;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.test.dto.UserInfo2;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

public class MySqlPerformanceTest {
    private void reinit(Connection con) throws SQLException {
        LambdaTemplate wrapper = new LambdaTemplate(con);
        try {
            wrapper.jdbc().execute("drop table if exists user_info");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            wrapper.jdbc().loadSQL("/dbvisitor_coverage/user_info_for_mysql.sql");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initData(Connection con, int count) throws SQLException {
        LambdaTemplate wrapper = new LambdaTemplate(con);
        Insert<UserInfo2> insert = wrapper.insert(UserInfo2.class);
        for (int i = 0; i < count; i++) {
            UserInfo2 tbUser = new UserInfo2();
            //tbUser.setUid("id_" + i);
            tbUser.setName(String.format("默认用户_%s", i));
            tbUser.setLoginName(String.format("acc_%s", i));
            tbUser.setPassword(String.format("pwd_%s", i));
            tbUser.setSeq(i);
            tbUser.setEmail(String.format("autoUser_%s@hasor.net", i));
            tbUser.setCreateTime(new Date());
            insert.applyEntity(tbUser);
            //
            if (i % 500 == 0) {
                insert.executeSumResult();
                System.out.println("write to db. " + i);
            }
        }
        insert.executeSumResult();
    }

    @Test
    public void mysqlInsertQuery_1() throws SQLException {
        long t = System.currentTimeMillis();
        try (Connection con = DsUtils.mysqlConn()) {
            LambdaTemplate wrapper = new LambdaTemplate(con);
            //
            reinit(con);
            initData(con, 2000);
            //
            int tbUsersCount = wrapper.query(UserInfo2.class).queryForCount();
            System.out.println("query for list/map.");
            wrapper.query(UserInfo2.class).queryForMapList();
            assert tbUsersCount == 2000;
            System.out.println("cost: " + (System.currentTimeMillis() - t));
        }
    }

    @Test
    public void mysqlInsertQuery_2() throws SQLException {
        long t = System.currentTimeMillis();
        try (Connection con = DsUtils.mysqlConn()) {
            LambdaTemplate wrapper = new LambdaTemplate(con);
            //
            reinit(con);
            initData(con, 1000);
            //
            int tbUsersCount = wrapper.query(UserInfo2.class).queryForCount();
            System.out.println("query for list/map.");
            wrapper.query(UserInfo2.class).queryForMapList();
            assert tbUsersCount == 1000;
            System.out.println("cost: " + (System.currentTimeMillis() - t));
        }
    }
}
