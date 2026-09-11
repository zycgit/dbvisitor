/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.mapper;

import net.hasor.dbvisitor.mapper.dto.ResultTypeMixMapper;
import org.junit.Test;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2013-12-10
 */
public class ResultTypeMapperMixTest {
    @Test
    public void selectBool_1() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(ResultTypeMixMapper.class);

        StatementDef def = registry.findStatement(ResultTypeMixMapper.class, "selectBool_1");
        assert def.getResultType() == boolean.class;
    }

    @Test
    public void selectBool_2() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(ResultTypeMixMapper.class);

        StatementDef def = registry.findStatement(ResultTypeMixMapper.class, "selectBool_2");
        assert def.getResultType() == boolean.class;
    }

    @Test
    public void selectBool_3() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(ResultTypeMixMapper.class);

        StatementDef def = registry.findStatement(ResultTypeMixMapper.class, "selectBool_3");
        assert def.getResultType() == boolean.class;
    }

    @Test
    public void selectShort_1() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(ResultTypeMixMapper.class);

        StatementDef def = registry.findStatement(ResultTypeMixMapper.class, "selectShort_1");
        assert def.getResultType() == Short.class;
    }

    @Test
    public void selectShort_2() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(ResultTypeMixMapper.class);

        StatementDef def = registry.findStatement(ResultTypeMixMapper.class, "selectShort_2");
        assert def.getResultType() == Short.class;
    }

    @Test
    public void selectDate_1() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(ResultTypeMixMapper.class);

        StatementDef def = registry.findStatement(ResultTypeMixMapper.class, "selectDate_1");
        assert def.getResultType() == java.util.Date.class;
    }

    @Test
    public void selectDate_2() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(ResultTypeMixMapper.class);

        StatementDef def = registry.findStatement(ResultTypeMixMapper.class, "selectDate_2");
        assert def.getResultType() == java.sql.Date.class;
    }

    @Test
    public void selectDate_3() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(ResultTypeMixMapper.class);

        StatementDef def = registry.findStatement(ResultTypeMixMapper.class, "selectDate_3");
        assert def.getResultType() == java.sql.Time.class;
    }
}
