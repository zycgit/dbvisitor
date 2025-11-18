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
package net.hasor.dbvisitor.jdbc.extractor;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import net.hasor.dbvisitor.jdbc.JdbcOperations;
import net.hasor.dbvisitor.jdbc.ResultSetExtractor;
import net.hasor.dbvisitor.types.TypeHandler;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @see JdbcOperations#queryForPairs(String, Class, Class)
 * @since 1.2
 */
public class PairsResultSetExtractor<K, V> implements ResultSetExtractor<Map<K, V>> {
    protected final TypeHandlerRegistry      typeHandler;
    private final   TypeHandler<? extends K> keyTypeHandler;
    private final   TypeHandler<? extends V> valueTypeHandler;

    public PairsResultSetExtractor() {
        this(TypeHandlerRegistry.DEFAULT);
    }

    public PairsResultSetExtractor(TypeHandlerRegistry typeHandler) {
        this.typeHandler = typeHandler;
        this.keyTypeHandler = null;
        this.valueTypeHandler = null;
    }

    public PairsResultSetExtractor(TypeHandler<? extends K> keyTypeHandler, TypeHandler<? extends V> valueTypeHandler) {
        this.typeHandler = TypeHandlerRegistry.DEFAULT;
        this.keyTypeHandler = keyTypeHandler;
        this.valueTypeHandler = valueTypeHandler;
    }

    public PairsResultSetExtractor(TypeHandlerRegistry typeHandler, Class<? extends K> keyType, Class<? extends V> valueType) {
        this.typeHandler = typeHandler;
        this.keyTypeHandler = (TypeHandler<K>) typeHandler.getTypeHandler(keyType);
        this.valueTypeHandler = (TypeHandler<V>) typeHandler.getTypeHandler(valueType);
    }

    @Override
    public Map<K, V> extractData(ResultSet rs) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        Map<K, V> mapOfPairs = this.createPairsMap();

        int count = rsmd.getColumnCount();
        int rowNum = 0;
        if (count >= 2) {
            TypeHandler<? extends K> kTypeHandler = this.keyTypeHandler != null ? this.keyTypeHandler : (TypeHandler<? extends K>) this.createKeyTypeHandler(rs);
            TypeHandler<? extends V> vTypeHandler = this.valueTypeHandler != null ? this.valueTypeHandler : (TypeHandler<? extends V>) this.createValueTypeHandler(rs);
            while (rs.next()) {
                rowNum++;
                K pairKey = this.extractKey(rs, rowNum, kTypeHandler);
                V pairValue = this.extractValue(rs, rowNum, vTypeHandler);
                mapOfPairs.put(pairKey, pairValue);
            }
        } else {
            TypeHandler<? extends K> kTypeHandler = this.keyTypeHandler != null ? this.keyTypeHandler : (TypeHandler<? extends K>) this.createKeyTypeHandler(rs);
            while (rs.next()) {
                rowNum++;
                K pairKey = this.extractKey(rs, rowNum, kTypeHandler);
                mapOfPairs.put(pairKey, null);
            }
        }
        return mapOfPairs;
    }

    protected TypeHandler<?> createKeyTypeHandler(ResultSet rs) throws SQLException {
        return this.typeHandler.getResultSetTypeHandler(rs, 1, null);
    }

    protected TypeHandler<?> createValueTypeHandler(ResultSet rs) throws SQLException {
        return this.typeHandler.getResultSetTypeHandler(rs, 2, null);
    }

    protected K extractKey(ResultSet rs, int rowNum, TypeHandler<? extends K> typeHandler) throws SQLException {
        return typeHandler.getResult(rs, 1);
    }

    /** 取得指定列的值 */
    protected V extractValue(ResultSet rs, int rowNum, TypeHandler<? extends V> typeHandler) throws SQLException {
        return typeHandler.getResult(rs, 2);
    }

    /** 创建一个 Map 用于存放数据 */
    protected Map<K, V> createPairsMap() {
        return new LinkedHashMap<>();
    }
}
