package net.hasor.dbvisitor.solon.integration;

import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.solon.annotation.Db;
import net.hasor.dbvisitor.template.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.wrapper.WrapperAdapter;
import org.noear.solon.core.BeanInjector;
import org.noear.solon.core.BeanWrap;
import org.noear.solon.core.VarHolder;
import org.noear.solon.data.datasource.DsUtils;

import javax.sql.DataSource;

/**
 * @author noear
 * @since 2.9
 */
public class DbBeanInjectorImpl implements BeanInjector<Db> {
    private final Configuration configuration;

    public DbBeanInjectorImpl(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void doInject(VarHolder vh, Db anno) {
        vh.required(true);

        DsUtils.observeDs(vh.context(), anno.value(), (dsWrap) -> {
            inject0(vh, dsWrap);
        });
    }

    private void inject0(VarHolder vh, BeanWrap dsBw) {
        try {
            inject1(vh, dsBw);
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private void inject1(VarHolder vh, BeanWrap dsBw) throws Exception {
        DataSource ds = dsBw.get();
        Class<?> clz = vh.getType();

        //@Db("db1") LambdaTemplate ; //顺序别乱变
        if (WrapperAdapter.class.isAssignableFrom(vh.getType())) {
            WrapperAdapter accessor = this.configuration.newWrapper(new SolonDynamicConnection(ds));

            vh.setValue(accessor);
            return;
        }

        //@Db("db1") JdbcTemplate ;
        if (JdbcTemplate.class.isAssignableFrom(vh.getType())) {
            JdbcTemplate accessor = new JdbcTemplate(new SolonDynamicConnection(ds));

            vh.setValue(accessor);
            return;
        }

        //@Db("db1") Session ;
        if (Session.class.isAssignableFrom(vh.getType())) {
            Session accessor = this.configuration.newSession(new SolonDynamicConnection(ds));

            vh.setValue(accessor);
            return;
        }

        //@Db("db1") UserMapper ;
        if (vh.getType().isInterface()) {
            Session accessor = this.configuration.newSession(new SolonDynamicConnection(ds));

            if (clz == BaseMapper.class) {
                Object obj = accessor.createBaseMapper((Class<?>) vh.getGenericType().getActualTypeArguments()[0]);
                vh.setValue(obj);
            } else {
                Object mapper = accessor.createMapper(vh.getType());
                vh.setValue(mapper);
            }
        }
    }
}
