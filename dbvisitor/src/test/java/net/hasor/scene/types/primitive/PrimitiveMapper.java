/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.scene.types.primitive;
import java.util.List;
import net.hasor.dbvisitor.mapper.RefMapper;

@RefMapper("/dbvisitor_scene/primitive/primitiveMapping_1.xml")
public interface PrimitiveMapper {

    List<String> queryString();

    List<Integer> queryInteger();
}
