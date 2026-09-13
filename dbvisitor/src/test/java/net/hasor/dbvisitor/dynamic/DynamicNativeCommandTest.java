/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.dynamic;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import net.hasor.dbvisitor.dynamic.segment.PlanDynamicSql;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.types.SqlArg;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DynamicNativeCommandTest {
    private SqlBuilder parse(String command, Map<String, Object> values) throws SQLException {
        PlanDynamicSql parsed = DynamicParsed.getParsedSql(command);
        assertEquals(command, parsed.getOriSqlString());
        return parsed.buildQuery(values, new TestQueryContext());
    }

    @Test
    public void jsonLiteralsRemainLiteral() throws SQLException {
        String command = """
                POST /index/_search {"query": {"bool": {"must": [{"term": {"id": 1}}]}},"size": 10,"enabled": true,"missing": null,"values": [false,2,-3.5,"a:b"]}
                """;
        SqlBuilder result = parse(command, Collections.emptyMap());
        assertEquals(command, result.getSqlString());
        assertEquals(0, result.getArgs().length);
    }

    @Test
    public void jsonPositionParametersAreNotExpressions() throws SQLException {
        String command = "PUT /index/_doc/1 {\"id\": ?,\"name\": ?,\"tags\": [?,?]}";
        SqlBuilder result = parse(command, Collections.emptyMap());
        assertEquals(command, result.getSqlString());
        assertEquals(4, result.getArgs().length);
    }

    @Test
    public void jsonBindingsRemainSeparateFromCommandText() throws SQLException {
        String value = "\"},\"admin\": true,\"name\": \"";
        String command = "POST /index/_search {\"name\": #{name},\"ids\": [#{ids[0]},#{ids[1]}]}";
        SqlBuilder result = parse(command, Map.of("name", value, "ids", new int[] { 7, 8 }));
        assertEquals("POST /index/_search {\"name\": ?,\"ids\": [?,?]}", result.getSqlString());
        assertEquals(3, result.getArgs().length);
        assertEquals(value, ((SqlArg) result.getArgs()[0]).getValue());
        assertEquals(7, ((SqlArg) result.getArgs()[1]).getValue());
        assertEquals(8, ((SqlArg) result.getArgs()[2]).getValue());
    }

    @Test
    public void jdbcTemplateBindsJsonValuesUsingPreparedStatement() throws SQLException {
        String value = "\"},\"admin\": true,\"name\": \"";
        String command = "PUT /index/_doc/1 {\"name\": #{name}}";
        String preparedCommand = "PUT /index/_doc/1 {\"name\": ?}";
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        when(connection.prepareStatement(preparedCommand)).thenReturn(statement);
        when(statement.executeUpdate()).thenReturn(1);

        assertEquals(1, new JdbcTemplate(connection).executeUpdate(command, Map.of("name", value)));

        verify(connection).prepareStatement(preparedCommand);
        verify(statement).setString(1, value);
    }

    @Test
    public void escapedObjectStringsAreBoundAsValues() throws SQLException {
        String value = "quote\" :fake ? #{fake}";
        SqlBuilder result = parse("db.items.find({name: #{name}})", Map.of("name", value));
        assertEquals("db.items.find({name: ?})", result.getSqlString());
        assertEquals(1, result.getArgs().length);
        assertEquals(value, ((SqlArg) result.getArgs()[0]).getValue());
    }

    @Test
    public void mongoKeysAndWhitespaceRemainCommandSyntax() throws SQLException {
        String command = "db.items.find({ enabled : true, age: {$gte: 18},'name': :name, tags: [#{name}]})";
        SqlBuilder result = parse(command, Map.of("name", "abc"));
        assertEquals("db.items.find({ enabled : true, age: {$gte: 18},'name': ?, tags: [?]})", result.getSqlString());
        assertEquals(2, result.getArgs().length);
        assertEquals("abc", ((SqlArg) result.getArgs()[0]).getValue());
    }

    @Test
    public void sqlOgnlAndPostgresCastsRemainSupported() throws SQLException {
        String command = "select :user.ids[0]::int, &name from users where id=:id";
        SqlBuilder result = parse(command, Map.of("user", Map.of("ids", new int[] { 3 }), "name", "abc", "id", 7));
        assertEquals("select ?::int, ? from users where id=?", result.getSqlString());
        assertEquals(3, result.getArgs().length);
        assertEquals(3, ((SqlArg) result.getArgs()[0]).getValue());
    }

    @Test
    public void sqlQuotesCommentsAndJdbcEscapesRemainSupported() throws SQLException {
        String command = "select \"path\\\", ':ignored', :id /* {\"fake\": ?} */ from users where created>{ts '2026-01-01 00:00:00'}";
        SqlBuilder result = parse(command, Map.of("id", 7));
        assertEquals(command.replace(":id", "?"), result.getSqlString());
        assertEquals(1, result.getArgs().length);
    }

    @Test
    public void httpQuerySeparatorsDoNotConsumeBindings() throws SQLException {
        String command = "PUT /index/_doc/{?}${'?op_type=create&refresh=true&_source=false'} {\"name\": ?}";
        SqlBuilder result = parse(command, Collections.emptyMap());
        assertEquals("PUT /index/_doc/{?}?op_type=create&refresh=true&_source=false {\"name\": ?}", result.getSqlString());
        assertEquals(2, result.getArgs().length);
        assertEquals("arg0", ((SqlArg) result.getArgs()[0]).getName());
        assertEquals("arg1", ((SqlArg) result.getArgs()[1]).getName());
    }

    @Test
    public void httpPathAndQueryValuesStillAcceptPlaceholders() throws SQLException {
        SqlBuilder result = parse("GET /index/_search${'?routing='}#{name}${'&pretty'}", Map.of("name", "abc"));
        assertEquals("GET /index/_search?routing=?&pretty", result.getSqlString());
        assertEquals(1, result.getArgs().length);
        assertEquals("abc", ((SqlArg) result.getArgs()[0]).getValue());
    }

    @Test
    public void multipleHttpCommandsAndHintsHaveIndependentUris() throws SQLException {
        String command = "/*+ overwrite_find_limit=true */ GET /index/_search${'?pretty'}; POST /index/_doc${'?refresh=true'} {\"id\": ?}";
        SqlBuilder result = parse(command, Collections.emptyMap());
        assertEquals("/*+ overwrite_find_limit=true */ GET /index/_search?pretty; POST /index/_doc?refresh=true {\"id\": ?}", result.getSqlString());
        assertEquals(1, result.getArgs().length);
    }

    @Test
    public void httpLikeSqlTextDoesNotChangeSqlBindings() throws SQLException {
        String command = "select 'GET /index?pretty', ? from users where id=?";
        SqlBuilder result = parse(command, Collections.emptyMap());
        assertEquals(command, result.getSqlString());
        assertEquals(2, result.getArgs().length);
    }

    @Test
    public void indexMacroPreservesUriSeparators() throws SQLException {
        TestQueryContext context = new TestQueryContext();
        context.addMacro("indexName", "index");
        String command = "PUT /@{macro,indexName}/_doc${'?refresh=true&op_type=create'} {\"id\": ?,\"name\": #{name}}";
        SqlBuilder result = DynamicParsed.getParsedSql(command).buildQuery(Map.of("arg0", 7, "name", "abc"), context);
        assertEquals("PUT /index/_doc?refresh=true&op_type=create {\"id\": ?,\"name\": ?}", result.getSqlString());
        assertEquals(2, result.getArgs().length);
        assertEquals(7, ((SqlArg) result.getArgs()[0]).getValue());
        assertEquals("abc", ((SqlArg) result.getArgs()[1]).getValue());
    }

    @Test
    public void macroSqlPrefixDoesNotDiscardPositionParameters() throws SQLException {
        TestQueryContext context = new TestQueryContext();
        context.addMacro("sql", "select ");
        SqlBuilder result = DynamicParsed.getParsedSql("@{macro,sql}?value=?").buildQuery(Map.of("arg0", 7, "arg1", 8), context);
        assertEquals("select ?value=?", result.getSqlString());
        assertEquals(2, result.getArgs().length);
        assertEquals(7, ((SqlArg) result.getArgs()[0]).getValue());
        assertEquals(8, ((SqlArg) result.getArgs()[1]).getValue());
    }

    @Test
    public void indexReplacementIsEvaluatedForEachExecution() throws SQLException {
        PlanDynamicSql parsed = DynamicParsed.getParsedSql("GET /${indexName}/_search${'?routing='}#{routing}");
        for (String index : new String[] { "first", "second" }) {
            SqlBuilder result = parsed.clone().buildQuery(Map.of("indexName", index, "routing", "tenant"), new TestQueryContext());
            assertEquals("GET /" + index + "/_search?routing=?", result.getSqlString());
            assertEquals(1, result.getArgs().length);
            assertEquals("tenant", ((SqlArg) result.getArgs()[0]).getValue());
        }
    }

    @Test
    public void indexReplacementPreservesEarlierPositionParameterOffsets() throws SQLException {
        TestQueryContext context = new TestQueryContext();
        SqlBuilder result = DynamicParsed.getParsedSql("?; POST /${indexName}/_doc${'?refresh=true'} {\"id\": ?}")
                .buildQuery(Map.of("indexName", "index", "arg0", "first", "arg1", 7), context);
        assertEquals("?; POST /index/_doc?refresh=true {\"id\": ?}", result.getSqlString());
        assertEquals(2, result.getArgs().length);
        assertEquals("first", ((SqlArg) result.getArgs()[0]).getValue());
        assertEquals(7, ((SqlArg) result.getArgs()[1]).getValue());
    }
}
