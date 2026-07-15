package net.hasor.dbvisitor.test.contract.api.lambda;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class LambdaPredicateContractTest extends AbstractNxnContractTest {
    protected int baseId() {
        return 740000;
    }

    @Test
    @Capability(CapabilityId.LAMBDA_PREDICATE_COMPARISON_DYNAMIC)
    public void lambdaPredicate_shouldHonorDynamicComparisonFlags() throws SQLException {
        insertAgeSet("NXN-Predicate-Dynamic-", baseId() + 10);

        long geEnabled = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "NXN-Predicate-Dynamic-%")//
                .ge(true, UserInfo::getAge, 30)//
                .queryForCount();
        long geDisabled = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "NXN-Predicate-Dynamic-%")//
                .ge(false, UserInfo::getAge, 30)//
                .queryForCount();
        long ltEnabled = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "NXN-Predicate-Dynamic-%")//
                .lt(true, UserInfo::getAge, 22)//
                .queryForCount();
        long leEnabled = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "NXN-Predicate-Dynamic-%")//
                .le(true, UserInfo::getAge, 22)//
                .queryForCount();
        long neEnabled = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "NXN-Predicate-Dynamic-%")//
                .ne(true, UserInfo::getAge, 25)//
                .queryForCount();

        assertEquals(2, geEnabled);
        assertEquals(5, geDisabled);
        assertEquals(1, ltEnabled);
        assertEquals(2, leEnabled);
        assertEquals(4, neEnabled);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_PREDICATE_COMPARISON_MIXED)
    public void lambdaPredicate_shouldCombineComparisonOperators() throws SQLException {
        insertAgeSet("NXN-Predicate-Mixed-", baseId() + 30);

        long closed = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "NXN-Predicate-Mixed-%")//
                .ge(UserInfo::getAge, 20)//
                .le(UserInfo::getAge, 30)//
                .queryForCount();
        long open = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "NXN-Predicate-Mixed-%")//
                .gt(UserInfo::getAge, 18)//
                .lt(UserInfo::getAge, 35)//
                .queryForCount();
        long mixed = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "NXN-Predicate-Mixed-%")//
                .ge(UserInfo::getAge, 22)//
                .lt(UserInfo::getAge, 35)//
                .ne(UserInfo::getName, "NXN-Predicate-Mixed-22")//
                .queryForCount();

        assertEquals(3, closed);
        assertEquals(3, open);
        assertEquals(2, mixed);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_PREDICATE_RANGE_BOUNDARIES)
    public void lambdaPredicate_shouldApplyAllRangeBoundaryVariants() throws SQLException {
        insertRangeSet("NXN-Predicate-Range-", baseId() + 100);

        long closedClosed = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "NXN-Predicate-Range-%")//
                .rangeClosedClosed(UserInfo::getAge, 20, 30)//
                .queryForCount();
        long openOpen = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "NXN-Predicate-Range-%")//
                .rangeOpenOpen(UserInfo::getAge, 20, 30)//
                .queryForCount();
        long openClosed = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "NXN-Predicate-Range-%")//
                .rangeOpenClosed(UserInfo::getAge, 20, 30)//
                .queryForCount();
        long closedOpen = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "NXN-Predicate-Range-%")//
                .rangeClosedOpen(UserInfo::getAge, 20, 30)//
                .queryForCount();
        long equalBounds = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "NXN-Predicate-Range-%")//
                .rangeBetween(UserInfo::getAge, 25, 25)//
                .queryForCount();

        assertEquals(3, closedClosed);
        assertEquals(1, openOpen);
        assertEquals(2, openClosed);
        assertEquals(2, closedOpen);
        assertEquals(1, equalBounds);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_PREDICATE_RANGE_NOT_VARIANTS)
    public void lambdaPredicate_shouldApplyNotRangeVariants() throws SQLException {
        insertRangeSet("NXN-Predicate-NotRange-", baseId() + 200);

        long notBetween = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "NXN-Predicate-NotRange-%")//
                .rangeNotBetween(UserInfo::getAge, 20, 30)//
                .queryForCount();
        long notOpenClosed = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "NXN-Predicate-NotRange-%")//
                .rangeNotOpenClosed(UserInfo::getAge, 20, 30)//
                .queryForCount();
        long notClosedOpen = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "NXN-Predicate-NotRange-%")//
                .rangeNotClosedOpen(UserInfo::getAge, 20, 30)//
                .queryForCount();
        long reversedBetween = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "NXN-Predicate-NotRange-%")//
                .rangeBetween(UserInfo::getAge, 30, 20)//
                .queryForCount();

        assertEquals(2, notBetween);
        assertEquals(3, notOpenClosed);
        assertEquals(3, notClosedOpen);
        assertEquals(0, reversedBetween);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_PREDICATE_RANGE_DYNAMIC)
    public void lambdaPredicate_shouldHonorDynamicRangeFlags() throws SQLException {
        insertRangeSet("NXN-Predicate-DynRange-", baseId() + 300);

        long betweenEnabled = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "NXN-Predicate-DynRange-%")//
                .rangeBetween(true, UserInfo::getAge, 20, 25)//
                .queryForCount();
        long betweenDisabled = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "NXN-Predicate-DynRange-%")//
                .rangeBetween(false, UserInfo::getAge, 20, 25)//
                .queryForCount();
        long notBetweenEnabled = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "NXN-Predicate-DynRange-%")//
                .rangeNotBetween(true, UserInfo::getAge, 20, 25)//
                .queryForCount();
        long notBetweenDisabled = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "NXN-Predicate-DynRange-%")//
                .rangeNotBetween(false, UserInfo::getAge, 20, 25)//
                .queryForCount();

        assertEquals(2, betweenEnabled);
        assertEquals(5, betweenDisabled);
        assertEquals(3, notBetweenEnabled);
        assertEquals(5, notBetweenDisabled);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_PREDICATE_IN_SINGLE_AND_LARGE)
    public void lambdaPredicate_shouldSupportSingleAndLargeInCollections() throws SQLException {
        requiresNxnFeature(FeatureId.LARGE_IN_LIST);
        insert(baseId() + 401, "NXN-Predicate-In-Single-25", 25, "in@nxn.test");
        insert(baseId() + 402, "NXN-Predicate-In-Single-30", 30, "in@nxn.test");
        for (int i = 1; i <= 50; i++) {
            insert(baseId() + 500 + i, "NXN-Predicate-In-Large-" + i, 20 + i, "in@nxn.test");
        }

        long single = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "NXN-Predicate-In-Single-%")//
                .in(UserInfo::getAge, Arrays.asList(25))//
                .queryForCount();
        List<Integer> largeList = new ArrayList<>();
        for (int i = 1; i <= 1500; i++) {
            largeList.add(20 + i);
        }
        long large = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "NXN-Predicate-In-Large-%")//
                .in(UserInfo::getAge, largeList)//
                .queryForCount();

        assertEquals(1, single);
        assertEquals(50, large);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_PREDICATE_NOT_IN_NULL)
    public void lambdaPredicate_shouldUseSqlNotInNullSemantics() throws SQLException {
        requiresNxnFeature(FeatureId.SQL_NOT_IN_NULL_SEMANTICS);

        insert(baseId() + 611, "NXN-Predicate-NotInNull-20", 20, "notin@nxn.test");
        insert(baseId() + 612, "NXN-Predicate-NotInNull-25", 25, "notin@nxn.test");
        insert(baseId() + 613, "NXN-Predicate-NotInNull-Null", null, "notin@nxn.test");

        long count = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "NXN-Predicate-NotInNull-%")//
                .notIn(UserInfo::getAge, Arrays.asList(20, null))//
                .queryForCount();

        assertEquals(0, count);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_PREDICATE_LIKE_VARIANTS)
    public void lambdaPredicate_shouldApplyLikeLeftRightAndContains() throws SQLException {
        insert(baseId() + 701, "NXN-Predicate-Like-TestUser", 25, "like@nxn.test");
        insert(baseId() + 702, "NXN-Predicate-Like-UserAccount", 30, "like@nxn.test");
        insert(baseId() + 703, "NXN-Predicate-Like-MyUser", 35, "like@nxn.test");
        insert(baseId() + 704, "NXN-Predicate-Like-Admin", 40, "like@nxn.test");

        long contains = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, ids(701, 702, 703, 704))//
                .like(UserInfo::getName, "User")//
                .queryForCount();
        long right = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, ids(701, 702, 703, 704))//
                .likeRight(UserInfo::getName, "NXN-Predicate-Like-User")//
                .queryForCount();
        long left = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, ids(701, 702, 703, 704))//
                .likeLeft(UserInfo::getName, "User")//
                .queryForCount();

        assertEquals(3, contains);
        assertEquals(1, right);
        assertEquals(2, left);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_PREDICATE_NOT_LIKE_VARIANTS)
    public void lambdaPredicate_shouldApplyNotLikeLeftRightAndContains() throws SQLException {
        insert(baseId() + 801, "NXN-Predicate-NotLike-TestUser", 25, "like@nxn.test");
        insert(baseId() + 802, "NXN-Predicate-NotLike-MyTest", 30, "like@nxn.test");
        insert(baseId() + 803, "NXN-Predicate-NotLike-Admin", 35, "like@nxn.test");
        insert(baseId() + 804, "NXN-Predicate-NotLike-User", 40, "like@nxn.test");

        long notContains = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, ids(801, 802, 803, 804))//
                .notLike(UserInfo::getName, "Test")//
                .queryForCount();
        long notRight = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, ids(801, 802, 803, 804))//
                .notLikeRight(UserInfo::getName, "NXN-Predicate-NotLike-Test")//
                .queryForCount();
        long notLeft = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, ids(801, 802, 803, 804))//
                .notLikeLeft(UserInfo::getName, "User")//
                .queryForCount();

        assertEquals(2, notContains);
        assertEquals(3, notRight);
        assertEquals(2, notLeft);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_PREDICATE_LIKE_NULL_AND_MULTI)
    public void lambdaPredicate_shouldHandleLikeNullsAndMultipleLikePredicates() throws SQLException {
        insert(baseId() + 901, "NXN-Predicate-LikeNull-Test", 25, "ln@test.com");
        insert(baseId() + 902, null, 30, "ln@test.com");
        insert(baseId() + 911, "NXN-Predicate-LikeMulti-Test", 25, "multi@test.com");
        insert(baseId() + 912, "NXN-Predicate-LikeMulti-Test", 30, "multi@other.com");
        insert(baseId() + 913, "NXN-Predicate-LikeMulti-User", 35, "multi@test.com");

        long nullSkipped = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, ids(901, 902))//
                .like(UserInfo::getName, "Test")//
                .queryForCount();
        long multi = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, ids(911, 912, 913))//
                .like(UserInfo::getName, "Test")//
                .like(UserInfo::getEmail, "@test")//
                .queryForCount();

        assertEquals(1, nullSkipped);
        assertEquals(1, multi);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_PREDICATE_STRING_COMPARISON)
    public void lambdaPredicate_shouldSupportStringPropertyComparisonOperators() throws SQLException {
        insertAgeSet("NXN-Predicate-String-Compare-", baseId() + 1000);

        long eq = lambdaTemplate.query(UserInfo.class)//
                .eq("name", "NXN-Predicate-String-Compare-18")//
                .queryForCount();
        long ne = lambdaTemplate.query(UserInfo.class)//
                .like("name", "NXN-Predicate-String-Compare-%")//
                .ne("age", 18)//
                .queryForCount();
        long gt = lambdaTemplate.query(UserInfo.class)//
                .like("name", "NXN-Predicate-String-Compare-%")//
                .gt("age", 25)//
                .queryForCount();
        long ge = lambdaTemplate.query(UserInfo.class)//
                .like("name", "NXN-Predicate-String-Compare-%")//
                .ge("age", 25)//
                .queryForCount();
        long lt = lambdaTemplate.query(UserInfo.class)//
                .like("name", "NXN-Predicate-String-Compare-%")//
                .lt("age", 25)//
                .queryForCount();
        long le = lambdaTemplate.query(UserInfo.class)//
                .like("name", "NXN-Predicate-String-Compare-%")//
                .le("age", 25)//
                .queryForCount();

        assertEquals(1, eq);
        assertEquals(4, ne);
        assertEquals(2, gt);
        assertEquals(3, ge);
        assertEquals(2, lt);
        assertEquals(3, le);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_PREDICATE_STRING_DYNAMIC)
    public void lambdaPredicate_shouldHonorStringPropertyDynamicFlags() throws SQLException {
        insertAgeSet("NXN-Predicate-String-Dynamic-", baseId() + 1100);

        long enabled = lambdaTemplate.query(UserInfo.class)//
                .like("name", "NXN-Predicate-String-Dynamic-%")//
                .ge(true, "age", 25)//
                .notIn(true, "age", Arrays.asList(30, 35))//
                .queryForCount();
        long disabled = lambdaTemplate.query(UserInfo.class)//
                .like("name", "NXN-Predicate-String-Dynamic-%")//
                .ge(false, "age", 25)//
                .notIn(false, "age", Arrays.asList(30, 35))//
                .queryForCount();
        long nullSkipped = lambdaTemplate.query(UserInfo.class)//
                .like("name", "NXN-Predicate-String-Dynamic-%")//
                .eq(false, "name", "missing")//
                .isNull(false, "email")//
                .queryForCount();

        assertEquals(1, enabled);
        assertEquals(5, disabled);
        assertEquals(5, nullSkipped);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_PREDICATE_STRING_LIKE)
    public void lambdaPredicate_shouldSupportStringPropertyLikeVariants() throws SQLException {
        insert(baseId() + 1201, "NXN-Predicate-String-Name-Alice", 18, "string-like@nxn.test");
        insert(baseId() + 1202, "NXN-Predicate-String-Name-Bob", 22, "string-like@nxn.test");
        insert(baseId() + 1203, "NXN-Predicate-String-Name-Charlie", 25, "string-like@nxn.test");
        insert(baseId() + 1204, "NXN-Predicate-String-Name-Diana", 30, "string-like@nxn.test");
        insert(baseId() + 1205, "NXN-Predicate-String-Name-Eve", 35, "string-like@nxn.test");

        long contains = lambdaTemplate.query(UserInfo.class)//
                .in("id", ids(1201, 1202, 1203, 1204, 1205))//
                .like("name", "li")//
                .queryForCount();
        long notContains = lambdaTemplate.query(UserInfo.class)//
                .in("id", ids(1201, 1202, 1203, 1204, 1205))//
                .notLike("name", "li")//
                .queryForCount();
        long right = lambdaTemplate.query(UserInfo.class)//
                .in("id", ids(1201, 1202, 1203, 1204, 1205))//
                .likeRight(true, "name", "NXN-Predicate-String-Name-Ch")//
                .queryForCount();
        long notRight = lambdaTemplate.query(UserInfo.class)//
                .in("id", ids(1201, 1202, 1203, 1204, 1205))//
                .notLikeRight(true, "name", "NXN-Predicate-String-Name-Ch")//
                .queryForCount();
        long left = lambdaTemplate.query(UserInfo.class)//
                .in("id", ids(1201, 1202, 1203, 1204, 1205))//
                .likeLeft(true, "name", "e")//
                .queryForCount();
        long notLeft = lambdaTemplate.query(UserInfo.class)//
                .in("id", ids(1201, 1202, 1203, 1204, 1205))//
                .notLikeLeft(true, "name", "e")//
                .queryForCount();

        assertEquals(2, contains);
        assertEquals(3, notContains);
        assertEquals(1, right);
        assertEquals(4, notRight);
        assertEquals(3, left);
        assertEquals(2, notLeft);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_PREDICATE_STRING_COLLECTION_NULL_RANGE)
    public void lambdaPredicate_shouldSupportStringPropertyCollectionNullRangeAndSampleMap() throws SQLException {
        insert(baseId() + 1301, "NXN-Predicate-String-Mixed-A", 18, "mixed-a@nxn.test");
        insert(baseId() + 1302, "NXN-Predicate-String-Mixed-B", 22, "mixed-b@nxn.test");
        insert(baseId() + 1303, "NXN-Predicate-String-Mixed-C", 25, null);
        insert(baseId() + 1304, "NXN-Predicate-String-Mixed-D", 30, "mixed-d@nxn.test");
        insert(baseId() + 1305, "NXN-Predicate-String-Mixed-E", 35, null);

        long inCount = lambdaTemplate.query(UserInfo.class)//
                .in("id", ids(1301, 1302, 1303, 1304, 1305))//
                .in("age", Arrays.asList(18, 25, 35))//
                .queryForCount();
        long notInCount = lambdaTemplate.query(UserInfo.class)//
                .in("id", ids(1301, 1302, 1303, 1304, 1305))//
                .notIn("age", Arrays.asList(18, 25, 35))//
                .queryForCount();
        long nullCount = lambdaTemplate.query(UserInfo.class)//
                .in("id", ids(1301, 1302, 1303, 1304, 1305))//
                .isNull("email")//
                .queryForCount();
        long notNullCount = lambdaTemplate.query(UserInfo.class)//
                .in("id", ids(1301, 1302, 1303, 1304, 1305))//
                .isNotNull("email")//
                .queryForCount();
        long between = lambdaTemplate.query(UserInfo.class)//
                .in("id", ids(1301, 1302, 1303, 1304, 1305))//
                .between("age", 20, 30)//
                .queryForCount();
        long notBetween = lambdaTemplate.query(UserInfo.class)//
                .in("id", ids(1301, 1302, 1303, 1304, 1305))//
                .notBetween("age", 20, 30)//
                .queryForCount();

        Map<String, Object> sample = new HashMap<>();
        sample.put("name", "NXN-Predicate-String-Mixed-C");
        sample.put("age", 25);
        long sampleMap = lambdaTemplate.query(UserInfo.class)//
                .eqBySampleMap(sample)//
                .queryForCount();

        assertEquals(3, inCount);
        assertEquals(2, notInCount);
        assertEquals(2, nullCount);
        assertEquals(3, notNullCount);
        assertEquals(3, between);
        assertEquals(2, notBetween);
        assertEquals(1, sampleMap);
    }

    private void insertAgeSet(String prefix, int startId) throws SQLException {
        int[] ages = { 18, 22, 25, 30, 35 };
        for (int i = 0; i < ages.length; i++) {
            insert(startId + i, prefix + ages[i], ages[i], "predicate@nxn.test");
        }
    }

    private void insertRangeSet(String prefix, int startId) throws SQLException {
        int[] ages = { 15, 20, 25, 30, 35 };
        for (int i = 0; i < ages.length; i++) {
            insert(startId + i, prefix + ages[i], ages[i], "range@nxn.test");
        }
    }

    private List<Integer> ids(int... offsets) {
        List<Integer> ids = new ArrayList<>();
        for (int offset : offsets) {
            ids.add(baseId() + offset);
        }
        return ids;
    }

    private void insert(int id, String name, Integer age, String email) throws SQLException {
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)", //
                new Object[] { id, name, age, email, new Date() });
    }
}
