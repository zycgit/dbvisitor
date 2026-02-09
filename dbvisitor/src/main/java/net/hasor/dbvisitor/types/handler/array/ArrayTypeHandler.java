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
package net.hasor.dbvisitor.types.handler.array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.sql.*;
import java.time.*;
import java.time.chrono.JapaneseDate;
import java.util.Calendar;
import java.util.concurrent.ConcurrentHashMap;
import net.hasor.dbvisitor.types.handler.AbstractTypeHandler;

/**
 * 读写 jdbc 数组类型
 * @author Clinton Begin
 * @author 赵永春 (zyc@hasor.net)
 */
public class ArrayTypeHandler extends AbstractTypeHandler<Object> {
    protected static final ConcurrentHashMap<Class<?>, JDBCType> STANDARD_MAPPING;

    static {
        STANDARD_MAPPING = new ConcurrentHashMap<>();
        STANDARD_MAPPING.put(boolean.class, JDBCType.BOOLEAN);
        STANDARD_MAPPING.put(Boolean.class, JDBCType.BOOLEAN);
        STANDARD_MAPPING.put(byte.class, JDBCType.TINYINT);
        STANDARD_MAPPING.put(Byte.class, JDBCType.TINYINT);
        STANDARD_MAPPING.put(short.class, JDBCType.SMALLINT);
        STANDARD_MAPPING.put(Short.class, JDBCType.SMALLINT);
        STANDARD_MAPPING.put(int.class, JDBCType.INTEGER);
        STANDARD_MAPPING.put(Integer.class, JDBCType.INTEGER);
        STANDARD_MAPPING.put(long.class, JDBCType.BIGINT);
        STANDARD_MAPPING.put(Long.class, JDBCType.BIGINT);
        STANDARD_MAPPING.put(float.class, JDBCType.FLOAT);
        STANDARD_MAPPING.put(Float.class, JDBCType.FLOAT);
        STANDARD_MAPPING.put(double.class, JDBCType.DOUBLE);
        STANDARD_MAPPING.put(Double.class, JDBCType.DOUBLE);
        STANDARD_MAPPING.put(Calendar.class, JDBCType.CHAR);
        STANDARD_MAPPING.put(char.class, JDBCType.CHAR);
        // java time
        STANDARD_MAPPING.put(java.util.Date.class, JDBCType.TIMESTAMP);
        STANDARD_MAPPING.put(java.sql.Date.class, JDBCType.TIMESTAMP);
        STANDARD_MAPPING.put(java.sql.Timestamp.class, JDBCType.TIMESTAMP);
        STANDARD_MAPPING.put(java.sql.Time.class, JDBCType.TIMESTAMP);
        STANDARD_MAPPING.put(Instant.class, JDBCType.TIMESTAMP);
        STANDARD_MAPPING.put(LocalDateTime.class, JDBCType.TIMESTAMP);
        STANDARD_MAPPING.put(LocalDate.class, JDBCType.TIMESTAMP);
        STANDARD_MAPPING.put(LocalTime.class, JDBCType.TIMESTAMP);
        STANDARD_MAPPING.put(ZonedDateTime.class, JDBCType.TIMESTAMP);
        STANDARD_MAPPING.put(JapaneseDate.class, JDBCType.TIMESTAMP);
        STANDARD_MAPPING.put(YearMonth.class, JDBCType.TIMESTAMP);
        STANDARD_MAPPING.put(Year.class, JDBCType.TIMESTAMP);
        STANDARD_MAPPING.put(Month.class, JDBCType.TIMESTAMP);
        STANDARD_MAPPING.put(OffsetDateTime.class, JDBCType.TIMESTAMP);
        STANDARD_MAPPING.put(OffsetTime.class, JDBCType.TIMESTAMP);
        // java extensions Types
        STANDARD_MAPPING.put(String.class, JDBCType.VARCHAR);
        STANDARD_MAPPING.put(BigInteger.class, JDBCType.BIGINT);
        STANDARD_MAPPING.put(BigDecimal.class, JDBCType.NUMERIC);
        STANDARD_MAPPING.put(Byte[].class, JDBCType.VARBINARY);
        STANDARD_MAPPING.put(byte[].class, JDBCType.VARBINARY);
        STANDARD_MAPPING.put(URL.class, JDBCType.DATALINK);
        STANDARD_MAPPING.put(URI.class, JDBCType.DATALINK);
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, Integer jdbcType) throws SQLException {
        if (parameter instanceof Array) {
            // it's the user's responsibility to properly free() the Array instance
            ps.setArray(i, (Array) parameter);
        } else {
            if (!parameter.getClass().isArray()) {
                throw new SQLException("ArrayType Handler requires SQL array or java array parameter and does not support type " + parameter.getClass());
            }
            Class<?> componentType = parameter.getClass().getComponentType();
            String arrayTypeName = resolveTypeName(componentType);
            Array array = ps.getConnection().createArrayOf(arrayTypeName, (Object[]) parameter);
            ps.setArray(i, array);
            array.free();
        }
    }

    protected String resolveTypeName(Class<?> type) {
        return STANDARD_MAPPING.getOrDefault(type, JDBCType.JAVA_OBJECT).getName();
    }

