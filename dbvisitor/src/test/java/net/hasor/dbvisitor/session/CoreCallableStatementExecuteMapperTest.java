package net.hasor.dbvisitor.session;
import net.hasor.cobble.CollectionUtils;
import net.hasor.dbvisitor.dialect.PageObject;
import net.hasor.dbvisitor.dialect.provider.MySqlDialect;
import net.hasor.dbvisitor.mapper.StatementDef;
import net.hasor.dbvisitor.session.dto.CoreCallableStatementExecuteMapper;
import net.hasor.dbvisitor.template.jdbc.core.JdbcTemplate;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class CoreCallableStatementExecuteMapperTest {
    private void reinit(Connection con) throws SQLException {
        JdbcTemplate jdbc = new JdbcTemplate(con);
        jdbc.execute("drop table if exists proc_table_for_stat;");
        jdbc.execute("create table proc_table_for_stat( c_id int primary key, c_name varchar(200));");
        jdbc.execute("insert into proc_table_for_stat (c_id,c_name) values (1, 'aaa');");
        jdbc.execute("insert into proc_table_for_stat (c_id,c_name) values (2, 'bbb');");
        jdbc.execute("insert into proc_table_for_stat (c_id,c_name) values (3, 'ccc');");

        jdbc.execute("drop procedure if exists proc_select_cross_table_for_stat;");
        jdbc.execute(""//
                + "create procedure proc_select_cross_table_for_stat(in p_name varchar(200), out p_out varchar(200))" //
                + " begin " //
                + "   select * from proc_table_for_stat where c_name =  p_name ;" //
                + "   select * from proc_table_for_stat where c_name != p_name ;" //
                + "   set p_out = p_name;"//
                + " end;");
    }

    @Test
    public void executeCall_1() throws Exception {
        Configuration config = new Configuration();
        config.options().setDefaultDialect(new MySqlDialect());
        CallableStatementExecute exec = new CallableStatementExecute(config);

        config.loadMapper(CoreCallableStatementExecuteMapper.class);
        StatementDef def = config.findStatement(CoreCallableStatementExecuteMapper.class, "executeCall1");

        try (Connection con = DsUtils.mysqlConn()) {
            reinit(con);

            Map<String, Object> ctx = CollectionUtils.asMap(//
                    "arg0", "aaa",//
                    "arg1", "this is arg");
            Map<String, Object> result = (Map<String, Object>) exec.execute(con, def, ctx, null, false);

            assert result.size() == 3;
            assert result.get("arg1").equals("aaa");
            assert result.get("res1") instanceof List && ((List) result.get("res1")).size() == 1;
            assert result.get("res2") instanceof List && ((List) result.get("res2")).size() == 2;
        }
    }

    @Test
    public void executeCall_2() throws Exception {
        Configuration config = new Configuration();
        config.options().setDefaultDialect(new MySqlDialect());
        CallableStatementExecute exec = new CallableStatementExecute(config);

        config.loadMapper(CoreCallableStatementExecuteMapper.class);
        StatementDef def = config.findStatement(CoreCallableStatementExecuteMapper.class, "executeCall2");

        try (Connection con = DsUtils.mysqlConn()) {
            reinit(con);

            Map<String, Object> ctx = CollectionUtils.asMap(//
                    "arg0", "aaa",//
                    "arg1", "this is arg");
            Map<String, Object> result = (Map<String, Object>) exec.execute(con, def, ctx, null, false);

            assert result.size() == 4;
            assert result.get("arg1").equals("aaa");
            assert result.get("#result-set-1") instanceof List && ((List) result.get("#result-set-1")).size() == 1;
            assert result.get("#result-set-2") instanceof List && ((List) result.get("#result-set-2")).size() == 2;
            assert result.get("#update-count-3").equals(0);
        }
    }

    @Test
    public void page_1() throws Exception {
        Configuration config = new Configuration();
        config.options().setDefaultDialect(new MySqlDialect());
        CallableStatementExecute exec = new CallableStatementExecute(config);

        config.loadMapper(CoreCallableStatementExecuteMapper.class);
        StatementDef def = config.findStatement(CoreCallableStatementExecuteMapper.class, "selectByPage");

        try (Connection con = DsUtils.mysqlConn()) {
            reinit(con);

            try {
                exec.execute(con, def, null, new PageObject(2, 0), true);
                assert false;
            } catch (Exception e) {
                assert e.getMessage().equals("CALLABLE does not support paging query, please using PREPARED.");
            }
        }
    }
}
