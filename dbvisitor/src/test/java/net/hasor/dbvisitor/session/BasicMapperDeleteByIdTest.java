package net.hasor.dbvisitor.session;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.session.dto.UserInfo;
import net.hasor.dbvisitor.session.dto.UserInfo2;
import net.hasor.dbvisitor.session.dto.UserInfo3;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BasicMapperDeleteByIdTest {
    @Test
    public void deleteById_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            List<UserInfo2> list = mapper.query().queryForList();
            UserInfo2 u1 = list.get(0);

            assert mapper.query().queryForCount() == 3;
            assert mapper.deleteById(u1.getUid()) == 1;
            assert mapper.query().queryForCount() == 2;
            assert mapper.loadBy(u1) == null;
        }
    }

    @Test
    public void deleteByIds_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            List<UserInfo2> list = mapper.query().queryForList();
            UserInfo2 u1 = list.get(0);
            UserInfo2 u2 = list.get(1);

            assert mapper.query().queryForCount() == 3;
            assert mapper.deleteByIds(Arrays.asList(u1.getUid(), u2.getUid())) == 2;
            assert mapper.query().queryForCount() == 1;
        }
    }

    //

    @Test
    public void badDelete_1() throws Exception {
        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            mapper.deleteById(null);
            assert false;
        } catch (NullPointerException e) {
            assert e.getMessage().equals("id is null.");
        }

        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            mapper.deleteByIds(null);
            assert false;
        } catch (NullPointerException e) {
            assert e.getMessage().equals("idList is null.");
        }
    }

    @Test
    public void badDelete_2() throws Exception {
        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo> mapper = s.createBaseMapper(UserInfo.class);
            mapper.deleteById(1);
            assert false;
        } catch (UnsupportedOperationException e) {
            assert e.getMessage().endsWith("missing primary key.");
        }

        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo> mapper = s.createBaseMapper(UserInfo.class);
            mapper.deleteByIds(Arrays.asList(1, 2));
            assert false;
        } catch (UnsupportedOperationException e) {
            assert e.getMessage().endsWith("missing primary key.");
        }
    }

    @Test
    public void badDelete_3() throws Exception {
        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo3> mapper = s.createBaseMapper(UserInfo3.class);
            mapper.deleteById(1);
            assert false;
        } catch (UnsupportedOperationException e) {
            assert e.getMessage().startsWith("does not support composite primary key,");
        }

        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo3> mapper = s.createBaseMapper(UserInfo3.class);
            mapper.deleteByIds(Arrays.asList(1, 2));
            assert false;
        } catch (UnsupportedOperationException e) {
            assert e.getMessage().startsWith("does not support composite primary key,");
        }
    }

    @Test
    public void emptyDelete_1() throws Exception {
        try (Session s = new Configuration().newSession(DsUtils.h2Conn())) {
            BaseMapper<UserInfo2> mapper = s.createBaseMapper(UserInfo2.class);
            assert mapper.deleteByIds(Collections.emptyList()) == 0;

            assert mapper.deleteByIds(Arrays.asList(null, null)) == 0;
        }
    }
}
