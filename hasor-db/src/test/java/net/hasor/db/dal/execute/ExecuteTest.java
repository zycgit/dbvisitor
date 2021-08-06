package net.hasor.db.dal.execute;
import net.hasor.db.dal.dynamic.rule.RuleRegistry;
import net.hasor.db.dal.repository.MapperRegistry;
import net.hasor.db.jdbc.core.JdbcTemplate;
import net.hasor.db.mapping.resolve.MappingOptions;
import net.hasor.db.metadata.AbstractMetadataServiceSupplierTest;
import net.hasor.db.metadata.provider.JdbcMetadataProvider;
import net.hasor.test.db.dto.TbUser2;
import net.hasor.test.db.utils.DsUtils;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;

public class ExecuteTest extends AbstractMetadataServiceSupplierTest<JdbcMetadataProvider> {
    @Override
    protected Connection initConnection() throws SQLException {
        return DsUtils.localMySQL();
    }

    @Override
    protected JdbcMetadataProvider initRepository(Connection con) {
        return new JdbcMetadataProvider(con);
    }

    @Override
    protected void beforeTest(JdbcTemplate jdbcTemplate, JdbcMetadataProvider repository) throws SQLException, IOException {
        applySql("drop table tb_user");
        applySql("drop table proc_table_ref");
        applySql("drop table proc_table");
        applySql("drop table t3");
        applySql("drop table t1");
        applySql("drop table test_user");
        //
        jdbcTemplate.loadSplitSQL(";", StandardCharsets.UTF_8, "/net_hasor_db/metadata/mysql_script.sql");
        jdbcTemplate.loadSplitSQL(";", StandardCharsets.UTF_8, "/net_hasor_db/dal_dynamic/execute/execute_for_mysql.sql");
        //
        jdbcTemplate.execute("drop procedure if exists proc_select_user;");
        jdbcTemplate.execute("create procedure proc_select_user(out p_out double) begin set p_out=123.123; select * from test_user; end;");
    }

    @Test
    public void listUserList_1() throws IOException, SQLException {
        MapperRegistry.DEFAULT.loadMapper("/net_hasor_db/dal_dynamic/execute/execute.xml", MappingOptions.buildOverwrite());
        MapperDalExecute dalExecute = new MapperDalExecute("net.hasor.test.db.dal.execute.TestExecuteDal", MapperRegistry.DEFAULT, RuleRegistry.DEFAULT);
        //
        Object execute1 = dalExecute.execute(connection, "initUser", new HashMap<>());
        Object execute2 = dalExecute.execute(connection, "listUserList_1", new HashMap<>());
        //
        assert execute1.equals(2);
        assert execute2 != null && execute2 instanceof List;
        assert ((List<?>) execute2).size() == 2;
        //
        TbUser2 tbUser1 = (TbUser2) ((List<?>) execute2).get(0);
        assert tbUser1.getUid().equals("11");
        assert tbUser1.getName().equals("12");
        assert tbUser1.getAccount().equals("13");
        assert tbUser1.getPassword().equals("14");
        assert tbUser1.getMail().equals("15");
        assert tbUser1.getIndex() == 16;
        assert new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(tbUser1.getCreateTime()).equals("2021-07-20 00:00:00");
        TbUser2 tbUser2 = (TbUser2) ((List<?>) execute2).get(1);
        assert tbUser2.getUid().equals("21");
        assert tbUser2.getName().equals("22");
        assert tbUser2.getAccount().equals("23");
        assert tbUser2.getPassword().equals("24");
        assert tbUser2.getMail().equals("25");
        assert tbUser2.getIndex() == 26;
        assert new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(tbUser2.getCreateTime()).equals("2021-07-20 00:00:00");
    }

    @Test
    public void listUserList_2() throws IOException, SQLException {
        MapperRegistry.DEFAULT.loadMapper("/net_hasor_db/dal_dynamic/execute/execute.xml", MappingOptions.buildOverwrite());
        MapperDalExecute dalExecute = new MapperDalExecute("net.hasor.test.db.dal.execute.TestExecuteDal", MapperRegistry.DEFAULT, RuleRegistry.DEFAULT);
        //
        Object execute1 = dalExecute.execute(connection, "initUser", new HashMap<>());
        Object execute2 = dalExecute.execute(connection, "listUserList_2", new HashMap<>());
        //
        assert execute1.equals(2);
        assert execute2 != null && execute2 instanceof List;
        assert ((List<?>) execute2).size() == 2;
        //
        TbUser2 tbUser1 = (TbUser2) ((List<?>) execute2).get(0);
        assert tbUser1.getUid().equals("11");
        assert tbUser1.getName().equals("12");
        assert tbUser1.getAccount().equals("13");
        assert tbUser1.getPassword().equals("14");
        assert tbUser1.getMail().equals("15");
        assert tbUser1.getIndex() == 16;
        assert new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(tbUser1.getCreateTime()).equals("2021-07-20 00:00:00");
        TbUser2 tbUser2 = (TbUser2) ((List<?>) execute2).get(1);
        assert tbUser2.getUid().equals("21");
        assert tbUser2.getName().equals("22");
        assert tbUser2.getAccount().equals("23");
        assert tbUser2.getPassword().equals("24");
        assert tbUser2.getMail().equals("25");
        assert tbUser2.getIndex() == 26;
        assert new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(tbUser2.getCreateTime()).equals("2021-07-20 00:00:00");
    }

    @Test
    public void procedure_1() throws SQLException, IOException {
        MapperRegistry.DEFAULT.loadMapper("/net_hasor_db/dal_dynamic/execute/execute.xml", MappingOptions.buildOverwrite());
        MapperDalExecute dalExecute = new MapperDalExecute("net.hasor.test.db.dal.execute.TestExecuteDal", MapperRegistry.DEFAULT, RuleRegistry.DEFAULT);
        //
        Object execute1 = dalExecute.execute(connection, "callSelectUser", new HashMap<>());
        assert execute1.equals(2);
    }
}
