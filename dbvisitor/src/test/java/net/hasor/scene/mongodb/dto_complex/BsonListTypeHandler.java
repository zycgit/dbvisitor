package net.hasor.scene.mongodb.dto_complex;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.mongodb.MongoClientSettings;
import net.hasor.dbvisitor.types.TypeHandler;
import org.bson.BsonDocument;
import org.bson.BsonDocumentReader;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

/**
 * 通用的 BSON -> List<POJO> TypeHandler。
 * <p/>
 * 用法：
 * - 单对象：new BsonTypeHandler<>(Address.class)
 * - 列表对象：new BsonTypeHandler<>(List.class, OrderItem.class)
 */
public class BsonListTypeHandler implements TypeHandler<List<Object>> {
    private final Class<?>      elementType;
    private final CodecRegistry codecRegistry;

    public BsonListTypeHandler(Class<?> elementType) {
        if (elementType == null) {
            throw new IllegalArgumentException("Type argument cannot be null");
        }
        this.elementType = elementType;
        this.codecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));
    }

    public Class<?> getElementType() {
        return this.elementType;
    }

    @Override
    public void setParameter(PreparedStatement ps, int i, List<Object> parameter, Integer jdbcType) throws SQLException {
        ps.setObject(i, parameter);
    }

    @Override
    public List<Object> getResult(ResultSet rs, String columnName) throws SQLException {
        return toObject(rs.getObject(columnName));
    }

    @Override
    public List<Object> getResult(ResultSet rs, int columnIndex) throws SQLException {
        return toObject(rs.getObject(columnIndex));
    }

    @Override
    public List<Object> getResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toObject(cs.getObject(columnIndex));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private List<Object> toObject(Object obj) throws SQLException {
        if (obj == null) {
            return null;
        }

        if (obj instanceof List) {
            List src = (List) obj;
            List result = new ArrayList(src.size());
            for (Object item : src) {
                if (item instanceof Document) {
                    Document doc = (Document) item;
                    BsonDocument bsonDoc = doc.toBsonDocument(elementType, codecRegistry);
                    Codec codec = codecRegistry.get(elementType);
                    Object decoded = codec.decode(new BsonDocumentReader(bsonDoc), DecoderContext.builder().build());
                    result.add(decoded);
                } else {
                    result.add(item);
                }
            }
            return (List<Object>) result;
        }

        throw new SQLException("Unsupported type: " + obj.getClass().getName());
    }
}
