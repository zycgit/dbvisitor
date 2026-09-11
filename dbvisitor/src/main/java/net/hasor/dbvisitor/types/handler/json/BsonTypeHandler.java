/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.types.handler.json;
import java.sql.SQLException;
import java.util.List;
import net.hasor.dbvisitor.types.NoCache;
import org.bson.BsonDocument;
import org.bson.BsonDocumentReader;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;

/**
 * 通用的 {@code BSON -> POJO} TypeHandler。
 * <p>
 * 用法：
 * - 单对象：{@code new BsonTypeHandler<>(Address.class)}
 * - 列表对象：{@code new BsonTypeHandler<>(List.class, OrderItem.class)}
 */
@NoCache
public class BsonTypeHandler extends AbstractBsonTypeHandler {
    private final Class<?> fieldType;

    public BsonTypeHandler(Class<?> documentType) {
        if (documentType == null) {
            throw new IllegalArgumentException("Type argument cannot be null");
        }
        this.fieldType = documentType;
    }

    public Class<?> getFieldType() {
        return this.fieldType;
    }

    @Override
    protected Object toObject(Object obj) throws SQLException {
        if (obj == null) {
            return null;
        }

        if (obj instanceof Document && !List.class.isAssignableFrom(this.fieldType)) {
            Document doc = (Document) obj;
            BsonDocument bsonDoc = doc.toBsonDocument(this.fieldType, CODEC_REGISTRY);
            Codec<?> codec = CODEC_REGISTRY.get(this.fieldType);
            return codec.decode(new BsonDocumentReader(bsonDoc), DecoderContext.builder().build());
        }

        if (fieldType.isInstance(obj)) {
            return obj;
        }

        throw new SQLException("Unsupported type: " + obj.getClass().getName());
    }
}
