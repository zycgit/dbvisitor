/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic7;

import java.sql.SQLException;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.mapper.*;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.realdb.elastic7.material.ElasticMatrixFixture;

public final class Elastic7KeyMapperFixture implements AutoCloseable {
    private final ElasticMatrixFixture fixture = new ElasticMatrixFixture();
    public JdbcTemplate open(String environment) throws SQLException { return fixture.open(environment); }
    public void prepareReservation(String name) throws SQLException {
        new JdbcTemplate(fixture.connection()).executeUpdate("POST /" + fixture.index()
                + "/_doc {\"reserved_for\": ?,\"reserved_key\": ?}",
                new Object[] { name, java.util.UUID.randomUUID().toString() });
    }
    public Session session() throws Exception {
        Session session = fixture.session();
        String path = "/" + fixture.index();
        session.getConfiguration().addMacro("esKeyInsert", "POST " + path
                + "/_doc {\"name\": #{name},\"age\": #{age},\"email\": #{email}}");
        session.getConfiguration().addMacro("esKeyExplicit", "PUT " + path
                + "/_doc/${id} {\"name\": #{name},\"age\": #{age},\"email\": #{email}}");
        session.getConfiguration().addMacro("esKeyRead", "POST " + path
                + "/_search {\"_source\": [\"name\"],\"query\": {\"ids\": {\"values\": [#{id}]}}}");
        session.getConfiguration().addMacro("esKeyBefore", "POST " + path
                + "/_search {\"_source\": [\"reserved_key\"],\"query\": {\"term\": {\"reserved_for.keyword\": #{name}}}}");
        session.getConfiguration().addMacro("esKeyAfterInsert", "PUT " + path
                + "/_doc/${name} {\"name\": #{name},\"age\": #{age},\"email\": #{email},\"key_ref\": #{name}}");
        session.getConfiguration().addMacro("esKeyAfter", "POST " + path
                + "/_search {\"_source\": [\"key_ref\"],\"query\": {\"term\": {\"name\": #{name}}}}");
        return session;
    }
    @Override
    public void close() throws Exception { fixture.close(); }
    public static class Document {
        private String id;
        private String name;
        private Integer age;
        private String email;
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
    @SimpleMapper
    public interface NativeKeys {
        @Insert(value = "@{macro, esKeyInsert}", useGeneratedKeys = true, keyProperty = "id")
        int generated(Document document);
        @Insert(value = "@{macro, esKeyInsert}", useGeneratedKeys = true, keyProperty = "id", keyColumn = "_ID")
        int column(Document document);
        @Insert("@{macro, esKeyExplicit}")
        int explicit(Document document);
        @Query("@{macro, esKeyRead}")
        String name(@Param("id") String id);
        @SelectKeySql(value = "@{macro, esKeyBefore}", keyProperty = "id", order = Order.Before)
        @Insert("@{macro, esKeyExplicit}")
        int before(Document document);
        @SelectKeySql(value = "@{macro, esKeyAfter}", keyProperty = "id", order = Order.After)
        @Insert("@{macro, esKeyAfterInsert}")
        int after(Document document);
        @SelectKeySql(value = "@{macro, esKeyBefore}", keyProperty = "id", order = Order.Before,
                statementType = StatementType.Prepared, timeout = 30, fetchSize = 1, resultSetType = ResultSetType.DEFAULT)
        @Insert("@{macro, esKeyExplicit}")
        int options(Document document);
    }
}
