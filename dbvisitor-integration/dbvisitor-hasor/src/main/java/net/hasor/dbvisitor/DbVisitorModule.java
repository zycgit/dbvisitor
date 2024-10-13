/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.dbvisitor;
import net.hasor.cobble.BeanUtils;
import net.hasor.cobble.MatchUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.loader.MatchType;
import net.hasor.cobble.loader.ScanEvent;
import net.hasor.cobble.loader.providers.ClassPathResourceLoader;
import net.hasor.core.*;
import net.hasor.core.setting.SettingNode;
import net.hasor.dbvisitor.dynamic.rule.RuleRegistry;
import net.hasor.dbvisitor.dal.session.DalSession;
import net.hasor.dbvisitor.dialect.SqlDialectRegister;
import net.hasor.dbvisitor.jdbc.JdbcOperations;
import net.hasor.dbvisitor.jdbc.core.JdbcAccessor;
import net.hasor.dbvisitor.jdbc.core.JdbcConnection;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.LambdaOperations;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapping.resolve.MappingOptions;
import net.hasor.dbvisitor.provider.JdbcTemplateProvider;
import net.hasor.dbvisitor.provider.LambdaTemplateProvider;
import net.hasor.dbvisitor.provider.TransactionManagerProvider;
import net.hasor.dbvisitor.transaction.*;
import net.hasor.dbvisitor.transaction.support.LocalTransactionManager;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;
import net.hasor.utils.ScanClassPath;

import javax.sql.DataSource;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static net.hasor.dbvisitor.ConfigKeys.*;

/**
 *
 */
public class DbVisitorModule implements net.hasor.core.Module {
    private final MappingOptions options = MappingOptions.buildNew();

    @Override
    public void loadModule(ApiBinder apiBinder) throws Exception {
        Settings settings = apiBinder.getEnvironment().getSettings();
        String multipleDs = settings.getString(MultipleDataSource.getConfigKey());
        String optAutoMapping = settings.getString(OptAutoMapping.getConfigKey(), OptAutoMapping.getDefaultValue());
        String optCamelCase = settings.getString(OptCamelCase.getConfigKey(), OptCamelCase.getDefaultValue());
        String optCaseInsensitive = settings.getString(OptCaseInsensitive.getConfigKey(), OptCaseInsensitive.getDefaultValue());
        String optUseDelimited = settings.getString(OptUseDelimited.getConfigKey(), OptUseDelimited.getDefaultValue());
        String optSqlDialect = settings.getString(OptSqlDialect.getConfigKey(), OptSqlDialect.getDefaultValue());
        if (StringUtils.isNotBlank(optAutoMapping)) {
            this.options.setAutoMapping(Boolean.parseBoolean(optAutoMapping));
        }
        if (StringUtils.isNotBlank(optCamelCase)) {
            this.options.setMapUnderscoreToCamelCase(Boolean.parseBoolean(optCamelCase));
        }
        if (StringUtils.isNotBlank(optCaseInsensitive)) {
            this.options.setCaseInsensitive(Boolean.parseBoolean(optCaseInsensitive));
        }
        if (StringUtils.isNotBlank(optUseDelimited)) {
            this.options.setUseDelimited(Boolean.parseBoolean(optUseDelimited));
        }
        if (StringUtils.isNotBlank(optSqlDialect)) {
            this.options.setDefaultDialect(SqlDialectRegister.findOrCreate(optSqlDialect, null));
        }

        if (StringUtils.isNotBlank(multipleDs)) {
            String[] dsNames = multipleDs.split(",");
            for (String dbName : dsNames) {
                configOneDb(dbName, apiBinder, settings);
            }
        } else {
            configOneDb(null, apiBinder, settings);
        }
    }

