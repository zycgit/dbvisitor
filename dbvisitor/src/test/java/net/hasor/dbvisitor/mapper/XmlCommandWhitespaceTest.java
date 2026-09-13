/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.mapper;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import net.hasor.dbvisitor.dynamic.SqlBuilder;
import net.hasor.dbvisitor.jdbc.core.JdbcQueryContext;
import net.hasor.dbvisitor.mapper.def.SqlConfig;
import net.hasor.dbvisitor.mapper.resolve.XmlSqlConfigResolve;
import org.junit.Test;
import org.xml.sax.InputSource;
import static org.junit.Assert.*;

public class XmlCommandWhitespaceTest {
    private SqlBuilder build(String body, Map<String, Object> values) throws Exception {
        String xml = "<select id='test'>" + body + "</select>";
        SqlConfig config = new XmlSqlConfigResolve().parseSqlConfig("test", DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new InputSource(new StringReader(xml))).getDocumentElement());
        return config.buildQuery(values, new JdbcQueryContext());
    }

    @Test
    public void spacesSeparateAdjacentDynamicArguments() throws Exception {
        SqlBuilder result = build("SET <if test='true'>#{key}</if> <if test='true'>#{value}</if>", Map.of("key", "k", "value", "v"));
        assertEquals("SET ? ?", result.getSqlString());
        assertEquals(2, result.getArgs().length);
    }

    @Test
    public void scoreBoundsKeepTheirSeparator() throws Exception {
        SqlBuilder result = build("ZRANGEBYSCORE scores <if test='true'>-inf</if> <if test='true'>+inf</if>", Collections.emptyMap());
        assertEquals("ZRANGEBYSCORE scores -inf +inf", result.getSqlString());
    }

    @Test
    public void spacesSeparateAdjacentForeachGroups() throws Exception {
        SqlBuilder result = build("CMD <foreach collection='items' item='item' separator=' '>#{item}</foreach> "
                + "<foreach collection='items' item='item' separator=' '>#{item}</foreach>", Map.of("items", List.of("a", "b")));
        assertEquals("CMD ? ? ? ?", result.getSqlString());
        assertEquals(4, result.getArgs().length);
    }

    @Test
    public void newlinesRemainCommandSeparators() throws Exception {
        SqlBuilder result = build("<if test='true'>SET a 1</if>\n<if test='true'>GET a</if>", Collections.emptyMap());
        assertEquals("SET a 1\nGET a", result.getSqlString());
    }

    @Test
    public void adjacentFragmentsDoNotGainInventedSpaces() throws Exception {
        SqlBuilder result = build("GET prefix<if test='true'>suffix</if><if test='true'>tail</if>", Collections.emptyMap());
        assertEquals("GET prefixsuffixtail", result.getSqlString());
    }

    @Test
    public void emptyConditionalClausesRemainEmpty() throws Exception {
        SqlBuilder result = build("SELECT 1<where> <if test='false'>id=#{id}</if> </where><set> <if test='false'>name=#{name}</if> </set>"
                + "<foreach collection='items' item='item'> #{item} </foreach>", Map.of("items", Collections.emptyList()));
        assertEquals("SELECT 1", result.getSqlString());
        assertEquals(0, result.getArgs().length);
    }

    @Test
    public void chooseFormattingDoesNotBecomeABranch() throws Exception {
        SqlBuilder result = build("SELECT <choose>\n<when test='true'>1</when>\n<otherwise>2</otherwise>\n</choose>", Collections.emptyMap());
        assertEquals("SELECT 1", result.getSqlString());
    }

    @Test
    public void cdataWhitespaceIsPreserved() throws Exception {
        SqlBuilder result = build("<if test='true'>GET a</if><![CDATA[\n]]><if test='true'>GET b</if>", Collections.emptyMap());
        assertEquals("GET a\nGET b", result.getSqlString());
    }

    @Test
    public void httpUriWithIndexReplacementKeepsQuerySeparatorLiteral() throws Exception {
        SqlBuilder result = build("POST /${indexName}/_doc${'?refresh=true'} {<if test='true'>\"id\": #{id}</if>}", Map.of("indexName", "index", "id", 7));
        assertEquals("POST /index/_doc?refresh=true {\"id\": ?}", result.getSqlString());
        assertEquals(1, result.getArgs().length);
    }
}
