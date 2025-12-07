package net.hasor.dbvisitor.session;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.session.dto.UserInfo;
import net.hasor.dbvisitor.session.dto.UserInfo2;
import net.hasor.dbvisitor.session.dto.UserInfo3;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

public class BasicMapperQueryByIdTest {
    @Test
    public void selectById_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            List<UserInfo2> list = mapper.query().queryForList();
            UserInfo2 u1 = list.get(0);

            assert mapper.selectById(u1.getUid()).getName().equals(u1.getName());
        }
    }

    @Test
    public void selectByIds_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            List<UserInfo2> list = mapper.query().queryForList();
            UserInfo2 u1 = list.get(0);
            UserInfo2 u2 = list.get(1);

            List<UserInfo2> result = mapper.selectByIds(Arrays.asList(u1.getUid(), u2.getUid()));
            Map<String, UserInfo2> groupById = new HashMap<>();
            for (UserInfo2 userInfo : result) {
                groupById.put(userInfo.getUid(), userInfo);
            }
            assert groupById.get(u1.getUid()).getName().equals(u1.getName());
            assert groupById.get(u2.getUid()).getName().equals(u2.getName());
        }
    }

    //

    @Test
    public void badSelect_1() throws Exception {
        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            mapper.selectById(null);
            assert false;
        } catch (NullPointerException e) {
            assert e.getMessage().equals("id is null.");
        }

        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            mapper.selectByIds(null);
            assert false;
        } catch (NullPointerException e) {
            assert e.getMessage().equals("idList is null.");
        }
    }

    @Test
    public void badSelect_2() throws Exception {
        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo> mapper = s.createBaseMapper(UserInfo.class);
            mapper.selectById(1);
            assert false;
        } catch (UnsupportedOperationException e) {
            assert e.getMessage().endsWith("missing primary key.");
        }

        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo> mapper = s.createBaseMapper(UserInfo.class);
            mapper.selectByIds(Arrays.asList(1, 2));
            assert false;
        } catch (UnsupportedOperationException e) {
            assert e.getMessage().endsWith("missing primary key.");
        }
    }

    @Test
    public void badSelect_3() throws Exception {
        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo3> mapper = s.createBaseMapper(UserInfo3.class);
            mapper.selectById(1);
            assert false;
        } catch (UnsupportedOperationException e) {
            assert e.getMessage().startsWith("does not support composite primary key,");
        }

        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo3> mapper = s.createBaseMapper(UserInfo3.class);
            mapper.selectByIds(Arrays.asList(1, 2));
            assert false;
        } catch (UnsupportedOperationException e) {
            assert e.getMessage().startsWith("does not support composite primary key,");
        }
    }
}
