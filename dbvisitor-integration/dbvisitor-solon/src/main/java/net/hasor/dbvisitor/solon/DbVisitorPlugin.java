package net.hasor.dbvisitor.solon;
import net.hasor.dbvisitor.mapper.RefMapper;
import net.hasor.dbvisitor.mapper.SimpleMapper;
import net.hasor.dbvisitor.session.Configuration;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.Plugin;

/**
 * @author noear
 * @since 1.8
 */
public class DbVisitorPlugin implements Plugin {
    private final Configuration configuration = new Configuration();

    @Override
    public void start(AppContext context) {
        context.beanInjectorAdd(Db.class, new DbBeanInjectorImpl(configuration));
        context.beanBuilderAdd(RefMapper.class, (clz, bw, anno) -> configuration.loadMapper(clz));
        context.beanBuilderAdd(SimpleMapper.class, (clz, bw, anno) -> configuration.loadMapper(clz));
    }
}
