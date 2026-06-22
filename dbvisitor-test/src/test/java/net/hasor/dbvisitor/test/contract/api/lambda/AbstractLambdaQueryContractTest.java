package net.hasor.dbvisitor.test.contract.api.lambda;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public abstract class AbstractLambdaQueryContractTest extends AbstractNxnContractTest {
    protected int baseId() {
        return 930000;
    }

    @Test
    @Capability(CapabilityId.LAMBDA_QUERY_COMPARE)
    public void lambdaQueryCompare_shouldApplyRangePredicates() throws SQLException {
        insertUsers("Cmp", new int[] { 18, 22, 25, 30, 35 }, baseId() + 10);

        long count = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "Cmp%")//
                .ge(UserInfo::getAge, 20)//
                .le(UserInfo::getAge, 30)//
                .queryForCount();

        assertEquals(3, count);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_QUERY_LIKE)
    public void lambdaQueryLike_shouldMatchStringPrefix() throws SQLException {
        insertByJdbc(baseId() + 21, "LikeAlpha", 21, "like1@test.com");
        insertByJdbc(baseId() + 22, "LikeBeta", 22, "like2@test.com");
        insertByJdbc(baseId() + 23, "OtherWord", 23, "like3@test.com");

        List<UserInfo> users = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "Like%")//
                .orderBy("id")//
                .queryForList();

        assertEquals(2, users.size());
        assertEquals("LikeAlpha", users.get(0).getName());
        assertEquals("LikeBeta", users.get(1).getName());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_QUERY_IN)
    public void lambdaQueryIn_shouldExpandCollectionParameter() throws SQLException {
        insertUsers("InQ", new int[] { 20, 25, 30, 35 }, baseId() + 30);

        List<UserInfo> users = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "InQ%")//
                .in(UserInfo::getAge, Arrays.asList(20, 30, 35))//
                .orderBy("age")//
                .queryForList();

        assertEquals(3, users.size());
        assertEquals(Integer.valueOf(20), users.get(0).getAge());
        assertEquals(Integer.valueOf(30), users.get(1).getAge());
        assertEquals(Integer.valueOf(35), users.get(2).getAge());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_QUERY_NULL)
    public void lambdaQueryNull_shouldMatchNullAndNotNullColumns() throws SQLException {
        insertByJdbc(baseId() + 41, "NullAge", null, "null-age@test.com");
        insertByJdbc(baseId() + 42, "NullEmail", 42, null);
        insertByJdbc(baseId() + 43, "NullNone", 43, "null-none@test.com");

        long nullAge = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "Null%")//
                .isNull(UserInfo::getAge)//
                .queryForCount();
        long nonNullEmail = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "Null%")//
                .isNotNull(UserInfo::getEmail)//
                .queryForCount();

        assertEquals(1, nullAge);
        assertEquals(2, nonNullEmail);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_QUERY_ORDER)
    public void lambdaQueryOrder_shouldReturnRowsInRequestedOrder() throws SQLException {
        insertUsers("Ord", new int[] { 22, 35, 28, 19 }, baseId() + 50);

        List<UserInfo> users = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "Ord%")//
                .desc("age")//
                .queryForList();

        assertEquals(4, users.size());
        assertEquals(Integer.valueOf(35), users.get(0).getAge());
        assertEquals(Integer.valueOf(19), users.get(3).getAge());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_QUERY_PAGE)
    public void lambdaQueryPage_shouldLimitAndOffsetResults() throws SQLException {
        for (int i = 1; i <= 12; i++) {
            insertByJdbc(baseId() + 60 + i, "PageQ" + i, 20 + i, "page" + i + "@test.com");
        }

        List<UserInfo> users = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "PageQ%")//
                .orderBy("id")//
                .initPage(5, 1)//
                .queryForList();

        assertNotNull(users);
        assertEquals(5, users.size());
        assertEquals(Integer.valueOf(baseId() + 66), users.get(0).getId());
        assertEquals(Integer.valueOf(baseId() + 70), users.get(4).getId());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_QUERY_COUNT)
    public void lambdaQueryCount_shouldReturnMatchingRowCount() throws SQLException {
        insertUsers("Cnt", new int[] { 31, 31, 32, 33, 31 }, baseId() + 80);

        long count = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "Cnt%")//
                .eq(UserInfo::getAge, 31)//
                .queryForCount();

        assertEquals(3, count);
    }

    private void insertUsers(String prefix, int[] ages, int startId) throws SQLException {
        for (int i = 0; i < ages.length; i++) {
            insertByJdbc(startId + i, prefix + (i + 1), ages[i], prefix.toLowerCase() + (i + 1) + "@test.com");
        }
    }

    private void insertByJdbc(int id, String name, Integer age, String email) throws SQLException {
        jdbcTemplate.executeUpdate(//
                "INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)", //
                new Object[] { id, name, age, email, new Date() });
    }
}
