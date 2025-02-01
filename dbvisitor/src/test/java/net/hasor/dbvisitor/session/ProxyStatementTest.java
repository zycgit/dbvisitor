package net.hasor.dbvisitor.session;
import net.hasor.cobble.CollectionUtils;
import net.hasor.cobble.convert.ConverterBean;
import net.hasor.cobble.ref.BeanMap;
import net.hasor.dbvisitor.dialect.provider.MySqlDialect;
import net.hasor.dbvisitor.session.dto.AutoIncrID;
import net.hasor.dbvisitor.session.dto.ProxyStatementMapper;
import net.hasor.dbvisitor.template.jdbc.core.JdbcTemplate;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class ProxyStatementTest {
    private void reinit(Connection con) throws SQLException, IOException {
        JdbcTemplate jdbc = new JdbcTemplate(con);
        jdbc.execute("drop table if exists user_info");
        jdbc.loadSQL("/dbvisitor_coverage/user_info_for_mysql.sql");
        jdbc.execute("insert into `devtester`.`user_info` (`user_uuid`, `user_name`, `login_name`, `login_password`, `email`, `seq`, `register_time`) values "//
                + "('1', 'user_1', '1', '1', '1', 1, now()),"   //
                + "('2', 'user_2', '2', '2', '2', 2, now()),"   //
                + "('3', 'user_3', '3', '3', '3', 3, now());");
        jdbc.execute("drop table if exists auto_id");
        jdbc.loadSQL("/dbvisitor_coverage/auto_id_for_mysql.sql");
    }

    @Test
    public void selectList_1() throws Exception {
        Configuration config = new Configuration();
        config.options().setDefaultDialect(new MySqlDialect());
        config.loadMapper(ProxyStatementMapper.class);
        FacadeStatement proxy = new FacadeStatement(ProxyStatementMapper.class.getName(), "selectList1", config);

        try (Connection con = DsUtils.mysqlConn()) {
            reinit(con);

            Map<String, Object> ctx = CollectionUtils.asMap("arg0", "user_1");
            List<?> result = (List<?>) proxy.execute(con, ctx, null, false);

            assert result.size() == 1;
        }
    }

    @Test
    public void selectList_2() throws Exception {
        Configuration config = new Configuration();
        config.options().setDefaultDialect(new MySqlDialect());
        config.loadMapper(ProxyStatementMapper.class);
        FacadeStatement proxy = new FacadeStatement(ProxyStatementMapper.class.getName(), "selectList2", config);

        try (Connection con = DsUtils.mysqlConn()) {
            reinit(con);

            Map<String, Object> ctx = CollectionUtils.asMap("arg0", "'user_1'");
            List<?> result = (List<?>) proxy.execute(con, ctx, null, false);

            assert result.size() == 1;
        }
    }

    @Test
    public void insertBean_1() throws Exception {
        Configuration config = new Configuration();
        config.options().setDefaultDialect(new MySqlDialect());
        config.loadMapper(ProxyStatementMapper.class);
        FacadeStatement proxy = new FacadeStatement(ProxyStatementMapper.class.getName(), "insertBean1", config);

        Map<String, Object> ctx = CollectionUtils.asMap(//
                "arg0", "10",       //
                "arg1", "user_10");

        try (Connection con = DsUtils.mysqlConn()) {
            reinit(con);

            assert ctx.size() == 2;
            assert ctx.get("id") == null;
            int insertResult = (int) proxy.execute(con, ctx, null, false);
            assert insertResult == 1;
            assert ctx.size() == 3;
            assert ctx.get("id").toString().equals("1");
        }
    }

    @Test
    public void insertBean_2() throws Exception {
        Configuration config = new Configuration();
        config.options().setDefaultDialect(new MySqlDialect());
        config.loadMapper(ProxyStatementMapper.class);
        FacadeStatement proxy = new FacadeStatement(ProxyStatementMapper.class.getName(), "insertBean2", config);

        Map<String, Object> ctx = CollectionUtils.asMap(//
                "arg0", "10",       //
                "arg1", "user_10");

        try (Connection con = DsUtils.mysqlConn()) {
            reinit(con);

            assert ctx.size() == 2;
            assert ctx.get("id") == null;
            int insertResult = (int) proxy.execute(con, ctx, null, false);
            assert insertResult == 1;
            assert ctx.size() == 3;
            assert ctx.get("id").toString().equals("1");
        }
    }

    @Test
    public void insertBean_3() throws Exception {
        Configuration config = new Configuration();
        config.options().setDefaultDialect(new MySqlDialect());
        config.loadMapper(ProxyStatementMapper.class);
        FacadeStatement proxy = new FacadeStatement(ProxyStatementMapper.class.getName(), "insertBean2", config);

        AutoIncrID incrID = new AutoIncrID();
        incrID.setUid("10");
        incrID.setName("user_10");

        try (Connection con = DsUtils.mysqlConn()) {
            reinit(con);
            BeanMap beanMap = new BeanMap(incrID);
            beanMap.setTransformConvert(ConverterBean.getInstance());

            assert incrID.getId() == null;
            int insertResult = (int) proxy.execute(con, beanMap, null, false);
            assert insertResult == 1;
            assert incrID.getId() == 1;
        }
    }
}
