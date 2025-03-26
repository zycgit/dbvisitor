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
package net.hasor.dbvisitor;
import net.hasor.cobble.BeanUtils;
import net.hasor.cobble.MatchUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.loader.ClassMatcher;
import net.hasor.cobble.loader.CobbleClassScanner;
import net.hasor.cobble.loader.MatchType;
import net.hasor.cobble.loader.ScanEvent;
import net.hasor.cobble.loader.providers.ClassPathResourceLoader;
import net.hasor.core.*;
import net.hasor.core.setting.SettingNode;
import static net.hasor.dbvisitor.ConfigKeys.*;
import net.hasor.dbvisitor.dialect.SqlDialectRegister;
import net.hasor.dbvisitor.jdbc.JdbcOperations;
import net.hasor.dbvisitor.jdbc.core.JdbcAccessor;
import net.hasor.dbvisitor.jdbc.core.JdbcConnection;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.provider.JdbcTemplateProvider;
import net.hasor.dbvisitor.provider.TransactionManagerProvider;
import net.hasor.dbvisitor.provider.WrapperAdapterProvider;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.transaction.*;
import net.hasor.dbvisitor.transaction.support.LocalTransactionManager;
import net.hasor.dbvisitor.wrapper.WrapperAdapter;
import net.hasor.dbvisitor.wrapper.WrapperOperations;

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

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-07-18
 */
public class DbVisitorModule implements net.hasor.core.Module {
    @Override
    public void loadModule(ApiBinder apiBinder) throws Exception {
        String multipleDs = apiBinder.getEnvironment().getSettings().getString(MultipleDataSource.getConfigKey());
        if (StringUtils.isNotBlank(multipleDs)) {
            String[] dsNames = multipleDs.split(",");
            for (String dbName : dsNames) {
                configOneDb(dbName, apiBinder);
            }
        } else {
            configOneDb(null, apiBinder);
        }
    }

    private void configOneDb(String dbName, ApiBinder apiBinder) throws Exception {
        BindInfo<DataSource> dsInfo = this.configDataSource(dbName, apiBinder);

        Supplier<DataSource> dsProvider = apiBinder.getProvider(dsInfo);
        this.bindJdbc(dbName, apiBinder, dsProvider);
        this.bindTrans(dbName, apiBinder, dsProvider);

        BindInfo<Configuration> configInfo = configureBySettings(dbName, apiBinder);
        BindInfo<Session> session = this.configSession(dbName, apiBinder, dsInfo, configInfo);

        this.loadMapper(dbName, apiBinder, session);
    }

