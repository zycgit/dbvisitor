/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.types.handler.io;
import java.io.*;
import java.sql.*;
import net.hasor.cobble.io.IOUtils;
import net.hasor.dbvisitor.types.handler.AbstractTypeHandler;

/**
 * 使用 {@link InputStream} 类型读写 jdbc {@link java.sql.SQLXML} 数据。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-31
 */
public class SqlXmlAsInputStreamTypeHandler extends AbstractTypeHandler<InputStream> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, InputStream parameter, Integer jdbcType) throws SQLException {
        SQLXML sqlxml = ps.getConnection().createSQLXML();
        try (OutputStream writer = sqlxml.setBinaryStream()) {
            IOUtils.copy(parameter, writer);
            ps.setSQLXML(i, sqlxml);
        } catch (IOException e) {
            throw new SQLException("Error copy xml data to SQLXML for parameter #" + i + " with JdbcType " + jdbcType + ", Cause: " + e.getMessage(), e);
        } finally {
            sqlxml.free();
        }
    }

    @Override
    public InputStream getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return sqlXmlToStream(rs.getSQLXML(columnName));
    }

    @Override
    public InputStream getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return sqlXmlToStream(rs.getSQLXML(columnIndex));
    }

    @Override
    public InputStream getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return sqlXmlToStream(cs.getSQLXML(columnIndex));
    }

    protected InputStream sqlXmlToStream(SQLXML sqlxml) throws SQLException {
        if (sqlxml == null) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (InputStream binaryStream = sqlxml.getBinaryStream()) {
            IOUtils.copy(binaryStream, baos);
        } catch (IOException e) {
            throw new SQLException("read binary Xml Data failed : " + e.getMessage(), e);
        } finally {
            sqlxml.free();
        }
        return new ByteArrayInputStream(baos.toByteArray());
    }
}
