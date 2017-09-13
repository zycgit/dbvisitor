/*
 * Copyright 2008-2009 the original author or authors.
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
package net.hasor.db;
import net.hasor.core.*;
import net.hasor.core.classcode.matcher.AopMatchers;
import net.hasor.core.provider.InstanceProvider;
import net.hasor.core.provider.SingleProvider;
import net.hasor.db.jdbc.JdbcOperations;
import net.hasor.db.jdbc.core.JdbcOperationsProvider;
import net.hasor.db.jdbc.core.JdbcTemplate;
import net.hasor.db.jdbc.core.JdbcTemplateProvider;
import net.hasor.db.transaction.TransactionManager;
import net.hasor.db.transaction.TransactionTemplate;
import net.hasor.db.transaction.interceptor.TransactionInterceptor;
import net.hasor.db.transaction.interceptor.Transactional;
import net.hasor.db.transaction.provider.TransactionManagerProvider;
import net.hasor.db.transaction.provider.TransactionTemplateProvider;
import net.hasor.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.lang.reflect.Method;
/**
 * DB 模块。
 * @author 赵永春(zyc@hasor.net)
 * @version : 2017-03-23
 */
public class DBModule implements Module {
    protected Logger logger = LoggerFactory.getLogger(getClass());
    private String               dataSourceID;
    private Provider<DataSource> dataSource;
    //
    /** 添加数据源 */
    public DBModule(DataSource dataSource) {
        this(null, dataSource);
    }
    /** 添加数据源 */
    public DBModule(Provider<DataSource> dataSource) {
        this(null, dataSource);
    }
    /** 添加数据源 */
    public DBModule(String name, DataSource dataSource) {
        Hasor.assertIsNotNull(dataSource, "dataSource is null.");
        this.dataSourceID = name;
        this.dataSource = new InstanceProvider<DataSource>(Hasor.assertIsNotNull(dataSource));
    }
    /** 添加数据源 */
    public DBModule(String name, Provider<DataSource> dataSource) {
        Hasor.assertIsNotNull(dataSource, "dataSource is null.");
        this.dataSourceID = name;
        this.dataSource = dataSource;
    }
    //
    @Override
    public void loadModule(ApiBinder apiBinder) throws Throwable {
        Provider<TransactionManager> managerProvider = new TransactionManagerProvider(this.dataSource);
        Provider<TransactionTemplate> templateProvider = new TransactionTemplateProvider(this.dataSource);
        //
        if (StringUtils.isBlank(this.dataSourceID)) {
            apiBinder.bindType(DataSource.class).toProvider(this.dataSource);
            apiBinder.bindType(TransactionManager.class).toProvider(new SingleProvider<TransactionManager>(managerProvider));
            apiBinder.bindType(TransactionTemplate.class).toProvider(new SingleProvider<TransactionTemplate>(templateProvider));
            apiBinder.bindType(JdbcTemplate.class).toProvider(new JdbcTemplateProvider(this.dataSource));
            apiBinder.bindType(JdbcOperations.class).toProvider(new JdbcOperationsProvider(this.dataSource));
        } else {
            apiBinder.bindType(DataSource.class).nameWith(this.dataSourceID).toProvider(this.dataSource);
            apiBinder.bindType(TransactionManager.class).nameWith(this.dataSourceID).toProvider(new SingleProvider<TransactionManager>(managerProvider));
            apiBinder.bindType(TransactionTemplate.class).nameWith(this.dataSourceID).toProvider(new SingleProvider<TransactionTemplate>(templateProvider));
            apiBinder.bindType(JdbcTemplate.class).nameWith(this.dataSourceID).toProvider(new JdbcTemplateProvider(this.dataSource));
            apiBinder.bindType(JdbcOperations.class).nameWith(this.dataSourceID).toProvider(new JdbcOperationsProvider(this.dataSource));
        }
        //
        TransactionInterceptor tranInter = new TransactionInterceptor(this.dataSource);
        Matcher<Class<?>> matcherClass = AopMatchers.annotatedWithClass(Transactional.class);
        Matcher<Method> matcherMethod = AopMatchers.annotatedWithMethod(Transactional.class);
        apiBinder.bindInterceptor(matcherClass, matcherMethod, tranInter);
    }
}
