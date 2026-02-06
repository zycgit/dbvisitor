/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.dbvisitor.test.oneapi.suite.programmatic;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import net.hasor.dbvisitor.test.oneapi.AbstractOneApiTest;
import net.hasor.dbvisitor.types.SqlArg;
import net.hasor.dbvisitor.types.handler.number.IntegerTypeHandler;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 存储过程测试 - 使用 PostgreSQL PROCEDURE
 * <p>重点测试：</p>
 * <ul>
 *   <li>1. <b>首要测试</b>：使用 JdbcTemplate 高级接口 call() 方法，展示 dbVisitor 对存储过程的支持特性</li>
 *   <li>2. 演示 SqlArg 传参、call(String)、call(String, Object) 等高级封装接口</li>
 *   <li>3. <b>PostgreSQL PROCEDURE 只支持 INOUT 参数</b>（不支持 OUT 参数，这是 PostgreSQL 的设计限制）</li>
 *   <li>4. 涵盖命名参数(:name)和位置参数(?)的参数绑定方式</li>
 *   <li>5. PostgreSQL PROCEDURE 与 JDBC 高级 API 的最佳实践</li>
 * </ul>
 * <h3>测试方法说明</h3>
 * <h4>一、高级接口测试（主要测试场景 - PostgreSQL 存储过程调用）</h4>
 * <ul>
 *   <li>{@link #testHighLevel_Call_SimpleInOut()} - call() 方法调用带 INOUT 参数的存储过程</li>
 *   <li>{@link #testHighLevel_Call_ArrayParams()} - call(String, Object[]) 数组参数</li>
 *   <li>{@link #testHighLevel_Call_MapParams()} - call(String, Map) 命名参数</li>
 *   <li>{@link #testHighLevel_Call_InOut()} - INOUT 参数测试</li>
 *   <li>{@link #testHighLevel_Call_MultipleInOut()} - 多个 INOUT 参数</li>
 *   <li>{@link #testHighLevel_Call_SqlArg()} - 使用 SqlArg 指定类型处理器</li>
 *   <li>{@link #testHighLevel_Call_ComplexParams()} - 复杂参数组合（IN + INOUT）</li>
 * </ul>
 * <h3>技术说明</h3>
 * <ul>
 *   <li>使用 PostgreSQL PROCEDURE（而非 FUNCTION）</li>
 *   <li>使用 {@code CALL procedure_name(params)} 语法调用</li>
 *   <li><b>PostgreSQL PROCEDURE 只支持 INOUT 参数，不支持 OUT 参数</b></li>
 *   <li>通过 INOUT 参数返回结果，不使用 RETURNS</li>
 *   <li>所有存储过程在 setup() 方法中动态创建</li>
 *   <li><b>高级接口测试展示 dbVisitor 的便捷性：call() 方法而非底层 CallableStatement</b></li>
 * </ul>
 */
public class StoredProcedureTest extends AbstractOneApiTest {

    @Before
    @Override
    public void setup() throws IOException, SQLException {
        super.setup();
        createStoredProcedures();
    }

    /**
     * 创建测试用的存储过程（PostgreSQL PROCEDURE，只支持 INOUT 参数）
     */
    private void createStoredProcedures() throws SQLException {
        // 插入测试数据（表已经由 AbstractOneApiTest 创建）
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email) VALUES (1, 'Alice', 25, 'alice@test.com')");
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email) VALUES (2, 'Bob', 30, 'bob@test.com')");
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email) VALUES (3, 'Charlie', 35, 'charlie@test.com')");

        // PostgreSQL PROCEDURE 只支持 INOUT 参数，不支持 OUT 参数

        // 1. 简单的加法存储过程
        jdbcTemplate.execute("DROP PROCEDURE IF EXISTS sp_add_numbers CASCADE");
        jdbcTemplate.execute("CREATE PROCEDURE sp_add_numbers(IN a INT, IN b INT, INOUT result INT) " +//
                "LANGUAGE plpgsql AS $$ BEGIN result := a + b; END $$");

        // 2. 多个 INOUT 参数：计算和、差、积、商
        jdbcTemplate.execute("DROP PROCEDURE IF EXISTS sp_calc_numbers CASCADE");
        jdbcTemplate.execute("CREATE PROCEDURE sp_calc_numbers(" +//
                "IN a INT, IN b INT, " +//
                "INOUT sum_result INT, INOUT diff_result INT, " +//
                "INOUT mult_result INT, INOUT div_result DECIMAL) " +//
                "LANGUAGE plpgsql AS $$ BEGIN " +//
                "sum_result := a + b; diff_result := a - b; " +//
                "mult_result := a * b; div_result := ROUND(a::DECIMAL / b, 2); END $$");

        // 3. 字符串转换：INOUT + IN 参数
        jdbcTemplate.execute("DROP PROCEDURE IF EXISTS sp_transform_string CASCADE");
        jdbcTemplate.execute("CREATE PROCEDURE sp_transform_string(INOUT text_value VARCHAR, IN suffix VARCHAR) " +//
                "LANGUAGE plpgsql AS $$ BEGIN text_value := UPPER(text_value) || suffix; END $$");

        // 4. 查询用户信息：IN + INOUT 参数
        jdbcTemplate.execute("DROP PROCEDURE IF EXISTS sp_get_user_info CASCADE");
        jdbcTemplate.execute("CREATE PROCEDURE sp_get_user_info(" +//
                "IN user_id INT, INOUT user_name VARCHAR, INOUT user_age INT) " +//
                "LANGUAGE plpgsql AS $$ BEGIN " +//
                "SELECT name, age INTO user_name, user_age FROM user_info WHERE id = user_id; END $$");

        // 5. 计数器更新：INOUT 参数用于输入输出
        jdbcTemplate.execute("DROP PROCEDURE IF EXISTS sp_update_counter CASCADE");
        jdbcTemplate.execute("CREATE PROCEDURE sp_update_counter(INOUT counter INT, IN increment INT) " +//
                "LANGUAGE plpgsql AS $$ BEGIN counter := counter + increment; END $$");
    }

    // ========== 高级接口测试：使用 call() 方法 ==========

    /**
     * 测试高级接口 - call() 方法调用带 INOUT 参数的存储过程
     * 展示：使用 call() 方法自动处理 INOUT 参数（输入0，返回结果）
     */
    @Test
    public void testHighLevel_Call_SimpleInOut() throws SQLException {
        // 使用 CALL 语法（不是 {call ...}）
        Map<String, Object> result = jdbcTemplate.call("CALL sp_add_numbers(?, ?, ?)",//
                new Object[] { 10, 5, SqlArg.asInOut("result", 0, java.sql.Types.INTEGER) });

        assertNotNull(result);
        assertTrue(result.containsKey("result"));
        assertEquals(15, result.get("result")); // 10 + 5 = 15
    }

    /**
     * 测试高级接口 - call(String, Object[]) 数组参数
     * 展示：使用位置参数数组传参，包含 IN 和 INOUT
     */
    @Test
    public void testHighLevel_Call_ArrayParams() throws SQLException {
        Object[] params = new Object[] { 20, 8, SqlArg.asInOut("result", 0, java.sql.Types.INTEGER) };
        Map<String, Object> result = jdbcTemplate.call("CALL sp_add_numbers(?, ?, ?)", params);

        assertNotNull(result);
        assertEquals(28, result.get("result")); // 20 + 8 = 28
    }

    /**
     * 测试高级接口 - call(String, Map) 命名参数
     * 展示：使用 Map 传递命名参数
     */
    @Test
    public void testHighLevel_Call_MapParams() throws SQLException {
        Map<String, Object> params = new HashMap<>();
        params.put("a", 15);
        params.put("b", 3);
        params.put("result", SqlArg.asInOut("result", 0, java.sql.Types.INTEGER));

        Map<String, Object> result = jdbcTemplate.call("CALL sp_add_numbers(:a, :b, :result)", params);

        assertNotNull(result);
        assertEquals(18, result.get("result")); // 15 + 3 = 18
    }

    /**
     * 测试高级接口 - INOUT 参数
     * 展示：INOUT 参数的输入输出自动处理
     */
    @Test
    public void testHighLevel_Call_InOut() throws SQLException {
        Object[] params = new Object[] { SqlArg.asInOut("text_value", "hello", java.sql.Types.VARCHAR), "!!!" };
        Map<String, Object> result = jdbcTemplate.call("CALL sp_transform_string(?, ?)", params);

        assertNotNull(result);
        assertTrue(result.containsKey("text_value"));
        assertEquals("HELLO!!!", result.get("text_value")); // "hello" 转大写 + "!!!"
    }

    /**
     * 测试高级接口 - 多个 INOUT 参数
     * 展示：自动映射多个 INOUT 参数到 Map
     */
    @Test
    public void testHighLevel_Call_MultipleInOut() throws SQLException {
        Object[] params = new Object[] {//
                50, //
                10, //
                SqlArg.asInOut("sum_result", 0, java.sql.Types.INTEGER),  //
                SqlArg.asInOut("diff_result", 0, java.sql.Types.INTEGER), //
                SqlArg.asInOut("mult_result", 0, java.sql.Types.INTEGER), //
                SqlArg.asInOut("div_result", java.math.BigDecimal.ZERO, java.sql.Types.DECIMAL) };

        Map<String, Object> result = jdbcTemplate.call("CALL sp_calc_numbers(?, ?, ?, ?, ?, ?)", params);

        assertNotNull(result);
        assertEquals(60, result.get("sum_result"));  // 50 + 10
        assertEquals(40, result.get("diff_result")); // 50 - 10
        assertEquals(500, result.get("mult_result")); // 50 * 10
        assertEquals(new java.math.BigDecimal("5.00"), result.get("div_result")); // 50 / 10
    }

    /**
     * 测试高级接口 - 使用 SqlArg 指定类型处理器
     * 展示：使用 SqlArg 可以精确控制参数的类型处理
     */
    @Test
    public void testHighLevel_Call_SqlArg() throws SQLException {
        SqlArg[] params = new SqlArg[] {//
                SqlArg.valueOf(12, new IntegerTypeHandler()), //
                SqlArg.valueOf(3, new IntegerTypeHandler()),  //
                SqlArg.asInOut("result", 0, java.sql.Types.INTEGER)//
        };

        Map<String, Object> result = jdbcTemplate.call("CALL sp_add_numbers(?, ?, ?)", params);

        assertNotNull(result);
        assertEquals(15, result.get("result")); // 12 + 3 = 15
    }

    /**
     * 测试高级接口 - 复杂参数组合（IN + INOUT）
     * 展示：混合使用 IN、INOUT 参数
     */
    @Test
    public void testHighLevel_Call_ComplexParams() throws SQLException {
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", 1);
        params.put("user_name", SqlArg.asInOut("user_name", "", java.sql.Types.VARCHAR));
        params.put("user_age", SqlArg.asInOut("user_age", 0, java.sql.Types.INTEGER));

        Map<String, Object> result = jdbcTemplate.call("CALL sp_get_user_info(:user_id, :user_name, :user_age)", params);

        assertNotNull(result);
        assertEquals("Alice", result.get("user_name"));
        assertEquals(25, result.get("user_age"));
    }
}
