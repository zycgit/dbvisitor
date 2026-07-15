package net.hasor.dbvisitor.test.contract.feature.procedure;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import net.hasor.cobble.CollectionUtils;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import net.hasor.dbvisitor.types.SqlArg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class ProcTest extends AbstractNxnContractTest {
    @Before
    public void createProcedureFixtures() throws SQLException {
        if (!profile().supportsFeature(FeatureId.PROCEDURE)) {
            return;
        }

        jdbcTemplate.executeUpdate("DELETE FROM user_info WHERE id IN (918001, 918002, 918003)");
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email) VALUES (918001, 'ProcAlice', 25, 'proc-alice@test.com')");
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email) VALUES (918002, 'ProcBob', 30, 'proc-bob@test.com')");
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email) VALUES (918003, 'ProcCharlie', 35, 'proc-charlie@test.com')");

        createProcedureDefinitions();
    }

    protected void createProcedureDefinitions() throws SQLException {
        throw new UnsupportedOperationException("Procedure fixture SQL must be provided by the concrete data source test.");
    }

    @Test
    @Capability(CapabilityId.PROCEDURE_CALL_POSITIONAL_INOUT)
    public void callProcedurePosInOutParam() throws SQLException {
        requiresNxnFeature(FeatureId.PROCEDURE);

        Map<String, Object> result = jdbcTemplate.call(addNumbersPositionalCallSql(), //
                new Object[] { 10, 5, SqlArg.asInOut("result", 0, java.sql.Types.INTEGER) });

        assertNotNull(result);
        assertTrue(result.containsKey("result"));
        assertEquals(15, result.get("result"));
    }

    @Test
    @Capability(CapabilityId.PROCEDURE_CALL_NAMED_INOUT)
    public void callProcedureNamedInOutParam() throws SQLException {
        requiresNxnFeature(FeatureId.PROCEDURE);
        Map<String, Object> params = new HashMap<>();
        params.put("a", 12);
        params.put("b", 8);
        params.put("result", SqlArg.asInOut("result", 0, java.sql.Types.INTEGER));

        Map<String, Object> result = jdbcTemplate.call(addNumbersNamedCallSql(), params);

        assertNotNull(result);
        assertEquals(20, result.get("result"));
    }

    @Test
    @Capability(CapabilityId.PROCEDURE_CALL_HASH_PARAMETER)
    public void callProcedureHashStyleInOutParam() throws SQLException {
        requiresNxnFeature(FeatureId.PROCEDURE);

        Map<String, Object> result = jdbcTemplate.call(transformStringHashCallSql(), //
                CollectionUtils.asMap("text_value", "hello", "suffix", "!!!"));

        assertNotNull(result);
        assertEquals("HELLO!!!", result.get("text_value"));
    }

    @Test
    @Capability(CapabilityId.PROCEDURE_CALL_MULTIPLE_INOUT)
    public void callProcedureMultiInOutParams() throws SQLException {
        requiresNxnFeature(FeatureId.PROCEDURE);
        Object[] params = new Object[] { //
                50, //
                10, //
                SqlArg.asInOut("sum_result", 0, java.sql.Types.INTEGER), //
                SqlArg.asInOut("diff_result", 0, java.sql.Types.INTEGER), //
                SqlArg.asInOut("mult_result", 0, java.sql.Types.INTEGER), //
                SqlArg.asInOut("div_result", BigDecimal.ZERO, java.sql.Types.DECIMAL) };

        Map<String, Object> result = jdbcTemplate.call(calcNumbersCallSql(), params);

        assertNotNull(result);
        assertEquals(60, result.get("sum_result"));
        assertEquals(40, result.get("diff_result"));
        assertEquals(500, result.get("mult_result"));
        assertEquals(new BigDecimal("5").stripTrailingZeros(), ((BigDecimal) result.get("div_result")).stripTrailingZeros());
    }

    @Test
    @Capability(CapabilityId.PROCEDURE_CALL_DATA_LOOKUP)
    public void callProcedureDataLookupThroughInOutParams() throws SQLException {
        requiresNxnFeature(FeatureId.PROCEDURE);
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", 918001);
        params.put("user_name", SqlArg.asInOut("user_name", "", java.sql.Types.VARCHAR));
        params.put("user_age", SqlArg.asInOut("user_age", 0, java.sql.Types.INTEGER));

        Map<String, Object> result = jdbcTemplate.call(getUserInfoNamedCallSql(), params);

        assertNotNull(result);
        assertEquals("ProcAlice", result.get("user_name"));
        assertEquals(25, result.get("user_age"));
    }

    @Test
    @Capability(CapabilityId.PROCEDURE_CALL_HASH_METADATA)
    public void callProcedureHashParamMetaVariants() throws SQLException {
        requiresNxnFeature(FeatureId.PROCEDURE);

        Map<String, Object> javaTypeResult = jdbcTemplate.call(addNumbersJavaTypeHashCallSql(), //
                CollectionUtils.asMap("a", 9, "b", 6, "result", 0));
        Map<String, Object> typeHandlerResult = jdbcTemplate.call(addNumbersTypeHandlerHashCallSql(), //
                CollectionUtils.asMap("a", 11, "b", 4, "result", 0));
        Map<String, Object> aliasResult = jdbcTemplate.call(updateCounterAliasHashCallSql(), //
                CollectionUtils.asMap("counter", 100, "increment", 25));
        Map<String, Object> inferredInResult = jdbcTemplate.call(addNumbersInferredHashCallSql(), //
                CollectionUtils.asMap("a", 3, "b", 7, "result", 0));

        assertNotNull(javaTypeResult);
        assertEquals(15, javaTypeResult.get("result"));
        assertEquals(15, typeHandlerResult.get("result"));
        assertEquals(125, aliasResult.get("cnt"));
        assertEquals(10, inferredInResult.get("result"));
    }

    @Test
    @Capability(CapabilityId.PROCEDURE_CALL_CURSOR_RESULT)
    public void callProcedureCursorOutParamToJavaTypeList() throws SQLException {
        requiresNxnFeature(FeatureId.PROCEDURE);
        requiresNxnFeature(FeatureId.PROCEDURE_CURSOR_RESULT);

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            JdbcTemplate txJdbc = new JdbcTemplate(conn);

            Map<String, Object> result = txJdbc.call(cursorUsersCallSql(), //
                    CollectionUtils.asMap("p_name", "ProcAlice"));

            assertNotNull(result);
            assertTrue(result.get("res") instanceof List);
            List<?> users = (List<?>) result.get("res");
            assertEquals(1, users.size());
            assertTrue(users.get(0) instanceof UserInfo);
            UserInfo user = (UserInfo) users.get(0);
            assertEquals("ProcAlice", user.getName());
            assertEquals(Integer.valueOf(25), user.getAge());
            conn.commit();
        }
    }

    @Test
    @Capability(CapabilityId.PROCEDURE_CALL_RESULT_SET)
    public void callProcedureReturnedResultSet() throws SQLException {
        requiresNxnFeature(FeatureId.PROCEDURE);
        requiresNxnFeature(FeatureId.PROCEDURE_RESULT_SET);

        Map<String, Object> result = jdbcTemplate.call(resultSetUsersCallSql(), //
                CollectionUtils.asMap("p_id", 918001));

        assertNotNull(result);
        assertTrue(result.get("#result-set-1") instanceof List);
        List<?> users = (List<?>) result.get("#result-set-1");
        assertEquals(1, users.size());
        assertTrue(users.get(0) instanceof Map);

        Map<?, ?> user = (Map<?, ?>) users.get(0);
        assertEquals("ProcAlice", rowValue(user, "name"));
        assertEquals(25, ((Number) rowValue(user, "age")).intValue());
    }

    private Object rowValue(Map<?, ?> row, String columnName) {
        for (Map.Entry<?, ?> entry : row.entrySet()) {
            Object key = entry.getKey();
            if (key != null && columnName.equalsIgnoreCase(key.toString())) {
                return entry.getValue();
            }
        }
        return null;
    }

    protected String addNumbersPositionalCallSql() {
        throw new UnsupportedOperationException("Procedure call SQL must be provided by the concrete data source test.");
    }

    protected String addNumbersNamedCallSql() {
        throw new UnsupportedOperationException("Procedure call SQL must be provided by the concrete data source test.");
    }

    protected String transformStringHashCallSql() {
        throw new UnsupportedOperationException("Procedure call SQL must be provided by the concrete data source test.");
    }

    protected String calcNumbersCallSql() {
        throw new UnsupportedOperationException("Procedure call SQL must be provided by the concrete data source test.");
    }

    protected String getUserInfoNamedCallSql() {
        throw new UnsupportedOperationException("Procedure call SQL must be provided by the concrete data source test.");
    }

    protected String addNumbersJavaTypeHashCallSql() {
        throw new UnsupportedOperationException("Procedure call SQL must be provided by the concrete data source test.");
    }

    protected String addNumbersTypeHandlerHashCallSql() {
        throw new UnsupportedOperationException("Procedure call SQL must be provided by the concrete data source test.");
    }

    protected String updateCounterAliasHashCallSql() {
        throw new UnsupportedOperationException("Procedure call SQL must be provided by the concrete data source test.");
    }

    protected String addNumbersInferredHashCallSql() {
        throw new UnsupportedOperationException("Procedure call SQL must be provided by the concrete data source test.");
    }

    protected String cursorUsersCallSql() {
        throw new UnsupportedOperationException("Procedure call SQL must be provided by the concrete data source test.");
    }

    protected String resultSetUsersCallSql() {
        throw new UnsupportedOperationException("Procedure call SQL must be provided by the concrete data source test.");
    }
}
