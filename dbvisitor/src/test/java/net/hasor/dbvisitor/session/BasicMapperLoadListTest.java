package net.hasor.dbvisitor.session;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.session.dto.UserInfo;
import net.hasor.dbvisitor.session.dto.UserInfo2;
import net.hasor.dbvisitor.session.dto.UserInfo3;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.util.*;

public class BasicMapperLoadListTest {
    @Test
    public void loadListBy_onekey_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            List<UserInfo2> list = mapper.query().queryForList();
            UserInfo2 u1 = list.get(0);
            UserInfo2 u2 = list.get(1);

            List<UserInfo2> result = mapper.loadListBy(Arrays.asList(u1, u2));
            Map<String, UserInfo2> groupById = new HashMap<>();
            for (UserInfo2 userInfo : result) {
                groupById.put(userInfo.getUid(), userInfo);
            }
            assert groupById.get(u1.getUid()).getLoginName().equals(u1.getLoginName());
            assert groupById.get(u2.getUid()).getLoginName().equals(u2.getLoginName());
        }
    }

    @Test
    public void loadListBy_onekey_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            List<UserInfo2> list = mapper.query().queryForList();
            UserInfo2 u1 = new UserInfo2();
            u1.setUid(list.get(0).getUid());
            u1.setLoginName(list.get(0).getLoginName());
            UserInfo2 u2 = new UserInfo2();
            u2.setUid(list.get(1).getUid());
            u2.setLoginName(list.get(1).getLoginName());

            List<UserInfo2> result = mapper.loadListBy(Arrays.asList(u1, u2));
            Map<String, UserInfo2> groupById = new HashMap<>();
            for (UserInfo2 t : result) {
                groupById.put(t.getUid(), t);
            }
            assert groupById.get(u1.getUid()).getLoginName().equals(u1.getLoginName());
            assert groupById.get(u2.getUid()).getLoginName().equals(u2.getLoginName());
        }
    }

    @Test
    public void loadListBy_onekey_3() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            List<UserInfo2> list = mapper.query().queryForList();
            UserInfo3 u1 = new UserInfo3();
            u1.setUid(list.get(0).getUid());
            u1.setLoginName(list.get(0).getLoginName());
            UserInfo3 u2 = new UserInfo3();
            u2.setUid(list.get(1).getUid());
            u2.setLoginName(list.get(1).getLoginName());

            List<UserInfo2> result = mapper.loadListBy(Arrays.asList(u1, u2));
            Map<String, UserInfo2> groupById = new HashMap<>();
            for (UserInfo2 t : result) {
                groupById.put(t.getUid(), t);
            }
            assert groupById.get(u1.getUid()).getLoginName().equals(u1.getLoginName());
            assert groupById.get(u2.getUid()).getLoginName().equals(u2.getLoginName());
        }
    }

    @Test
    public void loadListBy_twokey_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo3> mapper = s.createBaseMapper(UserInfo3.class);
            List<UserInfo3> list = mapper.query().queryForList();
            UserInfo3 u1 = list.get(0);
            UserInfo3 u2 = list.get(1);

            List<UserInfo3> result = mapper.loadListBy(Arrays.asList(u1, u2));
            Map<String, UserInfo3> groupById = new HashMap<>();
            for (UserInfo3 t : result) {
                groupById.put(t.getUid() + t.getName(), t);
            }
            assert groupById.get(u1.getUid() + u1.getName()).getLoginName().equals(u1.getLoginName());
            assert groupById.get(u2.getUid() + u2.getName()).getLoginName().equals(u2.getLoginName());
        }
    }

    @Test
    public void loadListBy_twokey_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo3> mapper = s.createBaseMapper(UserInfo3.class);
            List<UserInfo3> list = mapper.query().queryForList();
            UserInfo3 u1 = new UserInfo3();
            u1.setUid(list.get(0).getUid());
            u1.setName(list.get(0).getName());
            u1.setLoginName(list.get(0).getLoginName());
            UserInfo3 u2 = new UserInfo3();
            u2.setUid(list.get(1).getUid());
            u2.setName(list.get(1).getName());
            u2.setLoginName(list.get(1).getLoginName());

            List<UserInfo3> result = mapper.loadListBy(Arrays.asList(u1, u2));
            Map<String, UserInfo3> groupById = new HashMap<>();
            for (UserInfo3 t : result) {
                groupById.put(t.getUid() + t.getName(), t);
            }
            assert groupById.get(u1.getUid() + u1.getName()).getLoginName().equals(u1.getLoginName());
            assert groupById.get(u2.getUid() + u2.getName()).getLoginName().equals(u2.getLoginName());
        }
    }

    @Test
    public void loadListBy_twokey_3() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo3> mapper = s.createBaseMapper(UserInfo3.class);
            List<UserInfo3> list = mapper.query().queryForList();
            UserInfo2 u1 = new UserInfo2();
            u1.setUid(list.get(0).getUid());
            u1.setName(list.get(0).getName());
            u1.setLoginName(list.get(0).getLoginName());
            UserInfo2 u2 = new UserInfo2();
            u2.setUid(list.get(1).getUid());
            u2.setName(list.get(1).getName());
            u2.setLoginName(list.get(1).getLoginName());

            List<UserInfo3> result = mapper.loadListBy(Arrays.asList(u1, u2));
            Map<String, UserInfo3> groupById = new HashMap<>();
            for (UserInfo3 t : result) {
                groupById.put(t.getUid() + t.getName(), t);
            }
            assert groupById.get(u1.getUid() + u1.getName()).getLoginName().equals(u1.getLoginName());
            assert groupById.get(u2.getUid() + u2.getName()).getLoginName().equals(u2.getLoginName());
        }
    }

    //

    @Test
    public void loadListByUseMap_onekey_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            List<Map<String, Object>> list = mapper.query().queryForMapList();
            Map<String, Object> u1 = list.get(0);
            Map<String, Object> u2 = list.get(1);

            List<UserInfo2> result = mapper.loadListBy(Arrays.asList(u1, u2));
            Map<String, UserInfo2> groupById = new HashMap<>();
            for (UserInfo2 userInfo : result) {
                groupById.put(userInfo.getUid(), userInfo);
            }
            assert groupById.get(u1.get("uid")).getLoginName().equals(u1.get("loginName"));
            assert groupById.get(u2.get("uid")).getLoginName().equals(u2.get("loginName"));
        }
    }

    @Test
    public void loadListByUseMap_onekey_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            List<Map<String, Object>> list = mapper.query().queryForMapList();
            Map<String, Object> u1 = new HashMap<>();
            u1.put("uid", list.get(0).get("uid"));
            u1.put("loginName", list.get(0).get("loginName"));
            Map<String, Object> u2 = new HashMap<>();
            u2.put("uid", list.get(1).get("uid"));
            u2.put("loginName", list.get(1).get("loginName"));

            List<UserInfo2> result = mapper.loadListBy(Arrays.asList(u1, u2));
            Map<String, UserInfo2> groupById = new HashMap<>();
            for (UserInfo2 userInfo : result) {
                groupById.put(userInfo.getUid(), userInfo);
            }
            assert groupById.get(u1.get("uid")).getLoginName().equals(u1.get("loginName"));
            assert groupById.get(u2.get("uid")).getLoginName().equals(u2.get("loginName"));
        }
    }

    //

    @Test
    public void loadListByUseMap_twokey_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo3> mapper = s.createBaseMapper(UserInfo3.class);
            List<UserInfo3> list = mapper.query().queryForList();
            UserInfo3 u1 = list.get(0);
            UserInfo3 u2 = list.get(1);

            List<UserInfo3> result = mapper.loadListBy(Arrays.asList(u1, u2));
            Map<String, UserInfo3> groupById = new HashMap<>();
            for (UserInfo3 t : result) {
                groupById.put(t.getUid() + t.getName(), t);
            }
            assert groupById.get(u1.getUid() + u1.getName()).getLoginName().equals(u1.getLoginName());
            assert groupById.get(u2.getUid() + u2.getName()).getLoginName().equals(u2.getLoginName());
        }
    }

    @Test
    public void loadListByUseMap_twokey_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo3> mapper = s.createBaseMapper(UserInfo3.class);
            List<Map<String, Object>> list = mapper.query().queryForMapList();
            Map<String, Object> u1 = new HashMap<>();
            u1.put("uid", list.get(0).get("uid"));
            u1.put("name", list.get(0).get("name"));
            u1.put("loginName", list.get(0).get("loginName"));
            Map<String, Object> u2 = new HashMap<>();
            u2.put("uid", list.get(1).get("uid"));
            u2.put("name", list.get(1).get("name"));
            u2.put("loginName", list.get(1).get("loginName"));

            List<UserInfo3> result = mapper.loadListBy(Arrays.asList(u1, u2));
            Map<String, UserInfo3> groupById = new HashMap<>();
            for (UserInfo3 t : result) {
                groupById.put(t.getUid() + t.getName(), t);
            }
            assert groupById.get(u1.get("uid") + "" + u1.get("name")).getLoginName().equals(u1.get("loginName"));
            assert groupById.get(u2.get("uid") + "" + u2.get("name")).getLoginName().equals(u2.get("loginName"));
        }
    }

    //

    @Test
    public void badLoadList_1() throws Exception {
        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            mapper.loadListBy(null);
            assert false;
        } catch (NullPointerException e) {
            assert e.getMessage().equals("refList is null.");
        }
    }

    @Test
    public void badLoadList_2_1() throws Exception {
        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo> mapper = s.createBaseMapper(UserInfo.class);
            mapper.loadListBy(Arrays.asList(new UserInfo()));
            assert false;
        } catch (UnsupportedOperationException e) {
            assert e.getMessage().endsWith(" missing primary key.");
        }

        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo> mapper = s.createBaseMapper(UserInfo.class);
            mapper.loadListBy(Arrays.asList(new UserInfo()));
            assert false;
        } catch (UnsupportedOperationException e) {
            assert e.getMessage().endsWith(" missing primary key.");
        }
    }

    @Test
    public void badLoadList_2_2() throws Exception {
        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo3> mapper = s.createBaseMapper(UserInfo3.class);
            List<Map<String, Object>> list = mapper.query().queryForMapList();
            Map<String, Object> tmp = list.get(0);
            Map<String, Object> u1 = new HashMap<>();
            u1.put("uid", tmp.get("uid"));

            mapper.loadListBy(Arrays.asList(u1));
        } catch (UnsupportedOperationException e) {
            assert e.getMessage().endsWith(" missing primary key.");
        }

        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo3> mapper = s.createBaseMapper(UserInfo3.class);
            List<UserInfo3> list = mapper.query().queryForList();
            UserInfo3 tmp = list.get(0);
            UserInfo u1 = new UserInfo();
            u1.setUserUuid(tmp.getUid());

            mapper.loadListBy(Arrays.asList(u1));
        } catch (UnsupportedOperationException e) {
            assert e.getMessage().endsWith(" missing primary key.");
        }
    }

    @Test
    public void emptyLoadList_1() throws Exception {
        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            assert mapper.loadListBy(Collections.emptyList()).size() == 0;
            assert mapper.loadListBy(Arrays.asList(null, null)).size() == 0;
        }
    }
}
