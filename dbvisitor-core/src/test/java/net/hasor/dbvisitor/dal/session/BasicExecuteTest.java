package net.hasor.dbvisitor.dal.session;
import net.hasor.dbvisitor.dal.repository.DalRegistry;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.test.db.dal.Mapper3Dal;
import net.hasor.test.db.dal.execute.TestExecuteDal;
import net.hasor.test.db.dal.execute.TestUser;
import net.hasor.test.db.dto.AutoId;
import net.hasor.test.db.dto.TbUser2;
import net.hasor.test.db.utils.DsUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BasicExecuteTest {
    private DalSession dalSession;

    @Before
    public void loadMapping() throws IOException, SQLException {
        DalRegistry dalRegistry = new DalRegistry();
        dalRegistry.loadMapper(TestExecuteDal.class);
        this.dalSession = new DalSession(DsUtils.mysqlConnection(), dalRegistry);
        this.beforeTest(this.dalSession.lambdaTemplate());
    }

    protected void beforeTest(JdbcTemplate jdbcTemplate) throws SQLException, IOException {
        jdbcTemplate.execute("drop table if exists test_user");
        jdbcTemplate.loadSplitSQL(";", StandardCharsets.UTF_8, "/net_hasor_db/dal_dynamic/execute/execute_for_mysql.sql");
        jdbcTemplate.execute("drop table if exists auto_id");
        jdbcTemplate.loadSplitSQL(";", StandardCharsets.UTF_8, "/net_hasor_db/auto_id_for_mysql.sql");

        jdbcTemplate.execute("drop procedure if exists proc_select_user;");
        jdbcTemplate.execute("create procedure proc_select_user(out p_out double) begin set p_out=123.123; select * from test_user; end;");
    }

    @Test
    public void listUserList_1() {
        TestExecuteDal dalExecute = this.dalSession.createMapper(TestExecuteDal.class);
        int i = dalExecute.initUser();
        assert i == 2;
        List<TbUser2> execute2 = dalExecute.listUserList_1("aaa");
        assert execute2.size() == 2;

        TbUser2 tbUser1 = execute2.get(0);
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
    public void listUserList_2() {
        TestExecuteDal dalExecute = this.dalSession.createMapper(TestExecuteDal.class);
        int i = dalExecute.initUser();
        assert i == 2;
        List<TestUser> execute2 = dalExecute.listUserList_2("aaa");
        assert execute2.size() == 2;

        TestUser tbUser1 = execute2.get(0);
        assert tbUser1.getUserUUID().equals("11");
        assert tbUser1.getName().equals("12");
        assert tbUser1.getLoginName().equals("13");
        assert tbUser1.getLoginPassword().equals("14");
        assert tbUser1.getEmail().equals("15");
        assert tbUser1.getIndex() == 16;
        assert new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(tbUser1.getRegisterTime()).equals("2021-07-20 00:00:00");

        TestUser tbUser2 = execute2.get(1);
        assert tbUser2.getUserUUID().equals("21");
        assert tbUser2.getName().equals("22");
        assert tbUser2.getLoginName().equals("23");
        assert tbUser2.getLoginPassword().equals("24");
        assert tbUser2.getEmail().equals("25");
        assert tbUser2.getIndex() == 26;
        assert new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(tbUser2.getRegisterTime()).equals("2021-07-20 00:00:00");
    }

    @Test
    public void selectKey_1() {
        TestExecuteDal dalExecute = this.dalSession.createMapper(TestExecuteDal.class);

        AutoId autoId = new AutoId();
        autoId.setId(null);
        autoId.setName("abc");
        autoId.setUid("uuid");
        assert autoId.getId() == null;

        dalExecute.insertAutoID_1(autoId);
        Integer id1 = autoId.getId();

        dalExecute.insertAutoID_1(autoId);
        Integer id2 = autoId.getId();

        assert id1 != null;
        assert id2 != null;
        assert !id1.equals(id2);
    }

    @Test
    public void selectKey_2() {
        TestExecuteDal dalExecute = this.dalSession.createMapper(TestExecuteDal.class);

        AutoId autoId = new AutoId();
        autoId.setId(null);
        autoId.setName("abc");
        autoId.setUid("uuid");
        assert autoId.getId() == null;

        dalExecute.insertAutoID_2(autoId);
        Integer id1 = autoId.getId();

        dalExecute.insertAutoID_2(autoId);
        Integer id2 = autoId.getId();

        assert id1 != null;
        assert id2 != null;
        assert !id1.equals(id2);
    }

    @Test
    public void call_1() {
        TestExecuteDal dalExecute = this.dalSession.createMapper(TestExecuteDal.class);

        Map<String, Object> args = new HashMap<>();
        args.put("abc", "abc");

        Map<String, Object> abc = dalExecute.callSelectUser(args);

        assert args.get("abc").toString().equals("123.123");
    }

    @Test
    public void defaultMethodTest() throws IOException {
        this.dalSession.getDalRegistry().loadMapper(Mapper3Dal.class);
        Mapper3Dal dalExecute = this.dalSession.createMapper(Mapper3Dal.class);

        BoundSql boundSql = dalExecute.testBind("12345678");

        assert boundSql.getSqlString().equals("SELECT * FROM tb_user WHERE name = ?");
        assert boundSql.getArgs()[0].equals("12345678");
    }
}
