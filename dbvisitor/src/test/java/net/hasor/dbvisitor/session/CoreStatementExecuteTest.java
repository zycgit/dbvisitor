package net.hasor.dbvisitor.session;
import net.hasor.cobble.CollectionUtils;
import net.hasor.dbvisitor.dialect.PageObject;
import net.hasor.dbvisitor.dialect.provider.MySqlDialect;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.mapper.StatementDef;
import net.hasor.dbvisitor.session.dto.CoreStatementExecuteMapper;
import net.hasor.dbvisitor.session.dto.UserInfo;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class CoreStatementExecuteTest {
    private void reinit(Connection con) throws SQLException, IOException {
        JdbcTemplate jdbc = new JdbcTemplate(con);
        jdbc.execute("drop table if exists user_info");
        jdbc.loadSQL("/dbvisitor_coverage/user_info_for_mysql.sql");
        jdbc.execute("insert into `devtester`.`user_info` (`user_uuid`, `user_name`, `login_name`, `login_password`, `email`, `seq`, `register_time`) values "//
                + "('1', 'user_1', '1', '1', '1', 1, now()),"   //
                + "('2', 'user_2', '2', '2', '2', 2, now()),"   //
                + "('3', 'user_3', '3', '3', '3', 3, now());");
    }

    @Test
    public void selectList_1() throws Exception {
        Configuration config = new Configuration();
        config.options().setDialect(new MySqlDialect());
        StatementExecute exec = new StatementExecute(config);

        config.loadMapper(CoreStatementExecuteMapper.class);
        StatementDef def = config.findStatement(CoreStatementExecuteMapper.class, "selectList1");

        try (Connection con = DsUtils.mysqlConn()) {
            reinit(con);

            Map<String, Object> ctx = CollectionUtils.asMap("arg0", "'user_1'");
            Map<String, Object> result = (Map<String, Object>) exec.execute(con, def, ctx, null, false);

            assert result.size() == 2;
            assert result.get("res1") instanceof List && ((List) result.get("res1")).size() == 1 && ((List) result.get("res1")).get(0) instanceof UserInfo;
            assert result.get("res2") instanceof List && ((List) result.get("res2")).size() == 2 && ((List) result.get("res2")).get(0) instanceof UserInfo;
        }
    }

    @Test
    public void selectList_2() throws Exception {
        Configuration config = new Configuration();
        config.options().setDialect(new MySqlDialect());
        StatementExecute exec = new StatementExecute(config);

        config.loadMapper(CoreStatementExecuteMapper.class);
        StatementDef def = config.findStatement(CoreStatementExecuteMapper.class, "selectList2");

        try (Connection con = DsUtils.mysqlConn()) {
            reinit(con);

            Map<String, Object> ctx = CollectionUtils.asMap("arg0", "'user_1'");
            List result = (List) exec.execute(con, def, ctx, null, false);

            assert result.size() == 1 && result.get(0) instanceof UserInfo;
        }
    }

    @Test
    public void insertBean_1() throws Exception {
        Configuration config = new Configuration();
        config.options().setDialect(new MySqlDialect());
        StatementExecute exec = new StatementExecute(config);

        config.loadMapper(CoreStatementExecuteMapper.class);
        StatementDef def1 = config.findStatement(CoreStatementExecuteMapper.class, "insertBean");
        StatementDef def2 = config.findStatement(CoreStatementExecuteMapper.class, "queryById");

        Map<String, Object> ctx = CollectionUtils.asMap(//
                "arg0", "'10'",             //
                "arg1", "'user_10'",        //
                "arg2", "'10'",             //
                "arg3", "'10'",             //
                "arg4", "'10'",             //
                "arg5", "'10'",             //
                "arg6", "'2025-01-11 12:12:12'"  //
        );

        try (Connection con = DsUtils.mysqlConn()) {
            reinit(con);

            int insertResult = (int) exec.execute(con, def1, ctx, null, false);
            assert insertResult == 1;

            Map<String, Object> queryResult = (Map<String, Object>) exec.execute(con, def2, ctx, null, false);
            assert queryResult.get("user_name").equals("user_10");
        }
    }

    @Test
    public void updateBean_1() throws Exception {
        Configuration config = new Configuration();
        config.options().setDialect(new MySqlDialect());
        StatementExecute exec = new StatementExecute(config);

        config.loadMapper(CoreStatementExecuteMapper.class);
        StatementDef def1 = config.findStatement(CoreStatementExecuteMapper.class, "updateBean");
        StatementDef def2 = config.findStatement(CoreStatementExecuteMapper.class, "queryById");

        try (Connection con = DsUtils.mysqlConn()) {
            reinit(con);

            Map<String, Object> ctx1 = CollectionUtils.asMap("arg0", "'upd_1000'");
            int insertResult = (int) exec.execute(con, def1, ctx1, null, false);
            assert insertResult == 3;

            Map<String, Object> ctx2 = CollectionUtils.asMap("arg0", "'1'");
            Map<String, Object> queryResult = (Map<String, Object>) exec.execute(con, def2, ctx2, null, false);
            assert queryResult.get("login_name").equals("upd_1000");
            assert queryResult.get("login_password").equals("1");
        }
    }

    @Test
    public void deleteBean_1() throws Exception {
        Configuration config = new Configuration();
        config.options().setDialect(new MySqlDialect());
        StatementExecute exec = new StatementExecute(config);

        config.loadMapper(CoreStatementExecuteMapper.class);
        StatementDef def1 = config.findStatement(CoreStatementExecuteMapper.class, "selectCount");
        StatementDef def2 = config.findStatement(CoreStatementExecuteMapper.class, "deleteBean");

        try (Connection con = DsUtils.mysqlConn()) {
            reinit(con);

            int cnt1 = (int) exec.execute(con, def1, null, null, false);
            assert cnt1 == 3;

            Map<String, Object> ctx2 = CollectionUtils.asMap("arg0", "'1'");
            int deleteResult = (int) exec.execute(con, def2, ctx2, null, false);
            assert deleteResult == 1;

            int cnt2 = (int) exec.execute(con, def1, null, null, false);
            assert cnt2 == 2;
        }
    }

    @Test
    public void page_1() throws Exception {
        Configuration config = new Configuration();
        config.options().setDialect(new MySqlDialect());
        StatementExecute exec = new StatementExecute(config);

        config.loadMapper(CoreStatementExecuteMapper.class);
        StatementDef def = config.findStatement(CoreStatementExecuteMapper.class, "selectByPage");

        try (Connection con = DsUtils.mysqlConn()) {
            reinit(con);

            try {
                exec.execute(con, def, null, new PageObject(2, 0), true);
                assert false;
            } catch (Exception e) {
                assert e.getMessage().equals("STATEMENT does not support paging query, please using PREPARED.");
            }
        }
    }
}
