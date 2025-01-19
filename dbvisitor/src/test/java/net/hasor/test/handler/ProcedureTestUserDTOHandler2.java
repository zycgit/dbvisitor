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
package net.hasor.test.handler;
import net.hasor.dbvisitor.template.jdbc.RowCallbackHandler;
import net.hasor.test.dto.ProcedureTestUserDTO;
import net.hasor.test.mapper.ProcedureTestUserDTOMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2013-12-10
 */
public class ProcedureTestUserDTOHandler2 implements RowCallbackHandler {
    private static final List<ProcedureTestUserDTO> result = new ArrayList<>();

    public static List<ProcedureTestUserDTO> getResult() {
        return result;
    }

    @Override
    public void processRow(ResultSet rs, int rowNum) throws SQLException {
        result.add(new ProcedureTestUserDTOMapper().mapRow(rs, rowNum));
    }
}