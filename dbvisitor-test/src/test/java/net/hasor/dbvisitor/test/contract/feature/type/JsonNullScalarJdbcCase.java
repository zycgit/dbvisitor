/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.type;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.material.model.types.JsonAnnotatedBean;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.assertNull;

@NxnContract
public abstract class JsonNullScalarJdbcCase extends JsonTypeJdbcSupport {
    // 能力归属：类型处理器 / JSON 序列化处理器 / JSON 转换。
    @Test
    @Capability(value = CapabilityId.TYPE_JSON_BIND_ANNOTATION_NULL, column = "types/json-serialization-handlers/conversion")
    public void jsonAnnotatedBean_shouldReadNullValue() throws SQLException {
        requiresNxnFeature(FeatureId.JSON);
        Object nullId = fixtureKey(baseId() + 10);
        jdbcTemplate.executeUpdate(insertCommand("id, json_varchar", "?", "?"), new Object[] { nullId, null });

        JsonAnnotatedBean loadedNull = jdbcTemplate.queryForObject(selectCommand("json_varchar"), new Object[] { nullId }, JsonAnnotatedBean.class);
        assertNull(loadedNull);
    }

    // 能力归属：类型处理器 / JSON 序列化处理器 / JSON 转换。
    @Test
    @Capability(value = CapabilityId.TYPE_JSON_NULL, column = "types/json-serialization-handlers/conversion")
    public void jsonNull_shouldRemainNull() throws SQLException {
        Object id = fixtureKey(baseId() + 12);
        jdbcTemplate.executeUpdate(insertCommand("id, json_varchar, json_mysql, nested_json", "?", "?", "?", "?"), //
                new Object[] { id, null, null, null });

        assertNull(jdbcTemplate.queryForObject(selectCommand("json_varchar"), new Object[] { id }, String.class));
    }
}
