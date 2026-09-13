/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.types.handler;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.hasor.cobble.reflect.resolvable.ResolvableType;
import net.hasor.dbvisitor.types.TypeHandler;
import net.hasor.dbvisitor.types.handler.json.BsonListTypeHandler;
import org.bson.Document;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class BsonListTypeHandlerTest {
    @Test
    public void convertsBsonDoublesToFloatsThroughAllResultAccessors() throws SQLException {
        TypeHandler<Object> handler = handler(List.class, Float.class);
        List<?> values = Arrays.asList(0.5d, -1.0d, null, 2.0f);
        ResultSet result = mock(ResultSet.class);
        CallableStatement callable = mock(CallableStatement.class);
        when(result.getObject(1)).thenReturn(values);
        when(result.getObject("embedding")).thenReturn(values);
        when(callable.getObject(1)).thenReturn(values);

        List<Float> expected = Arrays.asList(0.5f, -1.0f, null, 2.0f);
        assertEquals(expected, handler.getResult(result, 1));
        assertEquals(expected, handler.getResult(result, "embedding"));
        assertEquals(expected, handler.getResult(callable, 1));
        assertEquals(Double.class, values.get(0).getClass());
    }

    @Test
    public void convertsOtherScalarsUsingDeclaredElementType() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.getObject(1)).thenReturn(Arrays.asList(1, 2L, null));
        assertEquals(Arrays.asList(1L, 2L, null), handler(List.class, Long.class).getResult(result, 1));
        assertEquals(Arrays.asList("1", "2", null), handler(List.class, String.class).getResult(result, 1));
    }

    @Test
    public void retainsUntypedAndObjectElementsIncludingDocuments() throws SQLException {
        Document document = new Document("name", "sample");
        List<?> values = Arrays.asList(0.5d, document, null);
        ResultSet result = mock(ResultSet.class);
        when(result.getObject(1)).thenReturn(values);
        TypeHandler<Object> raw = new BsonListTypeHandler(ResolvableType.forClass(List.class));
        for (TypeHandler<Object> handler : Arrays.asList(raw, handler(List.class, Object.class))) {
            List<?> actual = (List<?>) handler.getResult(result, 1);
            assertEquals(values, actual);
            assertSame(values.get(0), actual.get(0));
            assertSame(document, actual.get(1));
            assertNull(actual.get(2));
        }
    }

    @Test
    public void decodesPojoDocumentsAndPreservesAlreadyTypedElements() throws SQLException {
        NamedValue existing = new NamedValue();
        existing.setName("existing");
        ResultSet result = mock(ResultSet.class);
        when(result.getObject(1)).thenReturn(Arrays.asList(new Document("name", "decoded"), existing, null));
        List<?> actual = (List<?>) handler(List.class, NamedValue.class).getResult(result, 1);
        assertEquals("decoded", ((NamedValue) actual.get(0)).getName());
        assertSame(existing, actual.get(1));
        assertNull(actual.get(2));
    }

    @Test
    public void supportsConcreteListsAndConvertsBeforeSetDeduplication() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.getObject(1)).thenReturn(Arrays.asList(0.5d, 0.5f, null));
        Object list = handler(ArrayList.class, Float.class).getResult(result, 1);
        assertEquals(ArrayList.class, list.getClass());
        assertEquals(Arrays.asList(0.5f, 0.5f, null), list);
        Object set = handler(Set.class, Float.class).getResult(result, 1);
        assertEquals(LinkedHashSet.class, set.getClass());
        assertEquals(Arrays.asList(0.5f, null), new ArrayList<>((Set<?>) set));
    }

    @Test
    public void retainsNullAndEmptyResultsAndRejectsNonLists() throws SQLException {
        TypeHandler<Object> handler = handler(List.class, Float.class);
        ResultSet result = mock(ResultSet.class);
        CallableStatement callable = mock(CallableStatement.class);
        assertNull(handler.getResult(result, 1));
        assertNull(handler.getResult(result, "embedding"));
        assertNull(handler.getResult(callable, 1));
        when(result.getObject(1)).thenReturn(Collections.emptyList());
        assertEquals(Collections.emptyList(), handler.getResult(result, 1));
        when(result.getObject(1)).thenReturn("not a BSON list");
        assertThrows(SQLException.class, () -> handler.getResult(result, 1));
    }

    @Test
    public void resolvesInheritedCollectionElementTypes() throws SQLException {
        TypeHandler<Object> handler = new BsonListTypeHandler(ResolvableType.forClass(FloatList.class));
        ResultSet result = mock(ResultSet.class);
        when(result.getObject(1)).thenReturn(Arrays.asList(0.5d, null));
        Object actual = handler.getResult(result, 1);
        assertEquals(FloatList.class, actual.getClass());
        assertEquals(Arrays.asList(0.5f, null), actual);
    }

    @Test
    public void rejectsNonCollectionsAndNonInstantiableCollectionTypes() {
        assertThrows(IllegalArgumentException.class, () -> new BsonListTypeHandler(null));
        assertThrows(IllegalArgumentException.class, () -> new BsonListTypeHandler(ResolvableType.forClass(Map.class)));
        assertThrows(IllegalArgumentException.class, () -> handler(AbstractList.class, Float.class));
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, //
                () -> new BsonListTypeHandler(ResolvableType.forClass(NoDefaultConstructorList.class)));
        assertTrue(error.getMessage().contains("public no-argument constructor"));
    }

    private static TypeHandler<Object> handler(Class<?> collectionType, Class<?> elementType) {
        return new BsonListTypeHandler(ResolvableType.forClassWithGenerics(collectionType, elementType));
    }

    public static class FloatList extends ArrayList<Float> {
    }

    public static class NoDefaultConstructorList extends ArrayList<Float> {
        public NoDefaultConstructorList(int capacity) {
            super(capacity);
        }
    }

    public static class NamedValue {
        private String name;

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
