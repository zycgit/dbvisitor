/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.adapter;
import java.sql.Date;
import java.sql.SQLException;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class NativeNamedFieldTypeContractTest extends AbstractNxnContractTest {
    public enum State {
        ACTIVE,
        INACTIVE
    }

    private final NativeBasicTypeFixture fixture = new NativeBasicTypeFixture();

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = this.fixture.open(profile().env(), "named_types");
    }

    @Test
    @Capability(CapabilityId.ADAPTER_NAMED_FIELD_DATE)
    public void date_shouldRoundTripByFieldName() throws SQLException {
        assertRoundTrip(Date.valueOf("2024-02-29"), Date.class);
    }

    @Test
    @Capability(CapabilityId.ADAPTER_NAMED_FIELD_ENUM)
    public void enum_shouldRoundTripByFieldName() throws SQLException {
        assertRoundTrip(State.INACTIVE, State.class);
    }

    @Test
    @Capability(CapabilityId.ADAPTER_NAMED_FIELD_BOOLEAN)
    public void boolean_shouldRoundTripByFieldName() throws SQLException {
        assertRoundTrip(Boolean.TRUE, Boolean.class);
        assertRoundTrip(Boolean.FALSE, Boolean.class);
    }

    private <T> void assertRoundTrip(T expected, Class<T> type) throws SQLException {
        int id = Boolean.FALSE.equals(expected) ? 2 : 1;
        this.jdbcTemplate.executeUpdate(this.fixture.insertCommand("named_types", "id,typed_value"), new Object[] { id, expected });
        Object actual = this.jdbcTemplate.queryForObject(this.fixture.selectCommand("named_types", "typed_value"), new Object[] { id }, (rs, row) -> TypeHandlerRegistry.DEFAULT.getTypeHandler(type).getResult(rs, "typed_value"));
        assertEquals(expected, actual);
    }

    @After
    public void closeFixture() throws SQLException {
        this.fixture.close();
    }
}
