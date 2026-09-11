/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.mapping;
import net.hasor.dbvisitor.mapping.dto.UserInfoUsingMap;
import org.junit.Test;

public class ErrorMappingTest {

    @Test
    public void alreadyExists() {
        try {
            MappingRegistry registry = new MappingRegistry();
            registry.loadMapping("/dbvisitor_coverage/basic_mapping/error_1.xml");
            registry.loadResultMapToSpace(UserInfoUsingMap.class);
            assert false;
        } catch (Exception e) {
            assert e.getMessage().contains("the resultMap 'net.hasor.dbvisitor.mapping.dto.userInfo' already exists.");
        }
    }
}
