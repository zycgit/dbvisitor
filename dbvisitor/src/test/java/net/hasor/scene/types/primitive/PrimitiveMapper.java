package net.hasor.scene.types.primitive;
import net.hasor.dbvisitor.mapper.RefMapper;

import java.util.List;

@RefMapper("/dbvisitor_scene/primitive/primitiveMapping_1.xml")
public interface PrimitiveMapper {

    List<String> queryString();

    List<Integer> queryInteger();
}
