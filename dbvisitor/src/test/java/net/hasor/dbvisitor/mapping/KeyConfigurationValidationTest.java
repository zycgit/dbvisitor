/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.mapping;

import java.sql.SQLException;
import net.hasor.dbvisitor.dialect.provider.MySqlDialect;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

public class KeyConfigurationValidationTest {
    @Test
    public void emptySequenceName_shouldFailBeforeDialectOrDatabaseAccess() {
        MappingRegistry registry = new MappingRegistry(null, Options.of().dialect(new MySqlDialect()));
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> registry.loadEntityToSpace(EmptySequence.class));
        assertEquals("@KeySeq config failed, no name specified.", error.getMessage());
    }

    @Test
    public void uuid32_shouldRejectIntegerPropertyWithoutDatabaseAccess() {
        WrongUuid32 entity = new WrongUuid32();
        MappingRegistry registry = new MappingRegistry();
        ColumnMapping column = registry.loadEntityToSpace(WrongUuid32.class).getPropertyByName("id");
        SQLException error = assertThrows(SQLException.class,
                () -> column.getKeySeqHolder().beforeApply(null, entity, column));
        assertEquals("UUID32 key holder requires String target property, but was java.lang.Integer", error.getMessage());
        assertNull(entity.getId());
    }

    @Test
    public void uuid36_shouldRejectIntegerPropertyWithoutDatabaseAccess() {
        WrongUuid36 entity = new WrongUuid36();
        MappingRegistry registry = new MappingRegistry();
        ColumnMapping column = registry.loadEntityToSpace(WrongUuid36.class).getPropertyByName("id");
        SQLException error = assertThrows(SQLException.class,
                () -> column.getKeySeqHolder().beforeApply(null, entity, column));
        assertEquals("UUID36 key holder requires String target property, but was java.lang.Integer", error.getMessage());
        assertNull(entity.getId());
    }

    @Table("key_config")
    public static class EmptySequence {
        @Column(primary = true, keyType = KeyType.Sequence)
        @KeySeq("")
        private Integer id;

        public Integer getId() {
            return this.id;
        }

        public void setId(Integer id) {
            this.id = id;
        }
    }

    @Table("key_config")
    public static class WrongUuid32 {
        @Column(primary = true, keyType = KeyType.UUID32)
        private Integer id;

        public Integer getId() {
            return this.id;
        }

        public void setId(Integer id) {
            this.id = id;
        }
    }

    @Table("key_config")
    public static class WrongUuid36 {
        @Column(primary = true, keyType = KeyType.UUID36)
        private Integer id;

        public Integer getId() {
            return this.id;
        }

        public void setId(Integer id) {
            this.id = id;
        }
    }
}
