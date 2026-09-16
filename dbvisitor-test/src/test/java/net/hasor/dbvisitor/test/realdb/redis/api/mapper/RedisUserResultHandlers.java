/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.jdbc.ResultSetExtractor;
import net.hasor.dbvisitor.jdbc.RowMapper;
import net.hasor.dbvisitor.test.contract.material.handler.RecordingRowCallbackHandler;
import net.hasor.dbvisitor.test.contract.material.handler.ResultHandlerProbe;
import net.hasor.dbvisitor.test.contract.material.handler.UserNameMapExtractor;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.types.handler.json.JsonTypeHandler;

/** Result handlers read Redis's actual ELEMENT column. */
public final class RedisUserResultHandlers {
    private RedisUserResultHandlers() {
    }

    private static UserInfo read(ResultSet result) throws SQLException {
        try {
            return (UserInfo) new JsonTypeHandler(UserInfo.class).getResult(result, "ELEMENT");
        } catch (ClassNotFoundException e) {
            throw new SQLException(e);
        }
    }

    public static class Rows implements RowMapper<UserInfo> {
        @Override
        public UserInfo mapRow(ResultSet result, int rowNum) throws SQLException {
            ResultHandlerProbe.record(result);
            UserInfo user = read(result);
            user.setName("[Row" + rowNum + "]" + user.getName());
            return user;
        }
    }

    public static class Extractor implements ResultSetExtractor<List<UserInfo>> {
        @Override
        public List<UserInfo> extractData(ResultSet result) throws SQLException {
            ResultHandlerProbe.record(result);
            List<UserInfo> users = new ArrayList<>();
            while (result.next()) {
                users.add(read(result));
            }
            return users;
        }
    }

    public static class Callback extends RecordingRowCallbackHandler {
        @Override
        protected int readId(ResultSet result) throws SQLException {
            return read(result).getId();
        }
    }

    public static class MapExtractor implements ResultSetExtractor<Map<Integer, String>> {
        @Override
        public Map<Integer, String> extractData(ResultSet result) throws SQLException {
            return UserNameMapExtractor.namesById(new Extractor().extractData(result));
        }
    }
}
