package net.hasor.test.dal.dynamic;
import net.hasor.dbvisitor.dynamic.DynamicSql;
import net.hasor.dbvisitor.dynamic.RegistryManager;
import net.hasor.dbvisitor.dynamic.logic.PlanDynamicSql;

public class TextBuilderContext extends RegistryManager {

    @Override
    public DynamicSql findMacro(String dynamicId) {
        if (dynamicId.endsWith("_allColumns")) {
            return new PlanDynamicSql("*");
        } else {
            return super.findMacro(dynamicId);
        }
    }
}
