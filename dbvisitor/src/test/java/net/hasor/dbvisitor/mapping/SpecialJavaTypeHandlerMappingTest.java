/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.mapping;

import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.hasor.cobble.reflect.resolvable.ResolvableType;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.types.TypeHandler;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;
import net.hasor.dbvisitor.types.handler.ObjectTypeHandler;
import net.hasor.dbvisitor.types.handler.json.JsonTypeHandler;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SpecialJavaTypeHandlerMappingTest {
    @Test
    public void explicitJsonHandler_shouldReadConfiguredLinkedList() throws Exception {
        ColumnMapping column = mappings(new TypeHandlerRegistry()).getPropertyByName("jsonStrings");
        assertEquals(LinkedList.class, column.getJavaType());
        assertTrue(column.getTypeHandler() instanceof JsonTypeHandler);

        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString(1)).thenReturn("[\"first\",\"second\"]", "[]");
        Object populated = column.getTypeHandler().getResult(resultSet, 1);
        assertEquals(LinkedList.class, populated.getClass());
        assertEquals(Arrays.asList("first", "second"), populated);
        Object empty = column.getTypeHandler().getResult(resultSet, 1);
        assertEquals(LinkedList.class, empty.getClass());
        assertTrue(((List<?>) empty).isEmpty());
    }

    @Test
    public void explicitResolvableHandler_shouldKeepArgumentsAndSeparateCachedTypes() {
        TableMapping<?> table = mappings(new TypeHandlerRegistry());
        CapturingHandler strings = capturing(table, "strings");
        assertEquals(LinkedList.class, strings.type.getRawClass());
        assertEquals(String.class, strings.type.resolveGeneric(0));
        assertEquals(LinkedList.class, table.getPropertyByName("strings").getJavaType());

        CapturingHandler beans = capturing(table, "beans");
        assertEquals(LinkedList.class, beans.type.getRawClass());
        assertEquals(Payload.class, beans.type.resolveGeneric(0));
        assertNotSame(strings, beans);

        CapturingHandler nested = capturing(table, "nested");
        assertEquals(LinkedHashMap.class, nested.type.getRawClass());
        assertEquals(String.class, nested.type.resolveGeneric(0));
        assertEquals(List.class, nested.type.resolveGeneric(1));
        assertEquals(Payload.class, nested.type.resolveGeneric(1, 0));
        assertEquals(LinkedHashMap.class, table.getPropertyByName("nested").getJavaType());
    }

    @Test
    public void explicitResolvableHandler_shouldKeepUnchangedAndDefaultImplementationTypes() throws Exception {
        TableMapping<?> table = mappings(new TypeHandlerRegistry());
        CapturingHandler unchanged = capturing(table, "envelope");
        assertEquals(MappedFields.class.getDeclaredField("envelope").getGenericType(), unchanged.type.getType());
        assertEquals(Envelope.class, unchanged.type.getRawClass());
        assertEquals(List.class, unchanged.type.resolveGeneric(0));
        assertEquals(Payload.class, unchanged.type.resolveGeneric(0, 0));

        CapturingHandler defaultList = capturing(table, "defaultList");
        assertEquals(ArrayList.class, defaultList.type.getRawClass());
        assertEquals(Payload.class, defaultList.type.resolveGeneric(0));
        assertEquals(ArrayList.class, table.getPropertyByName("defaultList").getJavaType());

        CapturingHandler fixedList = capturing(table, "fixedList");
        assertEquals(StringList.class, fixedList.type.getRawClass());
        assertEquals(String.class, fixedList.type.as(List.class).resolveGeneric(0));
    }

    @Test
    public void implicitHandlers_shouldUseEffectiveTypeAndKeepLookupPrecedence() {
        TypeHandlerRegistry registry = new TypeHandlerRegistry();
        TypeHandler<?> declaredTypeHandler = mock(TypeHandler.class);
        TypeHandler<?> jdbcHandler = mock(TypeHandler.class);
        TypeHandler<?> javaHandler = mock(TypeHandler.class);
        TypeHandler<?> crossHandler = mock(TypeHandler.class);
        TypeHandler<?> autoHandler = mock(TypeHandler.class);
        registry.register(Types.VARCHAR, jdbcHandler);
        registry.register(ArrayList.class, autoHandler);

        TableMapping<?> fallbackTable = mappings(registry);
        assertEquals(LinkedList.class, fallbackTable.getPropertyByName("implicit").getJavaType());
        assertSame(registry.getDefaultTypeHandler(), fallbackTable.getPropertyByName("implicit").getTypeHandler());
        assertSame(jdbcHandler, fallbackTable.getPropertyByName("implicitJdbc").getTypeHandler());
        assertEquals(ArrayList.class, fallbackTable.getPropertyByName("autoList").getJavaType());
        assertSame(autoHandler, fallbackTable.getPropertyByName("autoList").getTypeHandler());

        // A handler registered for an interface also applies to its concrete implementations.
        registry.register(List.class, declaredTypeHandler);
        TableMapping<?> interfaceTable = mappings(registry);
        assertSame(declaredTypeHandler, interfaceTable.getPropertyByName("implicit").getTypeHandler());
        assertSame(declaredTypeHandler, interfaceTable.getPropertyByName("implicitJdbc").getTypeHandler());

        registry.register(LinkedList.class, javaHandler);
        TableMapping<?> javaTable = mappings(registry);
        assertSame(javaHandler, javaTable.getPropertyByName("implicit").getTypeHandler());
        assertSame(javaHandler, javaTable.getPropertyByName("implicitJdbc").getTypeHandler());

        registry.register(Types.VARCHAR, LinkedList.class, crossHandler);
        TableMapping<?> crossTable = mappings(registry);
        assertSame(javaHandler, crossTable.getPropertyByName("implicit").getTypeHandler());
        assertSame(crossHandler, crossTable.getPropertyByName("implicitJdbc").getTypeHandler());
        assertEquals(LinkedList.class, capturing(crossTable, "strings").type.getRawClass());
    }

    private static TableMapping<?> mappings(TypeHandlerRegistry registry) {
        return new MappingRegistry(null, registry, Options.of()).loadEntityToSpace(MappedFields.class);
    }

    private static CapturingHandler capturing(TableMapping<?> table, String property) {
        TypeHandler<?> handler = table.getPropertyByName(property).getTypeHandler();
        assertTrue(handler instanceof CapturingHandler);
        return (CapturingHandler) handler;
    }

    public static class CapturingHandler extends ObjectTypeHandler {
        private final ResolvableType type;

        public CapturingHandler(ResolvableType type) {
            this.type = type;
        }
    }

    public static class Payload {
    }

    public static class Envelope<T> {
    }

    public static class StringList extends LinkedList<String> {
    }

    @Table("special_java_type")
    public static class MappedFields {
        @Column(specialJavaType = LinkedList.class, typeHandler = JsonTypeHandler.class)
        private List<String> jsonStrings;
        @Column(jdbcType = Types.VARCHAR, specialJavaType = LinkedList.class, typeHandler = CapturingHandler.class)
        private List<String> strings;
        @Column(specialJavaType = LinkedList.class, typeHandler = CapturingHandler.class)
        private List<Payload> beans;
        @Column(specialJavaType = LinkedHashMap.class, typeHandler = CapturingHandler.class)
        private Map<String, List<Payload>> nested;
        @Column(typeHandler = CapturingHandler.class)
        private Envelope<List<Payload>> envelope;
        @Column(typeHandler = CapturingHandler.class)
        private List<Payload> defaultList;
        @Column(specialJavaType = StringList.class, typeHandler = CapturingHandler.class)
        private List<String> fixedList;
        @Column(specialJavaType = LinkedList.class)
        private List<String> implicit;
        @Column(jdbcType = Types.VARCHAR, specialJavaType = LinkedList.class)
        private List<String> implicitJdbc;
        private List<String> autoList;

        public List<String> getJsonStrings() {
            return this.jsonStrings;
        }

        public void setJsonStrings(List<String> jsonStrings) {
            this.jsonStrings = jsonStrings;
        }

        public List<String> getStrings() {
            return this.strings;
        }

        public void setStrings(List<String> strings) {
            this.strings = strings;
        }

        public List<Payload> getBeans() {
            return this.beans;
        }

        public void setBeans(List<Payload> beans) {
            this.beans = beans;
        }

        public Map<String, List<Payload>> getNested() {
            return this.nested;
        }

        public void setNested(Map<String, List<Payload>> nested) {
            this.nested = nested;
        }

        public Envelope<List<Payload>> getEnvelope() {
            return this.envelope;
        }

        public void setEnvelope(Envelope<List<Payload>> envelope) {
            this.envelope = envelope;
        }

        public List<Payload> getDefaultList() {
            return this.defaultList;
        }

        public void setDefaultList(List<Payload> defaultList) {
            this.defaultList = defaultList;
        }

        public List<String> getFixedList() {
            return this.fixedList;
        }

        public void setFixedList(List<String> fixedList) {
            this.fixedList = fixedList;
        }

        public List<String> getImplicit() {
            return this.implicit;
        }

        public void setImplicit(List<String> implicit) {
            this.implicit = implicit;
        }

        public List<String> getImplicitJdbc() {
            return this.implicitJdbc;
        }

        public void setImplicitJdbc(List<String> implicitJdbc) {
            this.implicitJdbc = implicitJdbc;
        }

        public List<String> getAutoList() {
            return this.autoList;
        }

        public void setAutoList(List<String> autoList) {
            this.autoList = autoList;
        }
    }
}
