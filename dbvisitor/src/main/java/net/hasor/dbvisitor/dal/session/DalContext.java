package net.hasor.dbvisitor.dal.session;
import net.hasor.dbvisitor.dal.MapperRegistry;
import net.hasor.dbvisitor.dal.execute.TableReader;
import net.hasor.dbvisitor.dynamic.DynamicSql;
import net.hasor.dbvisitor.dynamic.RegistryManager;
import net.hasor.dbvisitor.dynamic.rule.RuleRegistry;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

/** 生成动态 SQL 的 Build 环境 */
public class DalContext extends RegistryManager {
    private final String         space;
    private final MapperRegistry dalRegistry;

    DalContext(String space, MapperRegistry dalRegistry) {
        this.space = space;
        this.dalRegistry = dalRegistry;
    }

    public DynamicSql findMacro(String dynamicId) {
        DynamicSql dynamicSql = this.dalRegistry.findDynamicSql(this.space, dynamicId);
        if (dynamicSql == null) {
            return super.findMacro(dynamicId);
        } else {
            return dynamicSql;
        }
    }

    public TableMapping<?> findTableMapping(String resultMap) {
        return this.dalRegistry.findBySpace(this.space, resultMap);
    }

    public TableReader<?> findTableReader(String resultType) {
        return this.dalRegistry.findTableReader(this.space, resultType);
    }

    @Override
    public TypeHandlerRegistry getTypeRegistry() {
        return this.dalRegistry.getTypeRegistry();
    }

    @Override
    public RuleRegistry getRuleRegistry() {
        return this.dalRegistry.getRuleRegistry();
    }

    @Override
    public ClassLoader getClassLoader() {
        return this.dalRegistry.getClassLoader();
    }
}
