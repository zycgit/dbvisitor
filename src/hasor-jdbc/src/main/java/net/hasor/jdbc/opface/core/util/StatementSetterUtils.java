/*
 * Copyright 2008-2009 the original ������(zyc@hasor.net).
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
package net.hasor.jdbc.opface.core.util;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import net.hasor.core.Hasor;
import net.hasor.jdbc.opface.core.value.DisposableSqlTypeValue;
import net.hasor.jdbc.opface.core.value.SqlTypeValue;
import net.hasor.jdbc.opface.core.value.SqlValue;
import net.hasor.jdbc.opface.parameter.SqlParameter;
import net.hasor.jdbc.opface.parameter.SqlVarParameter;
/**
 * 
 * @version : 2013-10-15
 * @author ������(zyc@hasor.net)
 */
public class StatementSetterUtils {
    private static Map<Class<?>, Integer> javaTypeToSqlTypeMap = new HashMap<Class<?>, Integer>(32);
    static {
        /* JDBC 3.0 only - not compatible with e.g. MySQL at present*/
        javaTypeToSqlTypeMap.put(boolean.class, new Integer(Types.BOOLEAN));
        javaTypeToSqlTypeMap.put(Boolean.class, new Integer(Types.BOOLEAN));
        //
        javaTypeToSqlTypeMap.put(byte.class, Types.TINYINT);
        javaTypeToSqlTypeMap.put(Byte.class, Types.TINYINT);
        javaTypeToSqlTypeMap.put(short.class, Types.SMALLINT);
        javaTypeToSqlTypeMap.put(Short.class, Types.SMALLINT);
        javaTypeToSqlTypeMap.put(int.class, Types.INTEGER);
        javaTypeToSqlTypeMap.put(Integer.class, Types.INTEGER);
        javaTypeToSqlTypeMap.put(long.class, Types.BIGINT);
        javaTypeToSqlTypeMap.put(Long.class, Types.BIGINT);
        javaTypeToSqlTypeMap.put(BigInteger.class, Types.BIGINT);
        javaTypeToSqlTypeMap.put(float.class, Types.FLOAT);
        javaTypeToSqlTypeMap.put(Float.class, Types.FLOAT);
        javaTypeToSqlTypeMap.put(double.class, Types.DOUBLE);
        javaTypeToSqlTypeMap.put(Double.class, Types.DOUBLE);
        javaTypeToSqlTypeMap.put(BigDecimal.class, Types.DECIMAL);
        javaTypeToSqlTypeMap.put(java.sql.Date.class, Types.DATE);
        javaTypeToSqlTypeMap.put(java.sql.Time.class, Types.TIME);
        javaTypeToSqlTypeMap.put(java.sql.Timestamp.class, Types.TIMESTAMP);
        javaTypeToSqlTypeMap.put(Blob.class, Types.BLOB);
        javaTypeToSqlTypeMap.put(Clob.class, Types.CLOB);
    }
    //
    /**���� Java ����Derive a default SQL type from the given Java type.*/
    public static int javaTypeToSqlParameterType(Class<?> javaType) {
        Integer sqlType = javaTypeToSqlTypeMap.get(javaType);
        if (sqlType != null)
            return sqlType;
        if (Number.class.isAssignableFrom(javaType))
            return Types.NUMERIC;
        if (isStringValue(javaType))
            return Types.VARCHAR;
        if (isDateValue(javaType) || Calendar.class.isAssignableFrom(javaType))
            return Types.TIMESTAMP;
        return SqlTypeValue.TYPE_UNKNOWN;
    }
    /***/
    public static void setParameterValue(PreparedStatement ps, int parameterPosition, SqlParameter paramValue, Object inValue) throws SQLException {
        setParameterValue(ps, parameterPosition, paramValue.getSqlType(), paramValue.getScale(), inValue);
    }
    /***/
    public static void setParameterValue(PreparedStatement ps, int parameterPosition, int sqlType, Object inValue) throws SQLException {
        setParameterValue(ps, parameterPosition, sqlType, null, inValue);
    }
    //
    private static void setParameterValue(PreparedStatement ps, int parameterPosition, int sqlType, Integer scale, Object inValue) throws SQLException {
        int sqlTypeToUse = sqlType;
        Integer scaleToUse = scale;
        Object inValueToUse = inValue;
        // 
        if (inValue instanceof SqlParameter) {
            sqlTypeToUse = ((SqlParameter) inValue).getSqlType();
            scaleToUse = ((SqlParameter) inValue).getScale();
        }
        if (inValue instanceof SqlVarParameter) {
            inValueToUse = ((SqlVarParameter) inValue).getValue();
        }
        //
        if (inValueToUse == null)
            setNull(ps, parameterPosition, sqlTypeToUse);
        else
            setValue(ps, parameterPosition, sqlTypeToUse, scaleToUse, inValueToUse);
    }
    private static void setValue(PreparedStatement ps, int paramIndex, int sqlType, Integer scale, Object inValue) throws SQLException {
        if (inValue instanceof SqlTypeValue) {
            ((SqlTypeValue) inValue).setTypeValue(ps, paramIndex, sqlType);
        } else if (inValue instanceof SqlValue) {
            ((SqlValue) inValue).setValue(ps, paramIndex);
        } else if (sqlType == Types.VARCHAR || sqlType == Types.LONGVARCHAR || (sqlType == Types.CLOB && isStringValue(inValue.getClass()))) {
            //�ַ�
            ps.setString(paramIndex, inValue.toString());
        } else if (sqlType == Types.DECIMAL || sqlType == Types.NUMERIC) {
            //����
            if (inValue instanceof BigDecimal)
                ps.setBigDecimal(paramIndex, (BigDecimal) inValue);
            else if (scale != null)
                ps.setObject(paramIndex, inValue, sqlType, scale);
            else
                ps.setObject(paramIndex, inValue, sqlType);
        } else if (sqlType == Types.DATE) {
            //����
            if (inValue instanceof java.util.Date) {
                /*ʱ��*/
                if (inValue instanceof java.sql.Date)
                    ps.setDate(paramIndex, (java.sql.Date) inValue);
                else
                    ps.setDate(paramIndex, new java.sql.Date(((java.util.Date) inValue).getTime()));
            } else if (inValue instanceof Calendar) {
                /*����*/
                Calendar cal = (Calendar) inValue;
                ps.setDate(paramIndex, new java.sql.Date(cal.getTime().getTime()), cal);
            } else {
                /*����*/
                ps.setObject(paramIndex, inValue, Types.DATE);
            }
        } else if (sqlType == Types.TIME) {
            //ʱ��
            if (inValue instanceof java.util.Date) {
                /*SQLʱ��*/
                if (inValue instanceof java.sql.Time)
                    ps.setTime(paramIndex, (java.sql.Time) inValue);
                else
                    ps.setTime(paramIndex, new java.sql.Time(((java.util.Date) inValue).getTime()));
            } else if (inValue instanceof Calendar) {
                /*����*/
                Calendar cal = (Calendar) inValue;
                ps.setTime(paramIndex, new java.sql.Time(cal.getTime().getTime()), cal);
            } else {
                /*����*/
                ps.setObject(paramIndex, inValue, Types.TIME);
            }
        } else if (sqlType == Types.TIMESTAMP) {
            //����ʱ��
            if (inValue instanceof java.util.Date) {
                /*SQLʱ���*/
                if (inValue instanceof java.sql.Timestamp)
                    ps.setTimestamp(paramIndex, (java.sql.Timestamp) inValue);
                else
                    ps.setTimestamp(paramIndex, new java.sql.Timestamp(((java.util.Date) inValue).getTime()));
            } else if (inValue instanceof Calendar) {
                /*����*/
                Calendar cal = (Calendar) inValue;
                ps.setTimestamp(paramIndex, new java.sql.Timestamp(cal.getTime().getTime()), cal);
            } else {
                /*����*/
                ps.setObject(paramIndex, inValue, Types.TIMESTAMP);
            }
        } else if (sqlType == SqlTypeValue.TYPE_UNKNOWN) {
            //��ȷ������
            if (isStringValue(inValue.getClass()))
                ps.setString(paramIndex, inValue.toString());
            else if (isDateValue(inValue.getClass()))
                ps.setTimestamp(paramIndex, new java.sql.Timestamp(((java.util.Date) inValue).getTime()));
            else if (inValue instanceof Calendar) {
                Calendar cal = (Calendar) inValue;
                ps.setTimestamp(paramIndex, new java.sql.Timestamp(cal.getTime().getTime()), cal);
            } else
                ps.setObject(paramIndex, inValue);//ͨ�õĲ������÷���
        } else {
            //ȷ������
            ps.setObject(paramIndex, inValue, sqlType);//ͨ�õĲ������÷���
        }
    }
    /**Set the specified PreparedStatement parameter to null, respecting database-specific peculiarities.*/
    private static void setNull(PreparedStatement ps, int paramIndex, int sqlType/*, String typeName*/) throws SQLException {
        if (sqlType == SqlTypeValue.TYPE_UNKNOWN) {
            boolean useSetObject = false;
            sqlType = Types.NULL;
            try {
                DatabaseMetaData dbmd = ps.getConnection().getMetaData();
                String databaseProductName = dbmd.getDatabaseProductName();
                String jdbcDriverName = dbmd.getDriverName();
                if (databaseProductName.startsWith("Informix") || jdbcDriverName.startsWith("Microsoft SQL Server")) {
                    useSetObject = true;
                } else if (databaseProductName.startsWith("DB2") || jdbcDriverName.startsWith("jConnect") || jdbcDriverName.startsWith("SQLServer") || jdbcDriverName.startsWith("Apache Derby")) {
                    sqlType = Types.VARCHAR;
                }
            } catch (Throwable ex) {
                Hasor.logDebug("Could not check database or driver name", ex);
            }
            if (useSetObject) {
                ps.setObject(paramIndex, null);
            } else {
                ps.setNull(paramIndex, sqlType);
            }
        } //else if (typeName != null) {
          //  ps.setNull(paramIndex, sqlType, typeName);
        /*} */else {
            ps.setNull(paramIndex, sqlType);
        }
    }
    /**
     * Clean up all resources held by parameter values which were passed to an
     * execute method. This is for example important for closing LOB values.
     * @param paramValues parameter values supplied. May be <code>null</code>.
     * @see DisposableSqlTypeValue#cleanup()
     * @see org.noe.lib.jdbcorm.jdbc.core.support.SqlLobValue#cleanup()
     */
    public static void cleanupParameters(Object[] paramValues) {
        if (paramValues != null)
            cleanupParameters(Arrays.asList(paramValues));
    }
    /**
     * Clean up all resources held by parameter values which were passed to an
     * execute method. This is for example important for closing LOB values.
     * @param paramValues parameter values supplied. May be <code>null</code>.
     * @see DisposableSqlTypeValue#cleanup()
     * @see org.noe.lib.jdbcorm.jdbc.core.support.SqlLobValue#cleanup()
     */
    public static void cleanupParameters(Collection paramValues) {
        if (paramValues != null) {
            for (Object inValue : paramValues) {
                if (inValue instanceof DisposableSqlTypeValue) {
                    ((DisposableSqlTypeValue) inValue).cleanup();
                } else if (inValue instanceof SqlValue) {
                    ((SqlValue) inValue).cleanup();
                }
            }
        }
    }
    /**Check whether the given value can be treated as a String value.*/
    private static boolean isStringValue(Class<?> inValueType) {
        // Consider any CharSequence (including StringBuffer and StringBuilder) as a String.
        return (CharSequence.class.isAssignableFrom(inValueType) || StringWriter.class.isAssignableFrom(inValueType));
    }
    /**Check whether the given value is a <code>java.util.Date</code>(but not one of the JDBC-specific subclasses).*/
    private static boolean isDateValue(Class<?> inValueType) {
        return (java.util.Date.class.isAssignableFrom(inValueType) && !(java.sql.Date.class.isAssignableFrom(inValueType) || java.sql.Time.class.isAssignableFrom(inValueType) || java.sql.Timestamp.class.isAssignableFrom(inValueType)));
    }
}