/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http:www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.dbvisitor.jdbc.paramer;
import net.hasor.dbvisitor.jdbc.*;
import net.hasor.dbvisitor.types.TypeHandler;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;
import net.hasor.test.AbstractDbTest;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;

import java.sql.Types;

import static net.hasor.dbvisitor.jdbc.SqlParameter.*;

/***
 *
 * @version : 2014-1-13
 * @author 赵永春 (zyc@hasor.net)
 */
public class SqlParameterUtilsTest extends AbstractDbTest {
    @Test
    public void withOutput_1() {
        OutSqlParameter parameter = SqlParameterUtils.withOutput(Types.BIGINT);
        assert parameter.getJdbcType() == Types.BIGINT;
        assert parameter.getTypeHandler() == null;
        assert parameter.getName() == null;
        assert parameter.getScale() == null;
        assert parameter.getTypeName() == null;
    }

    @Test
    public void withOutput_2() {
        OutSqlParameter parameter = SqlParameterUtils.withOutput(Types.BIGINT, 123);
        assert parameter.getJdbcType() == Types.BIGINT;
        assert parameter.getTypeHandler() == null;
        assert parameter.getName() == null;
        assert parameter.getScale() == 123;
        assert parameter.getTypeName() == null;
    }

    @Test
    public void withOutput_3() {
        OutSqlParameter parameter = SqlParameterUtils.withOutput(Types.BIGINT, "type");
        assert parameter.getJdbcType() == Types.BIGINT;
        assert parameter.getTypeHandler() == null;
        assert parameter.getName() == null;
        assert parameter.getScale() == null;
        assert parameter.getTypeName().equals("type");
    }

    @Test
    public void withOutput_4() {
        TypeHandler<?> handler = PowerMockito.mock(TypeHandler.class);
        OutSqlParameter parameter = SqlParameterUtils.withOutput(Types.BIGINT, handler);
        assert parameter.getJdbcType() == Types.BIGINT;
        assert parameter.getTypeHandler() == handler;
        assert parameter.getName() == null;
        assert parameter.getScale() == null;
        assert parameter.getTypeName() == null;
    }

    @Test
    public void withOutput_5() {
        TypeHandler<?> handler = PowerMockito.mock(TypeHandler.class);
        OutSqlParameter parameter = SqlParameterUtils.withOutput(Types.BIGINT, 123, handler);
        assert parameter.getJdbcType() == Types.BIGINT;
        assert parameter.getTypeHandler() == handler;
        assert parameter.getName() == null;
        assert parameter.getScale() == 123;
        assert parameter.getTypeName() == null;
    }

    @Test
    public void withOutput_6() {
        TypeHandler<?> handler = PowerMockito.mock(TypeHandler.class);
        OutSqlParameter parameter = SqlParameterUtils.withOutput(Types.BIGINT, "type", handler);
        assert parameter.getJdbcType() == Types.BIGINT;
        assert parameter.getTypeHandler() == handler;
        assert parameter.getName() == null;
        assert parameter.getScale() == null;
        assert parameter.getTypeName().equals("type");
    }

    @Test
    public void withOutput_7() {
        OutSqlParameter parameter = SqlParameterUtils.withOutputName("abc", Types.BIGINT);
        assert parameter.getJdbcType() == Types.BIGINT;
        assert parameter.getTypeHandler() == null;
        assert parameter.getName().equals("abc");
        assert parameter.getScale() == null;
        assert parameter.getTypeName() == null;

        try {
            SqlParameterUtils.withOutputName("", Types.BIGINT);
            assert false;
        } catch (Exception e) {
            assert e.getMessage().equals("paramName can not be empty or null.");
        }
    }

    @Test
    public void withOutput_8() {
        OutSqlParameter parameter = SqlParameterUtils.withOutputName("abc", Types.BIGINT, 123);
        assert parameter.getJdbcType() == Types.BIGINT;
        assert parameter.getTypeHandler() == null;
        assert parameter.getName().equals("abc");
        assert parameter.getScale() == 123;
        assert parameter.getTypeName() == null;

        try {
            SqlParameterUtils.withOutputName("", Types.BIGINT, 123);
            assert false;
        } catch (Exception e) {
            assert e.getMessage().equals("paramName can not be empty or null.");
        }
    }

