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
package net.hasor.dbvisitor.driver;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.List;
import java.util.Map;

public class JdbcArray implements Array {
    private final JdbcConnection connection;
    private final String         baseType;
    private       List<?>        resultValue;

    private void checkOpen() throws SQLException {
        if (this.resultValue == null) {
            throw new SQLException("Array has been freed.");
        }
    }

    JdbcArray(JdbcConnection connection, String baseType, List<?> resultValue) {
        this.connection = connection;
        this.baseType = baseType;
        this.resultValue = resultValue;
    }

    @Override
    public String getBaseTypeName() throws SQLException {
        this.checkOpen();
        return this.baseType;
    }

    @Override
    public int getBaseType() throws SQLException {
        this.checkOpen();
        return this.connection.typeSupport().getTypeNumber(this.baseType);
    }

    @Override
    public Object[] getArray() throws SQLException {
        this.checkOpen();
        return this.resultValue.toArray();
    }

    @Override
    public Object[] getArray(long index, int count) throws SQLException {
        this.checkOpen();
        if (index < 1 || count < 0 || index - 1 > this.resultValue.size() || count > this.resultValue.size() - (index - 1)) {
            throw new SQLException("Invalid Array slice.", JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
        }

        int fromIndex = (int) (index - 1);
        return this.resultValue.subList(fromIndex, fromIndex + count).toArray();
    }

    @Override
    public Object getArray(Map<String, Class<?>> map) throws SQLException {
        throw new SQLFeatureNotSupportedException("not support getArray(Map)");
    }

    @Override
    public Object getArray(long index, int count, Map<String, Class<?>> map) throws SQLException {
        throw new SQLFeatureNotSupportedException("not support getObject(long,int,Map)");
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        throw new SQLFeatureNotSupportedException("not support getObject(long,int,Map)");
    }

    @Override
    public ResultSet getResultSet(long index, int count) throws SQLException {
        throw new SQLFeatureNotSupportedException("not support getObject(long,int,Map)");
    }

    @Override
    public ResultSet getResultSet(Map<String, Class<?>> map) throws SQLException {
        throw new SQLFeatureNotSupportedException("not support getResultSet(Map)");
    }

    @Override
    public ResultSet getResultSet(long index, int count, Map<String, Class<?>> map) throws SQLException {
        throw new SQLFeatureNotSupportedException("not support getResultSet(long,int,Map)");
    }

    @Override
    public void free() {
        this.resultValue = null;
    }
}