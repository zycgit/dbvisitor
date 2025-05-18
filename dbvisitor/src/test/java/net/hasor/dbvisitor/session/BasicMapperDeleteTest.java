package net.hasor.dbvisitor.session;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.session.dto.UserInfo;
import net.hasor.dbvisitor.session.dto.UserInfo2;
import net.hasor.dbvisitor.session.dto.UserInfo3;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BasicMapperDeleteTest {
    @Test
    public void delete_onekey_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            List<UserInfo2> list = mapper.query().queryForList();
            UserInfo2 userInfo = list.get(0);

            assert mapper.query().queryForCount() == 3;
            assert mapper.delete(userInfo) == 1;
            assert mapper.query().queryForCount() == 2;
            assert mapper.loadBy(userInfo) == null;
        }
    }

    @Test
    public void delete_onekey_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            List<UserInfo2> list = mapper.query().queryForList();
            UserInfo2 userInfo = new UserInfo2();
            userInfo.setUid(list.get(0).getUid());

            assert mapper.query().queryForCount() == 3;
            assert mapper.delete(userInfo) == 1;
            assert mapper.query().queryForCount() == 2;
            assert mapper.loadBy(userInfo) == null;
        }
    }

    @Test
    public void delete_twokey_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo3> mapper = s.createBaseMapper(UserInfo3.class);
            List<UserInfo3> list = mapper.query().queryForList();
            UserInfo3 userInfo = list.get(0);

            assert mapper.query().queryForCount() == 6;
            assert mapper.delete(userInfo) == 1;
            assert mapper.query().queryForCount() == 5;
            assert mapper.loadBy(userInfo) == null;
        }
    }

    @Test
    public void delete_twokey_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo3> mapper = s.createBaseMapper(UserInfo3.class);
            List<UserInfo3> list = mapper.query().queryForList();
            UserInfo3 userInfo = new UserInfo3();
            userInfo.setUid(list.get(0).getUid());
            userInfo.setName(list.get(0).getName());

            assert mapper.query().queryForCount() == 6;
            assert mapper.delete(userInfo) == 1;
            assert mapper.query().queryForCount() == 5;
            assert mapper.loadBy(userInfo) == null;
        }
    }

    @Test
    public void delete_twokey_3() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo3> mapper = s.createBaseMapper(UserInfo3.class);
            List<UserInfo3> list = mapper.query().queryForList();
            UserInfo3 userInfo = new UserInfo3();
            userInfo.setUid(list.get(0).getUid());

            assert mapper.query().queryForCount() == 6;
            assert mapper.delete(userInfo) == 0;
            assert mapper.query().queryForCount() == 6;
            assert mapper.loadBy(list.get(0)) != null;
        }
    }

    //

    @Test
    public void deleteByMap_onekey_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            List<Map<String, Object>> list = mapper.query().queryForMapList();
            Map<String, Object> userInfo = list.get(0);

            assert mapper.query().queryForCount() == 3;
            assert mapper.deleteByMap(userInfo) == 1;
            assert mapper.query().queryForCount() == 2;
            assert mapper.loadBy(userInfo) == null;
        }
    }

    @Test
    public void deleteByMap_onekey_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            List<Map<String, Object>> list = mapper.query().queryForMapList();
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("uid", list.get(0).get("uid"));

            assert mapper.query().queryForCount() == 3;
            assert mapper.deleteByMap(userInfo) == 1;
            assert mapper.query().queryForCount() == 2;
            assert mapper.loadBy(userInfo) == null;
        }
    }

    @Test
    public void deleteByMap_twokey_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo3> mapper = s.createBaseMapper(UserInfo3.class);
            List<Map<String, Object>> list = mapper.query().queryForMapList();
            Map<String, Object> userInfo = list.get(0);
            userInfo.put("uid", list.get(0).get("uid"));
            userInfo.put("name", list.get(0).get("name"));

            assert mapper.query().queryForCount() == 6;
            assert mapper.deleteByMap(userInfo) == 1;
            assert mapper.query().queryForCount() == 5;
            assert mapper.loadBy(userInfo) == null;
        }
    }

    @Test
    public void deleteByMap_twokey_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo3> mapper = s.createBaseMapper(UserInfo3.class);
            List<Map<String, Object>> list = mapper.query().queryForMapList();
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("uid", list.get(0).get("uid"));
            userInfo.put("name", list.get(0).get("name"));

            assert mapper.query().queryForCount() == 6;
            assert mapper.deleteByMap(userInfo) == 1;
            assert mapper.query().queryForCount() == 5;
            assert mapper.loadBy(userInfo) == null;
        }
    }

    //

    @Test
    public void deleteUseMap_onekey_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper mapper = s.createBaseMapper(UserInfo2.class);
            List<Map<String, Object>> list = mapper.query().queryForMapList();
            Map<String, Object> userInfo = list.get(0);

            assert mapper.query().queryForCount() == 3;
            assert mapper.delete(userInfo) == 1;
            assert mapper.query().queryForCount() == 2;
            assert mapper.loadBy(userInfo) == null;
        }
    }

    @Test
    public void deleteUseMap_onekey_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper mapper = s.createBaseMapper(UserInfo2.class);
            List<Map<String, Object>> list = mapper.query().queryForMapList();
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("uid", list.get(0).get("uid"));

            assert mapper.query().queryForCount() == 3;
            assert mapper.delete(userInfo) == 1;
            assert mapper.query().queryForCount() == 2;
            assert mapper.loadBy(list.get(0)) == null;
        }
    }

    @Test
    public void deleteUseMap_twokey_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper mapper = s.createBaseMapper(UserInfo3.class);
            List<Map<String, Object>> list = mapper.query().queryForMapList();
            Map<String, Object> userInfo = list.get(0);

            assert mapper.query().queryForCount() == 6;
            assert mapper.delete(userInfo) == 1;
            assert mapper.query().queryForCount() == 5;
            assert mapper.loadBy(userInfo) == null;
        }
    }

    @Test
    public void deleteUseMap_twokey_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper mapper = s.createBaseMapper(UserInfo3.class);
            List<Map<String, Object>> list = mapper.query().queryForMapList();
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("uid", list.get(0).get("uid"));
            userInfo.put("name", list.get(0).get("name"));

            assert mapper.query().queryForCount() == 6;
            assert mapper.delete(userInfo) == 1;
            assert mapper.query().queryForCount() == 5;
            assert mapper.loadBy(list.get(0)) == null;
        }
    }

    //

    @Test
    public void badDelete_1() throws Exception {
        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            mapper.delete(null);
            assert false;
        } catch (NullPointerException e) {
            assert e.getMessage().equals("entity is null.");
        }

        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            mapper.deleteByMap(null);
            assert false;
        } catch (NullPointerException e) {
            assert e.getMessage().equals("entityMap is null.");
        }
    }

    @Test
    public void badDelete_2_1() throws Exception {
        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo> mapper = s.createBaseMapper(UserInfo.class);
            mapper.delete(new UserInfo());
            assert false;
        } catch (UnsupportedOperationException e) {
            assert e.getMessage().endsWith(" missing primary key.");
        }

        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo> mapper = s.createBaseMapper(UserInfo.class);
            mapper.deleteByMap(new HashMap<>());
            assert false;
        } catch (UnsupportedOperationException e) {
            assert e.getMessage().endsWith(" missing primary key.");
        }

    }

    @Test
    public void badDelete_2_2() throws Exception {
        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo3> mapper = s.createBaseMapper(UserInfo3.class);
            List<Map<String, Object>> list = mapper.query().queryForMapList();
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("uid", list.get(0).get("uid"));

            mapper.deleteByMap(userInfo);
        } catch (UnsupportedOperationException e) {
            assert e.getMessage().endsWith(" missing primary key.");
        }

        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper mapper = s.createBaseMapper(UserInfo3.class);
            List<Map<String, Object>> list = mapper.query().queryForMapList();
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("uid", list.get(0).get("uid"));

            mapper.delete(userInfo);
        } catch (UnsupportedOperationException e) {
            assert e.getMessage().endsWith(" missing primary key.");
        }
    }

    @Test
    public void badDelete_3() throws Exception {
        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper mapper = s.createBaseMapper(UserInfo2.class);
            UserInfo3 dat = new UserInfo3();
            dat.setLoginName("abc");

            mapper.delete(dat);
            assert false;
        } catch (ClassCastException e) {
            assert e.getMessage().endsWith("cannot be as " + UserInfo2.class.getName());
        }
    }

    @Test
    public void badDelete_4() throws Exception {
        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            Map<String, Object> map = new HashMap<>();
            map.put("uid", "abc");

            assert mapper.deleteByMap(map) == 0;
        }
    }

    @Test
    public void badDelete_5() throws Exception {
        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            Map<String, Object> map = new HashMap<>();
            map.put("abc", "abc");

            assert mapper.deleteByMap(map) == 0;
        } catch (UnsupportedOperationException e) {
            assert e.getMessage().endsWith(" missing primary key.");
        }
    }
}