    @Test
    public void withOutput_9() {
        OutSqlParameter parameter = SqlParameterUtils.withOutputName("abc", Types.BIGINT, "type");
        assert parameter.getJdbcType() == Types.BIGINT;
        assert parameter.getTypeHandler() == null;
        assert parameter.getName().equals("abc");
        assert parameter.getScale() == null;
        assert parameter.getTypeName().equals("type");

        try {
            SqlParameterUtils.withOutputName("", Types.BIGINT, "type");
            assert false;
        } catch (Exception e) {
            assert e.getMessage().equals("paramName can not be empty or null.");
        }
    }

    @Test
    public void withOutput_10() {
        TypeHandler<?> handler = PowerMockito.mock(TypeHandler.class);
        OutSqlParameter parameter = SqlParameterUtils.withOutputName("abc", Types.BIGINT, 123, handler);
        assert parameter.getJdbcType() == Types.BIGINT;
        assert parameter.getTypeHandler() == handler;
        assert parameter.getName().equals("abc");
        assert parameter.getScale() == 123;
        assert parameter.getTypeName() == null;

        try {
            SqlParameterUtils.withOutputName("", Types.BIGINT, 123, handler);
            assert false;
        } catch (Exception e) {
            assert e.getMessage().equals("paramName can not be empty or null.");
        }
    }

    @Test
    public void withOutput_11() {
        TypeHandler<?> handler = PowerMockito.mock(TypeHandler.class);
        OutSqlParameter parameter = SqlParameterUtils.withOutputName("abc", Types.BIGINT, "type", handler);
        assert parameter.getJdbcType() == Types.BIGINT;
        assert parameter.getTypeHandler() == handler;
        assert parameter.getName().equals("abc");
        assert parameter.getScale() == null;
        assert parameter.getTypeName().equals("type");

        try {
            SqlParameterUtils.withOutputName("", Types.BIGINT, "type", handler);
            assert false;
        } catch (Exception e) {
            assert e.getMessage().equals("paramName can not be empty or null.");
        }
    }

    @Test
    public void withOutput_12() {
        TypeHandler<?> handler = PowerMockito.mock(TypeHandler.class);
        OutSqlParameter parameter = SqlParameterUtils.withOutputName("abc", Types.BIGINT, handler);
        assert parameter.getJdbcType() == Types.BIGINT;
        assert parameter.getTypeHandler() == handler;
        assert parameter.getName().equals("abc");
        assert parameter.getScale() == null;
        assert parameter.getTypeName() == null;

        try {
            SqlParameterUtils.withOutputName("", Types.BIGINT, handler);
            assert false;
        } catch (Exception e) {
            assert e.getMessage().equals("paramName can not be empty or null.");
        }
    }

    @Test
    public void withInput_1() {
        Integer handler = TypeHandlerRegistry.toSqlType("dddd".getClass());

        InSqlParameter parameter = SqlParameterUtils.withInput("abc");
        assert parameter.getJdbcType() == handler;
        assert parameter.getTypeHandler() == null;
        assert parameter.getName() == null;
        assert parameter.getScale() == null;
        assert parameter.getTypeName() == null;
        assert parameter.getValue().equals("abc");
    }

    @Test
    public void withInput_2() {
        InSqlParameter parameter = SqlParameterUtils.withInput("abc", Types.BIGINT);
        assert parameter.getJdbcType() == Types.BIGINT;
        assert parameter.getTypeHandler() == null;
        assert parameter.getName() == null;
        assert parameter.getScale() == null;
        assert parameter.getTypeName() == null;
        assert parameter.getValue().equals("abc");
    }

    @Test
    public void withInput_3() {
        TypeHandler<?> handler = PowerMockito.mock(TypeHandler.class);
        InSqlParameter parameter = SqlParameterUtils.withInput("abc", Types.BIGINT, handler);
        assert parameter.getJdbcType() == Types.BIGINT;
        assert parameter.getTypeHandler() == handler;
        assert parameter.getName() == null;
        assert parameter.getScale() == null;
        assert parameter.getTypeName() == null;
        assert parameter.getValue().equals("abc");
    }

