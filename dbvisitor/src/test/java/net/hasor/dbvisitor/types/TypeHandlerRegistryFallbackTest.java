/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.types;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Types;
import java.util.UUID;
import net.hasor.dbvisitor.jdbc.core.JdbcQueryContext;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.Options;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class TypeHandlerRegistryFallbackTest {
    @Test
    public void unknownLookupsDoNotRegisterJavaJdbcOrCrossTypes() {
        TypeHandlerRegistry registry = new TypeHandlerRegistry();
        int unknownJdbcType = Integer.MIN_VALUE;
        for (int attempt = 0; attempt < 2; attempt++) {
            assertUnknown(registry, unknownJdbcType);
            assertSame(registry.getDefaultTypeHandler(), registry.getTypeHandler(PlainBean.class.getName()));
            assertUnknown(registry, unknownJdbcType);
            assertSame(registry.getDefaultTypeHandler(), registry.getTypeHandler(PlainBean.class, unknownJdbcType));
            assertUnknown(registry, unknownJdbcType);
            assertSame(registry.getDefaultTypeHandler(), registry.getTypeHandler(PlainBean.class));
            assertUnknown(registry, unknownJdbcType);
            assertSame(registry.getDefaultTypeHandler(), registry.getTypeHandler(unknownJdbcType));
            assertUnknown(registry, unknownJdbcType);
        }
    }

    @Test
    public void explicitFallbackRegistrationStillCountsAsAHandler() {
        TypeHandlerRegistry registry = new TypeHandlerRegistry();
        TypeHandler<?> fallback = registry.getDefaultTypeHandler();
        registry.getTypeHandler(PlainBean.class);
        registry.getTypeHandler(PlainBean.class, Types.OTHER);
        registry.register(Types.OTHER, PlainBean.class, fallback);
        assertTrue(registry.hasTypeHandler(PlainBean.class, Types.OTHER));
        assertFalse(registry.hasTypeHandler(PlainBean.class));
        assertSame(fallback, registry.getTypeHandler(PlainBean.class, Types.OTHER));
        registry.register(PlainBean.class, fallback);
        assertTrue(registry.hasTypeHandler(PlainBean.class));
        assertTrue(registry.hasTypeHandler(PlainBean.class.getName()));
        assertSame(fallback, registry.getTypeHandler(PlainBean.class));
        assertSame(fallback, registry.getTypeHandler(PlainBean.class.getName()));
        registry.register(Integer.MIN_VALUE, fallback);
        assertTrue(registry.hasTypeHandler(Integer.MIN_VALUE));
        assertSame(fallback, registry.getTypeHandler(Integer.MIN_VALUE));
    }

    @Test
    public void explicitHandlersAfterFallbackLookupsRetainCrossTypePrecedence() {
        TypeHandlerRegistry registry = new TypeHandlerRegistry();
        registry.getTypeHandler(PlainBean.class);
        registry.getTypeHandler(PlainBean.class, Types.OTHER);
        TypeHandler<?> javaHandler = mock(TypeHandler.class);
        TypeHandler<?> crossHandler = mock(TypeHandler.class);
        registry.register(PlainBean.class, javaHandler);
        registry.register(Types.OTHER, PlainBean.class, crossHandler);
        assertSame(javaHandler, registry.getTypeHandler(PlainBean.class));
        assertSame(javaHandler, registry.getTypeHandler(PlainBean.class.getName()));
        assertSame(javaHandler, registry.getTypeHandler(PlainBean.class, Types.VARCHAR));
        assertSame(crossHandler, registry.getTypeHandler(PlainBean.class, Types.OTHER));
        assertTrue(registry.hasTypeHandler(PlainBean.class));
        assertTrue(registry.hasTypeHandler(PlainBean.class, Types.OTHER));
        assertFalse(registry.hasTypeHandler(PlainBean.class, Types.VARCHAR));
    }

    @Test
    public void beanRowsAndNamedParametersRemainBeansAfterFallbackLookup() throws Exception {
        TypeHandlerRegistry registry = new TypeHandlerRegistry();
        MappingRegistry mappings = new MappingRegistry(null, registry, Options.of());
        JdbcQueryContext context = new JdbcQueryContext();
        context.setTypeRegistry(registry);
        try (Connection connection = DriverManager.getConnection("jdbc:h2:mem:registry_" + UUID.randomUUID())) {
            JdbcTemplate jdbc = new JdbcTemplate(connection, mappings, context);
            PlainBean parameters = new PlainBean();
            parameters.setId(7);
            parameters.setName("sample");
            String sql = "select cast(:id as integer) as id, cast(:name as varchar) as name";
            PlainBean before = jdbc.queryForObject(sql, parameters, PlainBean.class);
            assertEquals(parameters.getId(), before.getId());
            assertEquals(parameters.getName(), before.getName());

            registry.getTypeHandler(PlainBean.class);
            registry.getTypeHandler(PlainBean.class.getName());
            registry.getTypeHandler(PlainBean.class, Types.OTHER);

            PlainBean after = jdbc.queryForObject(sql, parameters, PlainBean.class);
            assertEquals(parameters.getId(), after.getId());
            assertEquals(parameters.getName(), after.getName());
            assertFalse(registry.hasTypeHandler(PlainBean.class));
            assertFalse(registry.hasTypeHandler(PlainBean.class.getName()));
            assertFalse(registry.hasTypeHandler(PlainBean.class, Types.OTHER));
        }
    }

    private static void assertUnknown(TypeHandlerRegistry registry, int jdbcType) {
        assertFalse(registry.hasTypeHandler(PlainBean.class));
        assertFalse(registry.hasTypeHandler(PlainBean.class.getName()));
        assertFalse(registry.hasTypeHandler(PlainBean.class, jdbcType));
        assertFalse(registry.hasTypeHandler(jdbcType));
        assertFalse(registry.getHandlerJavaTypes().contains(PlainBean.class.getName()));
    }

    public static class PlainBean {
        private Integer id;
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
}
