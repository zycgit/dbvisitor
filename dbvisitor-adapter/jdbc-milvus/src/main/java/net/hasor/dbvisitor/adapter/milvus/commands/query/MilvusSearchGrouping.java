package net.hasor.dbvisitor.adapter.milvus.commands.query;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import io.milvus.v2.service.vector.request.HybridSearchReq;
import io.milvus.v2.service.vector.request.SearchReq;
import net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandKeys;
import static net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandUtils.integerBound;

/** Native group windows are separate from the SQL/JDBC row window. */
final class MilvusSearchGrouping {
    private static final List<String> KEYS = Arrays.asList(MilvusCommandKeys.GROUP_BY_FIELD, MilvusCommandKeys.GROUP_LIMIT, MilvusCommandKeys.GROUP_OFFSET, MilvusCommandKeys.GROUP_SIZE, MilvusCommandKeys.STRICT_GROUP_SIZE);
    private final        String       field;
    private final        int          limit;
    private final        long         offset;
    private final        Integer      size;
    private final        Boolean      strict;

    private MilvusSearchGrouping(String field, int limit, long offset, Integer size, Boolean strict) {
        this.field = field;
        this.limit = limit;
        this.offset = offset;
        this.size = size;
        this.strict = strict;
    }

    static boolean isSpecified(Map<String, Object> properties) {
        return KEYS.stream().anyMatch(properties::containsKey);
    }

    static MilvusSearchGrouping extract(Map<String, Object> properties) throws SQLException {
        if (!isSpecified(properties)) {
            return null;
        }
        Object field = properties.remove(MilvusCommandKeys.GROUP_BY_FIELD);
        if (!(field instanceof String) || ((String) field).isBlank()) {
            throw new SQLException("Grouped search requires a nonempty " + MilvusCommandKeys.GROUP_BY_FIELD + " string.");
        }
        if (!properties.containsKey(MilvusCommandKeys.GROUP_LIMIT)) {
            throw new SQLException("Grouped search requires " + MilvusCommandKeys.GROUP_LIMIT + "; SQL LIMIT counts rows, not groups.");
        }
        int limit = positiveInt(properties.remove(MilvusCommandKeys.GROUP_LIMIT), MilvusCommandKeys.GROUP_LIMIT);
        long offset = properties.containsKey(MilvusCommandKeys.GROUP_OFFSET) ? integerBound(properties.remove(MilvusCommandKeys.GROUP_OFFSET), MilvusCommandKeys.GROUP_OFFSET, 0) : 0;
        Integer size = properties.containsKey(MilvusCommandKeys.GROUP_SIZE) ? positiveInt(properties.remove(MilvusCommandKeys.GROUP_SIZE), MilvusCommandKeys.GROUP_SIZE) : null;
        Boolean strict = null;
        if (properties.containsKey(MilvusCommandKeys.STRICT_GROUP_SIZE)) {
            Object value = properties.remove(MilvusCommandKeys.STRICT_GROUP_SIZE);
            if (!(value instanceof Boolean)) {
                throw new SQLException(MilvusCommandKeys.STRICT_GROUP_SIZE + " requires a boolean.");
            }
            strict = (Boolean) value;
        }
        try {
            Math.addExact(offset, limit);
        } catch (ArithmeticException e) {
            throw new SQLException("Group offset plus limit exceeds the SDK long range.", e);
        }
        return new MilvusSearchGrouping((String) field, limit, offset, size, strict);
    }

    void apply(SearchReq.SearchReqBuilder builder) {
        builder.groupByFieldName(field).topK(limit).offset(offset);
        if (size != null) {
            builder.groupSize(size);
        }
        if (strict != null) {
            builder.strictGroupSize(strict);
        }
    }

    void apply(HybridSearchReq.HybridSearchReqBuilder builder) {
        builder.groupByFieldName(field).limit(limit).offset(offset);
        if (size != null) {
            builder.groupSize(size);
        }
        if (strict != null) {
            builder.strictGroupSize(strict);
        }
    }

    private static int positiveInt(Object value, String name) throws SQLException {
        long bound = integerBound(value, name, 1);
        if (bound > Integer.MAX_VALUE) {
            throw new SQLException(name + " exceeds the SDK integer range.");
        }
        return (int) bound;
    }
}
