package net.hasor.test.db.dal.dynamic;
import net.hasor.db.dal.dynamic.DynamicContext;
import net.hasor.db.dal.dynamic.DynamicSql;
import net.hasor.db.dal.dynamic.nodes.TextDynamicSql;

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
