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

public abstract class LambdaLogicalConditionContractTest extends AbstractNxnContractTest {
    protected int baseId() {
        return 730000;
    }

    @Test
    @Capability(CapabilityId.LAMBDA_LOGIC_NESTED_AND_OR)
    public void lambdaLogic_shouldApplyNestedAndOrGroups() throws SQLException {
        insertUser(baseId() + 1, "NXN-Logic-Nested-A", 10, "group-a@nxn.test");
        insertUser(baseId() + 2, "NXN-Logic-Nested-B", 10, "group-b@nxn.test");
        insertUser(baseId() + 3, "NXN-Logic-Nested-C", 20, "group-a@nxn.test");
        insertUser(baseId() + 4, "NXN-Logic-Nested-D", 20, "group-b@nxn.test");

        long count = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, ids(1, 2, 3, 4))//
                .nested(q -> q.eq(UserInfo::getAge, 10)//
                        .eq(UserInfo::getEmail, "group-a@nxn.test"))//
                .or(q -> q.eq(UserInfo::getAge, 20)//
                        .eq(UserInfo::getEmail, "group-b@nxn.test"))//
                .queryForCount();

        assertEquals(2, count);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_LOGIC_DEEP_NESTED)
    public void lambdaLogic_shouldApplyNestedGroupAfterOuterPredicate() throws SQLException {
        insertUser(baseId() + 11, "NXN-Logic-Deep-X", 30, "deep@nxn.test");
        insertUser(baseId() + 12, "NXN-Logic-Deep-X", 50, "deep@nxn.test");

        UserInfo result = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, ids(11, 12))//
                .eq(UserInfo::getName, "NXN-Logic-Deep-X")//
                .and(q -> q.eq(UserInfo::getAge, 30)//
                        .or()//
                        .eq(UserInfo::getAge, 40))//
                .queryForObject();

        assertNotNull(result);
        assertEquals(Integer.valueOf(baseId() + 11), result.getId());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_LOGIC_MARKER_AND_NOT)
    public void lambdaLogic_shouldApplyNoArgAndAndNotMarkers() throws SQLException {
        insertUser(baseId() + 21, "NXN-Logic-Marker-And-1", 25, "marker@nxn.test");
        insertUser(baseId() + 22, "NXN-Logic-Marker-And-2", 30, "marker@nxn.test");
        insertUser(baseId() + 23, "NXN-Logic-Marker-And-3", 25, "other@nxn.test");
        insertUser(baseId() + 24, "NXN-Logic-Marker-Not-1", 25, "marker@nxn.test");
        insertUser(baseId() + 25, "NXN-Logic-Marker-Not-2", 30, "marker@nxn.test");
        insertUser(baseId() + 26, "NXN-Logic-Marker-Not-3", 35, "marker@nxn.test");

        long explicitAnd = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, ids(21, 22, 23))//
                .and()//
                .eq(UserInfo::getAge, 25)//
                .queryForCount();
        long explicitNot = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, ids(24, 25, 26))//
                .not()//
                .eq(UserInfo::getAge, 25)//
                .queryForCount();

        assertEquals(2, explicitAnd);
        assertEquals(2, explicitNot);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_LOGIC_DYNAMIC_OR_AND)
    public void lambdaLogic_shouldApplyOrAndConsumerOnlyWhenEnabled() throws SQLException {
        insertUser(baseId() + 31, "NXN-Logic-Dyn-Or-1", 20, "dyn@nxn.test");
        insertUser(baseId() + 32, "NXN-Logic-Dyn-Or-2", 25, "dyn@nxn.test");
        insertUser(baseId() + 33, "NXN-Logic-Dyn-Or-3", 30, "dyn@nxn.test");

        long orEnabled = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, ids(31, 32, 33))//
                .eq(UserInfo::getAge, 20)//
                .or(true, q -> q.eq(UserInfo::getAge, 30))//
                .queryForCount();
        long orDisabled = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, ids(31, 32, 33))//
                .eq(UserInfo::getAge, 20)//
                .or(false, q -> q.eq(UserInfo::getAge, 30))//
                .queryForCount();
        insertUser(baseId() + 34, "NXN-Logic-Dyn-And-1", 20, "group-a@nxn.test");
        insertUser(baseId() + 35, "NXN-Logic-Dyn-And-2", 25, "group-a@nxn.test");
        insertUser(baseId() + 36, "NXN-Logic-Dyn-And-3", 30, "group-b@nxn.test");
        long andEnabled = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, ids(34, 35, 36))//
                .and(true, q -> q.eq(UserInfo::getEmail, "group-a@nxn.test"))//
                .queryForCount();
        long andDisabled = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, ids(34, 35, 36))//
                .and(false, q -> q.eq(UserInfo::getEmail, "group-a@nxn.test"))//
                .queryForCount();

        assertEquals(2, orEnabled);
        assertEquals(1, orDisabled);
        assertEquals(2, andEnabled);
        assertEquals(3, andDisabled);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_LOGIC_DYNAMIC_NOT_NESTED)
    public void lambdaLogic_shouldApplyNotAndNestedConsumerOnlyWhenEnabled() throws SQLException {
        insertUser(baseId() + 41, "NXN-Logic-Dyn-Not-1", 20, "dyn@nxn.test");
        insertUser(baseId() + 42, "NXN-Logic-Dyn-Not-2", 25, "dyn@nxn.test");
        insertUser(baseId() + 43, "NXN-Logic-Dyn-Not-3", 30, "dyn@nxn.test");
        insertUser(baseId() + 44, "NXN-Logic-Dyn-Nested-1", 20, "group-a@nxn.test");
        insertUser(baseId() + 45, "NXN-Logic-Dyn-Nested-2", 25, "group-a@nxn.test");
        insertUser(baseId() + 46, "NXN-Logic-Dyn-Nested-3", 30, "group-b@nxn.test");

        long notEnabled = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, ids(41, 42, 43))//
                .not(true, q -> q.eq(UserInfo::getAge, 25))//
                .queryForCount();
        long notDisabled = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, ids(41, 42, 43))//
                .not(false, q -> q.eq(UserInfo::getAge, 25))//
                .queryForCount();
        long nestedEnabled = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, ids(44, 45, 46))//
                .nested(true, q -> q.eq(UserInfo::getEmail, "group-a@nxn.test")//
                        .ge(UserInfo::getAge, 25))//
                .queryForCount();
        long nestedDisabled = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, ids(44, 45, 46))//
                .nested(false, q -> q.eq(UserInfo::getEmail, "group-a@nxn.test")//
                        .ge(UserInfo::getAge, 25))//
                .queryForCount();

        assertEquals(2, notEnabled);
        assertEquals(3, notDisabled);
        assertEquals(1, nestedEnabled);
        assertEquals(3, nestedDisabled);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_LOGIC_NOT_NESTED)
    public void lambdaLogic_shouldNegateNestedCompositeCondition() throws SQLException {
        insertUser(baseId() + 51, "NXN-Logic-Not-Test", 25, "not@nxn.test");
        insertUser(baseId() + 52, "NXN-Logic-Not-Admin", 30, "not@nxn.test");
        insertUser(baseId() + 53, "NXN-Logic-Not-User", 25, "not@nxn.test");
        insertUser(baseId() + 54, "NXN-Logic-Not-Guest", 30, "not@nxn.test");

        long count = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, ids(51, 52, 53, 54))//
                .not(q -> q.eq(UserInfo::getAge, 25)//
                        .like(UserInfo::getName, "Test"))//
                .queryForCount();

        assertEquals(3, count);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_LOGIC_NOT_IN_LIKE)
    public void lambdaLogic_shouldCombineNotInAndNotLikeGroups() throws SQLException {
        insertUser(baseId() + 61, "NXN-Logic-NotIn-User1", 20, "not@nxn.test");
        insertUser(baseId() + 62, "NXN-Logic-NotIn-User2", 25, "not@nxn.test");
        insertUser(baseId() + 63, "NXN-Logic-NotIn-User3", 30, "not@nxn.test");
        insertUser(baseId() + 64, "NXN-Logic-NotIn-Admin", 30, "not@nxn.test");
        insertUser(baseId() + 65, "NXN-Logic-NotLike-Test", 25, "not@nxn.test");
        insertUser(baseId() + 66, "NXN-Logic-NotLike-Admin", 30, "not@nxn.test");
        insertUser(baseId() + 67, "NXN-Logic-NotLike-User", 35, "not@nxn.test");
        insertUser(baseId() + 68, "NXN-Logic-NotLike-Guest", 40, "not@nxn.test");

        long notInCount = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, ids(61, 62, 63, 64))//
                .notIn(UserInfo::getAge, Arrays.asList(20, 25))//
                .like(UserInfo::getName, "User")//
                .queryForCount();
        long notLikeCount = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, ids(65, 66, 67, 68))//
                .not(q -> q.like(UserInfo::getName, "Test")//
                        .or()//
                        .like(UserInfo::getName, "Admin"))//
                .queryForCount();

        assertEquals(1, notInCount);
        assertEquals(2, notLikeCount);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_LOGIC_NOT_BETWEEN_NULL)
    public void lambdaLogic_shouldCombineNotBetweenAndNullPredicates() throws SQLException {
        insertUser(baseId() + 71, "NXN-Logic-NotBetween-1", 15, "between@nxn.test");
        insertUser(baseId() + 72, "NXN-Logic-NotBetween-2", 25, "between@nxn.test");
        insertUser(baseId() + 73, "NXN-Logic-NotBetween-3", 35, "between@nxn.test");
        insertUser(baseId() + 74, "NXN-Logic-NotBetween-4", 40, null);
        insertUser(baseId() + 75, "NXN-Logic-NotNull-1", 25, "null@nxn.test");
        insertUser(baseId() + 76, "NXN-Logic-NotNull-2", null, "null@nxn.test");
        insertUser(baseId() + 77, "NXN-Logic-NotNull-3", 30, "null@nxn.test");

        long notBetween = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, ids(71, 72, 73, 74))//
                .rangeNotBetween(UserInfo::getAge, 20, 30)//
                .isNotNull(UserInfo::getEmail)//
                .queryForCount();
        long notNull = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, ids(75, 76, 77))//
                .not(q -> q.isNull(UserInfo::getAge))//
                .queryForCount();

        assertEquals(2, notBetween);
        assertEquals(2, notNull);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_LOGIC_DOUBLE_NOT)
    public void lambdaLogic_shouldTreatDoubleNotAsPositivePredicate() throws SQLException {
        insertUser(baseId() + 81, "NXN-Logic-DoubleNot-1", 25, "double@nxn.test");
        insertUser(baseId() + 82, "NXN-Logic-DoubleNot-2", 30, "double@nxn.test");

        UserInfo result = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, ids(81, 82))//
                .not(q -> q.not(n -> n.eq(UserInfo::getAge, 25)))//
                .queryForObject();

        assertNotNull(result);
        assertEquals(Integer.valueOf(baseId() + 81), result.getId());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_LOGIC_DYNAMIC_VALUE)
    public void lambdaLogic_shouldSkipDisabledValuePredicates() throws SQLException {
        insertUser(baseId() + 91, "NXN-Logic-DynamicValue-1", 25, "dynamic@nxn.test");
        insertUser(baseId() + 92, "NXN-Logic-DynamicValue-2", 30, "dynamic@nxn.test");
        insertUser(baseId() + 93, "NXN-Logic-DynamicValue-Other", 35, "dynamic@nxn.test");

        String absentName = null;
        Integer age = 25;
        long count = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, ids(91, 92, 93))//
                .like(UserInfo::getName, "DynamicValue")//
                .eq(absentName != null, UserInfo::getName, absentName)//
                .eq(age != null, UserInfo::getAge, age)//
                .queryForCount();
        long skipped = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, ids(91, 92, 93))//
                .gt(false, UserInfo::getAge, 20)//
                .like(false, UserInfo::getName, "Missing")//
                .queryForCount();

        assertEquals(1, count);
        assertEquals(3, skipped);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_LOGIC_IF_TRUE)
    public void lambdaLogic_shouldApplyIfTrueConsumerOnlyWhenEnabled() throws SQLException {
        insertUser(baseId() + 101, "NXN-Logic-IfTrue-25", 25, "iftrue@nxn.test");
        insertUser(baseId() + 102, "NXN-Logic-IfTrue-30", 30, "iftrue@nxn.test");
        insertUser(baseId() + 103, "NXN-Logic-IfTrue-35", 35, "iftrue@nxn.test");
        insertUser(baseId() + 104, "NXN-Logic-IfTrue-40", 40, "iftrue@nxn.test");
        insertUser(baseId() + 105, "NXN-Logic-IfTrue-45", 45, "iftrue@nxn.test");

        long enabled = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, ids(101, 102, 103, 104, 105))//
                .ifTrue(true, q -> q.gt(UserInfo::getAge, 30))//
                .queryForCount();
        long disabled = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, ids(101, 102, 103, 104, 105))//
                .ifTrue(false, q -> q.gt(UserInfo::getAge, 30))//
                .queryForCount();

        assertEquals(3, enabled);
        assertEquals(5, disabled);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_LOGIC_APPLY_RAW)
    public void lambdaLogic_shouldComposeRawApplyConditionsWithOtherPredicates() throws SQLException {
        insertUser(baseId() + 111, "NXN-Logic-Apply-25", 25, "apply@nxn.test");
        insertUser(baseId() + 112, "NXN-Logic-Apply-30", 30, "apply@nxn.test");
        insertUser(baseId() + 113, "NXN-Logic-Apply-35", 35, "apply@nxn.test");
        insertUser(baseId() + 114, "NXN-Logic-Apply-40", 40, "apply@nxn.test");
        insertUser(baseId() + 115, "NXN-Logic-Apply-45", 45, "apply@nxn.test");

        long raw = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, ids(111, 112, 113, 114, 115))//
                .apply("age > 30")//
                .queryForCount();
        long parameterized = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, ids(111, 112, 113, 114, 115))//
                .apply("age <= ?", 30)//
                .queryForCount();
        long only = lambdaTemplate.query(UserInfo.class)//
                .apply("name = ?", "NXN-Logic-Apply-25")//
                .queryForCount();
        long nestedOr = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, ids(111, 112, 113, 114, 115))//
                .nested(q -> {
                    try {
                        q.apply("age = 25")//
                                .or()//
                                .apply("age = 45");
                    } catch (SQLException e) {
                        throw new IllegalStateException(e);
                    }
                })//
                .queryForCount();

        assertEquals(3, raw);
        assertEquals(2, parameterized);
        assertEquals(1, only);
        assertEquals(2, nestedOr);
    }

    private List<Integer> ids(int... offsets) {
        Integer[] ids = new Integer[offsets.length];
        for (int i = 0; i < offsets.length; i++) {
            ids[i] = baseId() + offsets[i];
        }
        return Arrays.asList(ids);
    }

    private void insertUser(int id, String name, Integer age, String email) throws SQLException {
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)", //
                new Object[] { id, name, age, email, new Date() });
    }
}
