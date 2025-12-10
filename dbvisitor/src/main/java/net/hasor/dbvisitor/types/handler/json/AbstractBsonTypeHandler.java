package net.hasor.dbvisitor.types.handler.json;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.mongodb.MongoClientSettings;
import net.hasor.cobble.logging.Logger;
import net.hasor.dbvisitor.types.TypeHandler;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

/**
 * 通用的 BSON -> POJO TypeHandler。
 * <p/>
 * 用法：
 * - 单对象：new BsonTypeHandler<>(Address.class)
 * - 列表对象：new BsonTypeHandler<>(List.class, OrderItem.class)
 */
public abstract class AbstractBsonTypeHandler implements TypeHandler<Object> {
    private final static   Logger        logger = Logger.getLogger(AbstractBsonTypeHandler.class);
    protected static final CodecRegistry CODEC_REGISTRY;

    static {
        List<CodecRegistry> codecRegistryList = new ArrayList<>();

        try {
            codecRegistryList.add(MongoClientSettings.getDefaultCodecRegistry());
        } catch (Exception e) {
            logger.warn("load MongoClientSettings.getDefaultCodecRegistry() failed." + e.getMessage());
        }
        codecRegistryList.add(CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));

        CODEC_REGISTRY = CodecRegistries.fromRegistries(codecRegistryList);
    }

    @Override
    public void setParameter(PreparedStatement ps, int i, Object parameter, Integer jdbcType) throws SQLException {
        ps.setObject(i, parameter);
    }

    @Override
    public Object getResult(ResultSet rs, String columnName) throws SQLException {
        return toObject(rs.getObject(columnName));
    }

    @Override
    public Object getResult(ResultSet rs, int columnIndex) throws SQLException {
        return toObject(rs.getObject(columnIndex));
    }

    @Override
    public Object getResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toObject(cs.getObject(columnIndex));
    }

    protected abstract Object toObject(Object obj) throws SQLException;
}
