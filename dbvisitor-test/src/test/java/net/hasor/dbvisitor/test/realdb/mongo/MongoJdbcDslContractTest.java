package net.hasor.dbvisitor.test.realdb.mongo;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.test.contract.api.adapter.AdapterContractTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MongoProfile;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import org.junit.Test;

public class MongoJdbcDslContractTest extends AdapterContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MongoProfile.INSTANCE;
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MONGO_JDBC_DSL_CRUD)
    public void mongoJdbcDsl_shouldExecuteCollectionCrud() throws Exception {
        try (Connection c = newAdapterConnection()) {
            JdbcTemplate jdbc = new JdbcTemplate(c);
            dropUserInfo(jdbc);

            jdbc.execute("test.user_info.insert({name: 'mali', age: 26})");
            jdbc.execute("test.user_info.insert({name: 'dative', age: 32})");
            jdbc.execute("test.user_info.insert({name: 'jon wes', age: 41})");

            List<Map<String, Object>> list = jdbc.queryForList("test.user_info.find()");
            assertEquals(3, list.size());

            Map<String, Object> mali = jdbc.queryForMap("test.user_info.find({name: 'mali'})");
            String json = (String) mali.get("_JSON");
            assertTrue(json.contains("\"name\": \"mali\""));
            assertTrue(json.contains("\"age\": 26"));

            jdbc.execute("test.user_info.update({name: 'mali'}, {$set: {age: 27}})");
            mali = jdbc.queryForMap("test.user_info.find({name: 'mali'})");
            json = (String) mali.get("_JSON");
            assertTrue(json.contains("\"age\": 27"));

            jdbc.execute("test.user_info.remove({name: 'mali'})");
            list = jdbc.queryForList("test.user_info.find({name: 'mali'})");
            assertTrue(list.isEmpty());

            list = jdbc.queryForList("test.user_info.find()");
            assertEquals(2, list.size());
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MONGO_JDBC_DSL_BATCH_INSERT_COUNT)
    public void mongoJdbcDsl_shouldExecuteBatchInsertAndCount() throws Exception {
        try (Connection c = newAdapterConnection()) {
            JdbcTemplate jdbc = new JdbcTemplate(c);
            dropUserInfo(jdbc);

            jdbc.execute("test.user_info.insert([{name: 'mali', age: 26}, {name: 'dative', age: 32}])");

            int count = jdbc.queryForInt("test.user_info.count()");
            assertEquals(2, count);
        }
    }

    private void dropUserInfo(JdbcTemplate jdbc) {
        try {
            jdbc.execute("test.user_info.drop()");
        } catch (Exception ignored) {
        }
    }
}
