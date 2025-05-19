package net.hasor.dbvisitor.session;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.session.dto.UserInfo2;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

public class BasicMapperBaseTest {
    @Test
    public void basic_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);

            assert mapper.session() == s;
            assert mapper.jdbc() == s.jdbc();
        }
    }

    @Test
    public void countAll_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);

            assert mapper.countAll() == 3;
        }
    }
}
