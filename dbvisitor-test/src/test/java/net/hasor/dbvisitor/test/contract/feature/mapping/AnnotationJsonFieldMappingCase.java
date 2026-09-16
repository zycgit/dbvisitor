/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.mapping;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import net.hasor.dbvisitor.test.contract.material.model.annotation.BoundJsonFieldEntity;
import net.hasor.dbvisitor.test.contract.material.model.types.JsonAnnotatedBean;
import net.hasor.dbvisitor.test.contract.material.model.types.SpecialJsonTypeEntity;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.*;

@NxnContract
public abstract class AnnotationJsonFieldMappingCase extends AnnotationSpecialTypeSupport {
    // 能力归属：对象映射 / 字段类型与语句模板 / JSON 字段。
    @Test
    @Capability(value = CapabilityId.MAPPING_ANNOTATION_SPECIAL_TYPE_JSON_MAP, column = "mapping-keys/field-types-and-statement-templates/json-fields")
    public void annotationSpecialType_shouldRoundTripJsonMapAsLinkedHashMap() throws SQLException {
        requiresNxnFeature(FeatureId.JSON);
        int id = baseId() + 1;

        LinkedHashMap<String, Object> expected = new LinkedHashMap<>();
        expected.put("key", "value");
        expected.put("num", 123);
        expected.put("nested", Arrays.asList("中文", "quoted\"value", "path\\value"));
        SpecialJsonTypeEntity entity = new SpecialJsonTypeEntity();
        entity.setId(id);
        entity.setJsonMap(expected);
        insert(entity);

        SpecialJsonTypeEntity loaded = queryJson(id);
        assertNotNull(loaded.getJsonMap());
        assertTrue(loaded.getJsonMap() instanceof LinkedHashMap);
        assertEquals("value", loaded.getJsonMap().get("key"));
        assertEquals(123, ((Number) loaded.getJsonMap().get("num")).intValue());
        assertEquals(expected.get("nested"), loaded.getJsonMap().get("nested"));
        assertEquals(expected.size(), loaded.getJsonMap().size());
        assertNull(loaded.getJsonList());
        assertNull(loaded.getJsonSet());

        SpecialJsonTypeEntity empty = new SpecialJsonTypeEntity();
        empty.setId(id + 10);
        empty.setJsonMap(new LinkedHashMap<>());
        insert(empty);
        SpecialJsonTypeEntity loadedEmpty = queryJson(id + 10);
        assertTrue(loadedEmpty.getJsonMap() instanceof LinkedHashMap);
        assertEquals(empty.getJsonMap(), loadedEmpty.getJsonMap());
    }

    // 能力归属：对象映射 / 字段类型与语句模板 / JSON 字段。
    @Test
    @Capability(value = CapabilityId.MAPPING_ANNOTATION_SPECIAL_TYPE_JSON_LIST, column = "mapping-keys/field-types-and-statement-templates/json-fields")
    public void annotationSpecialType_shouldRoundTripJsonListAsLinkedList() throws SQLException {
        requiresNxnFeature(FeatureId.JSON);
        int id = baseId() + 2;

        SpecialJsonTypeEntity entity = new SpecialJsonTypeEntity();
        entity.setId(id);
        entity.setJsonList(new LinkedList<>(Arrays.asList("a", "b")));
        insert(entity);

        SpecialJsonTypeEntity loaded = queryJson(id);
        assertNotNull(loaded.getJsonList());
        assertTrue(loaded.getJsonList() instanceof LinkedList);
        assertEquals(Arrays.asList("a", "b"), loaded.getJsonList());
        assertNull(loaded.getJsonMap());
        assertNull(loaded.getJsonSet());

        SpecialJsonTypeEntity empty = new SpecialJsonTypeEntity();
        empty.setId(id + 10);
        empty.setJsonList(new LinkedList<>());
        insert(empty);
        SpecialJsonTypeEntity loadedEmpty = queryJson(id + 10);
        assertTrue(loadedEmpty.getJsonList() instanceof LinkedList);
        assertEquals(empty.getJsonList(), loadedEmpty.getJsonList());
    }

