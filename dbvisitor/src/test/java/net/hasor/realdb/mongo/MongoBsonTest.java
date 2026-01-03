package net.hasor.realdb.mongo;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.realdb.mongo.dto_bson.BsonTypesDto;
import net.hasor.test.AbstractDbTest;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

public class MongoBsonTest extends AbstractDbTest {
    static {
        try {
            javax.xml.parsers.SAXParserFactory.newInstance();
        } catch (Throwable e) {
            System.setProperty("javax.xml.parsers.SAXParserFactory", "com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl");
        }
    }

    @Test
    public void testBsonTypesCRUD() throws SQLException {
        try (Connection c = DsUtils.mongoConn()) {
            LambdaTemplate lambda = new LambdaTemplate(c);

            // 1. Prepare Data
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

            // 2. Insert
            int insertCount = lambda.insert(BsonTypesDto.class).applyEntity(dto).executeSumResult();
            assert insertCount == 1;

            // 3. Select & Verify
            BsonTypesDto selected = lambda.query(BsonTypesDto.class).eq(BsonTypesDto::getId, dto.getId()).queryForObject();

            assert selected != null;
            assert dto.getId().equals(selected.getId());
            assert dto.getStringValue().equals(selected.getStringValue());
            assert dto.getIntValue().equals(selected.getIntValue());
            assert dto.getLongValue().equals(selected.getLongValue());
            assert Math.abs(dto.getDoubleValue() - selected.getDoubleValue()) < 0.0001;
            assert dto.getBooleanValue().equals(selected.getBooleanValue());
            // Date might lose some precision depending on storage, but usually millisecond precision is kept
            assert Math.abs(dto.getDateValue().getTime() - selected.getDateValue().getTime()) < 1000;
            assert Arrays.equals(dto.getBytesValue(), selected.getBytesValue());

            assert selected.getListValue() != null;
            assert selected.getListValue().size() == 2;
            assert selected.getListValue().contains("Item 1");

            assert selected.getMapValue() != null;
            assert "value1".equals(selected.getMapValue().get("key1"));
            assert Integer.valueOf(100).equals(selected.getMapValue().get("key2")) || Long.valueOf(100).equals(selected.getMapValue().get("key2")); // Number type might vary

            // 4. Update
            dto.setStringValue("Updated String");
            dto.setIntValue(54321);
            dto.getListValue().add("Item 3");
            dto.getMapValue().put("key3", true);

            int updateCount = lambda.update(BsonTypesDto.class).eq(BsonTypesDto::getId, dto.getId()).updateTo(BsonTypesDto::getStringValue, dto.getStringValue()).updateTo(BsonTypesDto::getIntValue, dto.getIntValue()).updateTo(BsonTypesDto::getListValue, dto.getListValue()).updateTo(BsonTypesDto::getMapValue, dto.getMapValue()).doUpdate();

            assert updateCount == 1;

            // 5. Verify Update
            BsonTypesDto updated = lambda.query(BsonTypesDto.class).eq(BsonTypesDto::getId, dto.getId()).queryForObject();

            assert "Updated String".equals(updated.getStringValue());
            assert Integer.valueOf(54321).equals(updated.getIntValue());
            assert updated.getListValue().size() == 3;
            assert Boolean.TRUE.equals(updated.getMapValue().get("key3"));

            // 6. Delete
            int deleteCount = lambda.delete(BsonTypesDto.class).eq(BsonTypesDto::getId, dto.getId()).doDelete();
            assert deleteCount == 1;

            // 7. Verify Delete
            BsonTypesDto deleted = lambda.query(BsonTypesDto.class).eq(BsonTypesDto::getId, dto.getId()).queryForObject();
            assert deleted == null;
        }
    }
}
