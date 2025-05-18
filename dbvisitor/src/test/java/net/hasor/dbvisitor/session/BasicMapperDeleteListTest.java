package net.hasor.dbvisitor.session;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.session.dto.UserInfo;
import net.hasor.dbvisitor.session.dto.UserInfo2;
import net.hasor.dbvisitor.session.dto.UserInfo3;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.util.*;

public class BasicMapperDeleteListTest {
    @Test
    public void deleteList_onekey_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            List<UserInfo2> list = mapper.query().queryForList();
            UserInfo2 u1 = list.get(0);
            UserInfo2 u2 = list.get(1);

            assert mapper.query().queryForCount() == 3;
            assert mapper.deleteList(Arrays.asList(u1, u2)) == 2;
            assert mapper.query().queryForCount() == 1;
            assert mapper.loadBy(u1) == null;
            assert mapper.loadBy(u2) == null;
        }
    }

    @Test
    public void deleteList_onekey_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            List<UserInfo2> list = mapper.query().queryForList();
            UserInfo2 u1 = new UserInfo2();
            u1.setUid(list.get(0).getUid());
            UserInfo2 u2 = new UserInfo2();
            u2.setUid(list.get(1).getUid());

            assert mapper.query().queryForCount() == 3;
            assert mapper.deleteList(Arrays.asList(u1, u2)) == 2;
            assert mapper.query().queryForCount() == 1;
            assert mapper.loadBy(u1) == null;
            assert mapper.loadBy(u2) == null;
        }
    }

    @Test
    public void deleteList_twokey_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo3> mapper = s.createBaseMapper(UserInfo3.class);
            List<UserInfo3> list = mapper.query().queryForList();
            UserInfo3 u1 = list.get(0);
            UserInfo3 u2 = list.get(1);

            assert mapper.query().queryForCount() == 6;
            assert mapper.deleteList(Arrays.asList(u1, u2)) == 2;
            assert mapper.query().queryForCount() == 4;
            assert mapper.loadBy(u1) == null;
            assert mapper.loadBy(u2) == null;
        }
    }

    @Test
    public void deleteList_twokey_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo3> mapper = s.createBaseMapper(UserInfo3.class);
            List<UserInfo3> list = mapper.query().queryForList();
            UserInfo3 u1 = new UserInfo3();
            u1.setUid(list.get(0).getUid());
            u1.setName(list.get(0).getName());
            UserInfo3 u2 = new UserInfo3();
            u2.setUid(list.get(1).getUid());
            u2.setName(list.get(1).getName());

            assert mapper.query().queryForCount() == 6;
            assert mapper.deleteList(Arrays.asList(u1, u2)) == 2;
            assert mapper.query().queryForCount() == 4;
            assert mapper.loadBy(u1) == null;
            assert mapper.loadBy(u2) == null;
        }
    }

    @Test
    public void deleteList_twokey_3() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo3> mapper = s.createBaseMapper(UserInfo3.class);
            List<UserInfo3> list = mapper.query().queryForList();
            UserInfo3 u1 = new UserInfo3();
            u1.setUid(list.get(0).getUid());
            UserInfo3 u2 = new UserInfo3();
            u2.setUid(list.get(1).getUid());

            assert mapper.query().queryForCount() == 6;
            assert mapper.deleteList(Arrays.asList(u1, u2)) == 0;
            assert mapper.query().queryForCount() == 6;
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
            Map<String, Object> u1 = list.get(0);
            Map<String, Object> u2 = list.get(1);

            assert mapper.query().queryForCount() == 3;
            assert mapper.deleteListByMap(Arrays.asList(u1, u2)) == 2;
            assert mapper.query().queryForCount() == 1;
            assert mapper.loadBy(u1) == null;
            assert mapper.loadBy(u2) == null;
        }
    }

    @Test
    public void deleteListByMap_onekey_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            List<Map<String, Object>> list = mapper.query().queryForMapList();
            Map<String, Object> u1 = new HashMap<>();
            u1.put("uid", list.get(0).get("uid"));
            Map<String, Object> u2 = new HashMap<>();
            u2.put("uid", list.get(1).get("uid"));

            assert mapper.query().queryForCount() == 3;
            assert mapper.deleteListByMap(Arrays.asList(u1, u2)) == 2;
            assert mapper.query().queryForCount() == 1;
            assert mapper.loadBy(u1) == null;
            assert mapper.loadBy(u2) == null;
        }
    }

    @Test
    public void deleteListByMap_twokey_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo3> mapper = s.createBaseMapper(UserInfo3.class);
            List<Map<String, Object>> list = mapper.query().queryForMapList();
            Map<String, Object> u1 = list.get(0);
            Map<String, Object> u2 = list.get(1);

            assert mapper.query().queryForCount() == 6;
            assert mapper.deleteListByMap(Arrays.asList(u1, u2)) == 2;
            assert mapper.query().queryForCount() == 4;
            assert mapper.loadBy(u1) == null;
            assert mapper.loadBy(u2) == null;
        }
    }

    @Test
    public void deleteListByMap_twokey_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo3> mapper = s.createBaseMapper(UserInfo3.class);
            List<Map<String, Object>> list = mapper.query().queryForMapList();
            Map<String, Object> u1 = new HashMap<>();
            u1.put("uid", list.get(0).get("uid"));
            u1.put("name", list.get(0).get("name"));
            Map<String, Object> u2 = new HashMap<>();
            u2.put("uid", list.get(1).get("uid"));
            u2.put("name", list.get(1).get("name"));

            assert mapper.query().queryForCount() == 6;
            assert mapper.deleteListByMap(Arrays.asList(u1, u2)) == 2;
            assert mapper.query().queryForCount() == 4;
            assert mapper.loadBy(u1) == null;
            assert mapper.loadBy(u2) == null;
        }
    }

    @Test
    public void deleteListByMap_twokey_3() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo3> mapper = s.createBaseMapper(UserInfo3.class);
            List<Map<String, Object>> list = mapper.query().queryForMapList();
            Map<String, Object> u1 = new HashMap<>();
            u1.put("uid", list.get(0).get("uid"));
            Map<String, Object> u2 = new HashMap<>();
            u2.put("uid", list.get(1).get("uid"));

            assert mapper.query().queryForCount() == 6;
            assert mapper.deleteListByMap(Arrays.asList(u1, u2)) == 0;
            assert mapper.query().queryForCount() == 6;
        }
    }

    //

    @Test
    public void deleteListUseMap_onekey_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper mapper = s.createBaseMapper(UserInfo2.class);
            List<Map<String, Object>> list = mapper.query().queryForMapList();
            Map<String, Object> u1 = list.get(0);
            Map<String, Object> u2 = list.get(1);

            assert mapper.query().queryForCount() == 3;
            assert mapper.deleteList(Arrays.asList(u1, u2)) == 2;
            assert mapper.query().queryForCount() == 1;
            assert mapper.loadBy(u1) == null;
            assert mapper.loadBy(u2) == null;
        }
    }

    @Test
    public void deleteListUseMap_onekey_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper mapper = s.createBaseMapper(UserInfo2.class);
            List<Map<String, Object>> list = mapper.query().queryForMapList();
            Map<String, Object> u1 = new HashMap<>();
            u1.put("uid", list.get(0).get("uid"));
            Map<String, Object> u2 = new HashMap<>();
            u2.put("uid", list.get(1).get("uid"));

            assert mapper.query().queryForCount() == 3;
            assert mapper.deleteList(Arrays.asList(u1, u2)) == 2;
            assert mapper.query().queryForCount() == 1;
            assert mapper.loadBy(u1) == null;
            assert mapper.loadBy(u2) == null;
        }
    }

    @Test
    public void deleteListUseMap_twokey_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper mapper = s.createBaseMapper(UserInfo3.class);
            List<Map<String, Object>> list = mapper.query().queryForMapList();
            Map<String, Object> u1 = list.get(0);
            Map<String, Object> u2 = list.get(1);

            assert mapper.query().queryForCount() == 6;
            assert mapper.deleteList(Arrays.asList(u1, u2)) == 2;
            assert mapper.query().queryForCount() == 4;
            assert mapper.loadBy(u1) == null;
            assert mapper.loadBy(u2) == null;
        }
    }

    @Test
    public void deleteListUseMap_twokey_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper mapper = s.createBaseMapper(UserInfo3.class);
            List<Map<String, Object>> list = mapper.query().queryForMapList();
            Map<String, Object> u1 = new HashMap<>();
            u1.put("uid", list.get(0).get("uid"));
            u1.put("name", list.get(0).get("name"));
            Map<String, Object> u2 = new HashMap<>();
            u2.put("uid", list.get(1).get("uid"));
            u2.put("name", list.get(1).get("name"));

            assert mapper.query().queryForCount() == 6;
            assert mapper.deleteList(Arrays.asList(u1, u2)) == 2;
            assert mapper.query().queryForCount() == 4;
            assert mapper.loadBy(u1) == null;
            assert mapper.loadBy(u2) == null;
        }
    }

    @Test
    public void deleteListUseMap_twokey_3() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper mapper = s.createBaseMapper(UserInfo3.class);
            List<Map<String, Object>> list = mapper.query().queryForMapList();
            Map<String, Object> u1 = new HashMap<>();
            u1.put("uid", list.get(0).get("uid"));
            Map<String, Object> u2 = new HashMap<>();
            u2.put("uid", list.get(1).get("uid"));

            assert mapper.query().queryForCount() == 6;
            assert mapper.deleteList(Arrays.asList(u1, u2)) == 0;
            assert mapper.query().queryForCount() == 6;
        }
    }

    //

    @Test
    public void badDeleteList_1() throws Exception {
        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            mapper.deleteList(null);
            assert false;
        } catch (NullPointerException e) {
            assert e.getMessage().equals("entityList is null.");
        }

        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            mapper.deleteListByMap(null);
            assert false;
        } catch (NullPointerException e) {
            assert e.getMessage().equals("entityList is null.");
        }
    }

    @Test
    public void badDeleteList_2() throws Exception {
        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo> mapper = s.createBaseMapper(UserInfo.class);
            mapper.deleteList(Arrays.asList(new UserInfo()));
            assert false;
        } catch (UnsupportedOperationException e) {
            assert e.getMessage().endsWith(" missing primary key.");
        }

        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo> mapper = s.createBaseMapper(UserInfo.class);
            mapper.deleteListByMap(Arrays.asList(new HashMap<>()));
            assert false;
        } catch (UnsupportedOperationException e) {
            assert e.getMessage().endsWith(" missing primary key.");
        }
    }

    @Test
    public void badDeleteList_3() throws Exception {
        // for onePk
        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper mapper = s.createBaseMapper(UserInfo2.class);
            UserInfo3 dat = new UserInfo3();
            dat.setLoginName("abc");

            mapper.deleteList(Arrays.asList(dat));
            assert false;
        } catch (ClassCastException e) {
            assert e.getMessage().endsWith("cannot be as " + UserInfo2.class.getName());
        }
        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper mapper = s.createBaseMapper(UserInfo2.class);
            UserInfo3 dat1 = new UserInfo3();
            dat1.setLoginName("abc");
            UserInfo3 dat2 = new UserInfo3();
            dat2.setLoginName("abc");

            mapper.deleteList(Arrays.asList(dat1, dat2));
            assert false;
        } catch (ClassCastException e) {
            assert e.getMessage().endsWith("cannot be as " + UserInfo2.class.getName());
        }

        //
        // for twoPK
        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper mapper = s.createBaseMapper(UserInfo3.class);
            UserInfo2 dat = new UserInfo2();
            dat.setLoginName("abc");

            mapper.deleteList(Arrays.asList(dat));
            assert false;
        } catch (ClassCastException e) {
            assert e.getMessage().endsWith("cannot be as " + UserInfo3.class.getName());
        }
        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper mapper = s.createBaseMapper(UserInfo3.class);
            UserInfo2 dat1 = new UserInfo2();
            dat1.setLoginName("abc");
            UserInfo3 dat2 = new UserInfo3();
            dat2.setLoginName("abc");

            mapper.deleteList(Arrays.asList(dat1, dat2));
            assert false;
        } catch (ClassCastException e) {
            assert e.getMessage().endsWith("cannot be as " + UserInfo3.class.getName());
        }
    }

    @Test
    public void badDeleteList_4() throws Exception {
        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            Map<String, Object> map = new HashMap<>();
            map.put("uid", "abc");

            assert mapper.deleteListByMap(Arrays.asList(map)) == 0;
        }
    }

    @Test
    public void badDeleteList_5() throws Exception {
        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            Map<String, Object> map = new HashMap<>();
            map.put("abc", "abc");

            assert mapper.deleteListByMap(Arrays.asList(map)) == 0;
        } catch (UnsupportedOperationException e) {
            assert e.getMessage().endsWith(" missing primary key.");
        }
    }

    @Test
    public void emptyDelete_1() throws Exception {
        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            assert mapper.deleteList(Collections.emptyList()) == 0;
            assert mapper.deleteListByMap(Collections.emptyList()) == 0;

            assert mapper.deleteList(Arrays.asList(null, null)) == 0;
            assert mapper.deleteListByMap(Arrays.asList(null, null)) == 0;
        }
    }
}
