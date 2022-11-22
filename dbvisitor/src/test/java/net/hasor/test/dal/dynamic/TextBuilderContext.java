package net.hasor.test.dal.dynamic;
import net.hasor.dbvisitor.dal.dynamic.DynamicContext;
import net.hasor.dbvisitor.dal.dynamic.DynamicSql;
import net.hasor.dbvisitor.dal.dynamic.nodes.TextDynamicSql;

public class TextBuilderContext extends DynamicContext {

    @Override
    public DynamicSql findDynamic(String dynamicId) {
        if (dynamicId.endsWith("_allColumns")) {
            return new TextDynamicSql("*");
        } else {
            return super.findDynamic(dynamicId);
        }
    }
}