    @Override
    public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return extractArray(rs.getArray(columnName));
    }

    @Override
    public Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return extractArray(rs.getArray(columnIndex));
    }

    @Override
    public Object getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return extractArray(cs.getArray(columnIndex));
    }

    protected Object extractArray(Array array) throws SQLException {
        if (array == null) {
            return null;
        }
        try {
            Object result = array.getArray();
            // array.getArray() 返回的是 Object[]
            if (result instanceof Object[]) {
                Object[] objArray = (Object[]) result;
                
                // 对于空数组，尝试根据数据库类型推断 Java 类型
                // 注意：某些数据库（如H2）对空数组可能返回 baseTypeName="NULL"，此时返回 Object[]
                if (objArray.length == 0) {
                    return createTypedEmptyArray(array.getBaseTypeName());
                }
                
                // 检测第一个非 null 元素的类型
                Class<?> componentType = null;
                for (Object elem : objArray) {
                    if (elem != null) {
                        componentType = elem.getClass();
                        break;
                    }
                }
                
                if (componentType == null) {
                    // 所有元素都是 null，根据数据库类型推断
                    return createTypedArrayFromSqlType(array.getBaseTypeName(), objArray.length);
                }
                
                // 针对常见类型，创建强类型数组
                if (componentType == Integer.class) {
                    Integer[] typed = new Integer[objArray.length];
                    for (int i = 0; i < objArray.length; i++) {
                        typed[i] = (Integer) objArray[i];
                    }
                    return typed;
                } else if (componentType == String.class) {
                    String[] typed = new String[objArray.length];
                    for (int i = 0; i < objArray.length; i++) {
                        typed[i] = (String) objArray[i];
                    }
                    return typed;
                } else if (componentType == Float.class || componentType == Double.class) {
                    // H2 可能返回 Double，但我们需要 Float
                    Float[] typed = new Float[objArray.length];
                    for (int i = 0; i < objArray.length; i++) {
                        if (objArray[i] instanceof Number) {
                            typed[i] = ((Number) objArray[i]).floatValue();
                        } else {
                            typed[i] = (Float) objArray[i];
                        }
                    }
                    return typed;
                } else if (componentType == Long.class) {
                    Long[] typed = new Long[objArray.length];
                    for (int i = 0; i < objArray.length; i++) {
                        typed[i] = (Long) objArray[i];
                    }
                    return typed;
                } else if (componentType == Short.class) {
                    Short[] typed = new Short[objArray.length];
                    for (int i = 0; i < objArray.length; i++) {
                        typed[i] = (Short) objArray[i];
                    }
                    return typed;
                } else if (componentType == Boolean.class) {
                    Boolean[] typed = new Boolean[objArray.length];
                    for (int i = 0; i < objArray.length; i++) {
                        typed[i] = (Boolean) objArray[i];
                    }
                    return typed;
                }
                
                // 其他类型保持不变
                return objArray;
            }
            return result;
        } finally {
            array.free();
        }
    }
    
    /** 根据 SQL 数组基础类型名称创建空的强类型数组 */
    private Object createTypedEmptyArray(String baseTypeName) {
        if (baseTypeName == null) {
            return new Object[0];
        }
        switch (baseTypeName.toUpperCase()) {
            case "INTEGER":
            case "INT":
            case "INT4":    // PostgreSQL internal type name
                return new Integer[0];
            case "BIGINT":
            case "LONG":
            case "INT8":    // PostgreSQL internal type name
                return new Long[0];
            case "SMALLINT":
            case "SHORT":
            case "INT2":    // PostgreSQL internal type name
                return new Short[0];
            case "REAL":
            case "FLOAT":
            case "FLOAT4":  // PostgreSQL internal type name
                return new Float[0];
            case "DOUBLE":
            case "DOUBLE PRECISION":
            case "FLOAT8":  // PostgreSQL internal type name
                return new Double[0];
            case "NUMERIC":
            case "DECIMAL":
                return new java.math.BigDecimal[0];
            case "BOOLEAN":
            case "BOOL":
                return new Boolean[0];
            case "VARCHAR":
            case "CHAR":
            case "TEXT":
            case "STRING":
            case "BPCHAR":  // PostgreSQL internal type name for CHAR
                return new String[0];
            default:
                return new Object[0];
        }
    }
    
    /** 根据 SQL 类型创建指定长度的强类型数组（所有元素为 null） */
    private Object createTypedArrayFromSqlType(String baseTypeName, int length) {
        if (baseTypeName == null || length == 0) {
            return new Object[length];
        }
        switch (baseTypeName.toUpperCase()) {
            case "INTEGER":
            case "INT":
            case "INT4":    // PostgreSQL internal type name
                return new Integer[length];
            case "BIGINT":
            case "LONG":
            case "INT8":    // PostgreSQL internal type name
                return new Long[length];
            case "SMALLINT":
            case "SHORT":
            case "INT2":    // PostgreSQL internal type name
                return new Short[length];
            case "REAL":
            case "FLOAT":
            case "FLOAT4":  // PostgreSQL internal type name
                return new Float[length];
            case "DOUBLE":
            case "DOUBLE PRECISION":
            case "FLOAT8":  // PostgreSQL internal type name
                return new Double[length];
            case "NUMERIC":
            case "DECIMAL":
                return new java.math.BigDecimal[length];
            case "BOOLEAN":
            case "BOOL":
                return new Boolean[length];
            case "VARCHAR":
            case "CHAR":
            case "TEXT":
            case "STRING":
            case "BPCHAR":  // PostgreSQL internal type name for CHAR
                return new String[length];
            default:
                return new Object[length];
        }
    }
}