    @Test
    public void withInOut_1() {
        ValueSqlParameter inOut = (SqlParameter.ValueSqlParameter) SqlParameterUtils.withInOut("abc", Types.BIGINT);
        InSqlParameter asIn = (InSqlParameter) inOut;
        OutSqlParameter asOut = (OutSqlParameter) inOut;

        assert inOut.getName() == null;
        assert inOut.getJdbcType() == Types.BIGINT;
        assert inOut.getTypeName() == null;
        assert inOut.getScale() == null;
        assert asIn.getTypeHandler() == null;
        assert asIn.getValue().equals("abc");
        assert asOut.getTypeHandler() == null;
    }

    @Test
    public void withInOut_2() {
        ValueSqlParameter inOut = (SqlParameter.ValueSqlParameter) SqlParameterUtils.withInOut("abc", Types.BIGINT, 123);
        InSqlParameter asIn = (InSqlParameter) inOut;
        OutSqlParameter asOut = (OutSqlParameter) inOut;

        assert inOut.getName() == null;
        assert inOut.getJdbcType() == Types.BIGINT;
        assert inOut.getTypeName() == null;
        assert inOut.getScale() == 123;
        assert asIn.getTypeHandler() == null;
        assert asIn.getValue().equals("abc");
        assert asOut.getTypeHandler() == null;
    }

    @Test
    public void withInOut_3() {
        ValueSqlParameter inOut = (SqlParameter.ValueSqlParameter) SqlParameterUtils.withInOut("abc", Types.BIGINT, "type");
        InSqlParameter asIn = (InSqlParameter) inOut;
        OutSqlParameter asOut = (OutSqlParameter) inOut;

        assert inOut.getName() == null;
        assert inOut.getJdbcType() == Types.BIGINT;
        assert inOut.getTypeName().equals("type");
        assert inOut.getScale() == null;
        assert asIn.getTypeHandler() == null;
        assert asIn.getValue().equals("abc");
        assert asOut.getTypeHandler() == null;
    }

    @Test
    public void withInOut_4() {
        TypeHandler<?> handler = PowerMockito.mock(TypeHandler.class);
        ValueSqlParameter inOut = (SqlParameter.ValueSqlParameter) SqlParameterUtils.withInOut("abc", Types.BIGINT, handler);
        InSqlParameter asIn = (InSqlParameter) inOut;
        OutSqlParameter asOut = (OutSqlParameter) inOut;

        assert inOut.getName() == null;
        assert inOut.getJdbcType() == Types.BIGINT;
        assert inOut.getTypeName() == null;
        assert inOut.getScale() == null;
        assert asIn.getTypeHandler() == handler;
        assert asIn.getValue().equals("abc");
        assert asOut.getTypeHandler() == handler;
    }

    @Test
    public void withInOut_5() {
        TypeHandler<?> handler = PowerMockito.mock(TypeHandler.class);
        ValueSqlParameter inOut = (SqlParameter.ValueSqlParameter) SqlParameterUtils.withInOut("abc", Types.BIGINT, 123, handler);
        InSqlParameter asIn = (InSqlParameter) inOut;
        OutSqlParameter asOut = (OutSqlParameter) inOut;

        assert inOut.getName() == null;
        assert inOut.getJdbcType() == Types.BIGINT;
        assert inOut.getTypeName() == null;
        assert inOut.getScale() == 123;
        assert asIn.getTypeHandler() == handler;
        assert asIn.getValue().equals("abc");
        assert asOut.getTypeHandler() == handler;
    }

    @Test
    public void withInOut_6() {
        TypeHandler<?> handler = PowerMockito.mock(TypeHandler.class);
        ValueSqlParameter inOut = (SqlParameter.ValueSqlParameter) SqlParameterUtils.withInOut("abc", Types.BIGINT, "type", handler);
        InSqlParameter asIn = (InSqlParameter) inOut;
        OutSqlParameter asOut = (OutSqlParameter) inOut;

        assert inOut.getName() == null;
        assert inOut.getJdbcType() == Types.BIGINT;
        assert inOut.getTypeName().equals("type");
        assert inOut.getScale() == null;
        assert asIn.getTypeHandler() == handler;
        assert asIn.getValue().equals("abc");
        assert asOut.getTypeHandler() == handler;
    }

