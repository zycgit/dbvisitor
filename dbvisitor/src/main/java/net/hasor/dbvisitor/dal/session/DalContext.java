package net.hasor.dbvisitor.dal.session;
import net.hasor.dbvisitor.dal.dynamic.DynamicContext;
import net.hasor.dbvisitor.dal.dynamic.DynamicSql;
import net.hasor.dbvisitor.dal.dynamic.rule.RuleRegistry;
import net.hasor.dbvisitor.dal.repository.DalRegistry;
import net.hasor.dbvisitor.mapping.TableReader;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

/** 生成动态 SQL 的 Build 环境 */
class DalContext extends DynamicContext {
    private final String      space;
    private final DalRegistry dalRegistry;

    DalContext(String space, DalRegistry dalRegistry) {
        this.space = space;
        this.dalRegistry = dalRegistry;
    }

    public DynamicSql findDynamic(String dynamicId) {
        return this.dalRegistry.findDynamicSql(this.space, dynamicId);
    }

    public TableMapping<?> findTableMapping(String resultMap) {
        return this.dalRegistry.findMapping(this.space, resultMap);
    }

    public TableReader<?> findTableReader(String resultType) {
        return this.dalRegistry.findTableReader(this.space, resultType);
    }

    public TypeHandlerRegistry getTypeRegistry() {
        return this.dalRegistry.getTypeRegistry();
    }

    public RuleRegistry getRuleRegistry() {
        return this.dalRegistry.getRuleRegistry();
    }

    public ClassLoader getClassLoader() {
        return this.dalRegistry.getClassLoader();
    }
}
