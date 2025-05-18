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

public class BasicMapperLoadTest {
    @Test
    public void loadBy_onekey_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            List<UserInfo2> list = mapper.query().queryForList();
            UserInfo2 u1 = list.get(0);

            UserInfo2 copy = mapper.loadBy(u1);
            assert copy != u1;
            assert StringUtils.equals(u1.getLoginName(), copy.getLoginName());
            assert StringUtils.equals(u1.getPassword(), copy.getPassword());
        }
    }

    @Test
    public void loadBy_onekey_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            List<UserInfo2> list = mapper.query().queryForList();
            UserInfo2 tmp = list.get(0);
            UserInfo2 u1 = new UserInfo2();
            u1.setUid(tmp.getUid());

            UserInfo2 copy = mapper.loadBy(u1);
            assert copy != tmp;
            assert StringUtils.equals(tmp.getLoginName(), copy.getLoginName());
            assert StringUtils.equals(tmp.getPassword(), copy.getPassword());
        }
    }

    @Test
    public void loadBy_onekey_3() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            List<UserInfo2> list = mapper.query().queryForList();
            UserInfo2 tmp = list.get(0);
            UserInfo3 u1 = new UserInfo3();
            u1.setUid(tmp.getUid());

            UserInfo2 copy = mapper.loadBy(u1);
            assert copy != tmp;
            assert StringUtils.equals(tmp.getLoginName(), copy.getLoginName());
            assert StringUtils.equals(tmp.getPassword(), copy.getPassword());
        }
    }

    @Test
    public void loadBy_twokey_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo3> mapper = s.createBaseMapper(UserInfo3.class);
            List<UserInfo3> list = mapper.query().queryForList();
            UserInfo3 u1 = list.get(0);

            UserInfo3 copy = mapper.loadBy(u1);
            assert copy != u1;
            assert StringUtils.equals(u1.getLoginName(), copy.getLoginName());
            assert StringUtils.equals(u1.getPassword(), copy.getPassword());
        }
    }

    @Test
    public void loadBy_twokey_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo3> mapper = s.createBaseMapper(UserInfo3.class);
            List<UserInfo3> list = mapper.query().queryForList();
            UserInfo3 tmp = list.get(0);
            UserInfo3 u1 = new UserInfo3();
            u1.setUid(tmp.getUid());
            u1.setName(tmp.getName());

            UserInfo3 copy = mapper.loadBy(u1);
            assert copy != tmp;
            assert StringUtils.equals(tmp.getLoginName(), copy.getLoginName());
            assert StringUtils.equals(tmp.getPassword(), copy.getPassword());
        }
    }

    @Test
    public void loadBy_twokey_3() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo3> mapper = s.createBaseMapper(UserInfo3.class);
            List<UserInfo3> list = mapper.query().queryForList();
            UserInfo3 tmp = list.get(0);
            UserInfo2 u1 = new UserInfo2();
            u1.setUid(tmp.getUid());
            u1.setName(tmp.getName());

            UserInfo3 copy = mapper.loadBy(u1);
            assert copy != tmp;
            assert StringUtils.equals(tmp.getLoginName(), copy.getLoginName());
            assert StringUtils.equals(tmp.getPassword(), copy.getPassword());
        }
    }

    @Test
    public void loadBy_twokey_4() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo3> mapper = s.createBaseMapper(UserInfo3.class);
            List<UserInfo3> list = mapper.query().queryForList();
            UserInfo3 tmp = list.get(0);
            UserInfo2 u1 = new UserInfo2();
            u1.setUid(tmp.getUid());

            assert mapper.loadBy(u1) == null;
        }
    }

    //

    @Test
    public void loadByMap_onekey_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            List<Map<String, Object>> list = mapper.query().queryForMapList();
            Map<String, Object> u1 = list.get(0);

            UserInfo2 copy = mapper.loadBy(u1);
            assert copy != u1;
            assert StringUtils.equals((String) u1.get("loginName"), copy.getLoginName());
            assert StringUtils.equals((String) u1.get("password"), copy.getPassword());
        }
    }

    @Test
    public void loadByMap_onekey_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            List<Map<String, Object>> list = mapper.query().queryForMapList();
            Map<String, Object> tmp = list.get(0);
            Map<String, Object> u1 = new HashMap<>();
            u1.put("uid", list.get(0).get("uid"));

            UserInfo2 copy = mapper.loadBy(u1);
            assert StringUtils.equals((String) tmp.get("loginName"), copy.getLoginName());
            assert StringUtils.equals((String) tmp.get("password"), copy.getPassword());
        }
    }

    //

    @Test
    public void loadByMap_twokey_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo3> mapper = s.createBaseMapper(UserInfo3.class);
            List<Map<String, Object>> list = mapper.query().queryForMapList();
            Map<String, Object> u1 = list.get(0);

            UserInfo3 copy = mapper.loadBy(u1);
            assert copy != u1;
            assert StringUtils.equals((String) u1.get("loginName"), copy.getLoginName());
            assert StringUtils.equals((String) u1.get("password"), copy.getPassword());
        }
    }

    @Test
    public void loadByMap_twokey_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo3> mapper = s.createBaseMapper(UserInfo3.class);
            List<Map<String, Object>> list = mapper.query().queryForMapList();
            Map<String, Object> tmp = list.get(0);
            Map<String, Object> u1 = new HashMap<>();
            u1.put("uid", tmp.get("uid"));
            u1.put("name", tmp.get("name"));

            UserInfo3 copy = mapper.loadBy(u1);
            assert StringUtils.equals((String) tmp.get("loginName"), copy.getLoginName());
            assert StringUtils.equals((String) tmp.get("password"), copy.getPassword());
        }
    }

    //

    @Test
    public void badLoad_1() throws Exception {
        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            mapper.loadBy(null);
            assert false;
        } catch (NullPointerException e) {
            assert e.getMessage().equals("refData is null.");
        }
    }

    @Test
    public void badLoad_2_1() throws Exception {
        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo> mapper = s.createBaseMapper(UserInfo.class);
            mapper.loadBy(new UserInfo());
            assert false;
        } catch (UnsupportedOperationException e) {
            assert e.getMessage().endsWith(" missing primary key.");
        }

        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo> mapper = s.createBaseMapper(UserInfo.class);
            mapper.loadBy(new HashMap<>());
            assert false;
        } catch (UnsupportedOperationException e) {
            assert e.getMessage().endsWith(" missing primary key.");
        }
    }

    @Test
    public void badLoad_2_2() throws Exception {
        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo3> mapper = s.createBaseMapper(UserInfo3.class);
            List<Map<String, Object>> list = mapper.query().queryForMapList();
            Map<String, Object> tmp = list.get(0);
            Map<String, Object> u1 = new HashMap<>();
            u1.put("uid", tmp.get("uid"));

            mapper.loadBy(u1);
        } catch (UnsupportedOperationException e) {
            assert e.getMessage().endsWith(" missing primary key.");
        }

        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo3> mapper = s.createBaseMapper(UserInfo3.class);
            List<UserInfo3> list = mapper.query().queryForList();
            UserInfo3 tmp = list.get(0);
            UserInfo u1 = new UserInfo();
            u1.setUserUuid(tmp.getUid());

            mapper.loadBy(u1);
        } catch (UnsupportedOperationException e) {
            assert e.getMessage().endsWith(" missing primary key.");
        }
    }
}