    @Test
    public void withInOut_7() {
        ValueSqlParameter inOut = (SqlParameter.ValueSqlParameter) SqlParameterUtils.withInOutName("name", "abc", Types.BIGINT);
        InSqlParameter asIn = (InSqlParameter) inOut;
        OutSqlParameter asOut = (OutSqlParameter) inOut;

        assert inOut.getName().equals("name");
        assert inOut.getJdbcType() == Types.BIGINT;
        assert inOut.getTypeName() == null;
        assert inOut.getScale() == null;
        assert asIn.getTypeHandler() == null;
        assert asIn.getValue().equals("abc");
        assert asOut.getTypeHandler() == null;

        try {
            SqlParameterUtils.withInOutName("", "abc", Types.BIGINT);
            assert false;
        } catch (Exception e) {
            assert e.getMessage().equals("paramName can not be empty or null.");
        }
    }

    @Test
    public void withInOut_8() {
        ValueSqlParameter inOut = (SqlParameter.ValueSqlParameter) SqlParameterUtils.withInOutName("name", "abc", Types.BIGINT, 123);
        InSqlParameter asIn = (InSqlParameter) inOut;
        OutSqlParameter asOut = (OutSqlParameter) inOut;

        assert inOut.getName().equals("name");
        assert inOut.getJdbcType() == Types.BIGINT;
        assert inOut.getTypeName() == null;
        assert inOut.getScale() == 123;
        assert asIn.getTypeHandler() == null;
        assert asIn.getValue().equals("abc");
        assert asOut.getTypeHandler() == null;

        try {
            SqlParameterUtils.withInOutName("", "abc", Types.BIGINT, 123);
            assert false;
        } catch (Exception e) {
            assert e.getMessage().equals("paramName can not be empty or null.");
        }
    }

    @Test
    public void withInOut_9() {
        ValueSqlParameter inOut = (SqlParameter.ValueSqlParameter) SqlParameterUtils.withInOutName("name", "abc", Types.BIGINT, "type");
        InSqlParameter asIn = (InSqlParameter) inOut;
        OutSqlParameter asOut = (OutSqlParameter) inOut;

        assert inOut.getName().equals("name");
        assert inOut.getJdbcType() == Types.BIGINT;
        assert inOut.getTypeName().equals("type");
        assert inOut.getScale() == null;
        assert asIn.getTypeHandler() == null;
        assert asIn.getValue().equals("abc");
        assert asOut.getTypeHandler() == null;

        try {
            SqlParameterUtils.withInOutName("", "abc", Types.BIGINT, "type");
            assert false;
        } catch (Exception e) {
            assert e.getMessage().equals("paramName can not be empty or null.");
        }
    }

    @Test
    public void withInOut_10() {
        TypeHandler<?> handler = PowerMockito.mock(TypeHandler.class);
        ValueSqlParameter inOut = (SqlParameter.ValueSqlParameter) SqlParameterUtils.withInOutName("name", "abc", Types.BIGINT, handler);
        InSqlParameter asIn = (InSqlParameter) inOut;
        OutSqlParameter asOut = (OutSqlParameter) inOut;

        assert inOut.getName().equals("name");
        assert inOut.getJdbcType() == Types.BIGINT;
        assert inOut.getTypeName() == null;
        assert inOut.getScale() == null;
        assert asIn.getTypeHandler() == handler;
        assert asIn.getValue().equals("abc");
        assert asOut.getTypeHandler() == handler;

        try {
            SqlParameterUtils.withInOutName("", "abc", Types.BIGINT, handler);
            assert false;
        } catch (Exception e) {
            assert e.getMessage().equals("paramName can not be empty or null.");
        }
    }

