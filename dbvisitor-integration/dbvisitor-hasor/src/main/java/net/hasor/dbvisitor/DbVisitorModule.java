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
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.sql.DataSource;
import net.hasor.cobble.BeanUtils;
import net.hasor.cobble.MatchUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.loader.ClassMatcher;
import net.hasor.cobble.loader.CobbleClassScanner;
import net.hasor.cobble.loader.MatchType;
import net.hasor.cobble.loader.ScanEvent;
import net.hasor.cobble.loader.providers.ClassPathResourceLoader;
import net.hasor.core.*;
import net.hasor.dbvisitor.dialect.SqlDialectRegister;
import net.hasor.dbvisitor.jdbc.JdbcOperations;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.LambdaOperations;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.provider.JdbcTemplateProvider;
import net.hasor.dbvisitor.provider.LambdaTemplateProvider;
import net.hasor.dbvisitor.provider.TransactionManagerProvider;
import net.hasor.dbvisitor.provider.TransactionTemplateProvider;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.transaction.*;
import net.hasor.dbvisitor.transaction.support.TransactionHelper;
import static net.hasor.dbvisitor.ConfigKeys.*;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-07-18
 */
public class DbVisitorModule implements net.hasor.core.Module {
    @Override
    public void loadModule(ApiBinder apiBinder) throws Exception {
        String multipleDs = configString(apiBinder.getEnvironment().getSettings(), MultipleDataSource.getConfigKey(), null);
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
        String dataSourceType = configString(settings, configKey, DataSourceType.getDefaultValue());
        DataSource dataSource;
        if (StringUtils.isBlank(dataSourceType)) {
            dataSource = new DefaultDataSource();
        } else {
            Class<?> dsClass = apiBinder.getEnvironment().getClassLoader().loadClass(dataSourceType);
            dataSource = (DataSource) dsClass.newInstance();
        }

        applySettingsByPropertyName(settings, configKey, dataSource);

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
            apiBinder.bindType(JdbcTemplate.class).toProvider(tempProvider);
            apiBinder.bindType(JdbcOperations.class).toProvider(tempProvider);
            apiBinder.bindType(LambdaTemplate.class).toProvider(lambdaProvider);
            apiBinder.bindType(LambdaOperations.class).toProvider(lambdaProvider);
        } else {
            apiBinder.bindType(JdbcTemplate.class).nameWith(dbName).toProvider(tempProvider);
            apiBinder.bindType(JdbcOperations.class).nameWith(dbName).toProvider(tempProvider);
            apiBinder.bindType(LambdaTemplate.class).nameWith(dbName).toProvider(lambdaProvider);
            apiBinder.bindType(LambdaOperations.class).nameWith(dbName).toProvider(lambdaProvider);
        }
    }

    private void bindTrans(String dbName, ApiBinder apiBinder, Supplier<DataSource> dsProvider) {
        Supplier<TransactionManager> managerProvider = new TransactionManagerProvider(dsProvider);
        Supplier<TransactionTemplate> templateProvider = new TransactionTemplateProvider(dsProvider);

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
        String resources = configString(settings, configKey, MapperLocations.getDefaultValue());
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
        String configScanMarkerAnnotation = ScanMarkerAnnotation.buildConfigKey(dbName);
        String configScanMarkerInterface = ScanMarkerInterface.buildConfigKey(dbName);

        Boolean mapperDisabled = Boolean.parseBoolean(configString(settings, configMapperDisabled, MapperDisabled.getDefaultValue()));
        String mapperPackageConfig = configString(settings, configMapperPackages, MapperPackages.getDefaultValue());
        String scanMarkerAnnotation = configString(settings, configScanMarkerAnnotation, ScanMarkerAnnotation.getDefaultValue());
        String scanMarkerInterface = configString(settings, configScanMarkerInterface, ScanMarkerInterface.getDefaultValue());

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
                apiBinder.bindType(mapperCast).toProvider(dalMapper).asEagerSingleton();
            } else {
                apiBinder.bindType(mapperCast).nameWith(dbName).toProvider(dalMapper).asEagerSingleton();
            }
        }
    }

    private static class TranInterceptor implements MethodInterceptor {
        private final Supplier<DataSource> dataSource;

        public TranInterceptor(Supplier<DataSource> dataSource) {
            this.dataSource = Objects.requireNonNull(dataSource, "dataSource Provider is null.");
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
            TransactionManager manager = TransactionHelper.txManager(this.dataSource.get());
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

    private static void applySettingsByPropertyName(Settings settings, String configKey, Object dataSource) {
        if (settings == null || dataSource == null) {
            return;
        }
        PropertyDescriptor[] descriptors = BeanUtils.getPropertyDescriptors(dataSource.getClass());
        for (PropertyDescriptor descriptor : descriptors) {
            String propName = descriptor.getName();
            if ("class".equals(propName)) {
                continue;
            }
            String settingKey = configKey + "." + StringUtils.humpToLine(propName).replace('_', '-');
            String propValue = configString(settings, settingKey, null);
            if (StringUtils.isNotBlank(propValue)) {
                BeanUtils.writeProperty(dataSource, propName, propValue);
            }
        }
    }

    private String configValueOrDefault(String dbName, ConfigKeys configKey, Settings settings) {
        String s = configString(settings, configKey.buildConfigKey(dbName), null);
        if (StringUtils.isBlank(s)) {
            return configString(settings, configKey.getConfigKey(), configKey.getDefaultValue());
        } else {
            return s;
        }
    }

    private static String configString(Settings settings, String key, String defaultValue) {
        String value = settings.getString(key, null);
        if (StringUtils.isNotBlank(value)) {
            return value;
        }

        String underscoreKey = key.replace('-', '_');
        if (!underscoreKey.equals(key)) {
            value = settings.getString(underscoreKey, null);
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        }

        String camelKey = keyToCamelPath(key);
        if (!camelKey.equals(key)) {
            value = settings.getString(camelKey, null);
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        }

        return defaultValue;
    }

    private static String keyToCamelPath(String key) {
        if (StringUtils.isBlank(key)) {
            return key;
        }
        String[] segments = key.split("\\.");
        for (int i = 0; i < segments.length; i++) {
            String segment = segments[i];
            if (segment.indexOf('-') >= 0) {
                segments[i] = StringUtils.lineToHump(segment.replace('-', '_'));
            }
        }
        return String.join(".", segments);
    }
}
