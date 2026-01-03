package net.hasor.realdb.elastic6.dto_complex;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import net.hasor.dbvisitor.types.TypeHandler;

public class OrderItemListTypeHandler implements TypeHandler<List<OrderItem>> {
    @Override
    public void setParameter(PreparedStatement ps, int i, List<OrderItem> parameter, Integer jdbcType) throws SQLException {
        ps.setString(i, JSON.toJSONString(parameter));
    }

    @Override
    public List<OrderItem> getResult(ResultSet rs, String columnName) throws SQLException {
        String json = rs.getString(columnName);
        return JSON.parseObject(json, new TypeReference<List<OrderItem>>() {
        });
    }

    @Override
    public List<OrderItem> getResult(ResultSet rs, int columnIndex) throws SQLException {
        String json = rs.getString(columnIndex);
        return JSON.parseObject(json, new TypeReference<List<OrderItem>>() {
        });
    }

    @Override
    public List<OrderItem> getResult(CallableStatement cs, int columnIndex) throws SQLException {
        String json = cs.getString(columnIndex);
        return JSON.parseObject(json, new TypeReference<List<OrderItem>>() {
        });
    }
}