    private void configOneDb(String dbName, ApiBinder apiBinder, Settings settings) throws Exception {
        BindInfo<DataSource> dsInfo = this.configDataSource(dbName, apiBinder, settings);
        Supplier<DataSource> dsProvider = apiBinder.getProvider(dsInfo);

        this.bindJdbc(dbName, apiBinder, dsProvider);
        this.bindTrans(dbName, apiBinder, dsProvider);

        BindInfo<TypeHandlerRegistry> typeInfo = this.configTypeRegistry(dbName, apiBinder, settings);
        BindInfo<RuleRegistry> ruleInfo = this.configRuleRegistry(dbName, apiBinder, settings);
        BindInfo<DalSession> dalInfo = this.configDalSession(dbName, apiBinder, settings, dsInfo, typeInfo, ruleInfo);

        this.loadMapper(dbName, apiBinder, dalInfo, settings);
    }

    private BindInfo<DataSource> configDataSource(String dbName, ApiBinder apiBinder, Settings settings) throws Exception {
        String configKey = DataSourceType.buildConfigKey(dbName);
        String dataSourceType = settings.getString(configKey, DataSourceType.getDefaultValue());
        DataSource dataSource;
        if (StringUtils.isBlank(dataSourceType)) {
            dataSource = new DefaultDataSource();
        } else {
            Class<?> dsClass = apiBinder.getEnvironment().getClassLoader().loadClass(dataSourceType);
            dataSource = (DataSource) dsClass.newInstance();
        }

        SettingNode configNode = settings.getNode(configKey);
        String[] subKeys = configNode.getSubKeys();
        for (String key : subKeys) {
            String subValue = configNode.getSubValue(key);
            String propName = lineToHump(key);
            BeanUtils.writeProperty(dataSource, propName, subValue);
        }

        if (StringUtils.isBlank(dbName)) {
            return apiBinder.bindType(DataSource.class).toInstance(dataSource).toInfo();
        } else {
            return apiBinder.bindType(DataSource.class).nameWith(dbName).toInstance(dataSource).toInfo();
        }
    }

    private void bindJdbc(String dbName, ApiBinder apiBinder, Supplier<DataSource> dsProvider) {
        JdbcTemplateProvider tempProvider = new JdbcTemplateProvider(dsProvider);
        LambdaTemplateProvider lambdaProvider = new LambdaTemplateProvider(dsProvider);

        if (StringUtils.isBlank(dbName)) {
            apiBinder.bindType(JdbcAccessor.class).toProvider(tempProvider);
            apiBinder.bindType(JdbcConnection.class).toProvider(tempProvider);
            apiBinder.bindType(JdbcTemplate.class).toProvider(tempProvider);
            apiBinder.bindType(JdbcOperations.class).toProvider(tempProvider);
            apiBinder.bindType(LambdaTemplate.class).toProvider(lambdaProvider);
            apiBinder.bindType(LambdaOperations.class).toProvider(lambdaProvider);
        } else {
            apiBinder.bindType(JdbcAccessor.class).nameWith(dbName).toProvider(tempProvider);
            apiBinder.bindType(JdbcConnection.class).nameWith(dbName).toProvider(tempProvider);
            apiBinder.bindType(JdbcTemplate.class).nameWith(dbName).toProvider(tempProvider);
            apiBinder.bindType(JdbcOperations.class).nameWith(dbName).toProvider(tempProvider);
            apiBinder.bindType(LambdaTemplate.class).nameWith(dbName).toProvider(lambdaProvider);
            apiBinder.bindType(LambdaOperations.class).nameWith(dbName).toProvider(lambdaProvider);
        }
    }

    private void bindTrans(String dbName, ApiBinder apiBinder, Supplier<DataSource> dsProvider) {
        Supplier<TransactionManager> managerProvider = new TransactionManagerProvider(dsProvider);
        Supplier<TransactionTemplate> templateProvider = () -> {
            TransactionManager tm = new LocalTransactionManager(dsProvider.get());
            return new TransactionTemplateManager(tm);
        };

        if (StringUtils.isBlank(dbName)) {
            apiBinder.bindType(TransactionManager.class).toProvider(managerProvider);
            apiBinder.bindType(TransactionTemplate.class).toProvider(templateProvider);
        } else {
            apiBinder.bindType(TransactionManager.class).nameWith(dbName).toProvider(managerProvider);
            apiBinder.bindType(TransactionTemplate.class).nameWith(dbName).toProvider(templateProvider);
        }

        TranInterceptor tranInter = new TranInterceptor(dsProvider);
        apiBinder.bindInterceptor(new ClassAnnotationOf(Transactional.class), new MethodAnnotationOf(Transactional.class), tranInter);
    }

