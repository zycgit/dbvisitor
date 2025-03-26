/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.dbvisitor.solon;
import net.hasor.dbvisitor.jdbc.DynamicConnection;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import org.noear.solon.core.BeanWrap;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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