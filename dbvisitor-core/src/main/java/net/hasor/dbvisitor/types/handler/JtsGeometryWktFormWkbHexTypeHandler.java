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
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.codec.HexadecimalUtils;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 以 WKB(HEX) 格式读出并转换为 WKT；或以 WKT 为入参，最终以 WKB(HEX) 写入。
 *    数据库       应用
 *  WKB(HEX)  ->  WKT
 *  WKB(HEX)  <-  WKT
 * @author 赵永春 (zyc@hasor.net)
 */
public class JtsGeometryWktFormWkbHexTypeHandler extends AbstractJtsGeometryTypeHandler<String> {
    protected static String toHEX(byte[] wkb) {
        if (wkb == null) {
            return null;
        } else {
            return HexadecimalUtils.bytes2hex(wkb);
        }
    }

    protected static byte[] toBytes(String hex) {
        if (StringUtils.isBlank(hex)) {
            return null;
        } else {
            if (hex.startsWith("0x") || hex.startsWith("0X")) {
                hex = hex.substring(2);
            }
            return HexadecimalUtils.hex2bytes(hex);
        }
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, Integer jdbcType) throws SQLException {
        if (StringUtils.isBlank(parameter)) {
            ps.setString(i, null);
        } else {
            ps.setString(i, toHEX(toWKB(parameter)));
        }
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toWKT(toBytes(rs.getString(columnName)));
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toWKT(toBytes(rs.getString(columnIndex)));
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toWKT(toBytes(cs.getString(columnIndex)));
    }
}