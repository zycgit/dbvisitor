/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.mapper;

import java.sql.SQLException;
import java.util.*;
import net.hasor.dbvisitor.test.contract.api.mapper.annotation.AnnotationMapperResultMappingCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import net.hasor.dbvisitor.test.realdb.redis.api.mapper.RedisNativeMapperSupport.NativeMapper;
import net.hasor.dbvisitor.test.realdb.redis.dto1.RedisParameterUser;
import org.junit.After;
import org.junit.Before;

public class RedisAnnotationMapperResultMappingTest extends AnnotationMapperResultMappingCase {
    private final RedisMapperFixture  fixture = new RedisMapperFixture();
    private       NativeMapper        nativeMapper;
    private       RedisCoverageMapper coverage;

    @Override
    protected DataSourceProfile profile() {
        return RedisProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        fixture.open();
    }

    @Override
    @Before
    public void createAnnotationMapper() throws Exception {
        fixture.open();
        nativeMapper = fixture.session().createMapper(NativeMapper.class);
        coverage = fixture.session().createMapper(RedisCoverageMapper.class);
        fixture.session().jdbc().executeUpdate("HSET ? name mali", fixture.key("single"));
        fixture.session().jdbc().executeUpdate("HSET ? name mali age 18", fixture.key("hash"));
        fixture.session().jdbc().executeUpdate("RPUSH ? first second", fixture.key("list"));
        fixture.session().jdbc().executeUpdate("RPUSH ? 1 2 3", fixture.key("numbers"));
        fixture.session().jdbc().executeUpdate("SET ? 23", fixture.key("age"));
        fixture.session().jdbc().executeUpdate("SET ? mali", fixture.key("name"));
        fixture.session().jdbc().executeUpdate("SET ? ?", new Object[] { fixture.key("date"), expectedScalarDate() });
    }

    @After
    public void closeFixture() throws SQLException {
        fixture.close();
    }

    @Override
    protected List<?> fullEntityRows() throws Exception {
        return nativeMapper.entries(fixture.key("single"));
    }

    @Override
    protected List<?> partialEntityRows() throws Exception {
        return nativeMapper.partialEntries(fixture.key("single"));
    }

    @Override
    protected Map<String, Object> expectedFullEntity() {
        return Map.of("field", "name", "value", "mali");
    }

    @Override
    protected Map<String, Object> expectedPartialEntity() {
        Map<String, Object> expected = new LinkedHashMap<>();
        expected.put("field", "name");
        expected.put("value", null);
        return expected;
    }

    @Override
    protected List<String> nonNullFullProperties() {
        return List.of("field", "value");
    }

    @Override
    protected Map<String, Object> singleMap() throws Exception {
        return nativeMapper.map(fixture.key("single"));
    }

    @Override
    protected List<Map<String, Object>> mapList() throws Exception {
        return nativeMapper.maps(fixture.key("hash"));
    }

    @Override
    protected Map<String, Object> expectedMap() {
        return Map.of("FIELD", "name", "VALUE", "mali");
    }

    @Override
    protected int minimumMapRows() {
        return 2;
    }

    @Override
    protected Integer scalarInteger() throws Exception {
        return coverage.integer(fixture.key("age"));
    }

    @Override
    protected String scalarText() throws Exception {
        return nativeMapper.get(fixture.key("name"));
    }

    @Override
    protected String scalarTextExpected() {
        return "mali";
    }

    @Override
    protected int scalarCount() throws Exception {
        return coverage.count(fixture.key("numbers"));
    }

    @Override
    protected int minimumScalarCount() {
        return 3;
    }

    @Override
    protected Date scalarDate() throws Exception {
        return coverage.date(fixture.key("date"));
    }

    @Override
    protected Date expectedScalarDate() {
        return java.sql.Date.valueOf("2024-03-15");
    }

    @Override
    protected List<?> entityList() throws Exception {
        return nativeMapper.entries(fixture.key("hash"));
    }

    @Override
    protected List<String> stringList() throws Exception {
        return nativeMapper.list(fixture.key("list"));
    }

    @Override
    protected List<Integer> integerList() throws Exception {
        return nativeMapper.integers(fixture.key("numbers"));
    }

    @Override
    protected List<Map<String, Object>> expectedListEntities() {
        return List.of(Map.of("field", "name", "value", "mali"), Map.of("field", "age", "value", "18"));
    }

    @Override
    protected String expectedFirstName() {
        return "first";
    }

    @Override
    protected int expectedFirstId() {
        return 1;
    }

    @Override
    protected int insertNullableEntity() throws Exception {
        RedisParameterUser user = new RedisParameterUser();
        user.setId(1);
        user.setName("AnnoResultNull");
        return coverage.putBean(fixture.key("nullable"), user);
    }

    @Override
    protected Object nullableEntity() throws Exception {
        return coverage.bean(fixture.key("nullable"));
    }

    @Override
    protected Object missingEntity() throws Exception {
        return coverage.bean(fixture.key("absent"));
    }

    @Override
    protected String missingScalar() throws Exception {
        return nativeMapper.get(fixture.key("absent"));
    }

    @Override
    protected List<?> emptyEntityList() throws Exception {
        return nativeMapper.list(fixture.key("absent"));
    }

    @Override
    protected Integer expectedMapColumnCount() {
        return 2;
    }

    @Override
    protected List<Map<String, Object>> expectedMapRows() {
        return List.of(Map.of("FIELD", "name", "VALUE", "mali"), Map.of("FIELD", "age", "VALUE", "18"));
    }

    @Override
    protected Integer expectedScalarCount() {
        return 3;
    }

    @Override
    protected Integer expectedEntityCount() {
        return 2;
    }

    @Override
    protected List<String> expectedStringList() {
        return Arrays.asList("first", "second");
    }

    @Override
    protected List<Integer> expectedIntegerList() {
        return Arrays.asList(1, 2, 3);
    }

    @Override
    protected List<List<?>> additionalEmptyLists() throws Exception {
        return List.of(nativeMapper.entries(fixture.key("absent")));
    }
}
