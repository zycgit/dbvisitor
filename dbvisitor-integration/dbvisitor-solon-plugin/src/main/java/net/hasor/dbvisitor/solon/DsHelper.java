/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.solon;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;
import net.hasor.dbvisitor.jdbc.DynamicConnection;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import org.noear.solon.core.BeanWrap;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2025-03-20
 */
public class DsHelper {
    private static final Map<String, Session> dsSession = new ConcurrentHashMap<>();

    public static Session fetchSession(final BeanWrap dsBw, final MapperWrap mapperWrap) throws SQLException {
        String dsName = dsBw.name();
        if (dsSession.containsKey(dsName)) {
            return dsSession.get(dsName);
        } else {
            synchronized (DsHelper.class) {
                if (dsSession.containsKey(dsName)) {
                    return dsSession.get(dsName);
                }

                Configuration conf = mapperWrap.getConf();
                DataSource dsObject = dsBw.get();
                DynamicConnection dsProxy = new ConnectionProxy(dsObject);
                Session session = conf.newSession(dsProxy);
                dsSession.put(dsName, session);
                return session;
            }
        }
    }

    //final TransactionManager dbvTranManager = this.dsTranManager.computeIfAbsent(ds, LocalTransactionManager::new);
    //final TransactionTemplate dbvTranTemplate = this.dsTranTemplate.computeIfAbsent(ds, dsKey -> new TransactionTemplateManager(dbvTranManager));
    //    private static final Map<DataSource, TransactionManager>  dsTranManager  = new ConcurrentHashMap<>();
    //    private static final Map<DataSource, TransactionTemplate> dsTranTemplate = new ConcurrentHashMap<>();
    //    public TransactionManager getTransactionManager(DataSource ds) throws Exception {
    //        final TransactionManager tranManager = this.dsTranManager.computeIfAbsent(ds, LocalTransactionManager::new);
    //        final TransactionTemplate tranTemplate = this.dsTranTemplate.computeIfAbsent(ds, dsKey -> new TransactionTemplateManager(tranManager));
    //    }
}
