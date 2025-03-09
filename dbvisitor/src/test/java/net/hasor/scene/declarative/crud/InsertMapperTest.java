package net.hasor.scene.declarative.crud;

import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.session.dto.UserInfoMap;
import net.hasor.scene.declarative.crud.dto.UserTable;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.util.Date;
import java.util.List;

public class InsertMapperTest {
    @Test
    public void createUser_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        UserTable tbUser = new UserTable();
        tbUser.setUserUuid("111");
        tbUser.setName("112");
        tbUser.setLoginName("113");
        tbUser.setLoginPassword("114");
        tbUser.setEmail("115");
        tbUser.setSeq(116);
        tbUser.setRegisterTime(new Date());

        try (Session s = config.newSession(DsUtils.mysqlConn())) {
            UserMapper userMapper = s.createMapper(UserMapper.class);

            int i = userMapper.createUser(tbUser);
            assert i == 1;

            List<UserInfoMap> one = s.jdbc().queryForList("select * from user_info where user_name = '112'", UserInfoMap.class);
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
}
