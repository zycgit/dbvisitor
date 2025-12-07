package net.hasor.dbvisitor.session;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.session.dto.UserInfo;
import net.hasor.dbvisitor.session.dto.UserInfo2;
import net.hasor.dbvisitor.session.dto.UserInfo3;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

public class BasicMapperUpdateTest {
    @Test
    public void update_onekey_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            List<UserInfo2> list = mapper.query().queryForList();
            UserInfo2 userInfo = list.get(0);
            userInfo.setLoginName("11");
            userInfo.setPassword(null);

            assert mapper.update(userInfo) == 1;

            UserInfo2 tbUser1 = mapper.loadBy(userInfo);
            assert tbUser1 != userInfo;
            assert tbUser1.getLoginName().equals("11");
            assert tbUser1.getPassword() != null;
        }
    }

    @Test
    public void update_onekey_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            List<UserInfo2> list = mapper.query().queryForList();
            UserInfo2 userInfo = new UserInfo2();
            userInfo.setUid(list.get(0).getUid());
            userInfo.setLoginName("11");
            userInfo.setPassword(null);

            assert mapper.update(userInfo) == 1;

            UserInfo2 tbUser1 = mapper.loadBy(userInfo);
            assert tbUser1 != userInfo;
            assert tbUser1.getLoginName().equals("11");
            assert tbUser1.getPassword() != null;
        }
    }

    @Test
    public void update_twokey_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo3> mapper = s.createBaseMapper(UserInfo3.class);
            List<UserInfo3> list = mapper.query().queryForList();
            UserInfo3 userInfo = list.get(0);
            userInfo.setLoginName("11");
            userInfo.setPassword(null);

            assert mapper.update(userInfo) == 1;

            UserInfo3 tbUser1 = mapper.loadBy(userInfo);
            assert tbUser1 != userInfo;
            assert tbUser1.getLoginName().equals("11");
            assert tbUser1.getPassword() != null;
        }
    }

    @Test
    public void update_twokey_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo3> mapper = s.createBaseMapper(UserInfo3.class);
            List<UserInfo3> list = mapper.query().queryForList();
            UserInfo3 userInfo = new UserInfo3();
            userInfo.setUid(list.get(0).getUid());
            userInfo.setName(list.get(0).getName());
            userInfo.setLoginName("11");
            userInfo.setPassword(null);

            assert mapper.update(userInfo) == 1;

            UserInfo3 tbUser1 = mapper.loadBy(userInfo);
            assert tbUser1 != userInfo;
            assert tbUser1.getLoginName().equals("11");
            assert tbUser1.getPassword() != null;
        }
    }

    @Test
    public void update_twokey_3() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo3> mapper = s.createBaseMapper(UserInfo3.class);
            List<UserInfo3> list = mapper.query().queryForList();
            UserInfo3 userInfo = new UserInfo3();
            userInfo.setUid(list.get(0).getUid());
            userInfo.setLoginName("11");
            userInfo.setPassword(null);

            assert mapper.update(userInfo) == 0;
            assert mapper.loadBy(userInfo) == null;
            UserInfo3 tbUser1 = mapper.loadBy(list.get(0));
            assert tbUser1 != userInfo;
            assert tbUser1.getLoginName().equals(list.get(0).getLoginName());
            assert tbUser1.getPassword().equals(list.get(0).getPassword());
        }
    }

    //

    @Test
    public void updateByMap_onekey_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            List<Map<String, Object>> list = mapper.query().queryForMapList();
            Map<String, Object> userInfo = list.get(0);
            userInfo.put("loginName", "11");
            userInfo.put("password", null);

            assert mapper.updateByMap(userInfo) == 1;

            UserInfo2 tbUser1 = mapper.loadBy(userInfo);
            assert tbUser1 != userInfo;
            assert tbUser1.getLoginName().equals("11");
            assert tbUser1.getPassword() != null;
        }
    }

    @Test
    public void updateByMap_onekey_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            List<Map<String, Object>> list = mapper.query().queryForMapList();
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("uid", list.get(0).get("uid"));
            userInfo.put("loginName", "11");
            userInfo.put("password", null);

            assert mapper.updateByMap(userInfo) == 1;

            UserInfo2 tbUser1 = mapper.loadBy(userInfo);
            assert tbUser1.getLoginName().equals("11");
            assert tbUser1.getPassword() != null;
        }
    }

    @Test
    public void updateByMap_twokey_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo3> mapper = s.createBaseMapper(UserInfo3.class);
            List<Map<String, Object>> list = mapper.query().queryForMapList();
            Map<String, Object> userInfo = list.get(0);
            userInfo.put("uid", list.get(0).get("uid"));
            userInfo.put("name", list.get(0).get("name"));
            userInfo.put("loginName", "11");
            userInfo.put("password", null);

            assert mapper.updateByMap(userInfo) == 1;

            UserInfo3 tbUser1 = mapper.loadBy(userInfo);
            assert tbUser1.getLoginName().equals("11");
            assert tbUser1.getPassword() != null;
        }
    }

    @Test
    public void updateByMap_twokey_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo3> mapper = s.createBaseMapper(UserInfo3.class);
            List<Map<String, Object>> list = mapper.query().queryForMapList();
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("uid", list.get(0).get("uid"));
            userInfo.put("name", list.get(0).get("name"));
            userInfo.put("loginName", "11");
            userInfo.put("password", null);

            assert mapper.updateByMap(userInfo) == 1;

            UserInfo3 tbUser1 = mapper.loadBy(userInfo);
            assert tbUser1.getLoginName().equals("11");
            assert tbUser1.getPassword() != null;
        }
    }

    //

    @Test
    public void updateUseMap_onekey_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper mapper = s.createBaseMapper(UserInfo2.class);
            List<Map<String, Object>> list = mapper.query().queryForMapList();
            Map<String, Object> userInfo = list.get(0);
            userInfo.put("loginName", "11");
            userInfo.put("password", null);

            assert mapper.update(userInfo) == 1;

            UserInfo2 tbUser1 = ((BaseMapper<UserInfo2>) mapper).loadBy(userInfo);
            assert tbUser1 != userInfo;
            assert tbUser1.getLoginName().equals("11");
            assert tbUser1.getPassword() != null;
        }
    }

    @Test
    public void updateUseMap_onekey_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper mapper = s.createBaseMapper(UserInfo2.class);
            List<Map<String, Object>> list = mapper.query().queryForMapList();
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("uid", list.get(0).get("uid"));
            userInfo.put("loginName", "11");
            userInfo.put("password", null);

            assert mapper.update(userInfo) == 1;

            UserInfo2 tbUser1 = ((BaseMapper<UserInfo2>) mapper).loadBy(userInfo);
            assert tbUser1.getLoginName().equals("11");
            assert tbUser1.getPassword() != null;
        }
    }

    @Test
    public void updateUseMap_twokey_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper mapper = s.createBaseMapper(UserInfo3.class);
            List<Map<String, Object>> list = mapper.query().queryForMapList();
            Map<String, Object> userInfo = list.get(0);
            userInfo.put("uid", list.get(0).get("uid"));
            userInfo.put("name", list.get(0).get("name"));
            userInfo.put("loginName", "11");
            userInfo.put("password", null);

            assert mapper.update(userInfo) == 1;

            UserInfo3 tbUser1 = ((BaseMapper<UserInfo3>) mapper).loadBy(userInfo);
            assert tbUser1.getLoginName().equals("11");
            assert tbUser1.getPassword() != null;
        }
    }

    @Test
    public void updateUseMap_twokey_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper mapper = s.createBaseMapper(UserInfo3.class);
            List<Map<String, Object>> list = mapper.query().queryForMapList();
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("uid", list.get(0).get("uid"));
            userInfo.put("name", list.get(0).get("name"));
            userInfo.put("loginName", "11");
            userInfo.put("password", null);

            assert mapper.update(userInfo) == 1;

            UserInfo3 tbUser1 = ((BaseMapper<UserInfo3>) mapper).loadBy(userInfo);
            assert tbUser1.getLoginName().equals("11");
            assert tbUser1.getPassword() != null;
        }
    }

    //

    @Test
    public void badUpdate_1() throws Exception {
        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            mapper.update(null);
            assert false;
        } catch (NullPointerException e) {
            assert e.getMessage().equals("sample is null.");
        }

        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            mapper.updateByMap(null);
            assert false;
        } catch (NullPointerException e) {
            assert e.getMessage().equals("sampleMap is null.");
        }
    }

    @Test
    public void badUpdate_2_1() throws Exception {
        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo> mapper = s.createBaseMapper(UserInfo.class);
            mapper.update(new UserInfo());
            assert false;
        } catch (UnsupportedOperationException e) {
            assert e.getMessage().endsWith(" missing primary key.");
        }

        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo> mapper = s.createBaseMapper(UserInfo.class);
            mapper.updateByMap(new HashMap<>());
            assert false;
        } catch (UnsupportedOperationException e) {
            assert e.getMessage().endsWith(" missing primary key.");
        }
    }

    @Test
    public void badUpdate_2_2() throws Exception {
        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo3> mapper = s.createBaseMapper(UserInfo3.class);
            List<Map<String, Object>> list = mapper.query().queryForMapList();
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("uid", list.get(0).get("uid"));
            userInfo.put("loginName", "11");
            userInfo.put("password", null);

            mapper.updateByMap(userInfo);
        } catch (UnsupportedOperationException e) {
            assert e.getMessage().endsWith(" missing primary key.");
        }

        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper mapper = s.createBaseMapper(UserInfo3.class);
            List<Map<String, Object>> list = mapper.query().queryForMapList();
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("uid", list.get(0).get("uid"));
            userInfo.put("loginName", "11");
            userInfo.put("password", null);

            mapper.update(userInfo);
        } catch (UnsupportedOperationException e) {
            assert e.getMessage().endsWith(" missing primary key.");
        }
    }

    @Test
    public void badUpdate_3() throws Exception {
        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper mapper = s.createBaseMapper(UserInfo2.class);
            UserInfo3 dat = new UserInfo3();
            dat.setLoginName("abc");

            mapper.update(dat);
            assert false;
        } catch (ClassCastException e) {
            assert e.getMessage().endsWith("cannot be as " + UserInfo2.class.getName());
        }
    }

    @Test
    public void badUpdate_4() throws Exception {
        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            Map<String, Object> map = new HashMap<>();
            map.put("uid", "abc");

            assert mapper.updateByMap(map) == 0;
        }
    }
}
