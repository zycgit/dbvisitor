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
package net.hasor.dbvisitor.types.handler;
import net.hasor.cobble.io.IOUtils;

import java.io.*;
import java.sql.*;

/**
 * 使用 {@link Reader} 类型读写 jdbc {@link java.sql.SQLXML} 数据。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2020-10-31
 */
public class SqlXmlAsReaderTypeHandler extends AbstractTypeHandler<Reader> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Reader parameter, Integer jdbcType) throws SQLException {
        SQLXML sqlxml = ps.getConnection().createSQLXML();
        try (OutputStream writer = sqlxml.setBinaryStream()) {
            BufferedReader reader = new BufferedReader(parameter);
            String body = null;
            while ((body = reader.readLine()) != null) {
                writer.write(body.getBytes());
            }
            writer.flush();
            ps.setSQLXML(i, sqlxml);
        } catch (IOException e) {
            throw new SQLException("Error copy xml data to SQLXML for parameter #" + i + " with JdbcType " + jdbcType + ", Cause: " + e.getMessage(), e);
        } finally {
            sqlxml.free();
        }
    }

    @Override
    public Reader getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return sqlXmlToStream(rs.getSQLXML(columnName));
    }

    @Override
    public Reader getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return sqlXmlToStream(rs.getSQLXML(columnIndex));
    }

    @Override
    public Reader getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return sqlXmlToStream(cs.getSQLXML(columnIndex));
    }

    protected Reader sqlXmlToStream(SQLXML sqlxml) throws SQLException {
        if (sqlxml == null) {
            return null;
        }
        StringWriter sw = new StringWriter();
        try (Reader reader = sqlxml.getCharacterStream()) {
            IOUtils.copy(reader, sw);
        } catch (IOException e) {
            throw new SQLException("read chars Xml Data failed : " + e.getMessage(), e);
        } finally {
            sqlxml.free();
        }
        return new StringReader(sw.toString());
    }
}