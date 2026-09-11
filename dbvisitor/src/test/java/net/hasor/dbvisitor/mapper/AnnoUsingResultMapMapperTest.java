/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.mapper;

import net.hasor.dbvisitor.mapper.def.QueryType;
import net.hasor.dbvisitor.mapper.dto.MixUsingResultMapMapper;
import net.hasor.dbvisitor.mapper.dto.UserInfoExt;
import net.hasor.dbvisitor.mapper.dto.UserInfoUsingMap1;
import net.hasor.dbvisitor.mapper.dto.UserInfoUsingMap2;
import org.junit.Test;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2013-12-10
 */
public class AnnoUsingResultMapMapperTest {
    @Test
    public void usingMethodResultMap_1() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(MixUsingResultMapMapper.class);

        StatementDef def = registry.findStatement(MixUsingResultMapMapper.class, "usingMethodResultMap1");
        assert def != null;
        assert def.getConfigNamespace().equals(MixUsingResultMapMapper.class.getName());
        assert def.getResultType() == UserInfoExt.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void usingMethodResultMap_2() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(MixUsingResultMapMapper.class);

        StatementDef def = registry.findStatement(MixUsingResultMapMapper.class, "usingMethodResultMap2");
        assert def != null;
        assert def.getConfigNamespace().equals(MixUsingResultMapMapper.class.getName());
        assert def.getResultType() == UserInfoUsingMap1.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void usingMethodResultMap_3() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(MixUsingResultMapMapper.class);

        StatementDef def = registry.findStatement(MixUsingResultMapMapper.class, "usingMethodResultMap3");
        assert def != null;
        assert def.getConfigNamespace().equals(MixUsingResultMapMapper.class.getName());
        assert def.getResultType() == UserInfoUsingMap2.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void loadResultMap_1() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(MixUsingResultMapMapper.class);

        assert registry.mappingRegistry.findBySpace("net.hasor.dbvisitor.mapper.dto", "userInfo").entityType().equals(UserInfoUsingMap1.class);
        assert registry.mappingRegistry.findBySpace(MixUsingResultMapMapper.class.getName(), "userInfo").entityType().equals(UserInfoExt.class);
        assert registry.mappingRegistry.findBySpace("", UserInfoUsingMap2.class).entityType().equals(UserInfoUsingMap2.class);
    }
}
