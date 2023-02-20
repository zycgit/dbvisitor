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
package net.hasor.dbvisitor.faker.provider.postgresql.typehandler;
import net.hasor.cobble.codec.HexadecimalUtils;
import net.hasor.cobble.logging.Logger;
import net.hasor.dbvisitor.faker.generator.processor.TypeHandlerFactory;
import net.hasor.dbvisitor.types.TypeHandler;
import net.hasor.dbvisitor.types.handler.pg.PgArrayTypeHandler;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKBReader;

import java.util.Map;

/**
 * TypeHandlerFactory 实现类，用于创建 PG 数组类型读写器。
 * @version : 2023-02-19
 * @author 赵永春 (zyc@hasor.net)
 */
public class PgArrayTypeHandlerFactory implements TypeHandlerFactory {
    private static final Logger          logger  = Logger.getLogger(PgArrayTypeHandlerFactory.class);
    private static final GeometryFactory factory = new GeometryFactory();

    protected static String geometryString(String geometryString) {
        try {
            byte[] geometryBytes = HexadecimalUtils.hex2bytes(geometryString);
            Geometry object = new WKBReader(factory).read(geometryBytes);
            return object.toText();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return geometryString;
        }
    }

    @Override
    public TypeHandler<?> createTypeHandler(Map<String, Object> context) {
        String elementType = String.valueOf(context.get("columnType")); // 数组元素类型
        int dimCount = 0; // 数组维度

        while (elementType.length() > 0 && elementType.charAt(0) == '_') {
            elementType = elementType.substring(1);
            dimCount++;
        }

        return new FakerPgArrayTypeHandler(elementType, dimCount);
    }

    private static class FakerPgArrayTypeHandler extends PgArrayTypeHandler {
        public FakerPgArrayTypeHandler(String elementType, int dimCount) {
            super(elementType, dimCount);
        }

        protected PostgresReadArrayHandler createPostgresReadArrayHandler(String elementType) {
            if ("geometry".equals(elementType)) {
                return rs -> geometryString(rs.getString("VALUE"));
            }
            return super.createPostgresReadArrayHandler(elementType);
        }
    }
}
