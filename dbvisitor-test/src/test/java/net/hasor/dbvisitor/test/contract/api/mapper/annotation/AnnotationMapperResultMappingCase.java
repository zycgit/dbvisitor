/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.annotation;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.stream.IntStream;

import org.junit.Test;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import static org.junit.Assert.*;

@NxnContract
public abstract class AnnotationMapperResultMappingCase extends AnnotationMapperResultMappingSupport {
    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_ENTITY)
    public void queryResult_shouldMapFullAndPartialEntity() throws Exception {
        List<?> fullRows = fullEntityRows();
        List<?> partialRows = partialEntityRows();
        assertEquals(1, fullRows.size());
        assertEquals(1, partialRows.size());
        Object full = fullRows.get(0);
        Object partial = partialRows.get(0);
        assertNotNull(full);
        assertProperties(full, expectedFullEntity());
        for (String property : nonNullFullProperties()) {
            assertNotNull(property, propertyValue(full, property));
        }
        assertNotNull(partial);
        assertProperties(partial, expectedPartialEntity());
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_MAP)
    public void queryResult_shouldMapSingleAndListRowsToMap() throws Exception {
        Map<String, Object> row = singleMap();
        List<Map<String, Object>> rows = mapList();
        assertMapValues(row, expectedMap());
        if (expectedMapColumnCount() != null) {
            assertEquals(expectedMapColumnCount().intValue(), row.size());
        }
        assertTrue(rows.size() >= minimumMapRows());
        if (expectedMapRows() != null) {
            assertEquals(expectedMapRows().size(), rows.size());
            assertEquals(new HashSet<>(expectedMapRows()), new HashSet<>(rows));
        }
        for (String column : expectedMap().keySet()) {
            assertNotNull(column, value(rows.get(0), column));
        }
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_SCALAR)
    public void queryResult_shouldMapSingleColumnToScalarTypes() throws Exception {
        assertEquals(Integer.valueOf(23), scalarInteger());
        assertEquals(scalarTextExpected(), scalarText());
        int count = scalarCount();
        assertTrue(count >= minimumScalarCount());
        if (expectedScalarCount() != null) {
            assertEquals(expectedScalarCount().intValue(), count);
        }
        Date date = scalarDate();
        assertNotNull(date);
        if (expectedScalarDate() != null) {
            assertEquals(expectedScalarDate(), date);
        }
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_LIST)
    public void queryResult_shouldMapEntityAndScalarLists() throws Exception {
        List<?> users = entityList();
        List<String> names = stringList();
        List<Integer> ids = integerList();
        List<Map<String, Object>> expected = expectedListEntities();
        assertFalse(expected.isEmpty());
        assertTrue(users.size() >= expected.size());
        if (expectedEntityCount() != null) {
            assertEquals(expectedEntityCount().intValue(), users.size());
        }
        for (Object user : users) {
            boolean matches = false;
            for (Map<String, Object> candidate : expected) {
                boolean candidateMatches = true;
                for (Map.Entry<String, Object> field : candidate.entrySet()) {
                    candidateMatches &= Objects.equals(field.getValue(), propertyValue(user, field.getKey()));
                }
                matches |= candidateMatches;
            }
            assertTrue("Entity must match the selected native projection", matches);
        }
        assertTrue(names.contains(expectedFirstName()));
        assertTrue(ids.contains(expectedFirstId()));
        if (expectedStringList() != null) {
            assertEquals(expectedStringList(), names);
        }
        if (expectedIntegerList() != null) {
            assertEquals(expectedIntegerList(), ids);
        }
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_NULL)
    public void queryResult_shouldRepresentNoRowsAndNullColumns() throws Exception {
        assertEquals(1, insertNullableEntity());
        Object loaded = nullableEntity();
        assertNull(missingEntity());
        assertNull(missingScalar());
        assertTrue(emptyEntityList().isEmpty());
        for (List<?> empty : additionalEmptyLists()) {
            assertTrue(empty.isEmpty());
        }
        assertProperties(loaded, expectedNullableEntity());
    }

    protected List<?> fullEntityRows() throws Exception { return List.of(fullEntity()); }
    protected List<?> partialEntityRows() throws Exception { return List.of(partialEntity()); }
    protected Integer expectedMapColumnCount() { return null; }
    protected List<Map<String, Object>> expectedMapRows() { return null; }
    protected Integer expectedScalarCount() { return null; }
    protected Integer expectedEntityCount() { return null; }
    protected List<String> expectedStringList() { return null; }
    protected List<Integer> expectedIntegerList() { return null; }
    protected List<List<?>> additionalEmptyLists() throws Exception { return List.of(); }

    protected Object fullEntity() throws Exception {
        return mapper.selectUserById(baseId() + 1);
    }
    protected Object partialEntity() throws Exception {
        return mapper.selectUserPartial(baseId() + 5);
    }
    protected Map<String, Object> expectedFullEntity() {
        return Map.of("id", baseId() + 1, "name", "AnnoResult1", "age", 21, "email", "anno-result1@nxn.test");
    }
    protected Map<String, Object> expectedPartialEntity() {
        Map<String, Object> expected = new LinkedHashMap<>();
        expected.put("id", baseId() + 5);
        expected.put("name", "AnnoResult5");
        expected.put("age", null);
        expected.put("email", null);
        return expected;
    }
    protected List<String> nonNullFullProperties() {
        return List.of("createTime");
    }
    protected Map<String, Object> singleMap() throws Exception {
        return mapper.selectUserAsMap(baseId() + 2);
    }
    protected List<Map<String, Object>> mapList() throws Exception {
        return mapper.selectUsersAsMapList();
    }
    protected Map<String, Object> expectedMap() {
        return Map.of("id", baseId() + 2, "name", "AnnoResult2");
    }
    protected int minimumMapRows() {
        return 10;
    }
    protected Integer scalarInteger() throws Exception {
        return mapper.selectAgeById(baseId() + 3);
    }
    protected String scalarText() throws Exception {
        return mapper.selectNameById(baseId() + 4);
    }
    protected String scalarTextExpected() {
        return "AnnoResult4";
    }
    protected int scalarCount() throws Exception {
        return mapper.selectCount().intValue();
    }
    protected int minimumScalarCount() {
        return 10;
    }
    protected Date scalarDate() throws Exception {
        return mapper.selectCreateTimeById(baseId() + 7);
    }
    protected Date expectedScalarDate() {
        return null;
    }
    protected List<?> entityList() throws Exception {
        return mapper.selectUsersByAgeRange(21, 25);
    }
    protected List<String> stringList() throws Exception {
        return mapper.selectAllNames(baseId() + 1, baseId() + 10);
    }
    protected List<Integer> integerList() throws Exception {
        return mapper.selectIdRange(baseId() + 1, baseId() + 10);
    }
    protected List<Map<String, Object>> expectedListEntities() {
        return IntStream.rangeClosed(21, 25).mapToObj(age -> Map.<String, Object>of("age", age)).toList();
    }
    protected String expectedFirstName() {
        return "AnnoResult1";
    }
    protected int expectedFirstId() {
        return baseId() + 1;
    }
    protected int insertNullableEntity() throws Exception {
        UserInfo user = new UserInfo();
        user.setId(baseId() + 101);
        user.setName("AnnoResultNull");
        user.setAge(null);
        user.setEmail(null);
        user.setCreateTime(new Date());
        return mapper.insertUser(user);
    }
    protected Object nullableEntity() throws Exception {
        return mapper.selectUserById(baseId() + 101);
    }
    protected Object missingEntity() throws Exception {
        return mapper.selectUserById(baseId() + 999);
    }
    protected String missingScalar() throws Exception {
        return mapper.selectNameById(baseId() + 999);
    }
    protected List<?> emptyEntityList() throws Exception {
        return mapper.selectUsersByAgeRange(999, 1000);
    }
    protected Map<String, Object> expectedNullableEntity() {
        Map<String, Object> expected = new LinkedHashMap<>();
        expected.put("name", "AnnoResultNull");
        expected.put("age", null);
        expected.put("email", null);
        return expected;
    }
    private void assertMapValues(Map<String, Object> actual, Map<String, Object> expected) {
        assertFalse(expected.isEmpty());
        for (Map.Entry<String, Object> field : expected.entrySet()) {
            Object actualValue = value(actual, field.getKey());
            if (field.getValue() instanceof Number) {
                assertTrue(actualValue instanceof Number);
                assertEquals(((Number) field.getValue()).longValue(), ((Number) actualValue).longValue());
            } else {
                assertEquals(field.getKey(), field.getValue(), actualValue);
            }
        }
    }
    private void assertProperties(Object bean, Map<String, Object> expected) throws Exception {
        assertNotNull(bean);
        assertFalse(expected.isEmpty());
        for (Map.Entry<String, Object> field : expected.entrySet()) {
            assertEquals(field.getKey(), field.getValue(), propertyValue(bean, field.getKey()));
        }
    }
    private Object propertyValue(Object bean, String name) throws Exception {
        for (PropertyDescriptor property : Introspector.getBeanInfo(bean.getClass()).getPropertyDescriptors()) {
            if (property.getName().equals(name) && property.getReadMethod() != null) {
                return property.getReadMethod().invoke(bean);
            }
        }
        throw new AssertionError("Missing readable property: " + name);
    }
}
