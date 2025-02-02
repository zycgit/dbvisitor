package net.hasor.dbvisitor.session;
import net.hasor.dbvisitor.session.dto.AutoIncrID;
import net.hasor.dbvisitor.session.dto.BasicMapperByAnno;
import net.hasor.dbvisitor.session.dto.UserInfo;
import net.hasor.dbvisitor.session.dto.UserInfoMap;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BasicMapperByAnnoTest {

    @Test
    public void listUserList_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.mysqlConn())) {
            BasicMapperByAnno dalExecute = s.createMapper(BasicMapperByAnno.class);

            int i = dalExecute.initUser();
            assert i == 2;
            List<UserInfoMap> empty = dalExecute.listUserList_1("aaa");
            assert empty.isEmpty();

            List<UserInfoMap> one = dalExecute.listUserList_1("12");
            assert one.size() == 1;

            UserInfoMap tbUser1 = one.get(0);
            assert tbUser1.getUid().equals("11");
            assert tbUser1.getName().equals("12");
            assert tbUser1.getLoginName().equals("13");
            assert tbUser1.getPassword().equals("14");
            assert tbUser1.getEmail().equals("15");
            assert tbUser1.getSeq() == 16;
            assert new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(tbUser1.getCreateTime()).equals("2021-07-20 00:00:00");
        }
    }

    @Test
    public void listUserList_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.mysqlConn())) {
            BasicMapperByAnno dalExecute = s.createMapper(BasicMapperByAnno.class);

            int i = dalExecute.initUser();
            assert i == 2;
            List<UserInfoMap> empty = dalExecute.listUserList_1("aaa");
            assert empty.isEmpty();

            List<UserInfoMap> one = dalExecute.listUserList_1("12");
            assert one.size() == 1;

            UserInfoMap tbUser1 = one.get(0);
            assert tbUser1.getUid().equals("11");
            assert tbUser1.getName().equals("12");
            assert tbUser1.getLoginName().equals("13");
            assert tbUser1.getPassword().equals("14");
            assert tbUser1.getEmail().equals("15");
            assert tbUser1.getSeq() == 16;
            assert new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(tbUser1.getCreateTime()).equals("2021-07-20 00:00:00");
        }
    }

    @Test
    public void createUser_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        UserInfo tbUser = new UserInfo();
        tbUser.setUserUuid("111");
        tbUser.setName("112");
        tbUser.setLoginName("113");
        tbUser.setLoginPassword("114");
        tbUser.setEmail("115");
        tbUser.setSeq(116);
        tbUser.setRegisterTime(new Date());

        try (Session s = config.newSession(DsUtils.mysqlConn())) {
            BasicMapperByAnno dalExecute = s.createMapper(BasicMapperByAnno.class);

            int i = dalExecute.createUser(tbUser);
            assert i == 1;

            List<UserInfoMap> one = dalExecute.listUserList_1("112");
            assert one.size() == 1;

            UserInfoMap tbUser1 = one.get(0);
            assert tbUser1.getUid().equals("111");
            assert tbUser1.getName().equals("112");
            assert tbUser1.getLoginName().equals("113");
            assert tbUser1.getPassword().equals("114");
            assert tbUser1.getEmail().equals("115");
            assert tbUser1.getSeq() == 116;
            assert tbUser1.getCreateTime() != null;
        }
    }

    @Test
    public void selectKey_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.mysqlConn())) {
            BasicMapperByAnno dalExecute = s.createMapper(BasicMapperByAnno.class);

            AutoIncrID autoId = new AutoIncrID();
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
    }

    @Test
    public void selectKey_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.mysqlConn())) {
            BasicMapperByAnno dalExecute = s.createMapper(BasicMapperByAnno.class);

            AutoIncrID autoId = new AutoIncrID();
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
    }

    @Test
    public void call_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.mysqlConn())) {
            s.jdbc().execute("drop procedure if exists proc_select_user;");
            s.jdbc().execute("create procedure proc_select_user(out p_out decimal(6,3)) begin set p_out= 123.123; end;");
            BasicMapperByAnno dalExecute = s.createMapper(BasicMapperByAnno.class);

            Map<String, Object> args = new HashMap<>();
            args.put("abc", "abc");

            Map<String, Object> res = dalExecute.callSelectUser(args);

            assert res.get("abc").toString().equals("123.123");
        }
    }
}
