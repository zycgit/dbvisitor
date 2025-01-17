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
package net.hasor.dbvisitor.mapper;

import net.hasor.cobble.CollectionUtils;
import net.hasor.dbvisitor.dynamic.RegistryManager;
import net.hasor.dbvisitor.dynamic.SqlBuilder;
import net.hasor.dbvisitor.mapper.dto.AnnoQueryMapper;
import net.hasor.dbvisitor.types.SqlArg;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2013-12-10
 */
public class ClassResolveMapperTest {
    @Test
    public void annoQuery_1() throws ReflectiveOperationException, IOException, SQLException {
        Map<String, Object> ctx = CollectionUtils.asMap("abc", "this is abc");
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(AnnoQueryMapper.class);

        StatementDef testSelectArg = registry.findStatement(AnnoQueryMapper.class, "testSelectArg");
        assert testSelectArg != null;
        assert testSelectArg.getNamespace().equals(AnnoQueryMapper.class.getName());

        SqlBuilder sqlBuilder = testSelectArg.buildQuery(ctx, new RegistryManager());
        assert sqlBuilder.getSqlString().equals("select * from console_job where aac = ?");
        assert sqlBuilder.getArgs().length == 1;
        assert ((SqlArg) sqlBuilder.getArgs()[0]).getValue().equals("this is abc");
    }
}