    // 能力归属：对象映射 / 字段类型与语句模板 / JSON 字段。
    @Test
    @Capability(value = CapabilityId.MAPPING_ANNOTATION_SPECIAL_TYPE_JSON_SET, column = "mapping-keys/field-types-and-statement-templates/json-fields")
    public void annotationSpecialType_shouldRoundTripJsonSetAsHashSet() throws SQLException {
        requiresNxnFeature(FeatureId.JSON);
        int id = baseId() + 3;

        SpecialJsonTypeEntity entity = new SpecialJsonTypeEntity();
        entity.setId(id);
        entity.setJsonSet(new HashSet<>(Arrays.asList("x", "y")));
        insert(entity);

        SpecialJsonTypeEntity loaded = queryJson(id);
        assertNotNull(loaded.getJsonSet());
        assertTrue(loaded.getJsonSet() instanceof HashSet);
        assertEquals(new HashSet<>(Arrays.asList("x", "y")), loaded.getJsonSet());
        assertNull(loaded.getJsonMap());
        assertNull(loaded.getJsonList());

        SpecialJsonTypeEntity empty = new SpecialJsonTypeEntity();
        empty.setId(id + 10);
        empty.setJsonSet(new HashSet<>());
        insert(empty);
        SpecialJsonTypeEntity loadedEmpty = queryJson(id + 10);
        assertTrue(loadedEmpty.getJsonSet() instanceof HashSet);
        assertEquals(empty.getJsonSet(), loadedEmpty.getJsonSet());
    }

    // 能力归属：对象映射 / 字段类型与语句模板 / 业务类型注解的 JSON 字段。
    @Test
    @Capability(value = CapabilityId.MAPPING_ANNOTATION_JSON_BOUND_FIELD, column = "mapping-keys/field-types-and-statement-templates/json-fields")
    public void annotationJsonField_shouldUseTypeBoundHandlerForEntityReadAndWrite() throws SQLException {
        requiresNxnFeature(FeatureId.JSON);
        int id = baseId() + 21;
        JsonAnnotatedBean details = new JsonAnnotatedBean("Laptop", 5999.5, 10, "Electronics");
        BoundJsonFieldEntity entity = new BoundJsonFieldEntity();
        entity.setId(id);
        entity.setDetails(details);

        assertEquals(1, this.lambdaTemplate.insert(BoundJsonFieldEntity.class).applyEntity(entity).executeSumResult());
        BoundJsonFieldEntity loaded = queryBoundJson(id);
        assertNotNull(loaded);
        assertEquals(id, loaded.getId());
        assertEquals(details, loaded.getDetails());
        assertNotSame(details, loaded.getDetails());

        details.setCategory("memory-only-change");
        assertEquals("Electronics", queryBoundJson(id).getDetails().getCategory());

        JsonAnnotatedBean replacement = new JsonAnnotatedBean("Replacement", 9.5);
        assertEquals(1, this.lambdaTemplate.update(BoundJsonFieldEntity.class)//
                .eq(BoundJsonFieldEntity::getId, id)//
                .updateTo(BoundJsonFieldEntity::getDetails, replacement)//
                .doUpdate());
        BoundJsonFieldEntity updated = queryBoundJson(id);
        assertNotNull(updated);
        assertEquals(replacement, updated.getDetails());
        assertNull(updated.getDetails().getQuantity());
        assertNull(updated.getDetails().getCategory());

        SpecialJsonTypeEntity raw = queryJson(id);
        assertEquals("Replacement", raw.getJsonMap().get("productName"));
        assertEquals(9.5, ((Number) raw.getJsonMap().get("price")).doubleValue(), 0.0);
    }

    protected BoundJsonFieldEntity queryBoundJson(int id) throws SQLException {
        return this.lambdaTemplate.query(BoundJsonFieldEntity.class)//
                .eq(BoundJsonFieldEntity::getId, id)//
                .queryForObject();
    }
}
