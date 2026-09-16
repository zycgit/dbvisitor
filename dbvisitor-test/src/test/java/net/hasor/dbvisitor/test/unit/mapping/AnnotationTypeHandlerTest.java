/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.unit.mapping;

import java.sql.Types;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.test.contract.material.handler.UpperCaseTypeHandler;
import net.hasor.dbvisitor.test.contract.material.model.annotation.JdbcTypeUser;
import net.hasor.dbvisitor.test.contract.material.model.annotation.SpecialJavaTypeUser;
import net.hasor.dbvisitor.test.contract.material.model.annotation.TypeHandlerUser;
import org.junit.Test;
import static org.junit.Assert.*;

public class AnnotationTypeHandlerTest {
    @Test
    public void columnAnnotation_shouldExposeCustomTypeHandlerMetadata() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(TypeHandlerUser.class);

        TableMapping<?> mapping = registry.findByEntity(TypeHandlerUser.class);
        assertNotNull(mapping);

        ColumnMapping name = mapping.getPropertyByName("name");
        assertNotNull(name);
        assertTrue(name.getTypeHandler() instanceof UpperCaseTypeHandler);
    }

    @Test
    public void columnAnnotation_shouldKeepDefaultTypeHandlerWhenUnspecified() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(JdbcTypeUser.class);

        TableMapping<?> mapping = registry.findByEntity(JdbcTypeUser.class);
        ColumnMapping email = mapping.getPropertyByName("email");
        assertNotNull(email);
        assertFalse(email.getTypeHandler() instanceof UpperCaseTypeHandler);
        assertNotNull(email.getTypeHandler());
    }

    @Test
    public void columnAnnotation_shouldExposeJdbcTypeMetadata() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(JdbcTypeUser.class);

        TableMapping<?> mapping = registry.findByEntity(JdbcTypeUser.class);
        assertNotNull(mapping);

        ColumnMapping name = mapping.getPropertyByName("name");
        ColumnMapping age = mapping.getPropertyByName("age");
        assertNotNull(name);
        assertNotNull(age);
        assertEquals(Integer.valueOf(Types.VARCHAR), name.getJdbcType());
        assertEquals(Integer.valueOf(Types.INTEGER), age.getJdbcType());
    }

    @Test
    public void columnAnnotation_shouldExposeSpecialJavaTypeMetadata() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(SpecialJavaTypeUser.class);

        TableMapping<?> mapping = registry.findByEntity(SpecialJavaTypeUser.class);
        assertNotNull(mapping);

        ColumnMapping name = mapping.getPropertyByName("name");
        assertNotNull(name);
        assertEquals(String.class, name.getJavaType());
    }
}
