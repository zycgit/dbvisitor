/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.function;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import net.hasor.dbvisitor.jdbc.CallableStatementCallback;
import net.hasor.dbvisitor.jdbc.CallableStatementSetter;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import net.hasor.dbvisitor.types.SqlArg;
import net.hasor.dbvisitor.types.handler.number.IntegerTypeHandler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@NxnContract
public abstract class FunctionContractTest extends AbstractNxnContractTest {
    @Before
    public void createFunctionFixtures() throws SQLException {
        if (!profile().supportsFeature(FeatureId.FUNCTION)) {
            return;
        }

        jdbcTemplate.executeUpdate("DELETE FROM basic_types_test WHERE id IN (918101, 918102)");
        jdbcTemplate.executeUpdate("DELETE FROM user_info WHERE id IN (918101, 918102, 918103)");
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email) VALUES (918101, 'FuncAlice', 25, 'func-alice@test.com')");
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email) VALUES (918102, 'FuncBob', 30, 'func-bob@test.com')");
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email) VALUES (918103, 'FuncCharlie', 35, 'func-charlie@test.com')");
        jdbcTemplate.executeUpdate("INSERT INTO basic_types_test (id, int_value, string_value) VALUES (918101, 100, 'FuncType1')");
        jdbcTemplate.executeUpdate("INSERT INTO basic_types_test (id, int_value, string_value) VALUES (918102, 200, 'FuncType2')");

        createFunctionDefinitions();
    }

    protected void createFunctionDefinitions() throws SQLException {
        throw new UnsupportedOperationException("Function fixture SQL must be provided by the concrete data source test.");
    }

    @Test
    @Capability(CapabilityId.FUNCTION_QUERY_SCALAR)
    public void queryFunction_shouldReturnScalarValue() throws SQLException {
        requiresNxnFeature(FeatureId.FUNCTION);

        Integer result = jdbcTemplate.queryForObject(addNumbersQuerySql(), new Object[] { 10, 5 }, Integer.class);

        assertEquals(Integer.valueOf(15), result);
    }

    @Test
    @Capability(CapabilityId.FUNCTION_QUERY_NAMED_PARAMETER)
    public void queryFunction_shouldBindNamedParameters() throws SQLException {
        requiresNxnFeature(FeatureId.FUNCTION);

        Integer result = jdbcTemplate.queryForObject(multiplyNamedQuerySql(), Map.of("x", 7, "y", 8), Integer.class);

        assertEquals(Integer.valueOf(56), result);
    }

    @Test
    @Capability(CapabilityId.FUNCTION_QUERY_OUT_RECORD)
    public void queryFunction_shouldReturnOutParameterRecord() throws SQLException {
        requiresNxnFeature(FeatureId.FUNCTION_RECORD_RESULT);

        Map<String, Object> result = jdbcTemplate.queryForMap(calcNumbersQuerySql(), new Object[] { 100, 20 });

        assertNumberEquals(120, value(result, "sum_result"));
        assertNumberEquals(80, value(result, "diff_result"));
        assertNumberEquals(2000, value(result, "mult_result"));
        assertEquals(new BigDecimal("5"), ((BigDecimal) value(result, "div_result")).stripTrailingZeros());
    }

    @Test
    @Capability(CapabilityId.FUNCTION_QUERY_TABLE_RESULT)
    public void queryFunction_shouldReturnTableRows() throws SQLException {
        requiresNxnFeature(FeatureId.FUNCTION_TABLE_RESULT);

        List<Map<String, Object>> allRows = jdbcTemplate.queryForList(multiResultsetsQuerySql());
        List<Map<String, Object>> filtered = jdbcTemplate.queryForList(filterUsersQuerySql(), 30);

        assertEquals(5, allRows.size());
        assertEquals(2, filtered.size());
        assertEquals("FuncBob", value(filtered.get(0), "name"));
        assertNumberEquals(30, value(filtered.get(0), "age"));
        assertEquals("FuncCharlie", value(filtered.get(1), "name"));
        assertNumberEquals(35, value(filtered.get(1), "age"));
    }

    @Test
    @Capability(CapabilityId.FUNCTION_QUERY_NULL_FALLBACK)
    public void queryFunction_shouldHandleMissingLookupFallback() throws SQLException {
        requiresNxnFeature(FeatureId.FUNCTION_RECORD_RESULT);

        Map<String, Object> result = jdbcTemplate.queryForMap(complexParamsQuerySql(), new Object[] { 999999, 5 });

        assertNotNull(result);
        assertNumberEquals(6, value(result, "counter"));
        assertEquals("Unknown", value(result, "user_name"));
        assertNumberEquals(0, value(result, "user_age"));
    }

    @Test
    @Capability(CapabilityId.FUNCTION_CALL_CALLBACK)
    public void callFunction_shouldSupportCallableStatementCallbackAccess() throws SQLException {
        requiresNxnFeature(FeatureId.FUNCTION_CALL_CALLBACK);

        Integer sum = jdbcTemplate.call(addNumbersCallableSql(), new CallableStatementSetter() {
            @Override
            public void setValues(CallableStatement cs) throws SQLException {
                cs.setInt(1, 10);
                cs.setInt(2, 20);
            }
        }, new CallableStatementCallback<Integer>() {
            @Override
            public Integer doInCallableStatement(CallableStatement cs) throws SQLException {
                try (ResultSet rs = cs.executeQuery()) {
                    return rs.next() ? rs.getInt(1) : null;
                }
            }
        });
        assertEquals(Integer.valueOf(30), sum);
    }

    @Test
    @Capability(CapabilityId.FUNCTION_QUERY_PARAMETER_VARIANTS)
    public void queryFunction_shouldSupportSingleParameterSqlArgAndStringTransform() throws SQLException {
        requiresNxnFeature(FeatureId.FUNCTION);

        String username = jdbcTemplate.queryForObject(getUsernameQuerySql(), 918101, String.class);
        Integer sum = jdbcTemplate.queryForObject(addNumbersQuerySql(), //
                new SqlArg[] { SqlArg.valueOf(12, new IntegerTypeHandler()), SqlArg.valueOf(3, new IntegerTypeHandler()) }, Integer.class);
        Map<String, Object> transformed = jdbcTemplate.queryForMap(transformStringQuerySql(), new Object[] { "world", "!" });

        assertEquals("FuncAlice", username);
        assertEquals(Integer.valueOf(15), sum);
        assertEquals("WORLD!", value(transformed, "text_value"));
    }

    private Object value(Map<String, Object> row, String key) {
        if (row.containsKey(key)) {
            return row.get(key);
        }
        return row.get(key.toUpperCase());
    }

    private void assertNumberEquals(int expected, Object actual) {
        assertEquals(0, BigDecimal.valueOf(expected).compareTo(new BigDecimal(actual.toString())));
    }

    protected String addNumbersQuerySql() {
        throw new UnsupportedOperationException("Function query SQL must be provided by the concrete data source test.");
    }

    protected String multiplyNamedQuerySql() {
        throw new UnsupportedOperationException("Function query SQL must be provided by the concrete data source test.");
    }

    protected String calcNumbersQuerySql() {
        throw new UnsupportedOperationException("Function query SQL must be provided by the concrete data source test.");
    }

    protected String multiResultsetsQuerySql() {
        throw new UnsupportedOperationException("Function query SQL must be provided by the concrete data source test.");
    }

    protected String filterUsersQuerySql() {
        throw new UnsupportedOperationException("Function query SQL must be provided by the concrete data source test.");
    }

    protected String complexParamsQuerySql() {
        throw new UnsupportedOperationException("Function query SQL must be provided by the concrete data source test.");
    }

    protected String addNumbersCallableSql() {
        throw new UnsupportedOperationException("Function callable SQL must be provided by the concrete data source test.");
    }

    protected String calcNumbersCallableSql() {
        throw new UnsupportedOperationException("Function callable SQL must be provided by the concrete data source test.");
    }

    protected String getUsernameQuerySql() {
        throw new UnsupportedOperationException("Function query SQL must be provided by the concrete data source test.");
    }

    protected String transformStringQuerySql() {
        throw new UnsupportedOperationException("Function query SQL must be provided by the concrete data source test.");
    }
}