    private BindInfo<TypeHandlerRegistry> configTypeRegistry(String dbName, ApiBinder apiBinder, Settings settings) {
        String configKey = RefTypeRegistry.buildConfigKey(dbName);
        String refTypeRegistry = settings.getString(configKey, RefTypeRegistry.getDefaultValue());

        BindInfo<?> bindInfo;
        if (StringUtils.isNotBlank(refTypeRegistry)) {
            bindInfo = apiBinder.getBindInfo(refTypeRegistry);
            if (bindInfo == null) {
                bindInfo = apiBinder.findBindingRegister(refTypeRegistry, TypeHandlerRegistry.class);
            }
        } else {
            bindInfo = apiBinder.getBindInfo(TypeHandlerRegistry.class);
        }

        if (bindInfo == null) {
            bindInfo = apiBinder.bindType(TypeHandlerRegistry.class).toInstance(TypeHandlerRegistry.DEFAULT).toInfo();
        }

        return (BindInfo<TypeHandlerRegistry>) bindInfo;
    }

    private BindInfo<RuleRegistry> configRuleRegistry(String dbName, ApiBinder apiBinder, Settings settings) {
        String configKey = RefRuleRegistry.buildConfigKey(dbName);
        String refRuleRegistry = settings.getString(configKey, RefRuleRegistry.getDefaultValue());

        BindInfo<?> bindInfo;
        if (StringUtils.isNotBlank(refRuleRegistry)) {
            bindInfo = apiBinder.getBindInfo(refRuleRegistry);
            if (bindInfo == null) {
                bindInfo = apiBinder.findBindingRegister(refRuleRegistry, RuleRegistry.class);
            }
        } else {
            bindInfo = apiBinder.getBindInfo(RuleRegistry.class);
        }

        if (bindInfo == null) {
            bindInfo = apiBinder.bindType(RuleRegistry.class).toInstance(RuleRegistry.DEFAULT).toInfo();
        }

        return (BindInfo<RuleRegistry>) bindInfo;
    }

    private BindInfo<DalSession> configDalSession(String dbName, ApiBinder apiBinder, Settings settings,//
            BindInfo<DataSource> dsInfo, BindInfo<TypeHandlerRegistry> typeInfo, BindInfo<RuleRegistry> ruleInfo) throws IOException {
        String configKey = MapperLocations.buildConfigKey(dbName);
        String resources = settings.getString(configKey, MapperLocations.getDefaultValue());
        Set<URI> mappers = new HashSet<>();

        if (StringUtils.isNotBlank(resources)) {
            ClassPathResourceLoader classScannerLoader = new ClassPathResourceLoader(apiBinder.getEnvironment().getClassLoader());

            for (String resourceURL : resources.split(",")) {
                String resMapper = resourceURL.trim();
                if (StringUtils.startsWithIgnoreCase(resMapper, "classpath:")) {
                    resMapper = resMapper.substring("classpath:".length());
                }

                resMapper = MatchUtils.wildToRegex(resMapper);
                List<URI> tmp = classScannerLoader.scanResources(MatchType.Regex, ScanEvent::getResource, new String[] { resMapper });
                mappers.addAll(tmp);
            }
        }

        DalSessionSupplier sessionSupplier = HasorUtils.autoAware(apiBinder.getEnvironment(), new DalSessionSupplier(this.options, dsInfo, typeInfo, ruleInfo, mappers));
        if (StringUtils.isBlank(dbName)) {
            return apiBinder.bindType(DalSession.class).toProvider(sessionSupplier).toInfo();
        } else {
            return apiBinder.bindType(DalSession.class).nameWith(dbName).toProvider(sessionSupplier).toInfo();
        }
    }

