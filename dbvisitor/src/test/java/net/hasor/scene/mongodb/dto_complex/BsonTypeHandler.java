package net.hasor.scene.mongodb.dto_complex;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
 * 通用的 BSON -> POJO TypeHandler。
 * <p/>
 * 用法：
 * - 单对象：new BsonTypeHandler<>(Address.class)
 * - 列表对象：new BsonTypeHandler<>(List.class, OrderItem.class)
 */
public class BsonTypeHandler implements TypeHandler<Object> {
    private final Class<?>      documentType;
    private final CodecRegistry codecRegistry;

    public BsonTypeHandler(Class<?> documentType) {
        if (documentType == null) {
            throw new IllegalArgumentException("Type argument cannot be null");
        }
        this.documentType = documentType;
        this.codecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));
    }

    public Class<?> getDocumentType() {
        return this.documentType;
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

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Object toObject(Object obj) throws SQLException {
        if (obj == null) {
            return null;
        }

        if (obj instanceof Document && !List.class.isAssignableFrom(documentType)) {
            Document doc = (Document) obj;
            BsonDocument bsonDoc = doc.toBsonDocument(documentType, codecRegistry);
            Codec codec = codecRegistry.get(documentType);
            Object decoded = codec.decode(new BsonDocumentReader(bsonDoc), DecoderContext.builder().build());
            return decoded;
        }

        if (documentType.isInstance(obj)) {
            return obj;
        }

        throw new SQLException("Unsupported type: " + obj.getClass().getName());
    }
}
