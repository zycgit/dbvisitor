package net.hasor.dbvisitor.dal.session;
import net.hasor.cobble.WellKnowFormat;
import net.hasor.dbvisitor.dal.mapper.BaseMapper;
import net.hasor.dbvisitor.dal.repository.DalRegistry;
import net.hasor.dbvisitor.mapping.MappingOptions;
import net.hasor.test.dto.UserInfo2;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
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
        try (Connection con = DsUtils.h2Conn()) {
            DalRegistry dalRegistry = new DalRegistry(MappingOptions.buildNew().mapUnderscoreToCamelCase(true));
            DalSession dalSession = new DalSession(con, dalRegistry);
            BaseMapper<UserInfo2> mapper = dalSession.createBaseMapper(UserInfo2.class);

            assert mapper.getSession() == dalSession;
        }
    }

    @Test
    public void insert_1() throws Exception {
        try (Connection con = DsUtils.h2Conn()) {
            DalRegistry dalRegistry = new DalRegistry(MappingOptions.buildNew().mapUnderscoreToCamelCase(true));
            DalSession dalSession = new DalSession(con, dalRegistry);
            BaseMapper<UserInfo2> mapper = dalSession.createBaseMapper(UserInfo2.class);
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
        try (Connection con = DsUtils.h2Conn()) {
            DalRegistry dalRegistry = new DalRegistry(MappingOptions.buildNew().mapUnderscoreToCamelCase(true));
            DalSession dalSession = new DalSession(con, dalRegistry);
            BaseMapper<UserInfo2> mapper = dalSession.createBaseMapper(UserInfo2.class);
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
        try (Connection con = DsUtils.h2Conn()) {
            DalRegistry dalRegistry = new DalRegistry(MappingOptions.buildNew().mapUnderscoreToCamelCase(true));
            DalSession dalSession = new DalSession(con, dalRegistry);
            BaseMapper<UserInfo2> mapper = dalSession.createBaseMapper(UserInfo2.class);

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
        try (Connection con = DsUtils.h2Conn()) {
            DalRegistry dalRegistry = new DalRegistry(MappingOptions.buildNew().mapUnderscoreToCamelCase(true));
            DalSession dalSession = new DalSession(con, dalRegistry);
            BaseMapper<UserInfo2> mapper = dalSession.createBaseMapper(UserInfo2.class);

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
        try (Connection con = DsUtils.h2Conn()) {
            DalRegistry dalRegistry = new DalRegistry(MappingOptions.buildNew().mapUnderscoreToCamelCase(true));
            DalSession dalSession = new DalSession(con, dalRegistry);
            BaseMapper<UserInfo2> mapper = dalSession.createBaseMapper(UserInfo2.class);

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
        try (Connection con = DsUtils.h2Conn()) {
            DalRegistry dalRegistry = new DalRegistry(MappingOptions.buildNew().mapUnderscoreToCamelCase(true));
            DalSession dalSession = new DalSession(con, dalRegistry);
            BaseMapper<UserInfo2> mapper = dalSession.createBaseMapper(UserInfo2.class);

            assert mapper.countAll() == 3;

            List<UserInfo2> all = mapper.query().queryForList();

            UserInfo2 dat1 = all.get(0);
            UserInfo2 dat2 = all.get(1);
            dat1.setEmail("abc");
            dat2.setEmail("abc");

            assert mapper.updateById(dat1) == 1;
            assert mapper.updateById(dat2) == 1;

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
    public void upsertById_1() throws Exception {
        try (Connection con = DsUtils.h2Conn()) {
            DalRegistry dalRegistry = new DalRegistry(MappingOptions.buildNew().mapUnderscoreToCamelCase(true));
            DalSession dalSession = new DalSession(con, dalRegistry);
            BaseMapper<UserInfo2> mapper = dalSession.createBaseMapper(UserInfo2.class);

            assert mapper.countAll() == 3;

            UserInfo2 user1 = new UserInfo2();
            user1.setUid("11");
            user1.setName("12");
            user1.setLoginName("13");
            user1.setPassword("14");
            user1.setEmail("15");
            user1.setSeq(16);
            user1.setCreateTime(passer("2021-07-20 12:34:56"));

            assert mapper.upsertById(user1) == 1;
            assert mapper.countAll() == 4;

            UserInfo2 tbUser1 = mapper.selectById("11");
            assert tbUser1.getUid().equals("11");
            assert tbUser1.getName().equals("12");
            assert tbUser1.getLoginName().equals("13");
            assert tbUser1.getPassword().equals("14");
            assert tbUser1.getEmail().equals("15");
            assert tbUser1.getSeq() == 16;
        }
    }

    @Test
    public void upsertById_2() throws Exception {
        try (Connection con = DsUtils.h2Conn()) {
            DalRegistry dalRegistry = new DalRegistry(MappingOptions.buildNew().mapUnderscoreToCamelCase(true));
            DalSession dalSession = new DalSession(con, dalRegistry);
            BaseMapper<UserInfo2> mapper = dalSession.createBaseMapper(UserInfo2.class);

            assert mapper.countAll() == 3;
            List<UserInfo2> all = mapper.query().queryForList();
            UserInfo2 userInfo = all.get(0);

            UserInfo2 user1 = new UserInfo2();
            user1.setUid(userInfo.getUid());
            user1.setName("12");
            user1.setLoginName("13");
            user1.setPassword("14");
            user1.setSeq(16);
            user1.setCreateTime(passer("2021-07-20 12:34:56"));

            assert mapper.upsertById(user1) == 1;
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
    public void updateByMap_1() throws Exception {
        try (Connection con = DsUtils.h2Conn()) {
            DalRegistry dalRegistry = new DalRegistry(MappingOptions.buildNew().mapUnderscoreToCamelCase(true));
            DalSession dalSession = new DalSession(con, dalRegistry);
            BaseMapper<UserInfo2> mapper = dalSession.createBaseMapper(UserInfo2.class);

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
    public void deleteById_1() throws Exception {
        try (Connection con = DsUtils.h2Conn()) {
            DalRegistry dalRegistry = new DalRegistry(MappingOptions.buildNew().mapUnderscoreToCamelCase(true));
            DalSession dalSession = new DalSession(con, dalRegistry);
            BaseMapper<UserInfo2> mapper = dalSession.createBaseMapper(UserInfo2.class);

            assert mapper.countAll() == 3;
            List<UserInfo2> all = mapper.query().queryForList();

            assert mapper.deleteById(all.get(0).getUid()) == 1;

            assert mapper.countAll() == 2;
            assert mapper.selectById(all.get(0).getUid()) == null;
        }
    }

    @Test
    public void deleteById_2() throws Exception {
        try (Connection con = DsUtils.h2Conn()) {
            DalRegistry dalRegistry = new DalRegistry(MappingOptions.buildNew().mapUnderscoreToCamelCase(true));
            DalSession dalSession = new DalSession(con, dalRegistry);
            BaseMapper<UserInfo2> mapper = dalSession.createBaseMapper(UserInfo2.class);

            assert mapper.countAll() == 3;
            List<UserInfo2> all = mapper.query().queryForList();

            assert mapper.deleteByIds(all.stream().map(UserInfo2::getUid).collect(Collectors.toList())) == 3;

            assert mapper.countAll() == 0;
        }
    }

    @Test
    public void delete_1() throws Exception {
        try (Connection con = DsUtils.h2Conn()) {
            DalRegistry dalRegistry = new DalRegistry(MappingOptions.buildNew().mapUnderscoreToCamelCase(true));
            DalSession dalSession = new DalSession(con, dalRegistry);
            BaseMapper<UserInfo2> mapper = dalSession.createBaseMapper(UserInfo2.class);

            assert mapper.countAll() == 3;
            List<UserInfo2> all = mapper.query().queryForList();

            assert mapper.delete(all.get(0)) == 1;
            assert mapper.countAll() == 2;
            assert mapper.selectById(all.get(0).getUid()) == null;
        }
    }

    @Test
    public void selectById_1() throws Exception {
        try (Connection con = DsUtils.h2Conn()) {
            DalRegistry dalRegistry = new DalRegistry(MappingOptions.buildNew().mapUnderscoreToCamelCase(true));
            DalSession dalSession = new DalSession(con, dalRegistry);
            BaseMapper<UserInfo2> mapper = dalSession.createBaseMapper(UserInfo2.class);

            assert mapper.countAll() == 3;
            List<UserInfo2> all = mapper.query().queryForList();

            assert mapper.selectById(all.get(0).getUid()).getUid().equals(all.get(0).getUid());
        }
    }

    @Test
    public void selectById_2() throws Exception {
        try (Connection con = DsUtils.h2Conn()) {
            DalRegistry dalRegistry = new DalRegistry(MappingOptions.buildNew().mapUnderscoreToCamelCase(true));
            DalSession dalSession = new DalSession(con, dalRegistry);
            BaseMapper<UserInfo2> mapper = dalSession.createBaseMapper(UserInfo2.class);

            assert mapper.countAll() == 3;
            List<UserInfo2> all = mapper.query().queryForList();
            List<UserInfo2> all2 = mapper.selectByIds(all.stream().map(UserInfo2::getUid).collect(Collectors.toList()));

            List<String> all2Str = all2.stream().map(UserInfo2::getUid).collect(Collectors.toList());
            assert all2Str.contains(all.get(0).getUid());
            assert all2Str.contains(all.get(1).getUid());
            assert all2Str.contains(all.get(2).getUid());
        }
    }
}
