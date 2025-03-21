package net.hasor.dbvisitor.solon;

import net.hasor.cobble.ExceptionUtils;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.transaction.TransactionManager;
import net.hasor.dbvisitor.transaction.TransactionTemplate;
import net.hasor.dbvisitor.transaction.TransactionTemplateManager;
import net.hasor.dbvisitor.transaction.support.LocalTransactionManager;
import net.hasor.dbvisitor.wrapper.WrapperAdapter;
import org.noear.solon.core.BeanInjector;
import org.noear.solon.core.BeanWrap;
import org.noear.solon.core.VarHolder;
import org.noear.solon.data.datasource.DsUtils;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author noear
 * @since 2.9
 */
public class DbVisitorInjector implements BeanInjector<Db> {
    private final Map<String, MapperWrap>              mapperWrap;
    private final Map<DataSource, TransactionManager>  dsTranManager;
    private final Map<DataSource, TransactionTemplate> dsTranTemplate;

    public DbVisitorInjector(Map<String, MapperWrap> mapperWrap) {
        this.mapperWrap = mapperWrap;
        this.dsTranManager = new ConcurrentHashMap<>();
        this.dsTranTemplate = new ConcurrentHashMap<>();
    }

    @Override
    public void doInject(VarHolder vh, Db anno) {
        vh.required(true);

        DsUtils.observeDs(vh.context(), anno.value(), (dsWrap) -> {
            inject0(vh, dsWrap, this.mapperWrap.get(anno.value()));
        });
    }

    private void inject0(VarHolder vh, BeanWrap dsBw, MapperWrap mapperWrap) {
        try {
            inject1(vh, dsBw, mapperWrap);
        } catch (Exception e) {
            throw ExceptionUtils.toRuntime(e);
        }
    }

    private void inject1(VarHolder vh, BeanWrap dsBw, MapperWrap mapperWrap) throws Exception {
        Configuration conf = mapperWrap.getConf();
        DataSource ds = dsBw.get();
        Class<?> clz = vh.getType();
        final SolonDynamicConnection dconn = new SolonDynamicConnection(ds);
        final TransactionManager tranManager = this.dsTranManager.computeIfAbsent(ds, LocalTransactionManager::new);
        final TransactionTemplate tranTemplate = this.dsTranTemplate.computeIfAbsent(ds, dsKey -> new TransactionTemplateManager(tranManager));

        //@Db("db1") JdbcTemplate
        if (JdbcTemplate.class.isAssignableFrom(vh.getType())) {
            vh.setValue(conf.newJdbc(dconn));
            return;
        }

        //@Db("db1") WrapperAdapter
        if (WrapperAdapter.class.isAssignableFrom(vh.getType())) {
            vh.setValue(conf.newWrapper(dconn));
            return;
        }

        //@Db("db1") Session, session include status.
        //if (Session.class.isAssignableFrom(vh.getType())) {
        //    vh.setValue(conf.newSession(dconn));
        //    return;
        //}

        //@Db("db1") TransactionManager
        if (TransactionManager.class.isAssignableFrom(vh.getType())) {
            vh.setValue(tranManager);
            return;
        }

        //@Db("db1") TransactionTemplate
        if (TransactionTemplate.class.isAssignableFrom(vh.getType())) {
            vh.setValue(tranTemplate);
            return;
        }

        //@Db("db1") Configuration
        if (Configuration.class.isAssignableFrom(vh.getType())) {
            vh.setValue(conf);
            return;
        }

        //@Db("db1") UserMapper
        if (vh.getType().isInterface()) {
            Session session = conf.newSession(dconn);
            if (clz == BaseMapper.class) {
                Object mapper = session.createBaseMapper((Class<?>) vh.getGenericType().getActualTypeArguments()[0]);
                vh.setValue(mapper);
            } else {
                Object mapper = session.createMapper(vh.getType());
                vh.setValue(mapper);
            }
        }
    }
}
