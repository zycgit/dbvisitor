/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic7.material;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.types.TypeHandler;

/** A dense-vector index for the shared API contracts, without PostgreSQL-specific mapping. */
public final class ElasticVectorFixture implements AutoCloseable {
    private final String index = "nxn_vector_" + UUID.randomUUID().toString().replace("-", "");
    private Connection connection;
    private JdbcTemplate jdbc;
    private boolean created;

    public LambdaTemplate open() throws SQLException, IOException {
        OneApiDataSourceManager.assumeCurrentDataSource("es7");
        connection = OneApiDataSourceManager.getConnection("es7");
        jdbc = new JdbcTemplate(connection);
        jdbc.execute("PUT /" + index + " " + """
                {"mappings": {"properties": {"id": {"type": "integer"},"name": {"type": "keyword"},
                 "embedding": {"type": "dense_vector","dims": 128}}}}
                """);
        created = true;
        String mapping = """
                <?xml version="1.0" encoding="UTF-8"?>
                <!DOCTYPE mapper PUBLIC "-//dbvisitor.net//DTD Mapper 1.0//EN"
                    "https://www.dbvisitor.net/schema/dbvisitor-mapper.dtd">
                <mapper>
                    <entity table="%s" type="net.hasor.dbvisitor.test.contract.material.model.ProductVectorForPg">
                        <id column="id" property="id"/>
                        <mapping column="name" property="name"/>
                        <mapping column="embedding" property="embedding" typeHandler="%s"/>
                    </entity>
                </mapper>
                """.formatted(index, FloatListHandler.class.getName());
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapping(index, new ByteArrayInputStream(mapping.getBytes(StandardCharsets.UTF_8)));
        return new LambdaTemplate(connection, registry, null);
    }

    @Override
    public void close() throws SQLException {
        try {
            if (created) {
                jdbc.execute("DELETE /" + index);
            }
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    /** JSON numbers have no Java float/double distinction; the entity explicitly requires Float. */
    public static final class FloatListHandler implements TypeHandler<List<Float>> {
        @Override
        public void setParameter(PreparedStatement statement, int index, List<Float> value, Integer jdbcType) throws SQLException {
            statement.setObject(index, value);
        }

        @Override
        public List<Float> getResult(ResultSet result, String column) throws SQLException {
            return floats(result.getObject(column));
        }

        @Override
        public List<Float> getResult(ResultSet result, int column) throws SQLException {
            return floats(result.getObject(column));
        }

        @Override
        public List<Float> getResult(CallableStatement statement, int column) throws SQLException {
            return floats(statement.getObject(column));
        }

        private List<Float> floats(Object value) throws SQLException {
            if (value == null) {
                return null;
            }
            if (!(value instanceof List<?> values)) {
                throw new SQLException("Expected a numeric vector, got " + value.getClass().getName());
            }
            List<Float> result = new ArrayList<>(values.size());
            for (Object element : values) {
                if (!(element instanceof Number number)) {
                    throw new SQLException("Expected a numeric vector component");
                }
                result.add(number.floatValue());
            }
            return result;
        }
    }
}