    private static String lineToHump(String name) {
        // copy from spring jdbc 6.0.12 JdbcUtils.convertUnderscoreNameToPropertyName
        StringBuilder result = new StringBuilder();
        boolean nextIsUpper = false;
        if (name != null && name.length() > 0) {
            if (name.length() > 1 && name.charAt(1) == '-') {
                result.append(Character.toUpperCase(name.charAt(0)));
            } else {
                result.append(Character.toLowerCase(name.charAt(0)));
            }
            for (int i = 1; i < name.length(); i++) {
                char c = name.charAt(i);
                if (c == '-') {
                    nextIsUpper = true;
                } else {
                    if (nextIsUpper) {
                        result.append(Character.toUpperCase(c));
                        nextIsUpper = false;
                    } else {
                        result.append(Character.toLowerCase(c));
                    }
                }
            }
        }
        return result.toString();
    }

    //    private void loadResources(String dbName, ApiBinder apiBinder, Settings settings,//
    //            final BindInfo<TypeHandlerRegistry> typeInfo, final BindInfo<RuleRegistry> ruleInfo) throws IOException {
    //        HasorUtils.autoAware(apiBinder.getEnvironment(), appContext -> {
    //            TypeHandlerRegistry handlerRegistry = appContext.getInstance(typeInfo);
    //            RuleRegistry ruleRegistry = appContext.getInstance(ruleInfo);
    //            //
    //            // TODO setup.
    //        });
    //    }

    private void loadMapper(String dbName, ApiBinder apiBinder, BindInfo<DalSession> dalInfo, Settings settings) throws ClassNotFoundException {
        String configMapperDisabled = MapperDisabled.buildConfigKey(dbName);
        String configMapperPackages = MapperPackages.buildConfigKey(dbName);
        String configMapperScope = MapperScope.buildConfigKey(dbName);
        String configScanMarkerAnnotation = ScanMarkerAnnotation.buildConfigKey(dbName);
        String configScanMarkerInterface = ScanMarkerInterface.buildConfigKey(dbName);

        Boolean mapperDisabled = settings.getBoolean(configMapperDisabled, Boolean.parseBoolean(MapperDisabled.getDefaultValue()));
        String mapperPackageConfig = settings.getString(configMapperPackages, MapperPackages.getDefaultValue());
        String mapperScope = settings.getString(configMapperScope, MapperScope.getDefaultValue());
        String scanMarkerAnnotation = settings.getString(configScanMarkerAnnotation, ScanMarkerAnnotation.getDefaultValue());
        String scanMarkerInterface = settings.getString(configScanMarkerInterface, ScanMarkerInterface.getDefaultValue());

        if (mapperDisabled) {
            return;
        }

        if (StringUtils.isBlank(mapperPackageConfig)) {
            return;
        }

        String[] mapperPackages = mapperPackageConfig.split(",");
        Set<Class<?>> finalResult = new HashSet<>();

        if (StringUtils.isNotBlank(scanMarkerAnnotation)) {
            Class<?> scanAnnotationType = apiBinder.getEnvironment().getClassLoader().loadClass(scanMarkerAnnotation);
            Set<Class<?>> result1 = ScanClassPath.newInstance(mapperPackages).getClassSet(scanAnnotationType);
            finalResult.addAll(result1);
            finalResult.remove(scanAnnotationType);
        }

        if (StringUtils.isNotBlank(scanMarkerInterface)) {
            Class<?> scanInterfaceType = apiBinder.getEnvironment().getClassLoader().loadClass(scanMarkerInterface);
            Set<Class<?>> result2 = ScanClassPath.newInstance(mapperPackages).getClassSet(scanInterfaceType);
            finalResult.addAll(result2);
            finalResult.remove(scanInterfaceType);
        }

        for (Class<?> mapper : finalResult) {
            Class<Object> mapperCast = (Class<Object>) mapper;

            DalMapperSupplier dalMapper = new DalMapperSupplier(mapperCast, dalInfo);
            HasorUtils.pushStartListener(apiBinder.getEnvironment(), dalMapper);

            if (StringUtils.isBlank(dbName)) {
                apiBinder.bindType(mapperCast).toProvider(dalMapper).toScope(mapperScope);
            } else {
                apiBinder.bindType(mapperCast).nameWith(dbName).toProvider(dalMapper).toScope(mapperScope);
            }
        }
    }

    private static class TranInterceptor implements MethodInterceptor {
        private final    Supplier<DataSource> dataSource;
        private volatile TransactionManager   transactionManager;