    private BindInfo<DataSource> configDataSource(String dbName, ApiBinder apiBinder) throws Exception {
        Settings settings = apiBinder.getEnvironment().getSettings();
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
        WrapperAdapterProvider lambdaProvider = new WrapperAdapterProvider(dsProvider);

        if (StringUtils.isBlank(dbName)) {
            apiBinder.bindType(JdbcAccessor.class).toProvider(tempProvider);
            apiBinder.bindType(JdbcConnection.class).toProvider(tempProvider);
            apiBinder.bindType(JdbcTemplate.class).toProvider(tempProvider);
            apiBinder.bindType(JdbcOperations.class).toProvider(tempProvider);
            apiBinder.bindType(WrapperAdapter.class).toProvider(lambdaProvider);
            apiBinder.bindType(WrapperOperations.class).toProvider(lambdaProvider);
        } else {
            apiBinder.bindType(JdbcAccessor.class).nameWith(dbName).toProvider(tempProvider);
            apiBinder.bindType(JdbcConnection.class).nameWith(dbName).toProvider(tempProvider);
            apiBinder.bindType(JdbcTemplate.class).nameWith(dbName).toProvider(tempProvider);
            apiBinder.bindType(JdbcOperations.class).nameWith(dbName).toProvider(tempProvider);
            apiBinder.bindType(WrapperAdapter.class).nameWith(dbName).toProvider(lambdaProvider);
            apiBinder.bindType(WrapperOperations.class).nameWith(dbName).toProvider(lambdaProvider);
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

    private BindInfo<Configuration> configureBySettings(String dbName, ApiBinder apiBinder) {
        Options options = Options.of();
        Settings settings = apiBinder.getEnvironment().getSettings();
        String optAutoMapping = this.configValueOrDefault(dbName, OptAutoMapping, settings);
        String optCamelCase = this.configValueOrDefault(dbName, OptCamelCase, settings);
        String optCaseInsensitive = this.configValueOrDefault(dbName, OptCaseInsensitive, settings);
        String optUseDelimited = this.configValueOrDefault(dbName, OptUseDelimited, settings);
        String optIgnoreNonExistStatement = this.configValueOrDefault(dbName, OptIgnoreNonExistStatement, settings);
        String optSqlDialect = this.configValueOrDefault(dbName, OptSqlDialect, settings);

        if (StringUtils.isNotBlank(optAutoMapping)) {
            options.setAutoMapping(Boolean.parseBoolean(optAutoMapping));
        }
        if (StringUtils.isNotBlank(optCamelCase)) {
            options.setMapUnderscoreToCamelCase(Boolean.parseBoolean(optCamelCase));
        }
        if (StringUtils.isNotBlank(optCaseInsensitive)) {
            options.setCaseInsensitive(Boolean.parseBoolean(optCaseInsensitive));
        }
        if (StringUtils.isNotBlank(optUseDelimited)) {
            options.setUseDelimited(Boolean.parseBoolean(optUseDelimited));
        }
        if (StringUtils.isNotBlank(optIgnoreNonExistStatement)) {
            options.setIgnoreNonExistStatement(Boolean.parseBoolean(optIgnoreNonExistStatement));
        }
        if (StringUtils.isNotBlank(optSqlDialect)) {
            ClassLoader classLoader = apiBinder.getEnvironment().getClassLoader();
            options.setDialect(SqlDialectRegister.findOrCreate(optSqlDialect, classLoader));
        }

        return configureByConfig(dbName, apiBinder, new Configuration(options));
    }

    private BindInfo<Configuration> configureByConfig(String dbName, ApiBinder apiBinder, Configuration config) {
        if (StringUtils.isBlank(dbName)) {
            return apiBinder.bindType(Configuration.class).toInstance(config).toInfo();
        } else {
            return apiBinder.bindType(Configuration.class).nameWith(dbName).toInstance(config).toInfo();
        }
    }

    private BindInfo<Session> configSession(String dbName, ApiBinder apiBinder,//
            BindInfo<DataSource> dsInfo, BindInfo<Configuration> configInfo) throws IOException {
        Settings settings = apiBinder.getEnvironment().getSettings();
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

        SessionSupplier sessionSupplier = HasorUtils.autoAware(apiBinder.getEnvironment(), new SessionSupplier(configInfo, dsInfo, mappers));
        if (StringUtils.isBlank(dbName)) {
            return apiBinder.bindType(Session.class).toProvider(sessionSupplier).toInfo();
        } else {
            return apiBinder.bindType(Session.class).nameWith(dbName).toProvider(sessionSupplier).toInfo();
        }
    }

    private void loadMapper(String dbName, ApiBinder apiBinder, BindInfo<Session> dalInfo) throws ClassNotFoundException {
        Settings settings = apiBinder.getEnvironment().getSettings();
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

        if (mapperDisabled || StringUtils.isBlank(mapperPackageConfig)) {
            return;
        }

        String[] mapperPackages = mapperPackageConfig.split(",");
        Set<Class<?>> finalResult = new HashSet<>();
        CobbleClassScanner scanner = new CobbleClassScanner(apiBinder.getEnvironment().getClassLoader());

        if (StringUtils.isNotBlank(scanMarkerAnnotation)) {
            Class<?> scanAnnotationType = apiBinder.getEnvironment().getClassLoader().loadClass(scanMarkerAnnotation);
            Set<Class<?>> result1 = scanner.getClassSet(mapperPackages, c -> testClass(c, scanAnnotationType));
            finalResult.addAll(result1);
            finalResult.remove(scanAnnotationType);
        }

        if (StringUtils.isNotBlank(scanMarkerInterface)) {
            Class<?> scanInterfaceType = apiBinder.getEnvironment().getClassLoader().loadClass(scanMarkerInterface);
            Set<Class<?>> result2 = scanner.getClassSet(mapperPackages, c -> testClass(c, scanInterfaceType));
            finalResult.addAll(result2);
            finalResult.remove(scanInterfaceType);
        }

        for (Class<?> mapper : finalResult) {
            Class<Object> mapperCast = (Class<Object>) mapper;

            MapperSupplier dalMapper = new MapperSupplier(mapperCast, dalInfo);
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

        /** 在方法上找 Transactional ，如果找不到在到 类上找 Transactional ，如果依然没有，那么在所处的包(包括父包)上找 Transactional。 */
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

    private static boolean testClass(ClassMatcher.ClassMatcherContext matcherContext, Class<?> compareType) {
        ClassMatcher.ClassInfo classInfo = matcherContext.getClassInfo();
        String compareTypeName = compareType.getName();

        if (classInfo.className.equals(compareTypeName) || classInfo.superName.equals(compareTypeName)) {
            return true;
        }

        for (String castType : classInfo.castType) {
            if (castType.equals(compareTypeName)) {
                return true;
            }
        }
        for (String face : classInfo.annos) {
            if (face.equals(compareTypeName)) {
                return true;
            }
        }

        return false;
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

    private String configValueOrDefault(String dbName, ConfigKeys configKey, Settings settings) {
        String s = settings.getString(configKey.buildConfigKey(dbName), null);
        if (StringUtils.isBlank(s)) {
            return settings.getString(configKey.getConfigKey(), configKey.getDefaultValue());
        } else {
            return s;
        }
    }
}
