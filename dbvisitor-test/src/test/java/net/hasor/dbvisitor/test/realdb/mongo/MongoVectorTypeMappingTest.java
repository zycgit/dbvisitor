/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mongo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import net.hasor.cobble.reflect.resolvable.ResolvableType;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.test.contract.api.vector_query.VectorTypeMappingCase;
import net.hasor.dbvisitor.test.contract.material.model.ProductVectorForPg;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MongoProfile;
import net.hasor.dbvisitor.test.realdb.mongo.material.MongoEntityFixture;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;
import net.hasor.dbvisitor.types.handler.json.BsonListTypeHandler;
import org.junit.After;
import org.junit.Before;

/** Shared embedding storage assertions using BSON numeric arrays, without vector search. */
public class MongoVectorTypeMappingTest extends VectorTypeMappingCase {
    private final MongoEntityFixture fixture = new MongoEntityFixture();

    @Override
    protected DataSourceProfile profile() {
        return MongoProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws IOException, SQLException {
        this.jdbcTemplate = this.fixture.open();
        TypeHandlerRegistry handlers = new TypeHandlerRegistry();
        handlers.register(List.class, new BsonListTypeHandler(ResolvableType.forClassWithGenerics(List.class, Float.class)));
        MappingRegistry mappings = new MappingRegistry(null, handlers, Options.of());
        String mapping = """
                <?xml version="1.0" encoding="UTF-8"?>
                <!DOCTYPE mapper PUBLIC "-//dbvisitor.net//DTD Mapper 1.0//EN"
                        "https://www.dbvisitor.net/schema/dbvisitor-mapper.dtd">
                <mapper>
                    <entity table="%s" type="%s">
                        <id column="id" property="id"/>
                        <mapping column="name" property="name"/>
                        <mapping column="embedding" property="embedding"/>
                    </entity>
                </mapper>
                """.formatted(this.fixture.table(), ProductVectorForPg.class.getName());
        try (ByteArrayInputStream input = new ByteArrayInputStream(mapping.getBytes(StandardCharsets.UTF_8))) {
            mappings.loadMapping("mongo-vector-storage", input);
        }
        this.lambdaTemplate = new LambdaTemplate(this.jdbcTemplate.getConnection(), mappings, null);
    }

    @After
    public void closeFixture() throws SQLException {
        this.fixture.close();
    }
}
