package net.hasor.test.dal.dynamic;
import net.hasor.dbvisitor.dal.repository.parser.xmlnode.TextDynamicSql;
import net.hasor.dbvisitor.dynamic.DynamicContext;
import net.hasor.dbvisitor.dynamic.DynamicSql;

public class TextBuilderContext extends DynamicContext {

    @Override
    public DynamicSql findMacro(String dynamicId) {
        if (dynamicId.endsWith("_allColumns")) {
            return new TextDynamicSql("*");
        } else {
            return super.findMacro(dynamicId);
        }
    }
}
