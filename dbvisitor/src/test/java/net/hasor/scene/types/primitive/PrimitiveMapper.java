package net.hasor.scene.types.primitive;
import java.util.List;
import net.hasor.dbvisitor.mapper.RefMapper;

@RefMapper("/dbvisitor_scene/primitive/primitiveMapping_1.xml")
public interface PrimitiveMapper {

    List<String> queryString();

    List<Integer> queryInteger();
}
