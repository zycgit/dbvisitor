/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.lambda;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.dialect.provider.MilvusDialect;
import org.junit.Test;
import static org.junit.Assert.*;

public class MilvusInsertDialectTest {
    private final MilvusDialect dialect = new MilvusDialect();

    @Test
    public void updateShouldUseNativePartialUpsertWithBoundValues() {
        assertEquals("/*+ partial_update=true */ UPSERT INTO items (id, name) VALUES (?, ?)", sql(DuplicateKeyStrategy.Update));
        assertEquals(GeneratedKeyStrategy.OneByOne, this.dialect.generatedKeyStrategy(List.of("id"), List.of("id", "name"), List.of(), DuplicateKeyStrategy.Update));
    }

    @Test
    public void intoShouldRemainInsertAndPreserveValueTerms() {
        assertEquals("INSERT INTO items (id, name) VALUES (?, ?)", sql(DuplicateKeyStrategy.Into));
        assertEquals("INSERT INTO items (id, name) VALUES (?, 'fixed')", this.dialect.insertSql(DuplicateKeyStrategy.Into, GeneratedKeyStrategy.OneByOne, false, null, null, "items", List.of("id"), List.of("id", "name"), List.of(), 1, Map.of("name", "'fixed'")));
    }

    @Test
    public void ignoreAndUpdateWithoutPrimaryKeyShouldNotBecomePlainInsert() {
        assertFalse(this.dialect.supportDuplicateStrategy(List.of("id"), List.of("id", "name"), List.of(), DuplicateKeyStrategy.Ignore));
        assertFalse(this.dialect.supportDuplicateStrategy(List.of("id"), List.of("name"), List.of(), DuplicateKeyStrategy.Update));
        assertFalse(this.dialect.supportDuplicateStrategy(List.of(), List.of("name"), List.of(), DuplicateKeyStrategy.Update));
        assertThrows(UnsupportedOperationException.class, () -> sql(DuplicateKeyStrategy.Ignore));
    }

    private String sql(DuplicateKeyStrategy strategy) {
        return this.dialect.insertSql(strategy, GeneratedKeyStrategy.OneByOne, false, null, null, "items", List.of("id"), List.of("id", "name"), List.of(), 1, Map.of());
    }
}
