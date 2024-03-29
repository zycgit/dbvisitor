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
package net.hasor.dbvisitor.types;
import net.hasor.dbvisitor.types.handler.JsonUseForFastjson2TypeHandler;
import net.hasor.test.dto.UserFutures2;
import net.hasor.test.types.MyTypeHandler;
import org.junit.Test;

import java.io.InputStream;
import java.sql.Types;

public class AnnosTest {
    @Test
    public void testArrayTypeHandler_1() {
        TypeHandlerRegistry.DEFAULT.registerHandler(MyTypeHandler.class, new MyTypeHandler());
        TypeHandlerRegistry.DEFAULT.register(Types.VARCHAR, new MyTypeHandler());
        TypeHandlerRegistry.DEFAULT.register(StringBuilder.class, new MyTypeHandler());
        TypeHandlerRegistry.DEFAULT.registerCross(Types.BIGINT, InputStream.class, new MyTypeHandler());

        assert TypeHandlerRegistry.DEFAULT.hasTypeHandler(StringBuilder.class);
        assert TypeHandlerRegistry.DEFAULT.getTypeHandler(StringBuilder.class) instanceof MyTypeHandler;

        assert TypeHandlerRegistry.DEFAULT.getTypeHandler(Types.VARCHAR) instanceof MyTypeHandler;

        assert TypeHandlerRegistry.DEFAULT.getTypeHandler(String.class, Types.DATALINK) instanceof MyTypeHandler;
        assert TypeHandlerRegistry.DEFAULT.getTypeHandler(StringBuffer.class, Types.VARCHAR) instanceof MyTypeHandler;
        assert TypeHandlerRegistry.DEFAULT.getTypeHandler(InputStream.class, Types.BIGINT) instanceof MyTypeHandler;
    }

    @Test
    public void testBindTypeHandler_2() {
        assert TypeHandlerRegistry.DEFAULT.hasTypeHandler(UserFutures2.class);
        assert TypeHandlerRegistry.DEFAULT.getTypeHandler(UserFutures2.class) instanceof JsonUseForFastjson2TypeHandler;
        assert TypeHandlerRegistry.DEFAULT.getTypeHandler(UserFutures2.class) == TypeHandlerRegistry.DEFAULT.getTypeHandler(UserFutures2.class);
    }

}
