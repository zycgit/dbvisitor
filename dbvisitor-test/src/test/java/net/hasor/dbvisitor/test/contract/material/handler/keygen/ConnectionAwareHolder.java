/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.material.handler.keygen;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import net.hasor.dbvisitor.mapping.GeneratedKeyHandler;
import net.hasor.dbvisitor.mapping.GeneratedKeyHandlerContext;
import net.hasor.dbvisitor.mapping.GeneratedKeyHandlerFactory;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;

/**
 * 访问数据库连接的生成器
 */
public class ConnectionAwareHolder implements GeneratedKeyHandlerFactory {
    @Override
    public GeneratedKeyHandler createHolder(GeneratedKeyHandlerContext context) {
        return new GeneratedKeyHandler() {
            @Override
            public boolean onBefore() {
                return true;
            }

            @Override
            public Object beforeApply(Connection conn, Object entity, ColumnMapping mapping) throws SQLException {
                boolean elastic = "Elastic".equalsIgnoreCase(conn.getMetaData().getDatabaseProductName());
                boolean mongo = "Mongo".equalsIgnoreCase(conn.getMetaData().getDatabaseProductName());
                String command = mongo ? "db." + context.getTable() + ".find({}, {_id: 0, id: 1}).sort({id: -1}).limit(1)" : elastic
                        ? "POST /" + context.getTable() + "/_search " + """
                          {"size": 1, "_source": ["id"], "sort": [{"id": "desc"}]}
                          """
                        : "SELECT MAX(id) FROM user_info";
                Integer maxId = 0;
                try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(command)) {
                    if (rs.next()) {
                        maxId = elastic ? rs.getInt("id") : rs.getInt(1);
                    }
                }

                Integer nextId = maxId + 1;
                mapping.getHandler().set(entity, nextId);
                return nextId;
            }
        };
    }
}