    @Test
    public void withInOut_11() {
        TypeHandler<?> handler = PowerMockito.mock(TypeHandler.class);
        ValueSqlParameter inOut = (SqlParameter.ValueSqlParameter) SqlParameterUtils.withInOutName("name", "abc", Types.BIGINT, 123, handler);
        InSqlParameter asIn = (InSqlParameter) inOut;
        OutSqlParameter asOut = (OutSqlParameter) inOut;

        assert inOut.getName().equals("name");
        assert inOut.getJdbcType() == Types.BIGINT;
        assert inOut.getTypeName() == null;
        assert inOut.getScale() == 123;
        assert asIn.getTypeHandler() == handler;
        assert asIn.getValue().equals("abc");
        assert asOut.getTypeHandler() == handler;

        try {
            SqlParameterUtils.withInOutName("", "abc", Types.BIGINT, 123, handler);
            assert false;
        } catch (Exception e) {
            assert e.getMessage().equals("paramName can not be empty or null.");
        }
    }

    @Test
    public void withInOut_12() {
        TypeHandler<?> handler = PowerMockito.mock(TypeHandler.class);
        ValueSqlParameter inOut = (SqlParameter.ValueSqlParameter) SqlParameterUtils.withInOutName("name", "abc", Types.BIGINT, "type", handler);
        InSqlParameter asIn = (InSqlParameter) inOut;
        OutSqlParameter asOut = (OutSqlParameter) inOut;

        assert inOut.getName().equals("name");
        assert inOut.getJdbcType() == Types.BIGINT;
        assert inOut.getTypeName().equals("type");
        assert inOut.getScale() == null;
        assert asIn.getTypeHandler() == handler;
        assert asIn.getValue().equals("abc");
        assert asOut.getTypeHandler() == handler;

        try {
            SqlParameterUtils.withInOutName("", "abc", Types.BIGINT, "type", handler);
            assert false;
        } catch (Exception e) {
            assert e.getMessage().equals("paramName can not be empty or null.");
        }
    }

    @Test
    public void withReturnValue_1() {
        ReturnSqlParameter returnSqlParameter = SqlParameterUtils.withReturnValue("name");
        assert returnSqlParameter.getName().equals("name");
        assert returnSqlParameter.getResultSetExtractor() == null;
        assert returnSqlParameter.getRowMapper() == null;
        assert returnSqlParameter.getRowCallbackHandler() == null;

        try {
            SqlParameterUtils.withReturnValue("");
            assert false;
        } catch (Exception e) {
            assert e.getMessage().equals("paramName can not be empty or null.");
        }
    }

    @Test
    public void withReturnValue_2() {
        ResultSetExtractor<?> handler = PowerMockito.mock(ResultSetExtractor.class);
        ReturnSqlParameter returnSqlParameter = SqlParameterUtils.withReturnResult("name", handler);
        assert returnSqlParameter.getName().equals("name");
        assert returnSqlParameter.getResultSetExtractor() == handler;
        assert returnSqlParameter.getRowMapper() == null;
        assert returnSqlParameter.getRowCallbackHandler() == null;

        try {
            SqlParameterUtils.withReturnResult("", handler);
            assert false;
        } catch (Exception e) {
            assert e.getMessage().equals("paramName can not be empty or null.");
        }
    }

    @Test
    public void withReturnValue_3() {
        RowCallbackHandler handler = PowerMockito.mock(RowCallbackHandler.class);
        ReturnSqlParameter returnSqlParameter = SqlParameterUtils.withReturnResult("name", handler);
        assert returnSqlParameter.getName().equals("name");
        assert returnSqlParameter.getResultSetExtractor() == null;
        assert returnSqlParameter.getRowMapper() == null;
        assert returnSqlParameter.getRowCallbackHandler() == handler;

        try {
            SqlParameterUtils.withReturnResult("", handler);
            assert false;
        } catch (Exception e) {
            assert e.getMessage().equals("paramName can not be empty or null.");
        }
    }

    @Test
    public void withReturnValue_4() {
        RowMapper<?> handler = PowerMockito.mock(RowMapper.class);
        ReturnSqlParameter returnSqlParameter = SqlParameterUtils.withReturnResult("name", handler);
        assert returnSqlParameter.getName().equals("name");
        assert returnSqlParameter.getResultSetExtractor() == null;
        assert returnSqlParameter.getRowMapper() == handler;
        assert returnSqlParameter.getRowCallbackHandler() == null;

        try {
            SqlParameterUtils.withReturnResult("", handler);
            assert false;
        } catch (Exception e) {
            assert e.getMessage().equals("paramName can not be empty or null.");
        }
    }
}
