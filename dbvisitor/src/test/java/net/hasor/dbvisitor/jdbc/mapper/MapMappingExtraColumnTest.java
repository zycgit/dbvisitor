/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.jdbc.mapper;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.Table;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MapMappingExtraColumnTest {
    @Table("items")
    public static class Item {
        private Integer id;
        @Column("item_name")
        private String  name;

        public Integer getId() {
            return this.id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Test
    public void extraColumnsShouldBeIgnoredWithoutShiftingMappedColumnIndexes() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:h2:mem:map_" + UUID.randomUUID())) {
            JdbcTemplate jdbc = new JdbcTemplate(connection);
            MapMappingRowMapper mapper = new MapMappingRowMapper(Item.class, new MappingRegistry());
            List<Map<String, Object>> rows = jdbc.queryForList("""
                    SELECT 'leading' AS extra_before, 7 AS id, 'middle' AS extra_middle,
                           'kept' AS item_name, 'trailing' AS extra_after
                    """, mapper);
            assertEquals(List.of(Map.of("id", 7, "name", "kept")), rows);
        }
    }

    @Test
    public void entirelyUnmappedProjectionShouldReturnAnEmptyMap() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:h2:mem:map_" + UUID.randomUUID())) {
            JdbcTemplate jdbc = new JdbcTemplate(connection);
            MapMappingRowMapper mapper = new MapMappingRowMapper(Item.class, new MappingRegistry());
            List<Map<String, Object>> rows = jdbc.queryForList("SELECT 1 AS extra", mapper);
            assertEquals(1, rows.size());
            assertTrue(rows.get(0).isEmpty());
        }
    }
}
