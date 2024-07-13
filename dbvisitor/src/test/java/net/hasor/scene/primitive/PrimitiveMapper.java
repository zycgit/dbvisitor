package net.hasor.scene.primitive;
import net.hasor.dbvisitor.dal.repository.RefMapper;

import java.util.List;

@RefMapper("/dbvisitor_scene/primitive/primitiveMapping_1.xml")
public interface PrimitiveMapper {

    List<String> queryString();

    List<Integer> queryInteger();
}
