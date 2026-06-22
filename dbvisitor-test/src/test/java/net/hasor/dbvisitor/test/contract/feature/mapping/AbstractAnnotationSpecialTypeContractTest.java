package net.hasor.dbvisitor.test.contract.feature.mapping;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.junit.Test;

import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.contract.material.model.types.SpecialArrayTypeEntity;
import net.hasor.dbvisitor.test.contract.material.model.types.SpecialJsonTypeEntity;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public abstract class AbstractAnnotationSpecialTypeContractTest extends AbstractNxnContractTest {
    protected int baseId() {
        return 950000;
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_SPECIAL_TYPE_JSON_MAP)
    public void annotationSpecialType_shouldRoundTripJsonMapAsLinkedHashMap() throws SQLException {
        requiresNxnFeature(FeatureId.JSON);
        int id = baseId() + 1;

        LinkedHashMap<String, Object> expected = new LinkedHashMap<>();
        expected.put("key", "value");
        expected.put("num", 123);
        SpecialJsonTypeEntity entity = new SpecialJsonTypeEntity();
        entity.setId(id);
        entity.setJsonMap(expected);
        insert(entity);

        SpecialJsonTypeEntity loaded = queryJson(id);
        assertNotNull(loaded.getJsonMap());
        assertTrue(loaded.getJsonMap() instanceof LinkedHashMap);
        assertEquals("value", loaded.getJsonMap().get("key"));
        assertEquals(123, ((Number) loaded.getJsonMap().get("num")).intValue());
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_SPECIAL_TYPE_JSON_LIST)
    public void annotationSpecialType_shouldRoundTripJsonListAsLinkedList() throws SQLException {
        requiresNxnFeature(FeatureId.JSON);
        int id = baseId() + 2;

        SpecialJsonTypeEntity entity = new SpecialJsonTypeEntity();
        entity.setId(id);
        entity.setJsonList(new LinkedList<>(Arrays.asList("a", "b")));
        insert(entity);

        SpecialJsonTypeEntity loaded = queryJson(id);
        assertNotNull(loaded.getJsonList());
        assertEquals(Arrays.asList("a", "b"), loaded.getJsonList());
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_SPECIAL_TYPE_JSON_SET)
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
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_SPECIAL_TYPE_ARRAY)
    public void annotationSpecialType_shouldRoundTripNativeIntegerArray() throws SQLException {
        requiresNxnFeature(FeatureId.ARRAY);
        int id = baseId() + 4;
        Integer[] expected = new Integer[] { 10, 20 };

        SpecialArrayTypeEntity entity = new SpecialArrayTypeEntity();
        entity.setId(id);
        entity.setIntArray(expected);
        insert(entity);

        SpecialArrayTypeEntity loaded = queryArray(id);
        assertNotNull(loaded.getIntArray());
        assertArrayEquals(expected, loaded.getIntArray());
    }

    private void insert(SpecialJsonTypeEntity entity) throws SQLException {
        new LambdaTemplate(dataSource).insert(SpecialJsonTypeEntity.class).applyEntity(entity).executeSumResult();
    }

    private void insert(SpecialArrayTypeEntity entity) throws SQLException {
        new LambdaTemplate(dataSource).insert(SpecialArrayTypeEntity.class).applyEntity(entity).executeSumResult();
    }

    private SpecialJsonTypeEntity queryJson(int id) throws SQLException {
        SpecialJsonTypeEntity loaded = new LambdaTemplate(dataSource).query(SpecialJsonTypeEntity.class) //
                .eq(SpecialJsonTypeEntity::getId, id) //
                .queryForObject();
        assertNotNull(loaded);
        return loaded;
    }

    private SpecialArrayTypeEntity queryArray(int id) throws SQLException {
        SpecialArrayTypeEntity loaded = new LambdaTemplate(dataSource).query(SpecialArrayTypeEntity.class) //
                .eq(SpecialArrayTypeEntity::getId, id) //
                .queryForObject();
        assertNotNull(loaded);
        return loaded;
    }
}
