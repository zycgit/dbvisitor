package net.hasor.dbvisitor.session;
import net.hasor.cobble.time.DateTimeFormat;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.session.dto.UserInfo2;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class BasicMapperInsertTest {
    private static Date passer(String date) throws ParseException {
        if (date == null) {
            return null;
        } else {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
        }
    }

    private static String format(Date date) {
        if (date == null) {
            return null;
        } else {
            ZonedDateTime zonedDateTime = date.toInstant().atZone(ZoneId.systemDefault());
            return DateTimeFormat.WKF_DATE_TIME24.toPattern().format(zonedDateTime);
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
}
