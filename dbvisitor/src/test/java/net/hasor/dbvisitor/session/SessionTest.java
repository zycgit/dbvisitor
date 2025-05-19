package net.hasor.dbvisitor.session;
import net.hasor.cobble.CollectionUtils;
import net.hasor.dbvisitor.dialect.PageObject;
import net.hasor.dbvisitor.dialect.PageResult;
import net.hasor.dbvisitor.dialect.provider.MySqlDialect;
import net.hasor.dbvisitor.session.dto.AutoIncrID;
import net.hasor.dbvisitor.session.dto.ProxyStatementMapper;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class SessionTest {
    private Session reinit(Session s) throws SQLException, IOException {
        s.jdbc().execute("drop table if exists user_info");
        s.jdbc().loadSQL("/dbvisitor_coverage/user_info_for_mysql.sql");
        s.jdbc().execute("insert into `devtester`.`user_info` (`user_uuid`, `user_name`, `login_name`, `login_password`, `email`, `seq`, `register_time`) values "//
                + "('1', 'user_1', '1', '1', '1', 1, now()),"   //
                + "('2', 'user_2', '2', '2', '2', 2, now()),"   //
                + "('3', 'user_3', '3', '3', '3', 3, now());");
        s.jdbc().execute("drop table if exists auto_id");
        s.jdbc().loadSQL("/dbvisitor_coverage/auto_id_for_mysql.sql");
        return s;
    }

    @Test
    public void selectList_1() throws Exception {
        Configuration config = new Configuration();
        config.options().setDialect(new MySqlDialect());
        config.loadMapper(ProxyStatementMapper.class);

        try (Connection con = DsUtils.mysqlConn()) {
            Session s = reinit(config.newSession(con));

            Map<String, Object> ctx = CollectionUtils.asMap("arg0", "user_1");
            List<?> result = s.queryStatement(ProxyStatementMapper.class.getName() + ".selectList1", ctx);
            assert result.size() == 1;
        }
    }

    @Test
    public void selectList_2() throws Exception {
        Configuration config = new Configuration();
        config.options().setDialect(new MySqlDialect());
        config.loadMapper(ProxyStatementMapper.class);

        try (Connection con = DsUtils.mysqlConn()) {
            Session s = reinit(config.newSession(con));

            Map<String, Object> ctx = CollectionUtils.asMap("arg0", "'user_1'");
            List<?> result = s.queryStatement(ProxyStatementMapper.class.getName() + ".selectList2", ctx);
            assert result.size() == 1;
        }
    }

    @Test
    public void insertBean_1() throws Exception {
        Configuration config = new Configuration();
        config.options().setDialect(new MySqlDialect());
        config.loadMapper(ProxyStatementMapper.class);

        Map<String, Object> ctx = CollectionUtils.asMap(//
                "arg0", "10",       //
                "arg1", "user_10");

        try (Connection con = DsUtils.mysqlConn()) {
            Session s = reinit(config.newSession(con));

            assert ctx.size() == 2;
            assert ctx.get("id") == null;
            int insertResult = (int) s.executeStatement(ProxyStatementMapper.class.getName() + ".insertBean1", ctx);
            assert insertResult == 1;
            assert ctx.size() == 3;
            assert ctx.get("id").toString().equals("1");
        }
    }

    @Test
    public void insertBean_2() throws Exception {
        Configuration config = new Configuration();
        config.options().setDialect(new MySqlDialect());
        config.loadMapper(ProxyStatementMapper.class);

        Map<String, Object> ctx = CollectionUtils.asMap(//
                "arg0", "10",       //
                "arg1", "user_10");

        try (Connection con = DsUtils.mysqlConn()) {
            Session s = reinit(config.newSession(con));

            assert ctx.size() == 2;
            assert ctx.get("id") == null;
            int insertResult = (int) s.executeStatement(ProxyStatementMapper.class.getName() + ".insertBean2", ctx);
            assert insertResult == 1;
            assert ctx.size() == 3;
            assert ctx.get("id").toString().equals("1");
        }
    }

    @Test
    public void insertBean_3() throws Exception {
        Configuration config = new Configuration();
        config.options().setDialect(new MySqlDialect());
        config.loadMapper(ProxyStatementMapper.class);

        AutoIncrID incrID = new AutoIncrID();
        incrID.setUid("10");
        incrID.setName("user_10");

        try (Connection con = DsUtils.mysqlConn()) {
            Session s = reinit(config.newSession(con));

            assert incrID.getId() == null;
            int insertResult = (int) s.executeStatement(ProxyStatementMapper.class.getName() + ".insertBean2", incrID);
            assert insertResult == 1;
            assert incrID.getId() == 1;
        }
    }

    @Test
    public void page_1() throws Exception {
        Configuration config = new Configuration();
        config.options().setDialect(new MySqlDialect());
        config.loadMapper(ProxyStatementMapper.class);

        try (Connection con = DsUtils.mysqlConn()) {
            Session s = reinit(config.newSession(con));

            PageResult<?> pageResult;
            List<?> pageData;

            // pageQuery 1
            pageResult = s.pageStatement(ProxyStatementMapper.class.getName() + ".selectByPage", null, new PageObject(0, 2, 0));
            pageData = pageResult.getData();
            assert pageData.size() == 2;

            // pageQuery 2
            pageResult.nextPage();
            pageResult = s.pageStatement(ProxyStatementMapper.class.getName() + ".selectByPage", null, pageResult);
            pageData = pageResult.getData();
            assert pageData.size() == 1;
        }
    }
}
