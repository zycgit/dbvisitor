/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.mapper.dto;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import net.hasor.dbvisitor.jdbc.ResultSetExtractor;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2013-12-10
 */
public class UserNameResultSetExtractor implements ResultSetExtractor<List<String>> {
    @Override
    public List<String> extractData(ResultSet rs) throws SQLException {
        List<String> results = new ArrayList<>();
        while (rs.next()) {
            results.add(rs.getString("name"));
        }
        return results;
    }
}
