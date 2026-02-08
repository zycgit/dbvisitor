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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.hasor.cobble.CollectionUtils;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.test.oneapi.AbstractOneApiTest;
import net.hasor.dbvisitor.test.oneapi.model.UserInfo;
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
     * <p>使用 CREATE OR REPLACE 避免 DROP/CREATE 导致 PostgreSQL OID 缓存失效</p>
     */
    private void createStoredProcedures() throws SQLException {
        // 插入测试数据（表已经由 AbstractOneApiTest 创建）
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email) VALUES (1, 'Alice', 25, 'alice@test.com')");
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email) VALUES (2, 'Bob', 30, 'bob@test.com')");
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email) VALUES (3, 'Charlie', 35, 'charlie@test.com')");

        // PostgreSQL PROCEDURE 只支持 INOUT 参数，不支持 OUT 参数
        // 使用 CREATE OR REPLACE 保持 OID 不变，防止连接池缓存失效

        // 1. 简单的加法存储过程
        jdbcTemplate.execute("CREATE OR REPLACE PROCEDURE sp_add_numbers(IN a INT, IN b INT, INOUT result INT) " +//
                "LANGUAGE plpgsql AS $$ BEGIN result := a + b; END $$");

        // 2. 多个 INOUT 参数：计算和、差、积、商
        jdbcTemplate.execute("CREATE OR REPLACE PROCEDURE sp_calc_numbers(" +//
                "IN a INT, IN b INT, " +//
                "INOUT sum_result INT, INOUT diff_result INT, " +//
                "INOUT mult_result INT, INOUT div_result DECIMAL) " +//
                "LANGUAGE plpgsql AS $$ BEGIN " +//
                "sum_result := a + b; diff_result := a - b; " +//
                "mult_result := a * b; div_result := ROUND(a::DECIMAL / b, 2); END $$");

        // 3. 字符串转换：INOUT + IN 参数
        jdbcTemplate.execute("CREATE OR REPLACE PROCEDURE sp_transform_string(INOUT text_value VARCHAR, IN suffix VARCHAR) " +//
                "LANGUAGE plpgsql AS $$ BEGIN text_value := UPPER(text_value) || suffix; END $$");

        // 4. 查询用户信息：IN + INOUT 参数
        jdbcTemplate.execute("CREATE OR REPLACE PROCEDURE sp_get_user_info(" +//
                "IN user_id INT, INOUT user_name VARCHAR, INOUT user_age INT) " +//
                "LANGUAGE plpgsql AS $$ BEGIN " +//
                "SELECT name, age INTO user_name, user_age FROM user_info WHERE id = user_id; END $$");

        // 5. 计数器更新：INOUT 参数用于输入输出
        jdbcTemplate.execute("CREATE OR REPLACE PROCEDURE sp_update_counter(INOUT counter INT, IN increment INT) " +//
                "LANGUAGE plpgsql AS $$ BEGIN counter := counter + increment; END $$");

        // 6. 游标查询：通过 INOUT refcursor 返回结果集
        jdbcTemplate.execute("CREATE OR REPLACE PROCEDURE sp_cursor_users(" +//
                "IN p_name VARCHAR, INOUT p_cursor refcursor) " +//
                "LANGUAGE plpgsql AS $$ BEGIN " +//
                "OPEN p_cursor FOR SELECT id, name, age, email FROM user_info WHERE name = p_name; " +//
                "END $$");
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

    // ========== #{...} 参数传递测试 ==========

    /**
     * 测试 #{...} 参数 - 基础 INOUT 参数，指定 jdbcType
     * <p>参考 ProcedureTest.call_1：使用 #{name,mode=inout,jdbcType=xxx} 方式声明 INOUT 参数</p>
     */
    @Test
    public void testCall_HashParam_BasicInOut() throws SQLException {
        Map<String, Object> result = jdbcTemplate.call("CALL sp_add_numbers(" +//
                        "#{a,jdbcType=integer}, " + //
                        "#{b,jdbcType=integer}, " + //
                        "#{result,mode=inout,jdbcType=integer})",//
                CollectionUtils.asMap("a", 7, "b", 8, "result", 0));

        assertNotNull(result);
        assertEquals(15, result.get("result")); // 7 + 8 = 15
    }

    /**
     * 测试 #{...} 参数 - INOUT 参数使用 javaType 代替 jdbcType
     * <p>参考 ProcedureTest.procedure_out_3：使用 javaType=java.lang.Integer 方式</p>
     */
    @Test
    public void testCall_HashParam_InOut_WithJavaType() throws SQLException {
        Map<String, Object> result = jdbcTemplate.call("CALL sp_add_numbers(" + //
                        "#{a,jdbcType=integer}, " + //
                        "#{b,jdbcType=integer}, " + //
                        "#{result,mode=inout,javaType=java.lang.Integer})", //
                CollectionUtils.asMap("a", 9, "b", 6, "result", 0));

        assertNotNull(result);
        assertEquals(15, result.get("result")); // 9 + 6 = 15
    }

    /**
     * 测试 #{...} 参数 - INOUT 参数使用 typeHandler
     * <p>参考 ProcedureTest.procedure_out_1：使用 typeHandler=xxx 精确控制类型处理</p>
     */
    @Test
    public void testCall_HashParam_InOut_WithTypeHandler() throws SQLException {
        Map<String, Object> result = jdbcTemplate.call("CALL sp_add_numbers(" + //
                        "#{a,jdbcType=integer}, " + //
                        "#{b,jdbcType=integer}, " + //
                        "#{result,mode=inout,jdbcType=integer,typeHandler=net.hasor.dbvisitor.types.handler.number.IntegerTypeHandler})",//
                CollectionUtils.asMap("a", 11, "b", 4, "result", 0));

        assertNotNull(result);
        assertEquals(15, result.get("result")); // 11 + 4 = 15
    }

    /**
     * 测试 #{...} 参数 - 多个 INOUT 参数
     * <p>展示多个 #{...} INOUT 参数在一个存储过程调用中的使用</p>
     */
    @Test
    public void testCall_HashParam_MultipleInOut() throws SQLException {
        Map<String, Object> args = new HashMap<>();
        args.put("a", 12);
        args.put("b", 4);
        args.put("sum_result", 0);
        args.put("diff_result", 0);
        args.put("mult_result", 0);
        args.put("div_result", java.math.BigDecimal.ZERO);

        Map<String, Object> result = jdbcTemplate.call("CALL sp_calc_numbers(" + //
                "#{a,jdbcType=integer}, " + //
                "#{b,jdbcType=integer}, " + //
                "#{sum_result,mode=inout,jdbcType=integer}, " +  //
                "#{diff_result,mode=inout,jdbcType=integer}, " + //
                "#{mult_result,mode=inout,jdbcType=integer}, " + //
                "#{div_result,mode=inout,jdbcType=decimal})", args);

        assertNotNull(result);
        assertEquals(16, result.get("sum_result"));  // 12 + 4
        assertEquals(8, result.get("diff_result"));  // 12 - 4
        assertEquals(48, result.get("mult_result")); // 12 * 4
        assertEquals(new java.math.BigDecimal("3.00"), result.get("div_result")); // 12 / 4
    }

    /**
     * 测试 #{...} 参数 - 字符串类型 INOUT 参数同时作为输入输出
     * <p>展示 varchar 类型 INOUT 参数的 #{...} 用法</p>
     */
    @Test
    public void testCall_HashParam_StringInOut() throws SQLException {
        Map<String, Object> result = jdbcTemplate.call(//
                "CALL sp_transform_string(#{text_value,mode=inout,jdbcType=varchar}, #{suffix,jdbcType=varchar})",//
                CollectionUtils.asMap("text_value", "abc", "suffix", "123"));

        assertNotNull(result);
        assertEquals("ABC123", result.get("text_value"));
    }

    /**
     * 测试 #{...} 参数 - 使用 name 属性为输出参数指定别名
     * <p>参考 ProcedureTest.inout_1：使用 name=xxx 属性将返回值映射到不同的 key</p>
     */
    @Test
    public void testCall_HashParam_NameAlias() throws SQLException {
        Map<String, Object> result = jdbcTemplate.call("CALL sp_update_counter(" + //
                        "#{counter,name=cnt,mode=inout,jdbcType=integer}, " +//
                        "#{increment,jdbcType=integer})",//
                CollectionUtils.asMap("counter", 100, "increment", 25));

        assertNotNull(result);
        assertEquals(125, result.get("cnt")); // 100 + 25 = 125, output mapped to "cnt"
    }

    /**
     * 测试 #{...} 参数 - IN + INOUT 复杂参数组合查询用户
     * <p>参考文档 procedure.mdx：使用 #{...} 混合 IN 和 INOUT 参数</p>
     */
    @Test
    public void testCall_HashParam_QueryUserInfo() throws SQLException {
        Map<String, Object> result = jdbcTemplate.call("CALL sp_get_user_info(" +//
                        "#{user_id,jdbcType=integer}, " +              //
                        "#{user_name,mode=inout,jdbcType=varchar}, " + //
                        "#{user_age,mode=inout,jdbcType=integer})",    //
                CollectionUtils.asMap("user_id", 2, "user_name", "", "user_age", 0));

        assertNotNull(result);
        assertEquals("Bob", result.get("user_name"));
        assertEquals(30, result.get("user_age"));
    }

    /**
     * 测试 #{...} 参数 - INOUT 参数真正作为输入输出双向使用
     * <p>展示 INOUT 参数的输入值被存储过程修改后返回</p>
     */
    @Test
    public void testCall_HashParam_CounterUpdate() throws SQLException {
        Map<String, Object> result = jdbcTemplate.call(//
                "CALL sp_update_counter(#{counter,mode=inout,jdbcType=integer}, #{increment,jdbcType=integer})",//
                CollectionUtils.asMap("counter", 50, "increment", 30));

        assertNotNull(result);
        assertEquals(80, result.get("counter")); // 50 + 30 = 80
    }

    /**
     * 测试 #{...} 参数 - IN 参数省略 jdbcType（默认 mode=in，框架自动推断类型）
     * <p>参考 ProcedureTest.inout_1：#{inName} 不指定 jdbcType 也可正常工作</p>
     */
    @Test
    public void testCall_HashParam_InParamNoJdbcType() throws SQLException {
        Map<String, Object> result = jdbcTemplate.call(//
                "CALL sp_add_numbers(#{a}, #{b}, #{result,mode=inout,jdbcType=integer})",//
                CollectionUtils.asMap("a", 3, "b", 7, "result", 0));

        assertNotNull(result);
        assertEquals(10, result.get("result")); // 3 + 7 = 10
    }

    // ========== #{...} 游标参数测试 ==========

    /**
     * 测试 #{...} 参数 - 游标方式查询结果集，映射为 javaType
     * <p>参考 ProcedureTest.cursor_result_as_javaType_1：</p>
     * <ul>
     *   <li>使用 {@code #{res,mode=cursor,javaType=...}} 声明游标出参</li>
     *   <li>游标结果集自动映射为指定 javaType 的 List</li>
     *   <li>PostgreSQL 游标需要在事务中使用（autoCommit=false）</li>
     * </ul>
     */
    @Test
    public void testCall_HashParam_CursorResult() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            JdbcTemplate txJdbc = new JdbcTemplate(conn);

            // 方式1：#{res,mode=cursor,javaType=...}
            Map<String, Object> result1 = txJdbc.call("call sp_cursor_users(" +//
                            "#{p_name,jdbcType=varchar}, " + //
                            "#{res,mode=cursor,javaType=net.hasor.dbvisitor.test.oneapi.model.UserInfo})",//
                    CollectionUtils.asMap("p_name", "Alice"));

            assertNotNull(result1);
            assertTrue(result1.get("res") instanceof List);
            List<?> list1 = (List<?>) result1.get("res");
            assertEquals(1, list1.size());
            UserInfo user1 = (UserInfo) list1.get(0);
            assertEquals("Alice", user1.getName());
            assertEquals(Integer.valueOf(25), user1.getAge());
            conn.commit();

            // 方式2：#{name=res,mode=cursor,javaType=...}（使用 name= 属性指定输出名称）
            Map<String, Object> result2 = txJdbc.call("call sp_cursor_users(" +//
                            "#{p_name,jdbcType=varchar}, " + //
                            "#{name=res,mode=cursor,javaType=net.hasor.dbvisitor.test.oneapi.model.UserInfo})",//
                    CollectionUtils.asMap("p_name", "Bob"));

            assertNotNull(result2);
            assertTrue(result2.get("res") instanceof List);
            List<?> list2 = (List<?>) result2.get("res");
            assertEquals(1, list2.size());
            UserInfo user2 = (UserInfo) list2.get(0);
            assertEquals("Bob", user2.getName());
            assertEquals(Integer.valueOf(30), user2.getAge());
            conn.commit();
        }
    }
}
