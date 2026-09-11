package net.hasor.dbvisitor.adapter.milvus.commands.query;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Map;
import io.milvus.v2.service.vector.request.QueryIteratorReq;
import io.milvus.v2.service.vector.request.QueryReq;
import io.milvus.v2.service.vector.request.SearchIteratorReqV2;
import io.milvus.v2.service.vector.request.SearchReq;
import net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandKeys;

/** Request-level read options shared by bounded queries and their native iterators. */
final class MilvusQueryOptions {
    private Integer roundDecimal;
    private Boolean ignoreGrowing;
    private String  timezone;

    static MilvusQueryOptions extract(Map<String, Object> properties, boolean vectorSearch) throws SQLException {
        MilvusQueryOptions options = new MilvusQueryOptions();
        if (properties.containsKey(MilvusCommandKeys.ROUND_DECIMAL)) {
            if (!vectorSearch) {
                throw new SQLException("WITH round_decimal requires a vector search.");
            }
            options.roundDecimal = extractRoundDecimal(properties);
        }
        if (properties.containsKey(MilvusCommandKeys.IGNORE_GROWING)) {
            Object value = properties.remove(MilvusCommandKeys.IGNORE_GROWING);
            if (!(value instanceof Boolean)) {
                throw new SQLException("WITH ignore_growing requires a boolean.");
            }
            options.ignoreGrowing = (Boolean) value;
        }
        options.timezone = extractTimezone(properties);
        if (!vectorSearch && !properties.isEmpty()) {
            throw new SQLException("Unsupported scalar query WITH options: " + properties.keySet());
        }
        return options;
    }

    static Integer extractRoundDecimal(Map<String, Object> properties) throws SQLException {
        if (!properties.containsKey(MilvusCommandKeys.ROUND_DECIMAL)) {
            return null;
        }
        Object value = properties.remove(MilvusCommandKeys.ROUND_DECIMAL);
        try {
            if (!(value instanceof Number)) {
                throw new NumberFormatException();
            }
            return new BigDecimal(value.toString()).intValueExact();
        } catch (ArithmeticException | NumberFormatException e) {
            throw new SQLException("WITH round_decimal requires an integer.", e);
        }
    }

    static String extractTimezone(Map<String, Object> properties) throws SQLException {
        if (!properties.containsKey(MilvusCommandKeys.TIMEZONE)) {
            return null;
        }
        Object value = properties.remove(MilvusCommandKeys.TIMEZONE);
        if (!(value instanceof String)) {
            throw new SQLException("WITH timezone requires a string.");
        }
        return (String) value;
    }

    void apply(QueryReq.QueryReqBuilder builder) {
        if (ignoreGrowing != null) {
            builder.ignoreGrowing(ignoreGrowing);
        }
        if (timezone != null) {
            builder.timezone(timezone);
        }
    }

    void apply(QueryIteratorReq.QueryIteratorReqBuilder builder) {
        if (ignoreGrowing != null) {
            builder.ignoreGrowing(ignoreGrowing);
        }
        if (timezone != null) {
            builder.timezone(timezone);
        }
    }

    void apply(SearchReq.SearchReqBuilder builder) {
        if (roundDecimal != null) {
            builder.roundDecimal(roundDecimal);
        }
        if (ignoreGrowing != null) {
            builder.ignoreGrowing(ignoreGrowing);
        }
        if (timezone != null) {
            builder.timezone(timezone);
        }
    }

    void apply(SearchIteratorReqV2.SearchIteratorReqV2Builder builder) {
        if (roundDecimal != null) {
            builder.roundDecimal(roundDecimal);
        }
        if (ignoreGrowing != null) {
            builder.ignoreGrowing(ignoreGrowing);
        }
        if (timezone != null) {
            builder.timezone(timezone);
        }
    }
}
