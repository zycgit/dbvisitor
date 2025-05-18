package net.hasor.dbvisitor.session;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.session.dto.UserInfo;
import net.hasor.dbvisitor.session.dto.UserInfo2;
import net.hasor.dbvisitor.session.dto.UserInfo3;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BasicMapperUpsertTest {
    @Test
    public void upsert_onekey_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            List<UserInfo2> list = mapper.query().queryForList();
            UserInfo2 userInfo = list.get(0);
            userInfo.setLoginName("11");

            assert mapper.query().queryForCount() == 3;
            assert mapper.upsert(userInfo) == 1;
            assert mapper.query().queryForCount() == 3;

            UserInfo2 tbUser1 = mapper.loadBy(userInfo);
            assert tbUser1 != userInfo;
            assert tbUser1.getLoginName().equals("11");
            assert tbUser1.getPassword().equals(userInfo.getPassword());
        }
    }

    @Test
    public void upsert_onekey_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            List<UserInfo2> list = mapper.query().queryForList();
            UserInfo2 userInfo = new UserInfo2();
            userInfo.setUid(list.get(0).getUid());
            userInfo.setLoginName("11");

            assert mapper.query().queryForCount() == 3;
            assert mapper.upsert(userInfo) == 1;
            assert mapper.query().queryForCount() == 3;

            UserInfo2 tbUser1 = mapper.loadBy(userInfo);
            assert tbUser1 != userInfo;
            assert tbUser1.getLoginName().equals("11");
            assert tbUser1.getPassword() == null;
        }
    }

    @Test
    public void upsert_twokey_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo3> mapper = s.createBaseMapper(UserInfo3.class);
            List<UserInfo3> list = mapper.query().queryForList();
            UserInfo3 userInfo = list.get(0);
            userInfo.setLoginName("11");

            assert mapper.query().queryForCount() == 6;
            assert mapper.upsert(userInfo) == 1;
            assert mapper.query().queryForCount() == 6;

            UserInfo3 tbUser1 = mapper.loadBy(userInfo);
            assert tbUser1 != userInfo;
            assert tbUser1.getLoginName().equals("11");
            assert tbUser1.getPassword().equals(userInfo.getPassword());
        }
    }

    @Test
    public void upsert_twokey_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo3> mapper = s.createBaseMapper(UserInfo3.class);
            List<UserInfo3> list = mapper.query().queryForList();
            UserInfo3 userInfo = new UserInfo3();
            userInfo.setUid(list.get(0).getUid());
            userInfo.setName(list.get(0).getName());
            userInfo.setLoginName("11");

            assert mapper.query().queryForCount() == 6;
            assert mapper.upsert(userInfo) == 1;
            assert mapper.query().queryForCount() == 6;

            UserInfo3 tbUser1 = mapper.loadBy(userInfo);
            assert tbUser1 != userInfo;
            assert tbUser1.getLoginName().equals("11");
            assert tbUser1.getPassword() == null;
        }
    }

    @Test
    public void upsert_twokey_3() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo3> mapper = s.createBaseMapper(UserInfo3.class);
            List<UserInfo3> list = mapper.query().queryForList();
            UserInfo3 userInfo = new UserInfo3();
            userInfo.setUid(list.get(0).getUid());
            userInfo.setLoginName("11");

            assert mapper.query().queryForCount() == 6;
            assert mapper.upsert(userInfo) == 1;
            assert mapper.query().queryForCount() == 7;
            UserInfo3 tbUser1 = mapper.loadBy(userInfo);
            assert tbUser1 != userInfo;
            assert tbUser1.getUid().equals(userInfo.getUid());
            assert StringUtils.equals(tbUser1.getName(), userInfo.getName());
            assert tbUser1.getLoginName().equals(userInfo.getLoginName());
            assert StringUtils.equals(tbUser1.getPassword(), userInfo.getPassword());
        }
    }

    //

    @Test
    public void upsertByMap_onekey_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            List<Map<String, Object>> list = mapper.query().queryForMapList();
            Map<String, Object> userInfo = list.get(0);
            userInfo.put("loginName", "11");

            assert mapper.query().queryForCount() == 3;
            assert mapper.upsertByMap(userInfo) == 1;
            assert mapper.query().queryForCount() == 3;

            UserInfo2 tbUser1 = mapper.loadBy(userInfo);
            assert tbUser1 != userInfo;
            assert tbUser1.getLoginName().equals("11");
            assert tbUser1.getPassword().equals(userInfo.get("password"));
        }
    }

    @Test
    public void upsertByMap_onekey_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            List<Map<String, Object>> list = mapper.query().queryForMapList();
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("uid", list.get(0).get("uid"));
            userInfo.put("loginName", "11");

            assert mapper.query().queryForCount() == 3;
            assert mapper.upsertByMap(userInfo) == 1;
            assert mapper.query().queryForCount() == 3;

            UserInfo2 tbUser1 = mapper.loadBy(userInfo);
            assert tbUser1.getLoginName().equals("11");
            assert tbUser1.getPassword() == null;
        }
    }

    @Test
    public void upsertByMap_twokey_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo3> mapper = s.createBaseMapper(UserInfo3.class);
            List<Map<String, Object>> list = mapper.query().queryForMapList();
            Map<String, Object> userInfo = list.get(0);
            userInfo.put("uid", list.get(0).get("uid"));
            userInfo.put("name", list.get(0).get("name"));
            userInfo.put("loginName", "11");

            assert mapper.query().queryForCount() == 6;
            assert mapper.upsertByMap(userInfo) == 1;
            assert mapper.query().queryForCount() == 6;

            UserInfo3 tbUser1 = mapper.loadBy(userInfo);
            assert tbUser1.getLoginName().equals("11");
            assert tbUser1.getPassword().equals(userInfo.get("password"));
        }
    }

    @Test
    public void upsertByMap_twokey_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo3> mapper = s.createBaseMapper(UserInfo3.class);
            List<Map<String, Object>> list = mapper.query().queryForMapList();
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("uid", list.get(0).get("uid"));
            userInfo.put("name", list.get(0).get("name"));
            userInfo.put("loginName", "11");

            assert mapper.query().queryForCount() == 6;
            assert mapper.upsertByMap(userInfo) == 1;
            assert mapper.query().queryForCount() == 6;

            UserInfo3 tbUser1 = mapper.loadBy(userInfo);
            assert tbUser1.getLoginName().equals("11");
            assert tbUser1.getPassword() == null;
        }
    }

    @Test
    public void upsertByMap_twokey_3() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo3> mapper = s.createBaseMapper(UserInfo3.class);
            List<Map<String, Object>> list = mapper.query().queryForMapList();
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("uid", list.get(0).get("uid"));
            userInfo.put("name", "abc");
            userInfo.put("loginName", "11");

            assert mapper.query().queryForCount() == 6;
            assert mapper.upsertByMap(userInfo) == 1;
            assert mapper.query().queryForCount() == 7;
            UserInfo3 tbUser1 = mapper.loadBy(userInfo);
            assert tbUser1.getUid().equals(userInfo.get("uid"));
            assert StringUtils.equals(tbUser1.getName(), (String) userInfo.get("name"));
            assert tbUser1.getLoginName().equals(userInfo.get("loginName"));
            assert StringUtils.equals(tbUser1.getPassword(), (String) userInfo.get("password"));
        }
    }

    //

    @Test
    public void upsertUseMap_onekey_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper mapper = s.createBaseMapper(UserInfo2.class);
            List<Map<String, Object>> list = mapper.query().queryForMapList();
            Map<String, Object> userInfo = list.get(0);
            userInfo.put("loginName", "11");

            assert mapper.query().queryForCount() == 3;
            assert mapper.upsert(userInfo) == 1;
            assert mapper.query().queryForCount() == 3;

            UserInfo2 tbUser1 = ((BaseMapper<UserInfo2>) mapper).loadBy(userInfo);
            assert tbUser1 != userInfo;
            assert tbUser1.getLoginName().equals("11");
            assert tbUser1.getPassword().equals(userInfo.get("password"));
        }
    }

    @Test
    public void upsertUseMap_onekey_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper mapper = s.createBaseMapper(UserInfo2.class);
            List<Map<String, Object>> list = mapper.query().queryForMapList();
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("uid", list.get(0).get("uid"));
            userInfo.put("loginName", "11");

            assert mapper.query().queryForCount() == 3;
            assert mapper.upsert(userInfo) == 1;
            assert mapper.query().queryForCount() == 3;

            UserInfo2 tbUser1 = ((BaseMapper<UserInfo2>) mapper).loadBy(userInfo);
            assert tbUser1.getLoginName().equals("11");
            assert tbUser1.getPassword() == null;
        }
    }

    @Test
    public void upsertUseMap_twokey_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper mapper = s.createBaseMapper(UserInfo3.class);
            List<Map<String, Object>> list = mapper.query().queryForMapList();
            Map<String, Object> userInfo = list.get(0);
            userInfo.put("uid", list.get(0).get("uid"));
            userInfo.put("name", list.get(0).get("name"));
            userInfo.put("loginName", "11");

            assert mapper.query().queryForCount() == 6;
            assert mapper.upsert(userInfo) == 1;
            assert mapper.query().queryForCount() == 6;

            UserInfo3 tbUser1 = ((BaseMapper<UserInfo3>) mapper).loadBy(userInfo);
            assert tbUser1.getLoginName().equals("11");
            assert tbUser1.getPassword().equals(userInfo.get("password"));
        }
    }

    @Test
    public void upsertUseMap_twokey_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper mapper = s.createBaseMapper(UserInfo3.class);
            List<Map<String, Object>> list = mapper.query().queryForMapList();
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("uid", list.get(0).get("uid"));
            userInfo.put("name", list.get(0).get("name"));
            userInfo.put("loginName", "11");

            assert mapper.query().queryForCount() == 6;
            assert mapper.upsert(userInfo) == 1;
            assert mapper.query().queryForCount() == 6;

            UserInfo3 tbUser1 = ((BaseMapper<UserInfo3>) mapper).loadBy(userInfo);
            assert tbUser1.getLoginName().equals("11");
            assert tbUser1.getPassword() == null;
        }
    }

    @Test
    public void upsertUseMap_twokey_3() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper mapper = s.createBaseMapper(UserInfo3.class);
            List<Map<String, Object>> list = mapper.query().queryForMapList();
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("uid", list.get(0).get("uid"));
            userInfo.put("name", "abc");
            userInfo.put("loginName", "11");

            assert mapper.query().queryForCount() == 6;
            assert mapper.upsert(userInfo) == 1;
            assert mapper.query().queryForCount() == 7;

            UserInfo3 tbUser1 = ((BaseMapper<UserInfo3>) mapper).loadBy(userInfo);
            assert tbUser1.getUid().equals(userInfo.get("uid"));
            assert StringUtils.equals(tbUser1.getName(), (String) userInfo.get("name"));
            assert tbUser1.getLoginName().equals(userInfo.get("loginName"));
            assert StringUtils.equals(tbUser1.getPassword(), (String) userInfo.get("password"));
        }
    }

    //

    @Test
    public void badUpsert_1() throws Exception {
        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            mapper.upsert(null);
            assert false;
        } catch (NullPointerException e) {
            assert e.getMessage().equals("entity is null.");
        }

        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            mapper.upsertByMap(null);
            assert false;
        } catch (NullPointerException e) {
            assert e.getMessage().equals("entityMap is null.");
        }
    }

    @Test
    public void badUpsert_2() throws Exception {
        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo> mapper = s.createBaseMapper(UserInfo.class);
            mapper.upsert(new UserInfo());
            assert false;
        } catch (UnsupportedOperationException e) {
            assert e.getMessage().endsWith(" missing primary key.");
        }

        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo> mapper = s.createBaseMapper(UserInfo.class);
            mapper.upsertByMap(new HashMap<>());
            assert false;
        } catch (UnsupportedOperationException e) {
            assert e.getMessage().endsWith(" missing primary key.");
        }
    }

    @Test
    public void badUpsert_3() throws Exception {
        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper mapper = s.createBaseMapper(UserInfo2.class);
            UserInfo3 dat = new UserInfo3();
            dat.setLoginName("abc");

            mapper.upsert(dat);
            assert false;
        } catch (ClassCastException e) {
            assert e.getMessage().endsWith("cannot be as " + UserInfo2.class.getName());
        }
    }

    @Test
    public void badUpsert_5() throws Exception {
        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            Map<String, Object> map = new HashMap<>();
            map.put("uid", "abc");

            assert mapper.upsertByMap(map) == 0;
        }
    }
}
