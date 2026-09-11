/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.jdbc.core.test;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import net.hasor.dbvisitor.jdbc.RowCallbackHandler;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2013-12-10
 */
public class ProcedureTestUserDTOHandler implements RowCallbackHandler {
    private static final List<ProcedureTestUserDTO> result = new ArrayList<>();

    public static List<ProcedureTestUserDTO> getResult() {
        return result;
    }

    @Override
    public void processRow(ResultSet rs, int rowNum) throws SQLException {
        result.add(new ProcedureTestUserDTOMapper().mapRow(rs, rowNum));
    }
}
