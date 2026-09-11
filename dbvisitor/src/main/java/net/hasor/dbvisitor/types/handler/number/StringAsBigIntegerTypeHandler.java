/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.types.handler.number;
import java.math.BigInteger;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.types.handler.AbstractTypeHandler;

/**
 * 使用 {@link BigInteger} 类型读写 jdbc string 数据。
 * - 例如：已经超出了数据库存储精度范围的超大数就可以使用字符串存储，同时程序读取仍然是 {@link BigInteger}。
 * @author 赵永春 (zyc@hasor.net)
 */
public class StringAsBigIntegerTypeHandler extends AbstractTypeHandler<BigInteger> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, BigInteger parameter, Integer jdbcType) throws SQLException {
        ps.setString(i, parameter.toString());
    }

    @Override
    public BigInteger getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return StringUtils.isBlank(value) ? null : new BigInteger(value);
    }

    @Override
    public BigInteger getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return StringUtils.isBlank(value) ? null : new BigInteger(value);
    }

    @Override
    public BigInteger getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return StringUtils.isBlank(value) ? null : new BigInteger(value);
    }
}