        public TranInterceptor(Supplier<DataSource> dataSource) {
            this.dataSource = Objects.requireNonNull(dataSource, "dataSource Provider is null.");
        }

        private TransactionManager getTxManager() {
            if (this.transactionManager == null) {
                synchronized (this) {
                    if (this.transactionManager == null) {
                        this.transactionManager = new LocalTransactionManager(this.dataSource.get());
                    }
                }
            }
            return this.transactionManager;
        }

        /* 是否不需要回滚:true表示不要回滚 */
        private boolean testNoRollBackFor(Transactional tranAnno, Throwable e) {
            //1.test Class
            Class<? extends Throwable>[] noRollBackType = tranAnno.noRollbackFor();
            for (Class<? extends Throwable> cls : noRollBackType) {
                if (cls.isInstance(e)) {
                    return true;
                }
            }
            //2.test Name
            String[] noRollBackName = tranAnno.noRollbackForClassName();
            String errorType = e.getClass().getName();
            for (String name : noRollBackName) {
                if (errorType.equals(name)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public final Object invoke(final MethodInvocation invocation) throws Throwable {
            Method targetMethod = invocation.getMethod();
            Transactional tranInfo = tranAnnotation(targetMethod);
            if (tranInfo == null) {
                return invocation.proceed();
            }
            //0.准备事务环境
            TransactionManager manager = getTxManager();
            Propagation behavior = tranInfo.propagation();
            Isolation level = tranInfo.isolation();
            TransactionStatus tranStatus = manager.begin(behavior, level);
            //1.只读事务
            if (tranInfo.readOnly()) {
                tranStatus.setReadOnly();
            }
            //2.事务行为控制
            try {
                return invocation.proceed();
            } catch (Throwable e) {
                if (!this.testNoRollBackFor(tranInfo, e)) {
                    tranStatus.setRollback();
                }
                throw e;
            } finally {
                if (!tranStatus.isCompleted()) {
                    manager.commit(tranStatus);
                }
            }
        }

        /** 在方法上找 Transactional ，如果找不到在到 类上找 Transactional ，如果依然没有，那么在所处的包(包括父包)上找 Transactional。*/
        private Transactional tranAnnotation(Method targetMethod) {
            Transactional tran = targetMethod.getAnnotation(Transactional.class);
            if (tran == null) {
                Class<?> declaringClass = targetMethod.getDeclaringClass();
                tran = declaringClass.getAnnotation(Transactional.class);
            }
            return tran;
        }
    }

    /** 匹配类或类方法上标记的注解 */
    private static class ClassAnnotationOf implements Predicate<Class<?>> {
        private final Class<? extends Annotation> annotationType;

        public ClassAnnotationOf(final Class<? extends Annotation> annotationType) {
            this.annotationType = annotationType;
        }

        @Override
        public boolean test(final Class<?> matcherType) {
            Object[] annoByType1 = matcherType.getAnnotationsByType(this.annotationType);
            if (annoByType1.length > 0) {
                return true;
            }

            Object[] annoByType2 = Arrays.stream(matcherType.getMethods()).flatMap((Function<Method, Stream<?>>) method -> {
                return Arrays.stream(method.getAnnotationsByType(annotationType));
            }).toArray(Object[]::new);
            if (annoByType2.length > 0) {
                return true;
            }

            Object[] annoByType3 = Arrays.stream(matcherType.getDeclaredMethods()).flatMap((Function<Method, Stream<?>>) method -> {
                return Arrays.stream(method.getAnnotationsByType(annotationType));
            }).toArray(Object[]::new);

            return annoByType3.length > 0;
        }
    }

    /** 匹配方法上的注解 */
    private static class MethodAnnotationOf implements Predicate<Method> {
        private final Class<? extends Annotation> annotationType;

        public MethodAnnotationOf(final Class<? extends Annotation> annotationType) {
            this.annotationType = annotationType;
        }

        @Override
        public boolean test(final Method matcherType) {
            if (matcherType.isAnnotationPresent(this.annotationType)) {
                return true;
            } else {
                return matcherType.getDeclaringClass().isAnnotationPresent(this.annotationType);
            }
        }
    }
}
