package net.hasor.dbvisitor.test.oneapi.suite.programmatic;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.jdbc.CallableStatementCallback;
import net.hasor.dbvisitor.jdbc.CallableStatementSetter;
import net.hasor.dbvisitor.test.oneapi.AbstractOneApiTest;
import net.hasor.dbvisitor.types.SqlArg;
import net.hasor.dbvisitor.types.handler.number.IntegerTypeHandler;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 存储函数测试 - 使用 PostgreSQL FUNCTION
 * <p>重点测试：</p>
 * <ul>
 *   <li>1. <b>首要测试</b>：使用 JdbcTemplate 高级接口，展示 dbVisitor 对存储函数的支持特性</li>
 *   <li>2. 演示 SqlArg 传参、query/queryForObject/queryForList 等高级封装接口</li>
 *   <li>3. 涵盖 IN 参数、多返回值、TABLE 返回类型等场景</li>
 *   <li>4. 涵盖命名参数(:name)和位置参数(?)的参数绑定方式</li>
 *   <li>5. PostgreSQL FUNCTION 与 JDBC 高级 API 的最佳实践</li>
 * </ul>
 * <h3>测试方法说明</h3>
 * <h4>一、高级接口测试（主要测试场景 - PostgreSQL 函数调用）</h4>
 * <ul>
 *   <li>{@link #testHighLevel_Query_NoParams()} - queryForMap() 无参数查询，展示最简洁的 API</li>
 *   <li>{@link #testHighLevel_Query_ArrayParams()} - queryForObject() 数组参数，展示位置参数绑定</li>
 *   <li>{@link #testHighLevel_Query_MapParams()} - queryForObject() 命名参数，展示 :paramName 语法</li>
 *   <li>{@link #testHighLevel_Query_SingleParam()} - queryForObject() 单个参数，简化参数传递</li>
 *   <li>{@link #testHighLevel_SqlArg()} - 使用 SqlArg 指定类型处理器，展示高级类型映射</li>
 *   <li>{@link #testHighLevel_MultiOutParams()} - 多个返回值自动映射为 Map，展示输出参数处理</li>
 *   <li>{@link #testHighLevel_NamedParamsWithMultiOut()} - 命名参数 + 多返回值组合场景</li>
 *   <li>{@link #testHighLevel_StringTransform()} - 函数字符串转换，展示字符串处理</li>
 *   <li>{@link #testHighLevel_TableResult()} - 使用 queryForList 处理返回多行的存储过程</li>
 *   <li>{@link #testHighLevel_TableResultWithParams()} - 带参数返回多行记录</li>
 * </ul>
 * <h4>二、底层接口测试（覆盖完整性测试 - 使用 CallableStatement）</h4>
 * <ul>
 *   <li>{@link #testStoredFunction_InParams()} - 测试 IN 参数（简单数值计算）</li>
 *   <li>{@link #testStoredFunction_OutParams()} - 测试 OUT 参数（多个输出值）</li>
 *   <li>{@link #testStoredFunction_InOutParams()} - 测试 INOUT 参数（字符串转换）</li>
 *   <li>{@link #testStoredFunction_ComplexParams()} - 测试 IN + OUT + INOUT 混合场景</li>
 *   <li>{@link #testStoredFunction_MultipleResultSets()} - 测试多结果集输出</li>
 *   <li>{@link #testStoredFunction_CursorOutParam()} - 测试返回多行记录（TABLE 类型）</li>
 *   <li>{@link #testStoredFunction_CursorWithFilter()} - 测试带过滤条件的多行记录返回</li>
 *   <li>{@link #testStoredFunction_NullHandling()} - 测试 NULL 值处理</li>
 * </ul>
 * <h3>技术说明</h3>
 * <ul>
 *   <li>使用 PostgreSQL FUNCTION（而非 PROCEDURE）</li>
 *   <li>使用 {@code SELECT function_name(params)} 语法调用函数</li>
 *   <li>使用 TABLE 返回类型代替 REFCURSOR（简化 JDBC 处理逻辑）</li>
 *   <li>所有存储函数在 setup() 方法中动态创建</li>
 *   <li><b>高级接口测试展示 dbVisitor 的便捷性：query/queryForObject/queryForList而非底层 CallableStatement</b></li>
 *   <li><b>PostgreSQL 函数返回值直接作为查询结果，无需 OUT 参数</b></li>
 * </ul>
 */
public class StoredFunctionTest extends AbstractOneApiTest {

    @Before
    @Override
    public void setup() throws IOException, SQLException {
        super.setup();

        // 创建存储函数（PostgreSQL FUNCTION）
        createStoredFunctions();

        // 确保测试数据存在
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email) VALUES (1, 'Alice', 25, 'alice@test.com')");
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email) VALUES (2, 'Bob', 30, 'bob@test.com')");
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email) VALUES (3, 'Charlie', 35, 'charlie@test.com')");

        jdbcTemplate.executeUpdate("INSERT INTO basic_types_test (id, int_value, string_value) VALUES (1, 100, 'Test1')");
        jdbcTemplate.executeUpdate("INSERT INTO basic_types_test (id, int_value, string_value) VALUES (2, 200, 'Test2')");
    }

    /**
     * 创建测试所需的存储函数
     */
    private void createStoredFunctions() throws SQLException {
        // 删除旧的函数定义（如果存在）
        jdbcTemplate.execute("DROP FUNCTION IF EXISTS proc_add_numbers CASCADE");
        jdbcTemplate.execute("DROP FUNCTION IF EXISTS proc_calc_numbers CASCADE");
        jdbcTemplate.execute("DROP FUNCTION IF EXISTS proc_transform_string CASCADE");
        jdbcTemplate.execute("DROP FUNCTION IF EXISTS proc_multi_resultsets CASCADE");
        jdbcTemplate.execute("DROP FUNCTION IF EXISTS proc_get_users_cursor CASCADE");
        jdbcTemplate.execute("DROP FUNCTION IF EXISTS proc_complex_params CASCADE");
        jdbcTemplate.execute("DROP FUNCTION IF EXISTS proc_filter_users CASCADE");

        // 1. 测试 IN 参数：两个数字相加
        jdbcTemplate.execute("CREATE OR REPLACE FUNCTION proc_add_numbers(a INT, b INT) " + "RETURNS INT AS $$ BEGIN RETURN a + b; END; $$ LANGUAGE plpgsql");

        // 2. 测试 OUT 参数：计算两个数字的和、差、积、商
        jdbcTemplate.execute("CREATE OR REPLACE FUNCTION proc_calc_numbers(" + "IN a INT, IN b INT, " + "OUT sum_result INT, OUT diff_result INT, OUT mult_result INT, OUT div_result NUMERIC) " + "AS $$ BEGIN " + "sum_result := a + b; " + "diff_result := a - b; " + "mult_result := a * b; " + "div_result := a::NUMERIC / b; " + "END; $$ LANGUAGE plpgsql");

        // 3. 测试 INOUT 参数：字符串转大写并追加后缀
        jdbcTemplate.execute("CREATE OR REPLACE FUNCTION proc_transform_string(" + "INOUT text_value VARCHAR, IN suffix VARCHAR) " + "AS $$ BEGIN " + "text_value := UPPER(text_value) || suffix; " + "END; $$ LANGUAGE plpgsql");

        // 4. 测试多结果集：返回用户和基础数据的组合
        jdbcTemplate.execute("CREATE OR REPLACE FUNCTION proc_multi_resultsets() " + "RETURNS TABLE(result_set INT, name VARCHAR, value INT) " + "AS $$ BEGIN " + "RETURN QUERY SELECT 1 as result_set, u.name::VARCHAR, u.age as value " + "FROM user_info u ORDER BY u.id LIMIT 3; " + "RETURN QUERY SELECT 2 as result_set, b.string_value::VARCHAR, b.int_value as value " + "FROM basic_types_test b ORDER BY b.id LIMIT 2; " + "END; $$ LANGUAGE plpgsql");

        // 5. 测试游标 OUT 参数：返回用户列表（改为直接返回 TABLE）
        jdbcTemplate.execute("CREATE OR REPLACE FUNCTION proc_get_users_cursor() " + "RETURNS TABLE(id INT, name VARCHAR, age INT, email VARCHAR) " + "AS $$ BEGIN " + "RETURN QUERY SELECT u.id, u.name, u.age, u.email FROM user_info u ORDER BY u.id; " + "END; $$ LANGUAGE plpgsql");

        // 6. 测试复杂场景：IN + OUT + INOUT
        jdbcTemplate.execute("CREATE OR REPLACE FUNCTION proc_complex_params(" + "IN input_id INT, INOUT counter INT, OUT user_name VARCHAR, OUT user_age INT) " + "AS $$ BEGIN " + "counter := counter + 1; " + "SELECT name, age INTO user_name, user_age FROM user_info WHERE id = input_id; " + "IF user_name IS NULL THEN user_name := 'Unknown'; user_age := 0; END IF; " + "END; $$ LANGUAGE plpgsql");

        // 7. 测试游标参数（多行结果）：带过滤条件的用户查询（改为直接返回 TABLE）
        jdbcTemplate.execute("CREATE OR REPLACE FUNCTION proc_filter_users(IN min_age INT) " + "RETURNS TABLE(id INT, name VARCHAR, age INT) " + "AS $$ BEGIN " + "RETURN QUERY SELECT u.id, u.name, u.age FROM user_info u WHERE u.age >= min_age ORDER BY u.age, u.id; " + "END; $$ LANGUAGE plpgsql");

        // 8. 测试无参数调用：返回当前用户数量
        jdbcTemplate.execute("DROP FUNCTION IF EXISTS proc_get_user_count CASCADE");
        jdbcTemplate.execute("CREATE OR REPLACE FUNCTION proc_get_user_count() " + "RETURNS INT AS $$ DECLARE count INT; BEGIN SELECT COUNT(*) INTO count FROM user_info; RETURN count; END; $$ LANGUAGE plpgsql");

        // 9. 测试单个参数：根据 ID 获取用户名
        jdbcTemplate.execute("DROP FUNCTION IF EXISTS proc_get_username CASCADE");
        jdbcTemplate.execute("CREATE OR REPLACE FUNCTION proc_get_username(user_id INT) " + "RETURNS VARCHAR AS $$ DECLARE username VARCHAR; BEGIN SELECT name INTO username FROM user_info WHERE id = user_id; RETURN username; END; $$ LANGUAGE plpgsql");

        // 10. 测试命名参数：使用 Map 传递参数计算乘法
        jdbcTemplate.execute("DROP FUNCTION IF EXISTS proc_multiply CASCADE");
        jdbcTemplate.execute("CREATE OR REPLACE FUNCTION proc_multiply(x INT, y INT) " + "RETURNS INT AS $$ BEGIN RETURN x * y; END; $$ LANGUAGE plpgsql");
    }

    // ========== 场景 1: IN 参数测试 ==========

    /**
     * 测试存储过程 - 多个 IN 参数
     * 使用 jdbcTemplate.call() 调用存储过程
     */
    @Test
    public void testStoredFunction_InParams() throws SQLException {
        // PostgreSQL 函数调用：SELECT proc_add_numbers(10, 20)
        Integer result = jdbcTemplate.call("SELECT proc_add_numbers(?, ?)", new CallableStatementSetter() {
            @Override
            public void setValues(CallableStatement cs) throws SQLException {
                cs.setInt(1, 10);
                cs.setInt(2, 20);
            }
        }, new CallableStatementCallback<Integer>() {
            @Override
            public Integer doInCallableStatement(CallableStatement cs) throws SQLException {
                ResultSet rs = cs.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return null;
            }
        });

        assertNotNull(result);
        assertEquals(30, result.intValue());  // 10 + 20 = 30
    }

    // ========== 场景 2: OUT 参数测试 ==========

    /**
     * 测试存储过程 - OUT 参数
     * 调用 proc_calc_numbers 返回和、差、积、商四个 OUT 参数
     */
    @Test
    public void testStoredFunction_OutParams() throws SQLException {
        // PostgreSQL 函数有 OUT 参数时，使用 SELECT * FROM 调用
        Map<String, Object> result = jdbcTemplate.call("SELECT * FROM proc_calc_numbers(?, ?)", new CallableStatementSetter() {
            @Override
            public void setValues(CallableStatement cs) throws SQLException {
                cs.setInt(1, 100);  // IN: a
                cs.setInt(2, 20);   // IN: b
            }
        }, new CallableStatementCallback<Map<String, Object>>() {
            @Override
            public Map<String, Object> doInCallableStatement(CallableStatement cs) throws SQLException {
                ResultSet rs = cs.executeQuery();
                Map<String, Object> outValues = new HashMap<>();
                if (rs.next()) {
                    outValues.put("sum_result", rs.getInt("sum_result"));
                    outValues.put("diff_result", rs.getInt("diff_result"));
                    outValues.put("mult_result", rs.getInt("mult_result"));
                    outValues.put("div_result", rs.getBigDecimal("div_result"));
                }
                return outValues;
            }
        });

        assertNotNull(result);
        assertEquals(120, result.get("sum_result"));      // 100 + 20
        assertEquals(80, result.get("diff_result"));      // 100 - 20
        assertEquals(2000, result.get("mult_result"));    // 100 * 20
        // BigDecimal 比较（stripTrailingZeros 去除尾随零）
        assertEquals(new java.math.BigDecimal("5"), ((java.math.BigDecimal) result.get("div_result")).stripTrailingZeros());  // 100 / 20
    }

    // ========== 场景 3: INOUT 参数测试 ==========

    /**
     * 测试存储过程 - INOUT 参数
     * 调用 proc_transform_string 传入字符串并转大写追加后缀
     */
    @Test
    public void testStoredFunction_InOutParams() throws SQLException {
        // PostgreSQL 函数有 INOUT 参数时，使用 SELECT * FROM 调用
        String result = jdbcTemplate.call("SELECT * FROM proc_transform_string(?, ?)", new CallableStatementSetter() {
            @Override
            public void setValues(CallableStatement cs) throws SQLException {
                cs.setString(1, "hello");   // INOUT: text_value (输入 "hello")
                cs.setString(2, " WORLD");  // IN: suffix
            }
        }, new CallableStatementCallback<String>() {
            @Override
            public String doInCallableStatement(CallableStatement cs) throws SQLException {
                ResultSet rs = cs.executeQuery();
                if (rs.next()) {
                    return rs.getString(1);  // 获取 INOUT 参数的输出值
                }
                return null;
            }
        });

        assertNotNull(result);
        assertEquals("HELLO WORLD", result);  // 输入 "hello" 转大写并追加 " WORLD"
    }

    /**
     * 测试存储过程 - 复杂场景：IN + OUT + INOUT 混合
     * 调用 proc_complex_params
     */
    @Test
    public void testStoredFunction_ComplexParams() throws SQLException {
        Map<String, Object> result = jdbcTemplate.call("SELECT * FROM proc_complex_params(?, ?)", new CallableStatementSetter() {
            @Override
            public void setValues(CallableStatement cs) throws SQLException {
                cs.setInt(1, 1);   // IN: input_id (查询 id=1 的用户)
                cs.setInt(2, 10);  // INOUT: counter (输入 10)
            }
        }, new CallableStatementCallback<Map<String, Object>>() {
            @Override
            public Map<String, Object> doInCallableStatement(CallableStatement cs) throws SQLException {
                ResultSet rs = cs.executeQuery();
                Map<String, Object> outValues = new HashMap<>();
                if (rs.next()) {
                    outValues.put("counter", rs.getInt("counter"));
                    outValues.put("user_name", rs.getString("user_name"));
                    outValues.put("user_age", rs.getInt("user_age"));
                }
                return outValues;
            }
        });

        assertNotNull(result);
        assertEquals(11, result.get("counter"));        // INOUT: 10 + 1 = 11
        assertEquals("Alice", result.get("user_name")); // OUT: 用户名
        assertEquals(25, result.get("user_age"));       // OUT: 年龄
    }

    // ========== 场景 4: 多结果集测试 ==========

    /**
     * 测试存储过程 - 多结果集输出
     * 调用 proc_multi_resultsets 返回多行数据
     */
    @Test
    public void testStoredFunction_MultipleResultSets() throws SQLException {
        List<Map<String, Object>> results = jdbcTemplate.call("SELECT * FROM proc_multi_resultsets()", new CallableStatementSetter() {
            @Override
            public void setValues(CallableStatement cs) throws SQLException {
                // 无参数
            }
        }, new CallableStatementCallback<List<Map<String, Object>>>() {
            @Override
            public List<Map<String, Object>> doInCallableStatement(CallableStatement cs) throws SQLException {
                ResultSet rs = cs.executeQuery();
                List<Map<String, Object>> resultList = new java.util.ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("result_set", rs.getInt("result_set"));
                    row.put("name", rs.getString("name"));
                    row.put("value", rs.getInt("value"));
                    resultList.add(row);
                }
                return resultList;
            }
        });

        assertNotNull(results);
        assertTrue(results.size() >= 5);  // 至少有 3 个用户 + 2 个基础数据
    }

    // ========== 场景 5: OUT 游标参数测试 ==========

    /**
     * 测试存储过程 - 返回多行记录（TABLE 类型）
     * 调用 proc_get_users_cursor 返回用户列表
     * 注意：为简化测试，改为返回 TABLE 而不是 REFCURSOR
     */
    @Test
    public void testStoredFunction_CursorOutParam() throws SQLException {
        List<Map<String, Object>> users = jdbcTemplate.call("SELECT * FROM proc_get_users_cursor()", new CallableStatementSetter() {
            @Override
            public void setValues(CallableStatement cs) throws SQLException {
                // 无参数
            }
        }, new CallableStatementCallback<List<Map<String, Object>>>() {
            @Override
            public List<Map<String, Object>> doInCallableStatement(CallableStatement cs) throws SQLException {
                ResultSet rs = cs.executeQuery();
                List<Map<String, Object>> userList = new java.util.ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> user = new HashMap<>();
                    user.put("id", rs.getInt("id"));
                    user.put("name", rs.getString("name"));
                    user.put("age", rs.getInt("age"));
                    user.put("email", rs.getString("email"));
                    userList.add(user);
                }
                return userList;
            }
        });

        assertNotNull(users);
        assertEquals(3, users.size());
        assertEquals("Alice", users.get(0).get("name"));
        assertEquals(25, users.get(0).get("age"));
    }

    /**
     * 测试存储过程 - 带过滤条件返回多行记录
     * 调用 proc_filter_users 根据最小年龄过滤用户
     * 注意：为简化测试，改为返回 TABLE 而不是 REFCURSOR
     */
    @Test
    public void testStoredFunction_CursorWithFilter() throws SQLException {
        List<Map<String, Object>> users = jdbcTemplate.call("SELECT * FROM proc_filter_users(?)", new CallableStatementSetter() {
            @Override
            public void setValues(CallableStatement cs) throws SQLException {
                cs.setInt(1, 30);  // IN: min_age (最小年龄 30)
            }
        }, new CallableStatementCallback<List<Map<String, Object>>>() {
            @Override
            public List<Map<String, Object>> doInCallableStatement(CallableStatement cs) throws SQLException {
                ResultSet rs = cs.executeQuery();
                List<Map<String, Object>> userList = new java.util.ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> user = new HashMap<>();
                    user.put("id", rs.getInt("id"));
                    user.put("name", rs.getString("name"));
                    user.put("age", rs.getInt("age"));
                    userList.add(user);
                }
                return userList;
            }
        });

        assertNotNull(users);
        assertEquals(2, users.size());  // Bob(30) 和 Charlie(35)
        assertEquals("Bob", users.get(0).get("name"));
        assertEquals(30, users.get(0).get("age"));
        assertEquals("Charlie", users.get(1).get("name"));
        assertEquals(35, users.get(1).get("age"));
    }

    // ========== 场景 6: NULL 值处理 ==========

    /**
     * 测试存储过程 - NULL 值处理
     */
    @Test
    public void testStoredFunction_NullHandling() throws SQLException {
        Map<String, Object> result = jdbcTemplate.call("SELECT * FROM proc_complex_params(?, ?)", new CallableStatementSetter() {
            @Override
            public void setValues(CallableStatement cs) throws SQLException {
                cs.setInt(1, 999);  // IN: input_id (不存在的用户 ID)
                cs.setInt(2, 5);    // INOUT: counter
            }
        }, new CallableStatementCallback<Map<String, Object>>() {
            @Override
            public Map<String, Object> doInCallableStatement(CallableStatement cs) throws SQLException {
                ResultSet rs = cs.executeQuery();
                Map<String, Object> outValues = new HashMap<>();
                if (rs.next()) {
                    outValues.put("counter", rs.getInt("counter"));
                    outValues.put("user_name", rs.getString("user_name"));
                    outValues.put("user_age", rs.getInt("user_age"));
                }
                return outValues;
            }
        });

        assertNotNull(result);
        assertEquals(6, result.get("counter"));         // INOUT: 5 + 1 = 6
        assertEquals("Unknown", result.get("user_name")); // 未找到用户时的默认值
        assertEquals(0, result.get("user_age"));         // 默认年龄
    }

    // ========== 高级接口测试：避免直接操作 JDBC ==========

    /**
     * 测试高级接口 - queryForMap() 无参数调用
     * 展示：直接查询存储过程，无需手动管理 CallableStatement
     */
    @Test
    public void testHighLevel_Query_NoParams() throws SQLException {
        // PostgreSQL 函数作为查询结果返回
        Map<String, Object> result = jdbcTemplate.queryForMap("SELECT proc_get_user_count() as user_count");

        assertNotNull(result);
        assertEquals(3, result.get("user_count")); // 3 个用户
    }

    /**
     * 测试高级接口 - queryForObject() 数组参数
     * 展示：使用位置参数数组传参，自动处理参数绑定
     */
    @Test
    public void testHighLevel_Query_ArrayParams() throws SQLException {
        // 使用数组传递位置参数
        Object[] params = new Object[] { 10, 5 };
        Integer result = jdbcTemplate.queryForObject("SELECT proc_add_numbers(?, ?) as result", params, Integer.class);

        assertNotNull(result);
        assertEquals(Integer.valueOf(15), result); // 10 + 5 = 15
    }

    /**
     * 测试高级接口 - queryForObject() 命名参数
     * 展示：使用 Map 传递命名参数，支持 :paramName 语法
     */
    @Test
    public void testHighLevel_Query_MapParams() throws SQLException {
        // 使用 Map 传递命名参数
        Map<String, Object> params = new HashMap<>();
        params.put("x", 7);
        params.put("y", 8);

        Integer result = jdbcTemplate.queryForObject("SELECT proc_multiply(:x, :y) as result", params, Integer.class);

        assertNotNull(result);
        assertEquals(Integer.valueOf(56), result); // 7 * 8 = 56
    }

    /**
     * 测试高级接口 - queryForObject() 单个参数
     * 展示：传递单个参数，简化参数传递
     */
    @Test
    public void testHighLevel_Query_SingleParam() throws SQLException {
        // 使用单个参数
        Integer userId = 1;
        String result = jdbcTemplate.queryForObject("SELECT proc_get_username(?) as username", userId, String.class);

        assertNotNull(result);
        assertEquals("Alice", result);
    }

    /**
     * 测试高级接口 - 使用 SqlArg 指定类型处理器
     * 展示：使用 SqlArg 可以精确控制参数的类型处理
     */
    @Test
    public void testHighLevel_SqlArg() throws SQLException {
        // 使用 SqlArg 指定 TypeHandler
        SqlArg[] params = new SqlArg[] { SqlArg.valueOf(12, new IntegerTypeHandler()), SqlArg.valueOf(3, new IntegerTypeHandler()) };

        Integer result = jdbcTemplate.queryForObject("SELECT proc_add_numbers(?, ?) as result", params, Integer.class);

        assertNotNull(result);
        assertEquals(Integer.valueOf(15), result); // 12 + 3 = 15
    }

    /**
     * 测试高级接口 - 多个 OUT 参数返回
     * 展示：多个返回值自动映射为 Map 的键值对
     */
    @Test
    public void testHighLevel_MultiOutParams() throws SQLException {
        // PostgreSQL 函数返回 TABLE 类型,结果作为单行记录
        Object[] params = new Object[] { 50, 10 };
        Map<String, Object> result = jdbcTemplate.queryForMap("SELECT * FROM proc_calc_numbers(?, ?)", params);

        assertNotNull(result);
        assertEquals(60, result.get("sum_result"));      // 50 + 10
        assertEquals(40, result.get("diff_result"));     // 50 - 10
        assertEquals(500, result.get("mult_result"));    // 50 * 10
        assertEquals(new java.math.BigDecimal("5"), ((java.math.BigDecimal) result.get("div_result")).stripTrailingZeros()); // 50 / 10
    }

    /**
     * 测试高级接口 - 结合命名参数和 OUT 参数
     * 展示：命名参数 + OUT 参数的完整场景
     */
    @Test
    public void testHighLevel_NamedParamsWithMultiOut() throws SQLException {
        Map<String, Object> params = new HashMap<>();
        params.put("a", 100);
        params.put("b", 25);

        Map<String, Object> result = jdbcTemplate.queryForMap("SELECT * FROM proc_calc_numbers(:a, :b)", params);

        assertNotNull(result);
        assertEquals(125, result.get("sum_result"));     // 100 + 25
        assertEquals(75, result.get("diff_result"));     // 100 - 25
        assertEquals(2500, result.get("mult_result"));   // 100 * 25
        assertEquals(new java.math.BigDecimal("4"), ((java.math.BigDecimal) result.get("div_result")).stripTrailingZeros()); // 100 / 25
    }

    /**
     * 测试高级接口 - 函数字符串转换
     * 展示：函数返回转换后的字符串
     */
    @Test
    public void testHighLevel_StringTransform() throws SQLException {
        Object[] params = new Object[] { "world", "!" };
        Map<String, Object> result = jdbcTemplate.queryForMap("SELECT * FROM proc_transform_string(?, ?)", params);

        assertNotNull(result);
        assertEquals("WORLD!", result.get("text_value")); // "world" 转大写 + "!"
    }

    /**
     * 测试高级接口 - 返回多行记录（TABLE）
     * 展示：使用 queryForList 处理返回多行的存储过程
     */
    @Test
    public void testHighLevel_TableResult() throws SQLException {
        List<Map<String, Object>> users = jdbcTemplate.queryForList("SELECT * FROM proc_get_users_cursor()");

        assertNotNull(users);
        assertEquals(3, users.size());
        assertEquals("Alice", users.get(0).get("name"));
        assertEquals(25, users.get(0).get("age"));
        assertEquals("Bob", users.get(1).get("name"));
        assertEquals(30, users.get(1).get("age"));
    }

    /**
     * 测试高级接口 - 带参数返回多行记录
     * 展示：结合参数和多行结果
     */
    @Test
    public void testHighLevel_TableResultWithParams() throws SQLException {
        Map<String, Object> params = new HashMap<>();
        params.put("minAge", 30);

        List<Map<String, Object>> users = jdbcTemplate.queryForList("SELECT * FROM proc_filter_users(:minAge)", params);

        assertNotNull(users);
        assertEquals(2, users.size()); // Bob(30) 和 Charlie(35)
        assertEquals("Bob", users.get(0).get("name"));
        assertEquals("Charlie", users.get(1).get("name"));
    }
}
