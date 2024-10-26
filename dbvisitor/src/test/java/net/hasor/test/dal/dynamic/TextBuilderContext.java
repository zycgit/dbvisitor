package net.hasor.test.dal.dynamic;
import net.hasor.dbvisitor.dal.repository.parser.xmlnode.TextDynamicSql;
import net.hasor.dbvisitor.dynamic.DynamicSql;
import net.hasor.dbvisitor.dynamic.RegistryManager;

public class TextBuilderContext extends RegistryManager {

    @Override
    public DynamicSql findMacro(String dynamicId) {
        if (dynamicId.endsWith("_allColumns")) {
            return new TextDynamicSql("*");
        } else {
            return super.findMacro(dynamicId);
        }
    }
}
