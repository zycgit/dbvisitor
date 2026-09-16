/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.material.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.jdbc.ResultSetExtractor;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;

/** Supplies a non-List extractor result to a Mapper method declared to return Map. */
public class UserNameMapExtractor implements ResultSetExtractor<Map<Integer, String>> {
    @Override
    public Map<Integer, String> extractData(ResultSet resultSet) throws SQLException {
        return namesById(new CustomResultSetExtractor().extractData(resultSet));
    }

    public static Map<Integer, String> namesById(List<UserInfo> users) {
        Map<Integer, String> names = new LinkedHashMap<>();
        for (UserInfo user : users) {
            names.put(user.getId(), user.getName());
        }
        return names;
    }
}
