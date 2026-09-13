/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.type;

import java.sql.SQLException;

import org.junit.Test;

import net.hasor.cobble.CollectionUtils;
import net.hasor.dbvisitor.test.contract.material.model.types.JsonAnnotatedBean;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

@NxnContract
public abstract class JsonAnnotatedBeanJdbcCase extends JsonTypeJdbcSupport {
    @Test
    @Capability(CapabilityId.TYPE_JSON_BIND_ANNOTATION)
    public void jsonAnnotatedBean_shouldUseBoundTypeHandlerForPositionalNamedAndPartialValues() throws SQLException {
        requiresNxnFeature(FeatureId.JSON);
        Object positionalId = fixtureKey(baseId() + 8);
        Object namedId = fixtureKey(baseId() + 9);
        Object partialId = fixtureKey(baseId() + 11);
        JsonAnnotatedBean laptop = new JsonAnnotatedBean("Laptop", 5999.99, 10, "Electronics");
        JsonAnnotatedBean smartphone = new JsonAnnotatedBean("Smartphone", 3999.0, 20, "Mobile");
        JsonAnnotatedBean partial = new JsonAnnotatedBean("Tablet", 2999.0);

        jdbcTemplate.executeUpdate(insertCommand("id, json_varchar", "?", "?"), new Object[] { positionalId, laptop });
        jdbcTemplate.executeUpdate(//
                insertCommand("id, json_varchar", "#{id}", "#{product}"), //
                CollectionUtils.asMap("id", namedId, "product", smartphone));
        jdbcTemplate.executeUpdate(insertCommand("id, json_varchar", "?", "?"), new Object[] { partialId, partial });

        JsonAnnotatedBean loadedLaptop = jdbcTemplate.queryForObject(selectCommand("json_varchar"), new Object[] { positionalId }, JsonAnnotatedBean.class);
        JsonAnnotatedBean loadedSmartphone = jdbcTemplate.queryForObject(selectCommand("json_varchar"), new Object[] { namedId }, JsonAnnotatedBean.class);
        JsonAnnotatedBean loadedPartial = jdbcTemplate.queryForObject(selectCommand("json_varchar"), new Object[] { partialId }, JsonAnnotatedBean.class);

        assertJsonAnnotatedBean(loadedLaptop, "Laptop", 5999.99, 10, "Electronics");
        assertJsonAnnotatedBean(loadedSmartphone, "Smartphone", 3999.0, 20, "Mobile");
        assertJsonAnnotatedBean(loadedPartial, "Tablet", 2999.0, null, null);
    }
}
