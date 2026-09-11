/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.adapter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.lambda.EntityQuery;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;

/** Owns document-store row material and a private entity mapping, never the global registry. */
public final class NativeLambdaResultFixture {
    private final Map<Integer, String> labels = new LinkedHashMap<>();
    private LambdaTemplate lambda;

    public LambdaTemplate initialize(Connection connection, String collection) throws SQLException {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityAsTable(UserInfo.class, collection);
        this.lambda = new LambdaTemplate(connection, registry, null);
        return this.lambda;
    }

    public void insert(int id, String name, Integer age, String email) throws SQLException {
        UserInfo row = new UserInfo();
        row.setId(id);
        row.setName(name);
        row.setAge(age);
        row.setEmail(email);
        row.setCreateTime(new Date());
        this.lambda.insert(UserInfo.class).applyEntity(row).executeSumResult();
        this.labels.put(id, name);
    }

    public EntityQuery<? extends UserInfo> queryRows(String prefix) {
        // Select known fixture rows through native ID predicates; LIKE has its own capability tests.
        List<Integer> ids = new ArrayList<>();
        for (Map.Entry<Integer, String> entry : this.labels.entrySet()) {
            if (entry.getValue().startsWith(prefix)) {
                ids.add(entry.getKey());
            }
        }
        return this.lambda.query(UserInfo.class).in(UserInfo::getId, ids);
    }

    public boolean hasRows() {
        return !this.labels.isEmpty();
    }
}
