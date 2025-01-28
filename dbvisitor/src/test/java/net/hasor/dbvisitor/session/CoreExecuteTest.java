package net.hasor.dbvisitor.session;
import net.hasor.cobble.CollectionUtils;
import net.hasor.dbvisitor.dialect.provider.MySqlDialect;
import net.hasor.dbvisitor.dynamic.RegistryManager;
import net.hasor.dbvisitor.mapper.StatementDef;
import net.hasor.dbvisitor.session.dto.CoreAnnoBasicMapper;
import net.hasor.dbvisitor.session.dto.UserInfo;
import net.hasor.dbvisitor.template.jdbc.core.JdbcTemplate;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class CoreExecuteTest {
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
        RegistryManager registry = new RegistryManager();
        registry.setDialect(new MySqlDialect());
        PreparedStatementExecute exec = new PreparedStatementExecute(registry);

        registry.getMapperRegistry().loadMapper(CoreAnnoBasicMapper.class);
        StatementDef def = registry.getMapperRegistry().findStatement(CoreAnnoBasicMapper.class, "selectList1");

        try (Connection con = DsUtils.mysqlConn()) {
            reinit(con);

            Map<String, Object> ctx = CollectionUtils.asMap("arg0", "user_1");
            Map<String, Object> result = (Map<String, Object>) exec.execute(con, def, ctx, null);

            assert result.size() == 2;
            assert result.get("res1") instanceof List && ((List) result.get("res1")).size() == 1 && ((List) result.get("res1")).get(0) instanceof UserInfo;
            assert result.get("res2") instanceof List && ((List) result.get("res2")).size() == 2 && ((List) result.get("res2")).get(0) instanceof UserInfo;
        }
    }

    @Test
    public void selectList_2() throws Exception {
        RegistryManager registry = new RegistryManager();
        registry.setDialect(new MySqlDialect());
        PreparedStatementExecute exec = new PreparedStatementExecute(registry);

        registry.getMapperRegistry().loadMapper(CoreAnnoBasicMapper.class);
        StatementDef def = registry.getMapperRegistry().findStatement(CoreAnnoBasicMapper.class, "selectList2");

        try (Connection con = DsUtils.mysqlConn()) {
            reinit(con);

            Map<String, Object> ctx = CollectionUtils.asMap("arg0", "user_1");
            List result = (List) exec.execute(con, def, ctx, null);

            assert result.size() == 1 && result.get(0) instanceof UserInfo;
        }
    }

    @Test
    public void insertBean_1() throws Exception {
        RegistryManager registry = new RegistryManager();
        registry.setDialect(new MySqlDialect());
        PreparedStatementExecute exec = new PreparedStatementExecute(registry);

        registry.getMapperRegistry().loadMapper(CoreAnnoBasicMapper.class);
        StatementDef def1 = registry.getMapperRegistry().findStatement(CoreAnnoBasicMapper.class, "insertBean");
        StatementDef def2 = registry.getMapperRegistry().findStatement(CoreAnnoBasicMapper.class, "queryById");

        Map<String, Object> ctx = CollectionUtils.asMap(//
                "arg0", "10",       //
                "arg1", "user_10",  //
                "arg2", "10",       //
                "arg3", "10",       //
                "arg4", "10",       //
                "arg5", "10",       //
                "arg6", new Date()       //
        );

        try (Connection con = DsUtils.mysqlConn()) {
            reinit(con);

            int insertResult = (int) exec.execute(con, def1, ctx, null);
            assert insertResult == 1;

            Map<String, Object> queryResult = (Map<String, Object>) exec.execute(con, def2, ctx, null);
            assert queryResult.get("user_name").equals("user_10");
        }
    }
}
