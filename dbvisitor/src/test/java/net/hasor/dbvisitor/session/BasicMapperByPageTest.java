package net.hasor.dbvisitor.session;
import net.hasor.cobble.CollectionUtils;
import net.hasor.dbvisitor.dialect.PageObject;
import net.hasor.dbvisitor.dialect.PageResult;
import net.hasor.dbvisitor.dialect.SqlDialectRegister;
import net.hasor.dbvisitor.lambda.core.OrderType;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.session.dto.UserInfo2;
import net.hasor.dbvisitor.session.dto.UserInfo3;
import net.hasor.test.utils.DsUtils;
import static net.hasor.test.utils.TestUtils.INSERT_ARRAY;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BasicMapperByPageTest {

    private Session initPageData(Session s, int count) throws Exception {
        Object[][] arrayData = new Object[count][];
        for (int i = 0; i < count; i++) {
            arrayData[i] = new Object[] {//
                    "id_" + i,        // user_uuid
                    "wuguang_" + i,   // user_name
                    "login_" + i % 7, // login_name  0~6
                    "pwd_" + i,       // login_password
                    "wuguang" + i + "@hasor.net", // email
                    i,                // seq
                    new Date()        // register_time
            };
        }
        s.jdbc().execute("delete from user_info;");
        s.jdbc().executeBatch(INSERT_ARRAY, arrayData);
        return s;
    }

    @Test
    public void pageBySample_asc() throws Exception {
        Options option = Options.of().dialect(SqlDialectRegister.findOrCreate("h2")).mapUnderscoreToCamelCase(true);
        Configuration config = new Configuration(option);
        try (Session s = initPageData(config.newSession(DsUtils.h2Conn()), 8)) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);

            UserInfo2 sample = new UserInfo2();
            assert mapper.countBySample(sample) == mapper.countAll();

            //
            PageObject pageInfo = new PageObject(0, 3);

            // page1
            PageResult<UserInfo2> page1 = mapper.pageBySample(sample, pageInfo, CollectionUtils.asMap("uid", OrderType.ASC), null);
            List<UserInfo2> data1 = page1.getData();
            List<String> nameSet1 = data1.stream().map(UserInfo2::getName).collect(Collectors.toList());
            assert nameSet1.size() == 3;
            assert nameSet1.get(0).equals("wuguang_0");
            assert nameSet1.get(1).equals("wuguang_1");
            assert nameSet1.get(2).equals("wuguang_2");

            // page2
            pageInfo.nextPage();
            PageResult<UserInfo2> page2 = mapper.pageBySample(sample, pageInfo, CollectionUtils.asMap("uid", OrderType.ASC), null);
            List<UserInfo2> data2 = page2.getData();
            List<String> nameSet2 = data2.stream().map(UserInfo2::getName).collect(Collectors.toList());
            assert nameSet2.size() == 3;
            assert nameSet2.get(0).equals("wuguang_3");
            assert nameSet2.get(1).equals("wuguang_4");
            assert nameSet2.get(2).equals("wuguang_5");

            // page3
            pageInfo.nextPage();
            PageResult<UserInfo2> page3 = mapper.pageBySample(sample, pageInfo, CollectionUtils.asMap("uid", OrderType.ASC), null);
            List<UserInfo2> data3 = page3.getData();
            List<String> nameSet3 = data3.stream().map(UserInfo2::getName).collect(Collectors.toList());
            assert nameSet3.size() == 2;
            assert nameSet3.get(0).equals("wuguang_6");
            assert nameSet3.get(1).equals("wuguang_7");
        }
    }

    @Test
    public void pageBySample_desc() throws Exception {
        Options option = Options.of().dialect(SqlDialectRegister.findOrCreate("h2")).mapUnderscoreToCamelCase(true);
        Configuration config = new Configuration(option);
        try (Session s = initPageData(config.newSession(DsUtils.h2Conn()), 8)) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);

            UserInfo2 sample = new UserInfo2();
            assert mapper.countBySample(sample) == mapper.countAll();

            //
            PageObject pageInfo = new PageObject(0, 3);

            // page1
            PageResult<UserInfo2> page1 = mapper.pageBySample(sample, pageInfo, CollectionUtils.asMap("uid", OrderType.DESC), null);
            List<UserInfo2> data1 = page1.getData();
            List<String> nameSet1 = data1.stream().map(UserInfo2::getName).collect(Collectors.toList());
            assert nameSet1.size() == 3;
            assert nameSet1.get(0).equals("wuguang_7");
            assert nameSet1.get(1).equals("wuguang_6");
            assert nameSet1.get(2).equals("wuguang_5");

            // page2
            pageInfo.nextPage();
            PageResult<UserInfo2> page2 = mapper.pageBySample(sample, pageInfo, CollectionUtils.asMap("uid", OrderType.DESC), null);
            List<UserInfo2> data2 = page2.getData();
            List<String> nameSet2 = data2.stream().map(UserInfo2::getName).collect(Collectors.toList());
            assert nameSet2.size() == 3;
            assert nameSet2.get(0).equals("wuguang_4");
            assert nameSet2.get(1).equals("wuguang_3");
            assert nameSet2.get(2).equals("wuguang_2");

            // page3
            pageInfo.nextPage();
            PageResult<UserInfo2> page3 = mapper.pageBySample(sample, pageInfo, CollectionUtils.asMap("uid", OrderType.DESC), null);
            List<UserInfo2> data3 = page3.getData();
            List<String> nameSet3 = data3.stream().map(UserInfo2::getName).collect(Collectors.toList());
            assert nameSet3.size() == 2;
            assert nameSet3.get(0).equals("wuguang_1");
            assert nameSet3.get(1).equals("wuguang_0");
        }
    }

    //

    @Test
    public void pageBySample_where_1() throws Exception {
        Options option = Options.of().dialect(SqlDialectRegister.findOrCreate("h2")).mapUnderscoreToCamelCase(true);
        Configuration config = new Configuration(option);
        try (Session s = initPageData(config.newSession(DsUtils.h2Conn()), 50)) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);

            UserInfo2 sample = new UserInfo2();
            sample.setLoginName("login_4");
            assert mapper.countBySample(sample) == 7;

            //
            PageObject pageInfo = new PageObject(0, 3);

            // page1
            PageResult<UserInfo2> page1 = mapper.pageBySample(sample, pageInfo, CollectionUtils.asMap("uid", OrderType.DESC), null);
            List<UserInfo2> data1 = page1.getData();
            List<String> nameSet1 = data1.stream().map(UserInfo2::getName).collect(Collectors.toList());
            assert nameSet1.size() == 3;
            assert nameSet1.get(0).equals("wuguang_46");
            assert nameSet1.get(1).equals("wuguang_4");
            assert nameSet1.get(2).equals("wuguang_39");

            // page2
            pageInfo.nextPage();
            PageResult<UserInfo2> page2 = mapper.pageBySample(sample, pageInfo, CollectionUtils.asMap("uid", OrderType.ASC), null);
            List<UserInfo2> data2 = page2.getData();
            List<String> nameSet2 = data2.stream().map(UserInfo2::getName).collect(Collectors.toList());
            assert nameSet2.size() == 3;
            assert nameSet2.get(0).equals("wuguang_32");
            assert nameSet2.get(1).equals("wuguang_39");
            assert nameSet2.get(2).equals("wuguang_4");
        }
    }

    @Test
    public void pageBySample_where_2() throws Exception {
        Options option = Options.of().dialect(SqlDialectRegister.findOrCreate("h2")).mapUnderscoreToCamelCase(true);
        Configuration config = new Configuration(option);
        try (Session s = initPageData(config.newSession(DsUtils.h2Conn()), 50)) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);

            Map<String, Object> sample = new HashMap<>();
            sample.put("loginName", "login_4");
            assert mapper.countBySample(sample) == 7;

            //
            PageObject pageInfo = new PageObject(0, 3);

            // page1
            PageResult<UserInfo2> page1 = mapper.pageBySample(sample, pageInfo, CollectionUtils.asMap("uid", OrderType.DESC), null);
            List<UserInfo2> data1 = page1.getData();
            List<String> nameSet1 = data1.stream().map(UserInfo2::getName).collect(Collectors.toList());
            assert nameSet1.size() == 3;
            assert nameSet1.get(0).equals("wuguang_46");
            assert nameSet1.get(1).equals("wuguang_4");
            assert nameSet1.get(2).equals("wuguang_39");

            // page2
            pageInfo.nextPage();
            PageResult<UserInfo2> page2 = mapper.pageBySample(sample, pageInfo, CollectionUtils.asMap("uid", OrderType.ASC), null);
            List<UserInfo2> data2 = page2.getData();
            List<String> nameSet2 = data2.stream().map(UserInfo2::getName).collect(Collectors.toList());
            assert nameSet2.size() == 3;
            assert nameSet2.get(0).equals("wuguang_32");
            assert nameSet2.get(1).equals("wuguang_39");
            assert nameSet2.get(2).equals("wuguang_4");
        }
    }

    @Test
    public void pageBySample_where_3() throws Exception {
        Options option = Options.of().dialect(SqlDialectRegister.findOrCreate("h2")).mapUnderscoreToCamelCase(true);
        Configuration config = new Configuration(option);
        try (Session s = initPageData(config.newSession(DsUtils.h2Conn()), 50)) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);

            UserInfo3 sample = new UserInfo3();
            sample.setLoginName("login_4");
            assert mapper.countBySample(sample) == 7;

            //
            PageObject pageInfo = new PageObject(0, 3);

            // page1
            PageResult<UserInfo2> page1 = mapper.pageBySample(sample, pageInfo, CollectionUtils.asMap("uid", OrderType.DESC), null);
            List<UserInfo2> data1 = page1.getData();
            List<String> nameSet1 = data1.stream().map(UserInfo2::getName).collect(Collectors.toList());
            assert nameSet1.size() == 3;
            assert nameSet1.get(0).equals("wuguang_46");
            assert nameSet1.get(1).equals("wuguang_4");
            assert nameSet1.get(2).equals("wuguang_39");

            // page2
            pageInfo.nextPage();
            PageResult<UserInfo2> page2 = mapper.pageBySample(sample, pageInfo, CollectionUtils.asMap("uid", OrderType.ASC), null);
            List<UserInfo2> data2 = page2.getData();
            List<String> nameSet2 = data2.stream().map(UserInfo2::getName).collect(Collectors.toList());
            assert nameSet2.size() == 3;
            assert nameSet2.get(0).equals("wuguang_32");
            assert nameSet2.get(1).equals("wuguang_39");
            assert nameSet2.get(2).equals("wuguang_4");
        }
    }

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
