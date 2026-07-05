package net.hasor.dbvisitor.test.contract.api.jdbc;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import net.hasor.dbvisitor.dynamic.args.ArraySqlArgSource;
import net.hasor.dbvisitor.dynamic.args.BeanSqlArgSource;
import net.hasor.dbvisitor.dynamic.args.MapSqlArgSource;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.types.SqlArg;
import net.hasor.dbvisitor.types.handler.number.IntegerTypeHandler;
import net.hasor.dbvisitor.types.handler.string.StringTypeHandler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public abstract class JdbcParamTest extends AbstractNxnContractTest {
    protected int baseId() {
        return 660000;
    }

    @Test
    @Capability(CapabilityId.JDBC_PARAM_POSITIONAL_ARRAY)
    public void positionalArrayParamsObjectArray() throws SQLException {
        int id = baseId() + 1;
        insert(id, "NXN-Param-Array", 25, "nxn-param-array@test.com");

        UserInfo user = jdbcTemplate.queryForObject("SELECT * FROM user_info WHERE name = ? AND age > ?", //
                new Object[] { "NXN-Param-Array", 20 }, UserInfo.class);

        assertNotNull(user);
        assertEquals(Integer.valueOf(id), user.getId());
        assertEquals(Integer.valueOf(25), user.getAge());
    }

    @Test
    @Capability(CapabilityId.JDBC_PARAM_NULL)
    public void positionalArrayParamsNullValues() throws SQLException {
        int id = baseId() + 2;
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)", //
                new Object[] { id, "NXN-Param-Null", null, "nxn-param-null@test.com", new Date() });

        Map<String, Object> row = jdbcTemplate.queryForMap("SELECT name, age FROM user_info WHERE id = ?", new Object[] { id });

        assertEquals("NXN-Param-Null", value(row, "name"));
        assertNull(value(row, "age"));
    }

    @Test
    @Capability(CapabilityId.JDBC_PARAM_SQLARG)
    public void sqlArgParamsExplicitTypeHandlers() throws SQLException {
        int id = baseId() + 3;
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)", //
                new SqlArg[] { //
                        SqlArg.valueOf(id, new IntegerTypeHandler()), //
                        SqlArg.valueOf("NXN-Param-SqlArg", new StringTypeHandler()), //
                        SqlArg.valueOf(30, new IntegerTypeHandler()), //
                        SqlArg.valueOf("nxn-param-sqlarg@test.com"), //
                        SqlArg.valueOf(new Date()) //
                });

        assertEquals("nxn-param-sqlarg@test.com", jdbcTemplate.queryForObject("SELECT email FROM user_info WHERE id = ?", new Object[] { id }, String.class));
    }

    @Test
    @Capability(CapabilityId.JDBC_PARAM_STATEMENT_SETTER)
    public void preparedStmtSetterParams() throws SQLException {
        int id = baseId() + 4;
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)", ps -> {
            ps.setInt(1, id);
            ps.setString(2, "NXN-Param-Setter");
            ps.setInt(3, 28);
            ps.setString(4, "nxn-param-setter@test.com");
            ps.setDate(5, new java.sql.Date(System.currentTimeMillis()));
        });

        String email = jdbcTemplate.queryForObject("SELECT email FROM user_info WHERE name = ?", ps -> ps.setString(1, "NXN-Param-Setter"), String.class);

        assertEquals("nxn-param-setter@test.com", email);
    }

    @Test
    @Capability(CapabilityId.JDBC_PARAM_NAMED_COLON)
    public void namedColonParamsMapAndBeanValues() throws SQLException {
        int id = baseId() + 5;
        Map<String, Object> insertParams = new HashMap<>();
        insertParams.put("id", id);
        insertParams.put("name", "NXN-Param-Colon");
        insertParams.put("age", 31);
        insertParams.put("email", "nxn-param-colon@test.com");
        insertParams.put("createTime", new Date());
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time) VALUES (:id, :name, :age, :email, :createTime)", insertParams);

        UserInfo queryBean = new UserInfo();
        queryBean.setName("NXN-Param-Colon");
        queryBean.setAge(30);
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_info WHERE name = :name AND age > :age", queryBean, Long.class);

        assertEquals(Long.valueOf(1), count);
    }

    @Test
    @Capability(CapabilityId.JDBC_PARAM_NAMED_BRACE)
    public void namedBraceParamsMapValues() throws SQLException {
        int id = baseId() + 6;
        Map<String, Object> insertParams = new HashMap<>();
        insertParams.put("id", id);
        insertParams.put("name", "NXN-Param-Brace");
        insertParams.put("age", 32);
        insertParams.put("email", "nxn-param-brace@test.com");
        insertParams.put("createTime", new Date());
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time) VALUES (#{id}, #{name}, #{age}, #{email}, #{createTime})", insertParams);

        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_info WHERE id = #{id} AND name = #{name}", insertParams, Long.class);

        assertEquals(Long.valueOf(1), count);
    }

    @Test
    @Capability(CapabilityId.JDBC_PARAM_OGNL)
    public void namedParamsOgnlNestedAndIndexedValues() throws SQLException {
        int id = baseId() + 7;
        insert(id, "NXN-Param-Ognl", 33, "nxn-param-ognl@test.com");

        Map<String, Object> user = new HashMap<>();
        user.put("name", "NXN-Param-Ognl");
        user.put("info", new HashMap<String, Object>() {{
            put("age", 33);
        }});
        Map<String, Object> params = new HashMap<>();
        params.put("user", user);
        params.put("names", new String[] { "NXN-Param-Ognl", "Other" });
        params.put("ids", Arrays.asList(id, id + 1));

        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_info WHERE id = :ids[0] AND name = :names[0] AND age = :user.info.age", params, Long.class);

        assertEquals(Long.valueOf(1), count);
    }

    @Test
    @Capability(CapabilityId.JDBC_PARAM_TEXT_REPLACEMENT)
    public void textReplacementParamsInjectSqlIdsAndOrderClauses() throws SQLException {
        insert(baseId() + 8, "NXN-Param-Text-1", 20, "nxn-param-text-1@test.com");
        insert(baseId() + 9, "NXN-Param-Text-2", 34, "nxn-param-text-2@test.com");

        Map<String, Object> params = new HashMap<>();
        params.put("tableName", "user_info");
        params.put("column", "name");
        params.put("name", "NXN-Param-Text-2");
        params.put("orderBy", "age DESC");

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(//
                "SELECT ${column}, age FROM ${tableName} WHERE ${column} LIKE 'NXN-Param-Text-%' ORDER BY ${orderBy}", params);
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_info WHERE ${column} = #{name}", params, Long.class);

        assertEquals(Long.valueOf(1), count);
        assertEquals(2, rows.size());
        assertEquals(34, ((Number) value(rows.get(0), "age")).intValue());
    }

    @Test
    @Capability(CapabilityId.JDBC_PARAM_RULE_AND_IN_SET)
    public void ruleParamsExpandAndInAndSetClauses() throws SQLException {
        insert(baseId() + 10, "NXN-Param-Rule-1", 24, "nxn-param-rule-1@test.com");
        insert(baseId() + 11, "NXN-Param-Rule-2", 36, "nxn-param-rule-2@test.com");

        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("minAge", 20);
        queryParams.put("names", Arrays.asList("NXN-Param-Rule-1", "NXN-Param-Rule-2"));
        Long count = jdbcTemplate.queryForObject(//
                "SELECT COUNT(*) FROM user_info WHERE age > :minAge @{and, name IN @{in, :names}}", queryParams, Long.class);

        Map<String, Object> updateParams = new HashMap<>();
        updateParams.put("id", baseId() + 10);
        updateParams.put("age", 25);
        updateParams.put("email", null);
        int updated = jdbcTemplate.executeUpdate("UPDATE user_info SET @{set, age = :age} , @{set, email = :email} WHERE id = :id", updateParams);
        Integer newAge = jdbcTemplate.queryForObject("SELECT age FROM user_info WHERE id = ?", new Object[] { baseId() + 10 }, Integer.class);

        assertEquals(Long.valueOf(2), count);
        assertEquals(1, updated);
        assertEquals(Integer.valueOf(25), newAge);
    }

    @Test
    @Capability(CapabilityId.JDBC_PARAM_ARG_SOURCE)
    public void sqlArgSourcesArrayBeanAndMapSources() throws SQLException {
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time) VALUES (:arg0, :arg1, :arg2, :arg3, :arg4)", //
                new ArraySqlArgSource(new Object[] { baseId() + 12, "NXN-Param-Source-Array", 26, "nxn-param-source-array@test.com", new Date() }));

        UserInfo bean = new UserInfo();
        bean.setId(baseId() + 13);
        bean.setName("NXN-Param-Source-Bean");
        bean.setAge(29);
        bean.setEmail("nxn-param-source-bean@test.com");
        bean.setCreateTime(new Date());
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time) VALUES (:id, :name, :age, :email, :createTime)", new BeanSqlArgSource(bean));

        Map<String, Object> map = new HashMap<>();
        map.put("id", baseId() + 14);
        map.put("name", "NXN-Param-Source-Map");
        map.put("age", 34);
        map.put("email", "nxn-param-source-map@test.com");
        map.put("createTime", new Date());
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time) VALUES (#{id}, #{name}, #{age}, #{email}, #{createTime})", new MapSqlArgSource(map));

        Long count = jdbcTemplate.queryForObject(//
                "SELECT COUNT(*) FROM user_info WHERE name IN @{in, :names} AND age > :minAge", //
                new MapSqlArgSource(new HashMap<String, Object>() {{
                    put("names", Arrays.asList("NXN-Param-Source-Array", "NXN-Param-Source-Bean", "NXN-Param-Source-Map"));
                    put("minAge", 25);
                }}), Long.class);

        assertEquals(Long.valueOf(3), count);
    }

    private void insert(int id, String name, int age, String email) throws SQLException {
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)", //
                new Object[] { id, name, age, email, new Date() });
    }

    private Object value(Map<String, Object> row, String key) {
        assertNotNull(row);
        if (row.containsKey(key)) {
            return row.get(key);
        }
        if (row.containsKey(key.toUpperCase())) {
            return row.get(key.toUpperCase());
        }
        return row.get(key.toLowerCase());
    }
}
