/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.jdbc.core.test;
import net.hasor.dbvisitor.jdbc.mapper.BeanMappingRowMapper;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2013-12-10
 */
public class ProcedureTestUserDTOMapper extends BeanMappingRowMapper<ProcedureTestUserDTO> {
    public ProcedureTestUserDTOMapper() {
        super(ProcedureTestUserDTO.class);
    }
}
