/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic7.material;

import java.sql.SQLException;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.test.contract.material.model.naming.CamelCaseColumnOverrideUser;
import net.hasor.dbvisitor.test.contract.material.model.naming.CamelCaseDisabledUser;
import net.hasor.dbvisitor.test.contract.material.model.naming.CamelCaseEnabledUser;
import net.hasor.dbvisitor.test.contract.material.model.naming.PlainUser;
import net.hasor.dbvisitor.test.contract.material.model.naming.UpperCaseColumnStrictUser;
import net.hasor.dbvisitor.test.contract.material.model.naming.UpperCaseColumnUser;

/** The naming contracts vary entity mappings over the same native document fields. */
public final class ElasticNamingFixture implements AutoCloseable {
    private final ElasticMatrixFixture fixture = new ElasticMatrixFixture();

    public JdbcTemplate open(String environment) throws SQLException {
        return fixture.open(environment);
    }

    public String index() {
        return fixture.index();
    }

    public LambdaTemplate lambda(Options options) throws SQLException {
        MappingRegistry registry = new MappingRegistry(null, options);
        registry.loadEntityAsTable(CamelCaseColumnOverrideUser.class, index());
        registry.loadEntityAsTable(CamelCaseDisabledUser.class, index());
        registry.loadEntityAsTable(CamelCaseEnabledUser.class, index());
        registry.loadEntityAsTable(PlainUser.class, index());
        registry.loadEntityAsTable(UpperCaseColumnStrictUser.class, index());
        registry.loadEntityAsTable(UpperCaseColumnUser.class, index());
        return new LambdaTemplate(fixture.connection(), registry, null);
    }

    public String insert(String columns) {
        StringBuilder body = new StringBuilder();
        for (String column : columns.split(",")) {
            if (body.length() != 0) {
                body.append(',');
            }
            body.append('"').append(column.trim()).append("\": ?");
        }
        return "POST /" + index() + "/_doc?refresh=true {" + body + "}";
    }

    @Override
    public void close() throws SQLException {
        fixture.close();
    }
}
