package net.hasor.dbvisitor.test.realdb.elastic7.dto_complex;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import net.hasor.dbvisitor.types.TypeHandler;

public class OrderItemListTypeHandler implements TypeHandler<List<OrderItem>> {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public void setParameter(PreparedStatement ps, int i, List<OrderItem> parameter, Integer jdbcType) throws SQLException {
        try {
            ps.setString(i, MAPPER.writeValueAsString(parameter));
        } catch (Exception e) {
            throw new SQLException(e);
        }
    }

    @Override
    public List<OrderItem> getResult(ResultSet rs, String columnName) throws SQLException {
        String json = rs.getString(columnName);
        try {
            return MAPPER.readValue(json, new TypeReference<List<OrderItem>>() {});
        } catch (Exception e) {
            throw new SQLException(e);
        }
    }

    @Override
    public List<OrderItem> getResult(ResultSet rs, int columnIndex) throws SQLException {
        String json = rs.getString(columnIndex);
        try {
            return MAPPER.readValue(json, new TypeReference<List<OrderItem>>() {});
        } catch (Exception e) {
            throw new SQLException(e);
        }
    }

    @Override
    public List<OrderItem> getResult(CallableStatement cs, int columnIndex) throws SQLException {
        String json = cs.getString(columnIndex);
        try {
            return MAPPER.readValue(json, new TypeReference<List<OrderItem>>() {});
        } catch (Exception e) {
            throw new SQLException(e);
        }
    }
}
