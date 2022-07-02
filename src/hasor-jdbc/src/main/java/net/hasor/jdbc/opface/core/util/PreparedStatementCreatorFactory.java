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
package net.hasor.jdbc.opface.core.util;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import net.hasor.core.Hasor;
import net.hasor.jdbc.InvalidDataAccessException;
import net.hasor.jdbc.opface.PreparedStatementCreator;
import net.hasor.jdbc.opface.PreparedStatementSetter;
import net.hasor.jdbc.opface.core.ParameterDisposer;
import net.hasor.jdbc.opface.core.SqlProvider;
import net.hasor.jdbc.opface.parameter.SqlParameter;
import net.hasor.jdbc.opface.parameter.SqlVarParameter;
/**
 * Helper class that efficiently creates multiple {@link PreparedStatementCreator}
 * objects with different parameters based on a SQL statement and a single
 * set of parameter declarations.
 *
 * @author Rod Johnson
 * @author Thomas Risberg
 * @author Juergen Hoeller
 */
public class PreparedStatementCreatorFactory {
    /** The SQL, which won't change when the parameters change */
    private final String             sql;
    /** List of SqlParameter objects. May not be <code>null</code>. */
    private final List<SqlParameter> declaredParameters;
    private int                      resultSetType            = ResultSet.TYPE_FORWARD_ONLY;
    private boolean                  updatableResults         = false;
    private boolean                  returnGeneratedKeys      = false;
    private String[]                 generatedKeysColumnNames = null;
    /**
     * Create a new factory. Will need to add parameters via the
     * {@link #addParameter} method or have no parameters.
     */
    public PreparedStatementCreatorFactory(String sql) {
        this.sql = sql;
        this.declaredParameters = new LinkedList<SqlParameter>();
    }
    /**
     * Create a new factory with the given SQL and JDBC types.
     * @param sql SQL to execute
     * @param types int array of JDBC types
     */
    public PreparedStatementCreatorFactory(String sql, int[] types) {
        this.sql = sql;
        this.declaredParameters = SqlParameter.sqlTypesToAnonymousParameterList(types);
    }
    /**
     * Create a new factory with the given SQL and parameters.
     * @param sql SQL
     * @param declaredParameters list of {@link SqlParameter} objects
     * @see SqlParameter
     */
    public PreparedStatementCreatorFactory(String sql, List<SqlParameter> declaredParameters) {
        this.sql = sql;
        this.declaredParameters = declaredParameters;
    }
    /**
     * Add a new declared parameter.
     * <p>Order of parameter addition is significant.
     * @param param the parameter to add to the list of declared parameters
     */
    public void addParameter(SqlParameter param) {
        this.declaredParameters.add(param);
    }
    /**
     * Set whether to use prepared statements that return a specific type of ResultSet.
     * @param resultSetType the ResultSet type
     * @see java.sql.ResultSet#TYPE_FORWARD_ONLY
     * @see java.sql.ResultSet#TYPE_SCROLL_INSENSITIVE
     * @see java.sql.ResultSet#TYPE_SCROLL_SENSITIVE
     */
    public void setResultSetType(int resultSetType) {
        this.resultSetType = resultSetType;
    }
    /**
     * Set whether to use prepared statements capable of returning updatable ResultSets.
     */
    public void setUpdatableResults(boolean updatableResults) {
        this.updatableResults = updatableResults;
    }
    /**
     * Set whether prepared statements should be capable of returning auto-generated keys.
     */
    public void setReturnGeneratedKeys(boolean returnGeneratedKeys) {
        this.returnGeneratedKeys = returnGeneratedKeys;
    }
    /**
     * Set the column names of the auto-generated keys.
     */
    public void setGeneratedKeysColumnNames(String[] names) {
        this.generatedKeysColumnNames = names;
    }
    /**
     * Return a new PreparedStatementSetter for the given parameters.
     * @param params list of parameters (may be <code>null</code>)
     */
    public PreparedStatementSetter newPreparedStatementSetter(List params) {
        return new PreparedStatementCreatorImpl(params != null ? params : Collections.emptyList());
    }
    /**
     * Return a new PreparedStatementSetter for the given parameters.
     * @param params the parameter array (may be <code>null</code>)
     */
    public PreparedStatementSetter newPreparedStatementSetter(Object[] params) {
        return new PreparedStatementCreatorImpl(params != null ? Arrays.asList(params) : Collections.emptyList());
    }
    /**
     * Return a new PreparedStatementCreator for the given parameters.
     * @param params list of parameters (may be <code>null</code>)
     */
    public PreparedStatementCreator newPreparedStatementCreator(List<?> params) {
        return new PreparedStatementCreatorImpl(params != null ? params : Collections.emptyList());
    }
    /**
     * Return a new PreparedStatementCreator for the given parameters.
     * @param params the parameter array (may be <code>null</code>)
     */
    public PreparedStatementCreator newPreparedStatementCreator(Object[] params) {
        return new PreparedStatementCreatorImpl(params != null ? Arrays.asList(params) : Collections.emptyList());
    }
    /**
     * Return a new PreparedStatementCreator for the given parameters.
     * @param sqlToUse the actual SQL statement to use (if different from
     * the factory's, for example because of named parameter expanding)
     * @param params the parameter array (may be <code>null</code>)
     */
    public PreparedStatementCreator newPreparedStatementCreator(String sqlToUse, Object[] params) {
        return new PreparedStatementCreatorImpl(sqlToUse, params != null ? Arrays.asList(params) : Collections.emptyList());
    }
    /**
     * PreparedStatementCreator implementation returned by this class.
     */
    private class PreparedStatementCreatorImpl implements PreparedStatementCreator, PreparedStatementSetter, SqlProvider, ParameterDisposer {
        private final String actualSql;
        private final List   parameters;
        public PreparedStatementCreatorImpl(List<?> parameters) {
            this(sql, parameters);
        }
        public PreparedStatementCreatorImpl(String actualSql, List parameters) {
            this.actualSql = actualSql;
            Hasor.assertIsNotNull(parameters, "Parameters List must not be null");
            this.parameters = parameters;
            if (this.parameters.size() != declaredParameters.size()) {
                // account for named parameters being used multiple times
                Set<String> names = new HashSet<String>();
                for (int i = 0; i < parameters.size(); i++) {
                    Object param = parameters.get(i);
                    if (param instanceof SqlVarParameter) {
                        names.add(((SqlVarParameter) param).getName());
                    } else {
                        names.add("Parameter #" + i);
                    }
                }
                if (names.size() != declaredParameters.size()) {
                    throw new InvalidDataAccessException("SQL [" + sql + "]: given " + names.size() + " parameters but expected " + declaredParameters.size());
                }
            }
        }
        public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
            PreparedStatement ps = null;
            if (generatedKeysColumnNames != null || returnGeneratedKeys) {
                try {
                    if (generatedKeysColumnNames != null) {
                        ps = con.prepareStatement(this.actualSql, generatedKeysColumnNames);
                    } else {
                        ps = con.prepareStatement(this.actualSql, PreparedStatement.RETURN_GENERATED_KEYS);
                    }
                } catch (AbstractMethodError ex) {
                    throw new InvalidDataAccessException("The JDBC driver is not compliant to JDBC 3.0 and thus " + "does not support retrieval of auto-generated keys", ex);
                }
            } else if (resultSetType == ResultSet.TYPE_FORWARD_ONLY && !updatableResults) {
                ps = con.prepareStatement(this.actualSql);
            } else {
                ps = con.prepareStatement(this.actualSql, resultSetType, updatableResults ? ResultSet.CONCUR_UPDATABLE : ResultSet.CONCUR_READ_ONLY);
            }
            setValues(ps);
            return ps;
        }
        public void setValues(PreparedStatement ps) throws SQLException {
            int sqlColIndx = 1;
            for (int i = 0; i < this.parameters.size(); i++) {
                Object in = this.parameters.get(i);
                SqlParameter declaredParameter = null;
                // SqlParameterValue overrides declared parameter metadata, in particular for
                // independence from the declared parameter position in case of named parameters.
                if (in instanceof SqlVarParameter) {
                    SqlVarParameter paramValue = (SqlVarParameter) in;
                    in = paramValue.getValue();
                    declaredParameter = paramValue;
                } else {
                    if (declaredParameters.size() <= i) {
                        throw new InvalidDataAccessException("SQL [" + sql + "]: unable to access parameter number " + (i + 1) + " given only " + declaredParameters.size() + " parameters");
                    }
                    declaredParameter = declaredParameters.get(i);
                }
                if (in instanceof Collection && declaredParameter.getSqlType() != Types.ARRAY) {
                    Collection entries = (Collection) in;
                    for (Object entry : entries) {
                        if (entry instanceof Object[]) {
                            Object[] valueArray = ((Object[]) entry);
                            for (Object argValue : valueArray) {
                                StatementSetterUtils.setParameterValue(ps, sqlColIndx++, declaredParameter, argValue);
                            }
                        } else {
                            StatementSetterUtils.setParameterValue(ps, sqlColIndx++, declaredParameter, entry);
                        }
                    }
                } else {
                    StatementSetterUtils.setParameterValue(ps, sqlColIndx++, declaredParameter, in);
                }
            }
        }
        public String getSql() {
            return sql;
        }
        public void cleanupParameters() {
            StatementSetterUtils.cleanupParameters(this.parameters);
        }
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("PreparedStatementCreatorFactory.PreparedStatementCreatorImpl: sql=[");
            sb.append(sql).append("]; parameters=").append(this.parameters);
            return sb.toString();
        }
    }
}
