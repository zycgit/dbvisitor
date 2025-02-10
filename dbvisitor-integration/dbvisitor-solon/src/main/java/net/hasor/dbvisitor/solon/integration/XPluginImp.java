package net.hasor.dbvisitor.solon.integration;
import net.hasor.dbvisitor.mapper.RefMapper;
import net.hasor.dbvisitor.mapper.SimpleMapper;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.solon.annotation.Db;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.Plugin;

/**
 * @author noear
 * @since 1.8
 */
public class XPluginImp implements Plugin {
    private final Configuration configuration = new Configuration();

    @Override
    public void start(AppContext context) {

        //添加 db 注入处理
        context.beanInjectorAdd(Db.class, new DbBeanInjectorImpl(configuration));

        context.beanBuilderAdd(RefMapper.class, (clz, bw, anno) -> {
            configuration.loadMapper(clz);
        });
        context.beanBuilderAdd(SimpleMapper.class, (clz, bw, anno) -> {
            configuration.loadMapper(clz);
        });
    }
}
