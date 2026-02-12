package net.hasor.dbvisitor.types.handler.array;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import net.hasor.dbvisitor.types.TypeHandler;

/**
 * pgvector 的 vector 类型 TypeHandler
 * <p>将 List&lt;Float&gt; 和 PostgreSQL pgvector 的 vector 类型互转</p>
 * <ul>
 *   <li>写入：List&lt;Float&gt; → "[1.0,2.0,3.0]" 字符串，以 Types.OTHER 传入</li>
 *   <li>读取：pgvector 返回的字符串 "[1.0,2.0,3.0]" → List&lt;Float&gt;</li>
 * </ul>
 */
public class PgVectorTypeHandler implements TypeHandler<List<Float>> {

    /** List<Float> → pgvector 文本格式 "[1.0,2.0,3.0]" */
    private static String toVectorString(List<Float> vector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(vector.get(i));
        }
        sb.append(']');
        return sb.toString();
    }

    /** pgvector 文本格式 "[1.0,2.0,3.0]" → List<Float> */
    private static List<Float> parseVector(String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }
        // 去除方括号
        str = str.trim();
        if (str.startsWith("[")) {
            str = str.substring(1);
        }
        if (str.endsWith("]")) {
            str = str.substring(0, str.length() - 1);
        }
        String[] parts = str.split(",");
        List<Float> result = new ArrayList<>(parts.length);
        for (String part : parts) {
            result.add(Float.parseFloat(part.trim()));
        }
        return result;
    }

    @Override
    public void setParameter(PreparedStatement ps, int i, List<Float> parameter, Integer jdbcType) throws SQLException {
        if (parameter == null) {
            ps.setNull(i, Types.OTHER);
        } else {
            ps.setObject(i, toVectorString(parameter), Types.OTHER);
        }
    }

    @Override
    public List<Float> getResult(ResultSet rs, String columnName) throws SQLException {
        String val = rs.getString(columnName);
        return rs.wasNull() ? null : parseVector(val);
    }

    @Override
    public List<Float> getResult(ResultSet rs, int columnIndex) throws SQLException {
        String val = rs.getString(columnIndex);
        return rs.wasNull() ? null : parseVector(val);
    }

    @Override
    public List<Float> getResult(CallableStatement cs, int columnIndex) throws SQLException {
        String val = cs.getString(columnIndex);
        return cs.wasNull() ? null : parseVector(val);
    }
}
