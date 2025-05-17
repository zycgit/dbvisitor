package net.hasor.dbvisitor.session;
import net.hasor.cobble.WellKnowFormat;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.session.dto.UserInfo;
import net.hasor.dbvisitor.session.dto.UserInfo2;
import net.hasor.dbvisitor.session.dto.UserInfo3;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class BasicMapperTest {
    public static Date passer(String date) throws ParseException {
        if (date == null) {
            return null;
        } else {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
        }
    }

    public static String format(Date date) {
        if (date == null) {
            return null;
        } else {
            ZonedDateTime zonedDateTime = date.toInstant().atZone(ZoneId.systemDefault());
            return WellKnowFormat.WKF_DATE_TIME24.toPattern().format(zonedDateTime);
        }
    }

    @Test
    public void basic_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);

            assert mapper.session() == s;
        }
    }

    @Test
    public void insert_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            mapper.delete().allowEmptyWhere().doDelete();

            UserInfo2 user1 = new UserInfo2();
            user1.setUid("11");
            user1.setName("12");
            user1.setLoginName("13");
            user1.setPassword("14");
            user1.setEmail("15");
            user1.setSeq(16);
            user1.setCreateTime(passer("2021-07-20 12:34:56"));
            UserInfo2 user2 = new UserInfo2();
            user2.setUid("21");
            user2.setName("22");
            user2.setLoginName("23");
            user2.setPassword("24");
            user2.setEmail("25");
            user2.setSeq(26);
            user2.setCreateTime(passer("2021-07-20 12:34:56"));
            int insert = mapper.insert(Arrays.asList(user1, user2));
            assert insert == 2;
            assert mapper.countAll() == 2;

            List<UserInfo2> execute2 = mapper.query().queryForList();
            assert execute2.size() == 2;

            UserInfo2 tbUser1 = execute2.get(0);
            assert tbUser1.getUid().equals("11");
            assert tbUser1.getName().equals("12");
            assert tbUser1.getLoginName().equals("13");
            assert tbUser1.getPassword().equals("14");
            assert tbUser1.getEmail().equals("15");
            assert tbUser1.getSeq() == 16;
            assert format(user1.getCreateTime()).equals(format(tbUser1.getCreateTime()));
            UserInfo2 tbUser2 = execute2.get(1);
            assert tbUser2.getUid().equals("21");
            assert tbUser2.getName().equals("22");
            assert tbUser2.getLoginName().equals("23");
            assert tbUser2.getPassword().equals("24");
            assert tbUser2.getEmail().equals("25");
            assert tbUser2.getSeq() == 26;
            assert format(user2.getCreateTime()).equals(format(tbUser2.getCreateTime()));
        }
    }

    @Test
    public void insert_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            mapper.delete().allowEmptyWhere().doDelete();

            UserInfo2 user1 = new UserInfo2();
            user1.setUid("11");
            user1.setName("12");
            user1.setLoginName("13");
            user1.setPassword("14");
            user1.setEmail("15");
            user1.setSeq(16);
            user1.setCreateTime(passer("2021-07-20 12:34:56"));
            int insert = mapper.insert(user1);
            assert insert == 1;
            assert mapper.countAll() == 1;

            List<UserInfo2> execute2 = mapper.query().queryForList();
            assert execute2.size() == 1;

            UserInfo2 tbUser1 = execute2.get(0);
            assert tbUser1.getUid().equals("11");
            assert tbUser1.getName().equals("12");
            assert tbUser1.getLoginName().equals("13");
            assert tbUser1.getPassword().equals("14");
            assert tbUser1.getEmail().equals("15");
            assert tbUser1.getSeq() == 16;
            assert format(user1.getCreateTime()).equals(format(tbUser1.getCreateTime()));
        }
    }

    @Test
    public void listBySample_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);

            assert mapper.countAll() == 3;

            List<UserInfo2> all1 = mapper.listBySample(null);
            List<UserInfo2> all2 = mapper.query().queryForList();

            List<String> all1Ids = all1.stream().map(UserInfo2::getUid).collect(Collectors.toList());
            List<String> all2Ids = all2.stream().map(UserInfo2::getUid).collect(Collectors.toList());
            all1Ids.removeAll(all2Ids);
            assert all1Ids.isEmpty();
        }
    }

    @Test
    public void listBySample_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);

            assert mapper.countAll() == 3;

            List<UserInfo2> all = mapper.query().queryForList();

            List<UserInfo2> result = mapper.listBySample(all.get(0));
            assert result.size() == 1;
            assert result.get(0).getUid().equals(all.get(0).getUid());
            assert mapper.countBySample(all.get(0)) == 1;
        }
    }

    @Test
    public void listBySample_3() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);

            assert mapper.countAll() == 3;

            List<UserInfo2> all = mapper.query().queryForList();

            UserInfo2 sample = all.get(0);
            sample.setPassword(null);
            sample.setEmail(null);
            sample.setUid(null);

            List<UserInfo2> result = mapper.listBySample(sample);
            assert result.size() == 1;
            assert result.get(0).getName().equals(all.get(0).getName());
            assert result.get(0).getLoginName().equals(all.get(0).getLoginName());
            assert mapper.countBySample(sample) == 1;
        }
    }

    @Test
    public void listBySample_4() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);

            assert mapper.countAll() == 3;

            List<UserInfo2> all = mapper.query().queryForList();

            UserInfo2 dat1 = all.get(0);
            UserInfo2 dat2 = all.get(1);
            dat1.setEmail("abc");
            dat2.setEmail("abc");

            assert mapper.update(dat1) == 1;
            assert mapper.update(dat2) == 1;

            UserInfo2 sample = new UserInfo2();
            sample.setEmail("abc");
            List<UserInfo2> result = mapper.listBySample(sample);

            assert result.size() == 2;
            assert result.get(0).getEmail().equals("abc");
            assert result.get(1).getEmail().equals("abc");
            List<String> all1Ids = result.stream().map(UserInfo2::getUid).collect(Collectors.toList());
            assert all1Ids.contains(dat1.getUid());
            assert all1Ids.contains(dat2.getUid());
        }
    }

    @Test
    public void updateById_onekey() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            List<UserInfo2> list = mapper.query().queryForList();
            assert list.size() == 3;

            UserInfo2 userInfo = list.get(0);
            userInfo.setLoginName("11");
            userInfo.setPassword("12");

            assert mapper.update(userInfo) == 1;
            assert mapper.countAll() == 3;

            UserInfo2 tbUser1 = mapper.selectById(userInfo.getUid());
            assert tbUser1 != userInfo;
            assert tbUser1.getLoginName().equals(userInfo.getLoginName());
            assert tbUser1.getPassword().equals(userInfo.getPassword());
            assert tbUser1.getSeq().equals(userInfo.getSeq());
            assert tbUser1.getEmail().equals(userInfo.getEmail());
            assert tbUser1.getCreateTime().equals(userInfo.getCreateTime());
            assert tbUser1.getUid().equals(userInfo.getUid());
        }
    }

    @Test
    public void updateById_twokey() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo3> mapper = s.createBaseMapper(UserInfo3.class);
            List<UserInfo3> list = mapper.query().queryForList();
            UserInfo3 userInfo = list.get(0);

            //
            userInfo.setLoginName("$$$loginName$$$");
            mapper.update(userInfo);

            //
            UserInfo3 q1 = new UserInfo3();
            q1.setUid(userInfo.getUid());
            q1.setName(userInfo.getName());
            UserInfo3 q1res = mapper.selectById(q1);
            assert q1res != null;
            assert q1res.getLoginName().equals(userInfo.getLoginName());
        }
    }

    @Test
    public void updateMapById_onekey() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            List<Map<String, Object>> list = mapper.query().queryForMapList();
            assert list.size() == 3;

            Map<String, Object> userInfo = list.get(0);
            userInfo.put("loginName", "11");
            userInfo.put("password", "12");

            assert mapper.updateByMap(userInfo) == 1;
            assert mapper.countAll() == 3;

            UserInfo2 tbUser1 = mapper.selectById((Serializable) userInfo.get("uid"));
            assert tbUser1 != userInfo;
            assert tbUser1.getLoginName().equals(userInfo.get("loginName"));
            assert tbUser1.getPassword().equals(userInfo.get("password"));
            assert tbUser1.getSeq().equals(userInfo.get("seq"));
            assert tbUser1.getEmail().equals(userInfo.get("email"));
            assert tbUser1.getCreateTime().equals(userInfo.get("createTime"));
            assert tbUser1.getUid().equals((Serializable) userInfo.get("uid"));
        }
    }

    @Test
    public void updateMapById_twokey() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo3> mapper = s.createBaseMapper(UserInfo3.class);
            List<Map<String, Object>> list = mapper.query().queryForMapList();
            Map<String, Object> userInfo = list.get(0);

            //
            userInfo.put("loginName", "$$$loginName$$$");
            mapper.updateByMap(userInfo);

            //
            UserInfo3 q1 = new UserInfo3();
            q1.setUid((String) userInfo.get("uid"));
            q1.setName((String) userInfo.get("name"));
            UserInfo3 q1res = mapper.selectById(q1);
            assert q1res != null;
            assert q1res.getLoginName().equals(userInfo.get("loginName"));
        }
    }

    @Test
    public void upsertById_update_onekey() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            List<UserInfo2> list = mapper.query().queryForList();
            assert mapper.query().queryForCount() == 3;
            UserInfo2 userInfo = list.get(0);

            //
            userInfo.setLoginName("$$$loginName$$$");
            mapper.upsert(userInfo);
            assert mapper.query().queryForCount() == 3;

            //
            UserInfo2 q1 = new UserInfo2();
            q1.setUid(userInfo.getUid());
            q1.setName(userInfo.getName());
            UserInfo2 q1res = mapper.selectById(q1);
            assert q1res != null;
            assert q1res.getLoginName().equals(userInfo.getLoginName());
        }
    }

    @Test
    public void upsertById_insert_onekey() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            List<UserInfo2> list = mapper.query().queryForList();
            assert mapper.query().queryForCount() == 3;
            UserInfo2 userInfo = list.get(0);

            //
            userInfo.setUid("1");
            userInfo.setLoginName("$$$loginName$$$");
            mapper.upsert(userInfo);
            assert mapper.query().queryForCount() == 4;

            //
            UserInfo2 q1 = new UserInfo2();
            q1.setUid(userInfo.getUid());
            q1.setName(userInfo.getName());
            UserInfo2 q1res = mapper.selectById(q1);
            assert q1res != null;
            assert q1res.getLoginName().equals(userInfo.getLoginName());
        }
    }

    @Test
    public void upsertById_update_twokey() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo3> mapper = s.createBaseMapper(UserInfo3.class);
            List<UserInfo3> all = mapper.query().queryForList();
            UserInfo3 userInfo = all.get(0);
            assert all.size() == 6;

            UserInfo3 user1 = new UserInfo3();
            user1.setUid(userInfo.getUid());
            user1.setName(userInfo.getName());
            user1.setLoginName("2");
            user1.setPassword("3");
            user1.setEmail("4");
            user1.setSeq(5);
            user1.setCreateTime(passer("2021-07-20 12:34:56"));
            assert mapper.upsert(user1) == 1;
            assert mapper.query().queryForCount() == 6;

            //
            UserInfo3 tbUser1 = mapper.selectById(user1);
            assert tbUser1.getUid().equals(user1.getUid());
            assert tbUser1.getName().equals(user1.getName());
            assert tbUser1.getLoginName().equals(user1.getLoginName());
            assert tbUser1.getPassword().equals(user1.getPassword());
            assert tbUser1.getEmail().equals(user1.getEmail());
            assert tbUser1.getSeq().equals(user1.getSeq());
        }
    }

    @Test
    public void upsertById_insert_twokey() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo3> mapper = s.createBaseMapper(UserInfo3.class);
            List<UserInfo3> all = mapper.query().queryForList();
            UserInfo3 userInfo = all.get(0);
            assert all.size() == 6;

            UserInfo3 user1 = new UserInfo3();
            user1.setUid(userInfo.getUid());
            user1.setName("qwe");
            user1.setLoginName("2");
            user1.setPassword("3");
            user1.setEmail("4");
            user1.setSeq(5);
            user1.setCreateTime(passer("2021-07-20 12:34:56"));
            assert mapper.upsert(user1) == 1;
            assert mapper.query().queryForCount() == 7;

            //
            UserInfo3 tbUser1 = mapper.selectById(user1);
            assert tbUser1.getUid().equals(user1.getUid());
            assert tbUser1.getName().equals(user1.getName());
            assert tbUser1.getLoginName().equals(user1.getLoginName());
            assert tbUser1.getPassword().equals(user1.getPassword());
            assert tbUser1.getEmail().equals(user1.getEmail());
            assert tbUser1.getSeq().equals(user1.getSeq());
        }
    }

    @Test
    public void updateByMap_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);

            assert mapper.countAll() == 3;
            List<UserInfo2> all = mapper.query().queryForList();
            UserInfo2 userInfo = all.get(0);

            Map<String, Object> user1 = new HashMap<>();
            user1.put("uid", userInfo.getUid());
            user1.put("name", "12");
            user1.put("loginName", "13");
            user1.put("password", "14");
            user1.put("seq", 16);
            user1.put("createTime", passer("2021-07-20 12:34:56"));

            assert mapper.updateByMap(user1) == 1;
            assert mapper.countAll() == 3;

            UserInfo2 tbUser1 = mapper.selectById(userInfo.getUid());
            assert tbUser1.getUid().equals(userInfo.getUid());
            assert tbUser1.getName().equals("12");
            assert tbUser1.getLoginName().equals("13");
            assert tbUser1.getPassword().equals("14");
            assert tbUser1.getEmail().equals(userInfo.getEmail());
            assert tbUser1.getSeq() == 16;
        }
    }

    @Test
    public void deleteObject_onekey() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            List<UserInfo2> list = mapper.query().queryForList();
            assert list.size() == 3;

            assert mapper.delete(list.get(0)) == 1;

            assert mapper.countAll() == 2;
            assert mapper.selectById(list.get(0).getUid()) == null;
        }
    }

    @Test
    public void deleteObject_twokey() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo3> mapper = s.createBaseMapper(UserInfo3.class);
            List<UserInfo3> list = mapper.query().queryForList();
            assert list.size() == 6;

            assert mapper.delete(list.get(0)) == 1;

            assert mapper.countAll() == 5;
            assert mapper.selectById(list.get(0).getUid()) == null;
        }
    }

    @Test
    public void deleteMap_onekey() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            List<UserInfo2> list = mapper.query().queryForList();
            assert list.size() == 3;

            Map<String, Object> data = new HashMap<>();
            data.put("uid", list.get(0).getUid());
            assert mapper.deleteByMap(data) == 1;

            assert mapper.countAll() == 2;
            assert mapper.selectById(list.get(0).getUid()) == null;
        }
    }

    @Test
    public void deleteMap_twokey() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo3> mapper = s.createBaseMapper(UserInfo3.class);
            List<UserInfo3> list = mapper.query().queryForList();
            assert list.size() == 6;

            HashMap<String, Object> data = new HashMap<>();
            data.put("uid", list.get(0).getUid());
            data.put("name", list.get(0).getName());
            assert mapper.deleteByMap(data) == 1;

            assert mapper.countAll() == 5;
        }
    }

    @Test
    public void deleteById_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);

            assert mapper.countAll() == 3;
            List<UserInfo2> all = mapper.query().queryForList();

            assert mapper.deleteById(all.get(0).getUid()) == 1;

            assert mapper.countAll() == 2;
            assert mapper.selectById(all.get(0).getUid()) == null;
        }
    }

    @Test
    public void deleteById_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);

            assert mapper.countAll() == 3;
            List<UserInfo2> all = mapper.query().queryForList();

            assert mapper.deleteByIds(all.stream().map(UserInfo2::getUid).collect(Collectors.toList())) == 3;

            assert mapper.countAll() == 0;
        }
    }

    @Test
    public void selectById_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);

            assert mapper.countAll() == 3;
            List<UserInfo2> all = mapper.query().queryForList();

            assert mapper.selectById(all.get(0).getUid()).getUid().equals(all.get(0).getUid());
        }
    }

    @Test
    public void selectById_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo3> mapper = s.createBaseMapper(UserInfo3.class);
            List<UserInfo3> list = mapper.query().queryForList();
            UserInfo3 userInfo = list.get(0);

            UserInfo3 q1 = new UserInfo3();
            q1.setUid(userInfo.getUid());
            q1.setName(userInfo.getName());
            UserInfo3 q1res = mapper.selectById(q1);
            assert q1res != null;

            //
            UserInfo3 q2 = new UserInfo3();
            q2.setUid(userInfo.getUid());
            UserInfo3 q2res = mapper.selectById(q2);
            assert q2res == null;
        }
    }

    @Test
    public void selectById_3() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);

            assert mapper.countAll() == 3;
            List<UserInfo2> all = mapper.query().queryForList();
            List<UserInfo2> all2 = mapper.selectByIds(all.stream().map(UserInfo2::getUid).collect(Collectors.toList()));

            List<String> all2Str = all2.stream().map(UserInfo2::getUid).collect(Collectors.toList());
            assert all2Str.contains(all.get(0).getUid());
            assert all2Str.contains(all.get(1).getUid());
            assert all2Str.contains(all.get(2).getUid());
        }
    }

    @Test
    public void badUpdate_1() throws Exception {
        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            mapper.update(null);
            assert false;
        } catch (NullPointerException e) {
            assert e.getMessage().equals("entity is null.");
        }

        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            mapper.updateByMap(null);
            assert false;
        } catch (NullPointerException e) {
            assert e.getMessage().equals("map is null.");
        }
    }

    @Test
    public void badUpdate_2() throws Exception {
        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo> mapper = s.createBaseMapper(UserInfo.class);
            mapper.update(new UserInfo());
            assert false;
        } catch (UnsupportedOperationException e) {
            assert e.getMessage().endsWith("no primary key is identified");
        }

        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo> mapper = s.createBaseMapper(UserInfo.class);
            mapper.updateByMap(new HashMap<>());
            assert false;
        } catch (UnsupportedOperationException e) {
            assert e.getMessage().endsWith("no primary key is identified");
        }
    }

    @Test
    public void badUpdate_3() throws Exception {
        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper mapper = s.createBaseMapper(UserInfo2.class);
            Map<String, Object> map = new HashMap<>();
            map.put("loginName", "abc");

            mapper.update(map);
            assert false;
        } catch (ClassCastException e) {
            assert e.getMessage().startsWith("entity is not ");
        }
    }

    @Test
    public void badUpdate_4() throws Exception {
        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            Map<String, Object> map = new HashMap<>();
            map.put("loginName", "abc");

            mapper.updateByMap(map);
            assert false;
        } catch (UnsupportedOperationException e) {
            assert e.getMessage().endsWith("missing primary key.");
        }

        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            Map<String, Object> map = new HashMap<>();
            map.put("uid", null);

            mapper.updateByMap(map);
            assert false;
        } catch (UnsupportedOperationException e) {
            assert e.getMessage().endsWith("update is empty.");
        }
    }

    @Test
    public void badUpsert_1() throws Exception {
        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            mapper.upsert(null);
            assert false;
        } catch (NullPointerException e) {
            assert e.getMessage().equals("entity is null.");
        }
    }

    @Test
    public void badUpsert_2() throws Exception {
        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo> mapper = s.createBaseMapper(UserInfo.class);
            mapper.upsert(new UserInfo());
            assert false;
        } catch (UnsupportedOperationException e) {
            assert e.getMessage().endsWith("no primary key is identified");
        }
    }

    @Test
    public void badUpsert_3() throws Exception {
        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper mapper = s.createBaseMapper(UserInfo2.class);
            Map<String, Object> map = new HashMap<>();
            map.put("loginName", "abc");

            mapper.upsert(map);
            assert false;
        } catch (ClassCastException e) {
            assert e.getMessage().startsWith("entity is not ");
        }
    }

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
            assert e.getMessage().equals("map is null.");
        }
    }

    @Test
    public void badDelete_2() throws Exception {
        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo> mapper = s.createBaseMapper(UserInfo.class);
            mapper.delete(new UserInfo());
            assert false;
        } catch (UnsupportedOperationException e) {
            assert e.getMessage().endsWith("no primary key is identified");
        }

        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo> mapper = s.createBaseMapper(UserInfo.class);
            mapper.deleteByMap(new HashMap<>());
            assert false;
        } catch (UnsupportedOperationException e) {
            assert e.getMessage().endsWith("no primary key is identified");
        }
    }

    @Test
    public void badDelete_3() throws Exception {
        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper mapper = s.createBaseMapper(UserInfo2.class);
            Map<String, Object> map = new HashMap<>();
            map.put("loginName", "abc");

            mapper.delete(map);
            assert false;
        } catch (ClassCastException e) {
            assert e.getMessage().startsWith("entity is not ");
        }
    }

    @Test
    public void badDelete_4() throws Exception {
        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            Map<String, Object> map = new HashMap<>();
            map.put("loginName", "abc");

            mapper.deleteByMap(map);
            assert false;
        } catch (UnsupportedOperationException e) {
            assert e.getMessage().endsWith("missing primary key.");
        }
    }
}
