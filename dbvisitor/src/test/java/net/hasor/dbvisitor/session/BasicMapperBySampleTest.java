package net.hasor.dbvisitor.session;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.session.dto.UserInfo2;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

public class BasicMapperBySampleTest {
    @Test
    public void listBySample_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);

            List<UserInfo2> all = mapper.query().queryForList();
            List<UserInfo2> result = mapper.listBySample(all.get(0));
            assert result.size() == 1;
            assert result.get(0).getUid().equals(all.get(0).getUid());
        }
    }

    @Test
    public void listBySample_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);

            List<UserInfo2> all = mapper.query().queryForList();

            UserInfo2 sample = all.get(0);
            sample.setPassword(null);
            sample.setEmail(null);
            sample.setUid(null);

            List<UserInfo2> result = mapper.listBySample(sample);
            assert result.size() == 1;
            assert result.get(0).getName().equals(all.get(0).getName());
            assert result.get(0).getLoginName().equals(all.get(0).getLoginName());
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

            assert mapper.replace(dat1) == 1;
            assert mapper.replace(dat2) == 1;

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

    //

    @Test
    public void countBySample_1() throws Exception {
        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);

            assert mapper.countAll() == 3;
            assert mapper.countBySample(new UserInfo2()) == 3;
        }
    }

    @Test
    public void countBySample_2() throws Exception {
        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);

            List<UserInfo2> all = mapper.query().queryForList();
            assert mapper.countBySample(all.get(0)) == 1;
        }
    }

    //

    @Test
    public void badCount_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);

            mapper.countBySample(null);
        } catch (NullPointerException e) {
            assert e.getMessage().contains("sample is null");
        }
    }
}
