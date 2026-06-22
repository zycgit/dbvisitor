package net.hasor.dbvisitor.test.realdb.mongo;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.contract.api.adapter.AbstractAdapterContractTest;
import net.hasor.dbvisitor.test.realdb.mongo.material.BsonTypesDto;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MongoProfile;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import org.junit.Test;

public class MongoBsonContractTest extends AbstractAdapterContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MongoProfile.INSTANCE;
    }

    static {
        try {
            javax.xml.parsers.SAXParserFactory.newInstance();
        } catch (Throwable e) {
            System.setProperty("javax.xml.parsers.SAXParserFactory", "com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl");
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MONGO_BSON_TYPES)
    public void mongoBson_shouldRoundTripStructuredValuesWithLambda() throws Exception {
        try (Connection c = newAdapterConnection()) {
            LambdaTemplate lambda = new LambdaTemplate(c);
            BsonTypesDto dto = newBsonDto();

            assertEquals(1, lambda.insert(BsonTypesDto.class).applyEntity(dto).executeSumResult());

            BsonTypesDto selected = lambda.query(BsonTypesDto.class).eq(BsonTypesDto::getId, dto.getId()).queryForObject();
            assertNotNull(selected);
            assertEquals(dto.getId(), selected.getId());
            assertEquals(dto.getStringValue(), selected.getStringValue());
            assertEquals(dto.getIntValue(), selected.getIntValue());
            assertEquals(dto.getLongValue(), selected.getLongValue());
            assertTrue(Math.abs(dto.getDoubleValue() - selected.getDoubleValue()) < 0.0001);
            assertEquals(dto.getBooleanValue(), selected.getBooleanValue());
            assertTrue(Math.abs(dto.getDateValue().getTime() - selected.getDateValue().getTime()) < 1000);
            assertArrayEquals(dto.getBytesValue(), selected.getBytesValue());
            assertNotNull(selected.getListValue());
            assertEquals(2, selected.getListValue().size());
            assertTrue(selected.getListValue().contains("Item 1"));
            assertNotNull(selected.getMapValue());
            assertEquals("value1", selected.getMapValue().get("key1"));
            assertTrue(Integer.valueOf(100).equals(selected.getMapValue().get("key2")) || Long.valueOf(100).equals(selected.getMapValue().get("key2")));

            dto.setStringValue("Updated String");
            dto.setIntValue(54321);
            dto.getListValue().add("Item 3");
            dto.getMapValue().put("key3", true);

            int updateCount = lambda.update(BsonTypesDto.class)//
                    .eq(BsonTypesDto::getId, dto.getId())//
                    .updateTo(BsonTypesDto::getStringValue, dto.getStringValue())//
                    .updateTo(BsonTypesDto::getIntValue, dto.getIntValue())//
                    .updateTo(BsonTypesDto::getListValue, dto.getListValue())//
                    .updateTo(BsonTypesDto::getMapValue, dto.getMapValue())//
                    .doUpdate();
            assertEquals(1, updateCount);

            BsonTypesDto updated = lambda.query(BsonTypesDto.class).eq(BsonTypesDto::getId, dto.getId()).queryForObject();
            assertNotNull(updated);
            assertEquals("Updated String", updated.getStringValue());
            assertEquals(Integer.valueOf(54321), updated.getIntValue());
            assertEquals(3, updated.getListValue().size());
            assertEquals(Boolean.TRUE, updated.getMapValue().get("key3"));

            assertEquals(1, lambda.delete(BsonTypesDto.class).eq(BsonTypesDto::getId, dto.getId()).doDelete());
            assertNull(lambda.query(BsonTypesDto.class).eq(BsonTypesDto::getId, dto.getId()).queryForObject());
        }
    }

    private BsonTypesDto newBsonDto() {
        BsonTypesDto dto = new BsonTypesDto();
        dto.setId(UUID.randomUUID().toString());
        dto.setStringValue("Hello Mongo");
        dto.setIntValue(12345);
        dto.setLongValue(9876543210L);
        dto.setDoubleValue(3.14159);
        dto.setBooleanValue(true);
        dto.setDateValue(new Date());
        dto.setBytesValue("Binary Data".getBytes());

        List<String> list = new ArrayList<>();
        list.add("Item 1");
        list.add("Item 2");
        dto.setListValue(list);

        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", 100);
        dto.setMapValue(map);
        return dto;
    }
}
