/*
 * Copyright 2002-2008 the original author or authors.
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
package net.hasor.jdbc.jdbc.parameter;
import java.util.LinkedList;
import java.util.List;
import net.hasor.Hasor;
/**
 * �������һ��SQL�������塣
 * @author Rod Johnson
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @author ������(zyc@hasor.net)
 * @see java.sql.Types
 */
public class SqlParameter {
    /** The name of the parameter, if any */
    private String    name;
    /** SQL type constant from <code>java.sql.Types</code> */
    private final int sqlType;
    /** Used for types that are user-named like: STRUCT, DISTINCT, JAVA_OBJECT, named array types */
    private String    typeName;
    /** The scale to apply in case of a NUMERIC or DECIMAL type, if any */
    private Integer   scale;
    /**
     * Create a new anonymous SqlParameter, supplying the SQL type.
     * @param sqlType SQL type of the parameter according to <code>java.sql.Types</code>
     */
    public SqlParameter(int sqlType) {
        this.sqlType = sqlType;
    }
    /**
     * Create a new anonymous SqlParameter, supplying the SQL type.
     * @param sqlType SQL type of the parameter according to <code>java.sql.Types</code>
     * @param typeName the type name of the parameter (optional)
     */
    public SqlParameter(int sqlType, String typeName) {
        this.sqlType = sqlType;
        this.typeName = typeName;
    }
    /**
     * Create a new anonymous SqlParameter, supplying the SQL type.
     * @param sqlType SQL type of the parameter according to <code>java.sql.Types</code>
     * @param scale the number of digits after the decimal point
     * (for DECIMAL and NUMERIC types)
     */
    public SqlParameter(int sqlType, int scale) {
        this.sqlType = sqlType;
        this.scale = scale;
    }
    /**
     * Create a new SqlParameter, supplying name and SQL type.
     * @param name name of the parameter, as used in input and output maps
     * @param sqlType SQL type of the parameter according to <code>java.sql.Types</code>
     */
    public SqlParameter(String name, int sqlType) {
        this.name = name;
        this.sqlType = sqlType;
    }
    /**
     * Create a new SqlParameter, supplying name and SQL type.
     * @param name name of the parameter, as used in input and output maps
     * @param sqlType SQL type of the parameter according to <code>java.sql.Types</code>
     * @param typeName the type name of the parameter (optional)
     */
    public SqlParameter(String name, int sqlType, String typeName) {
        this.name = name;
        this.sqlType = sqlType;
        this.typeName = typeName;
    }
    /**
     * Create a new SqlParameter, supplying name and SQL type.
     * @param name name of the parameter, as used in input and output maps
     * @param sqlType SQL type of the parameter according to <code>java.sql.Types</code>
     * @param scale the number of digits after the decimal point
     * (for DECIMAL and NUMERIC types)
     */
    public SqlParameter(String name, int sqlType, int scale) {
        this.name = name;
        this.sqlType = sqlType;
        this.scale = scale;
    }
    /**
     * Copy constructor.
     * @param otherParam the SqlParameter object to copy from
     */
    public SqlParameter(SqlParameter otherParam) {
        Hasor.assertIsNotNull(otherParam, "SqlParameter object must not be null");
        this.name = otherParam.name;
        this.sqlType = otherParam.sqlType;
        this.typeName = otherParam.typeName;
        this.scale = otherParam.scale;
    }
    /**
     * Return the name of the parameter.
     */
    public String getName() {
        return this.name;
    }
    /**
     * Return the SQL type of the parameter.
     */
    public int getSqlType() {
        return this.sqlType;
    }
    /**
     * Return the type name of the parameter, if any.
     */
    public String getTypeName() {
        return this.typeName;
    }
    /**
     * Return the scale of the parameter, if any.
     */
    public Integer getScale() {
        return this.scale;
    }
    /**
     * Return whether this parameter holds input values that should be set
     * before execution even if they are <code>null</code>.
     * <p>This implementation always returns <code>true</code>.
     */
    public boolean isInputValueProvided() {
        return true;
    }
    /**
     * Return whether this parameter is an implicit return parameter used during the
     * results preocessing of the CallableStatement.getMoreResults/getUpdateCount.
     * <p>This implementation always returns <code>false</code>.
     */
    public boolean isResultsParameter() {
        return false;
    }
    /**
     * Convert a list of JDBC types, as defined in <code>java.sql.Types</code>,
     * to a List of SqlParameter objects as used in this package.
     */
    public static List<SqlParameter> sqlTypesToAnonymousParameterList(int[] types) {
        List<SqlParameter> result = new LinkedList<SqlParameter>();
        if (types != null) {
            for (int type : types) {
                result.add(new SqlParameter(type));
            }
        }
        return result;
    }
}