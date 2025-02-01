//package net.hasor.dbvisitor.session;
//import net.hasor.dbvisitor.dialect.BoundSql;
//import net.hasor.dbvisitor.mapping.MappingOptions;
//import net.hasor.dbvisitor.session.dal.MapperRegistry;
//import net.hasor.dbvisitor.template.jdbc.core.JdbcTemplate;
//import net.hasor.test.dal.Mapper3Dal;
//import net.hasor.test.dal.execute.TestExecuteDal;
//import net.hasor.test.dto.AutoId;
//import net.hasor.test.dto.UserInfo;
//import net.hasor.test.dto.UserInfo2;
//import net.hasor.test.utils.DsUtils;
//import org.junit.Test;
//
//import java.sql.Connection;
//import java.text.SimpleDateFormat;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class BasicExecuteTest {
//
//    @Test
//    public void listUserList_1() throws Exception {
//        try (Connection con = DsUtils.mysqlConn()) {
//            MapperRegistry dalRegistry = new MapperRegistry(MappingOptions.buildNew().mapUnderscoreToCamelCase(true));
//            DalSession dalSession = new DalSession(con, dalRegistry);
//
//            dalRegistry.loadMapper(TestExecuteDal.class);
//            TestExecuteDal dalExecute = dalSession.createMapper(TestExecuteDal.class);
//
//            int i = dalExecute.initUser();
//            assert i == 2;
//            List<UserInfo> execute2 = dalExecute.listUserList_1("aaa");
//            assert execute2.size() == 2;
//
//            UserInfo tbUser1 = execute2.get(0);
//            assert tbUser1.getUserUuid().equals("11");
//            assert tbUser1.getName().equals("12");
//            assert tbUser1.getLoginName().equals("13");
//            assert tbUser1.getLoginPassword().equals("14");
//            assert tbUser1.getEmail().equals("15");
//            assert tbUser1.getSeq() == 16;
//            assert new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(tbUser1.getRegisterTime()).equals("2021-07-20 00:00:00");
//            UserInfo tbUser2 = (UserInfo) ((List<?>) execute2).get(1);
//            assert tbUser2.getUserUuid().equals("21");
//            assert tbUser2.getName().equals("22");
//            assert tbUser2.getLoginName().equals("23");
//            assert tbUser2.getLoginPassword().equals("24");
//            assert tbUser2.getEmail().equals("25");
//            assert tbUser2.getSeq() == 26;
//            assert new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(tbUser2.getRegisterTime()).equals("2021-07-20 00:00:00");
//        }
//    }
//
//    @Test
//    public void listUserList_2() throws Exception {
//        try (Connection con = DsUtils.mysqlConn()) {
//            MapperRegistry dalRegistry = new MapperRegistry(MappingOptions.buildNew().mapUnderscoreToCamelCase(true));
//            DalSession dalSession = new DalSession(con, dalRegistry);
//
//            dalRegistry.loadMapper(TestExecuteDal.class);
//            TestExecuteDal dalExecute = dalSession.createMapper(TestExecuteDal.class);
//
//            int i = dalExecute.initUser();
//            assert i == 2;
//            List<UserInfo2> execute2 = dalExecute.listUserList_2("aaa");
//            assert execute2.size() == 2;
//
//            UserInfo2 tbUser1 = execute2.get(0);
//            assert tbUser1.getUid().equals("11");
//            assert tbUser1.getName().equals("12");
//            assert tbUser1.getLoginName().equals("13");
//            assert tbUser1.getPassword().equals("14");
//            assert tbUser1.getEmail().equals("15");
//            assert tbUser1.getSeq() == 16;
//            assert new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(tbUser1.getCreateTime()).equals("2021-07-20 00:00:00");
//
//            UserInfo2 tbUser2 = execute2.get(1);
//            assert tbUser2.getUid().equals("21");
//            assert tbUser2.getName().equals("22");
//            assert tbUser2.getLoginName().equals("23");
//            assert tbUser2.getPassword().equals("24");
//            assert tbUser2.getEmail().equals("25");
//            assert tbUser2.getSeq() == 26;
//            assert new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(tbUser2.getCreateTime()).equals("2021-07-20 00:00:00");
//        }
//    }
//
//    @Test
//    public void selectKey_1() throws Exception {
//        try (Connection con = DsUtils.mysqlConn()) {
//            MapperRegistry dalRegistry = new MapperRegistry(MappingOptions.buildNew().mapUnderscoreToCamelCase(true));
//            DalSession dalSession = new DalSession(con, dalRegistry);
//
//            dalRegistry.loadMapper(TestExecuteDal.class);
//            TestExecuteDal dalExecute = dalSession.createMapper(TestExecuteDal.class);
//
//            AutoId autoId = new AutoId();
//            autoId.setId(null);
//            autoId.setName("abc");
//            autoId.setUid("uuid");
//            assert autoId.getId() == null;
//
//            dalExecute.insertAutoID_1(autoId);
//            Integer id1 = autoId.getId();
//
//            dalExecute.insertAutoID_1(autoId);
//            Integer id2 = autoId.getId();
//
//            assert id1 != null;
//            assert id2 != null;
//            assert !id1.equals(id2);
//        }
//    }
//
//    @Test
//    public void selectKey_2() throws Exception {
//        try (Connection con = DsUtils.mysqlConn()) {
//            MapperRegistry dalRegistry = new MapperRegistry(MappingOptions.buildNew().mapUnderscoreToCamelCase(true));
//            DalSession dalSession = new DalSession(con, dalRegistry);
//
//            dalRegistry.loadMapper(TestExecuteDal.class);
//            TestExecuteDal dalExecute = dalSession.createMapper(TestExecuteDal.class);
//
//            AutoId autoId = new AutoId();
//            autoId.setId(null);
//            autoId.setName("abc");
//            autoId.setUid("uuid");
//            assert autoId.getId() == null;
//
//            dalExecute.insertAutoID_2(autoId);
//            Integer id1 = autoId.getId();
//
//            dalExecute.insertAutoID_2(autoId);
//            Integer id2 = autoId.getId();
//
//            assert id1 != null;
//            assert id2 != null;
//            assert !id1.equals(id2);
//        }
//    }
//
//    @Test
//    public void call_1() throws Exception {
//        try (Connection con = DsUtils.mysqlConn()) {
//            new JdbcTemplate(con).execute("drop procedure if exists proc_select_user;");
//            new JdbcTemplate(con).execute("create procedure proc_select_user(out p_out decimal(6,3)) begin set p_out= 123.123; end;");
//
//            MapperRegistry dalRegistry = new MapperRegistry(MappingOptions.buildNew().mapUnderscoreToCamelCase(true));
//            DalSession dalSession = new DalSession(con, dalRegistry);
//
//            dalRegistry.loadMapper(TestExecuteDal.class);
//            TestExecuteDal dalExecute = dalSession.createMapper(TestExecuteDal.class);
//
//            Map<String, Object> args = new HashMap<>();
//            args.put("abc", "abc");
//
//            dalExecute.callSelectUser(args); // out param in args
//
//            assert args.get("abc").toString().equals("123.123");
//        }
//    }
//
//    @Test
//    public void defaultMethodTest() throws Exception {
//        try (Connection con = DsUtils.mysqlConn()) {
//            MapperRegistry dalRegistry = new MapperRegistry(MappingOptions.buildNew().mapUnderscoreToCamelCase(true));
//            DalSession dalSession = new DalSession(con, dalRegistry);
//
//            dalSession.getRegistry().loadMapper(Mapper3Dal.class);
//            Mapper3Dal dalExecute = dalSession.createMapper(Mapper3Dal.class);
//
//            BoundSql boundSql = dalExecute.testBind("12345678");
//
//            assert boundSql.getSqlString().equals("SELECT * FROM user_info WHERE user_name = ?");
//            assert boundSql.getArgs()[0].equals("12345678");
//        }
//    }
//}
