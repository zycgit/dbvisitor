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
public class UserNameRowCallback implements RowCallbackHandler {
    private final List<String> result = new ArrayList<>();

    public int size() {
        return result.size();
    }

    public String getName(int i) {
        return result.get(i);
    }

    @Override
    public void processRow(ResultSet rs, int rowNum) throws SQLException {
        this.result.add(rs.getString("name"));
    }
}
