package net.hasor.dbvisitor.test.contract.api.lambda;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@NxnContract
public abstract class LambdaSecureTest extends AbstractNxnContractTest {
    protected int baseId() {
        return 780000;
    }

    @Test
    @Capability(CapabilityId.LAMBDA_SECURITY_VALUE_EQ)
    public void lambdaSecurityValueParamizeEqPayloads() throws SQLException {
        insertUser(baseId() + 1, "SecAlice", 25);
        insertUser(baseId() + 2, "SecBob", 30);

        assertEquals(0, lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getName, "SecAlice' OR '1'='1")//
                .queryForCount());
        assertEquals(0, lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getName, "' UNION SELECT id, name, age, email, create_time FROM user_info --")//
                .queryForCount());
        assertEquals(0, lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getName, "SecAlice'; DELETE FROM user_info; --")//
                .queryForCount());
        assertEquals("SecAlice", loadName(baseId() + 1));
        assertEquals("SecBob", loadName(baseId() + 2));
    }

    @Test
    @Capability(CapabilityId.LAMBDA_SECURITY_VALUE_LIKE_IN)
    public void lambdaSecurityValueParamizeLikeAndInPayloads() throws SQLException {
        insertUser(baseId() + 11, "SecLikeTarget", 25);
        insertUser(baseId() + 12, "SecOther", 30);
        insertUser(baseId() + 13, "SecInTarget", 35);

        List<UserInfo> likeResult = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, Arrays.asList(baseId() + 11, baseId() + 12))//
                .like(UserInfo::getName, "%' OR '1'='1' --")//
                .queryForList();
        assertEquals(0, likeResult.size());

        List<Object> namePayloads = Arrays.<Object>asList("SecInTarget' OR '1'='1", "x'; DROP TABLE users; --");
        List<UserInfo> inResult = lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 13)//
                .in(UserInfo::getName, namePayloads)//
                .queryForList();
        assertEquals(0, inResult.size());
        assertEquals("SecInTarget", loadName(baseId() + 13));
    }

    @Test
    @Capability(CapabilityId.LAMBDA_SECURITY_VALUE_BETWEEN)
    public void lambdaSecurityValueNotExpandBtwnPayloads() throws SQLException {
        insertUser(baseId() + 21, "SecBetween", 25);
        insertUser(baseId() + 22, "SecBetweenOther", 45);

        try {
            List<UserInfo> result = lambdaTemplate.query(UserInfo.class)//
                    .in(UserInfo::getId, Arrays.asList(baseId() + 21, baseId() + 22))//
                    .between("age", "0 OR 1=1 --", "100")//
                    .queryForList();
            assertEquals(0, result.size());
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
        assertEquals("SecBetween", loadName(baseId() + 21));
        assertEquals("SecBetweenOther", loadName(baseId() + 22));
    }

    @Test
    @Capability(CapabilityId.LAMBDA_SECURITY_APPLY_PARAMETER)
    public void lambdaSecurityValueParamizeApplyPlaceholders() throws SQLException {
        insertUser(baseId() + 31, "SecApply", 25);

        List<UserInfo> exact = lambdaTemplate.query(UserInfo.class)//
                .apply("name = ?", "SecApply")//
                .queryForList();
        assertEquals(1, exact.size());
        assertEquals(Integer.valueOf(baseId() + 31), exact.get(0).getId());

        List<UserInfo> injected = lambdaTemplate.query(UserInfo.class)//
                .apply("name = ?", "SecApply' OR '1'='1")//
                .queryForList();
        assertEquals(0, injected.size());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_SECURITY_APPLY_SCOPED_TRUE)
    public void lambdaSecurityValueApplyTruePredInsideExistingScope() throws SQLException {
        insertUser(baseId() + 32, "SecApplyScoped1", 25);
        insertUser(baseId() + 33, "SecApplyScoped2", 30);
        insertUser(baseId() + 34, "SecApplyScoped3", 35);

        try {
            lambdaTemplate.delete(UserInfo.class).doDelete();
            fail("Empty-where delete should require allowEmptyWhere().");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("allowEmptyWhere"));
        }

        int deleted = lambdaTemplate.delete(UserInfo.class)//
                .in(UserInfo::getId, Arrays.asList(baseId() + 32, baseId() + 33))//
                .apply("1=1")//
                .doDelete();

        assertMutationRows(2, deleted);
        assertEquals(0, lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, Arrays.asList(baseId() + 32, baseId() + 33))//
                .queryForCount());
        assertEquals("SecApplyScoped3", loadName(baseId() + 34));
    }

    @Test
    @Capability(CapabilityId.LAMBDA_SECURITY_METHOD_REF)
    public void lambdaSecurityValueMethodReferenceColsAndValuesSafe() throws SQLException {
        insertUser(baseId() + 41, "SecLambdaSafe", 25);

        UserInfo result = lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getName, "SecLambdaSafe")//
                .eq(UserInfo::getAge, 25)//
                .queryForObject();
        assertNotNull(result);
        assertEquals(Integer.valueOf(baseId() + 41), result.getId());

        assertEquals(0, lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "' OR 1=1 --")//
                .queryForCount());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_SECURITY_EQ_BY_SAMPLE)
    public void lambdaSecurityValueParamizeEqBySampleValues() throws SQLException {
        insertUser(baseId() + 51, "SecSampleSafe", 25);

        UserInfo sample = new UserInfo();
        sample.setName("' OR 1=1 --");
        List<UserInfo> result = lambdaTemplate.query(UserInfo.class)//
                .eqBySample(sample)//
                .queryForList();

        assertEquals(0, result.size());
        assertEquals("SecSampleSafe", loadName(baseId() + 51));
    }

    @Test
    @Capability(CapabilityId.LAMBDA_SECURITY_SECOND_ORDER)
    public void lambdaSecurityValueSecondOrderPayloadsAsValues() throws SQLException {
        String payload = "' OR '1'='1";
        insertUser(baseId() + 61, payload, 25);
        insertUser(baseId() + 62, "SecSecondOther", 26);

        UserInfo loaded = lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 61)//
                .queryForObject();
        assertNotNull(loaded);
        assertEquals(payload, loaded.getName());

        List<UserInfo> result = lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getName, loaded.getName())//
                .queryForList();
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(baseId() + 61), result.get(0).getId());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_SECURITY_FREEDOM_VALUE_INSERT)
    public void lambdaSecurityValueFreedomInsertedPayloadAsValue() throws SQLException {
        String payload = "admin'--";
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", baseId() + 71);
        row.put("name", payload);
        row.put("age", 30);
        row.put("email", "freedom-value@test.com");
        row.put("create_time", new Date());

        lambdaTemplate.insertFreedom("user_info")//
                .applyMap(row)//
                .executeSumResult();

        UserInfo loaded = lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 71)//
                .queryForObject();
        assertNotNull(loaded);
        assertEquals(payload, loaded.getName());
        assertEquals(1, lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getName, payload)//
                .queryForCount());
    }

    private void insertUser(int id, String name, Integer age) throws SQLException {
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)", //
                new Object[] { id, name, age, id + "@security.test", new Date() });
    }

    private String loadName(int id) throws SQLException {
        return lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, id)//
                .queryForObject().getName();
    }
}
