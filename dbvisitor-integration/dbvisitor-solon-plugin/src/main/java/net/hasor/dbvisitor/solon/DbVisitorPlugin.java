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
import java.net.URI;
import java.util.*;
import javax.sql.DataSource;
import net.hasor.cobble.ExceptionUtils;
import net.hasor.cobble.MatchUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.loader.ClassMatcher;
import net.hasor.cobble.loader.CobbleClassScanner;
import net.hasor.cobble.loader.MatchType;
import net.hasor.cobble.loader.ScanEvent;
import net.hasor.cobble.loader.providers.ClassPathResourceLoader;
import net.hasor.cobble.logging.Logger;
import net.hasor.dbvisitor.dialect.SqlDialectRegister;
import net.hasor.dbvisitor.jdbc.JdbcOperations;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.LambdaOperations;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import org.noear.solon.Solon;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.BeanWrap;
import org.noear.solon.core.Plugin;
import org.noear.solon.core.Props;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2025-03-20
 */
public class DbVisitorPlugin implements Plugin {
    private static final Logger log = Logger.getLogger(DbVisitorPlugin.class);

    @Override
    public void start(AppContext context) throws Exception {
        ClassLoader classLoader = context.getClassLoader();
        Map<String, MapperWrap> mapperWrapMap = new HashMap<>();

        // load dbvisitor
        Map<String, Props> allProps = context.app().cfg().getGroupedProp("dbvisitor");
        for (String key : allProps.keySet()) {
            Props itemProps = allProps.get(key);
            if (itemProps.isEmpty()) {
                log.error("dbVisitor config 'dbvisitor." + key + "' is skipped, props is empty.");
                continue;
            }

            Configuration conf = new Configuration(createOptions(classLoader, itemProps), classLoader);
            loadMapperFiles(conf, itemProps, classLoader);
            MapperWrap mapperWrap = new MapperWrap(conf);
            mapperWrapMap.put(key, mapperWrap);

            String mapperDisabled = itemProps.get(ConfigKeys.MapperDisabled.getConfigKey(), ConfigKeys.MapperDisabled.getDefaultValue());
            String mapperPackageConfig = itemProps.get(ConfigKeys.MapperPackages.getConfigKey(), ConfigKeys.MapperPackages.getDefaultValue());
            if (Boolean.parseBoolean(mapperDisabled) || StringUtils.isBlank(mapperPackageConfig)) {
                log.warn("dbVisitor config 'dbvisitor." + key + "' mapper is disabled.");
                continue;
            }

            loadMapper(itemProps, mapperPackageConfig, mapperWrap);
        }

        // add Mapper to solon
        Set<Class<?>> ambiguousMapper = fetchAmbiguousMapper(mapperWrapMap);
        context.subWrapsOfType(DataSource.class, bw -> {
            try {
                if (mapperWrapMap.containsKey(bw.name())) {
                    loadCoreInject(context, bw, mapperWrapMap, ambiguousMapper);
                }

                if (bw.typed()) {
                    loadTypedInject(context, bw, mapperWrapMap, ambiguousMapper);
                }
            } catch (Exception e) {
                throw ExceptionUtils.toRuntime(e);
            }
        });
    }

    private void loadCoreInject(AppContext context, BeanWrap dsBw, Map<String, MapperWrap> wrapMap, Set<Class<?>> ambiguousMapper) throws Exception {
        MapperWrap wrap = wrapMap.get(dsBw.name());

        // basic api for this DataSource.
        context.beanInjectorAdd(Db.class, new DbVisitorInjector(wrapMap));

        // mappers
        Session dbvSession = DsHelper.fetchSession(dsBw, wrap);
        for (Class<?> mapperType : wrap.getMapperType()) {
            if (ambiguousMapper.contains(mapperType)) {
                continue;// skip ambiguous.
            }

            Object mapper = dbvSession.createMapper(mapperType);
            BeanWrap beanWrap = Solon.context().wrap(mapperType.getSimpleName(), mapper);
            beanWrap.singletonSet(wrap.isSingleton(mapperType));
            context.putWrap(mapperType, beanWrap);
        }
    }

    private void loadTypedInject(AppContext context, BeanWrap dsBw, Map<String, MapperWrap> mapperWrapMap, Set<Class<?>> ambiguousMapper) throws Exception {
        if (!mapperWrapMap.containsKey(dsBw.name())) {
            return;
        }

        //ambiguousMapper using default.
        final MapperWrap mapperWrap = mapperWrapMap.get(dsBw.name());
        final Session dbvSession = DsHelper.fetchSession(dsBw, mapperWrap);
        BeanWrap beanWrap;

        //@Inject JdbcTemplate
        beanWrap = Solon.context().wrap(JdbcTemplate.class.getSimpleName(), dbvSession.jdbc(), true);
        beanWrap.singletonSet(true);
        context.putWrap(JdbcTemplate.class, beanWrap);
        beanWrap = Solon.context().wrap(JdbcOperations.class.getSimpleName(), dbvSession.jdbc(), true);
        beanWrap.singletonSet(true);
        context.putWrap(JdbcOperations.class, beanWrap);

        //@Inject WrapperAdapter
        beanWrap = Solon.context().wrap(LambdaTemplate.class.getSimpleName(), dbvSession.lambda(), true);
        beanWrap.singletonSet(true);
        context.putWrap(LambdaTemplate.class, beanWrap);
        beanWrap = Solon.context().wrap(LambdaOperations.class.getSimpleName(), dbvSession.lambda(), true);
        beanWrap.singletonSet(true);
        context.putWrap(LambdaOperations.class, beanWrap);

        //@Inject Configuration
        beanWrap = Solon.context().wrap(Configuration.class.getSimpleName(), dbvSession.getConfiguration(), true);
        beanWrap.singletonSet(true);
        context.putWrap(Configuration.class, beanWrap);

        //@Inject Session
        beanWrap = Solon.context().wrap(Session.class.getSimpleName(), dbvSession, true);
        beanWrap.singletonSet(true);
        context.putWrap(Session.class, beanWrap);

        //@Inject TransactionManager
        //beanWrap = Solon.context().wrap(TransactionManager.class.getSimpleName(), TransactionHelper.txManager().class);
        //beanWrap.singletonSet(true);
        //context.putWrap(TransactionManager.class, beanWrap);

        //@Inject TransactionTemplate
    }

    private static Set<Class<?>> fetchAmbiguousMapper(Map<String, MapperWrap> mapperWrapMap) {
        Set<Class<?>> ambiguous = new HashSet<>();
        Set<Class<?>> foundOnes = new HashSet<>();
        for (MapperWrap wrap : mapperWrapMap.values()) {
            for (Class<?> mapperType : wrap.getMapperType()) {
                if (ambiguous.contains(mapperType)) {
                    continue;
                }

                if (foundOnes.contains(mapperType)) {
                    ambiguous.add(mapperType);
                    foundOnes.remove(mapperType);
                } else {
                    foundOnes.add(mapperType);
                }
            }
        }

        if (ambiguous.isEmpty()) {
            log.info("dbVisitor " + foundOnes.size() + " mappers were found.");
        } else {
            log.warn("dbVisitor " + foundOnes.size() + " mappers and " + ambiguous.size() + " ambiguities were found.");
            log.warn("dbVisitor ambiguities mappers is " + StringUtils.join(ambiguous.toArray(), ","));
        }

        return ambiguous;
    }

    private static void loadMapper(Props itemProps, String mapperPackageConfig, MapperWrap mapperWrap) throws Exception {
        ClassLoader classLoader = mapperWrap.getConf().getClassLoader();
        String scanMarkerAnnotation = itemProps.get(ConfigKeys.ScanMarkerAnnotation.getConfigKey(), ConfigKeys.ScanMarkerAnnotation.getDefaultValue());
        String scanMarkerInterface = itemProps.get(ConfigKeys.ScanMarkerInterface.getConfigKey(), ConfigKeys.ScanMarkerInterface.getDefaultValue());

        String[] mapperPackages = mapperPackageConfig.split(",");
        CobbleClassScanner classScanner = new CobbleClassScanner(classLoader);
        Set<Class<?>> finalResult = new HashSet<>();

        if (StringUtils.isNotBlank(scanMarkerAnnotation)) {
            Class<?> scanAnnotationType = classLoader.loadClass(scanMarkerAnnotation);
            Set<Class<?>> result1 = classScanner.getClassSet(mapperPackages, c -> testClass(c, scanAnnotationType));
            finalResult.addAll(result1);
            finalResult.remove(scanAnnotationType);
        }

        if (StringUtils.isNotBlank(scanMarkerInterface)) {
            Class<?> scanInterfaceType = classLoader.loadClass(scanMarkerInterface);
            Set<Class<?>> result2 = classScanner.getClassSet(mapperPackages, c -> testClass(c, scanInterfaceType));
            finalResult.addAll(result2);
            finalResult.remove(scanInterfaceType);
        }

        for (Class<?> mapperType : finalResult) {
            mapperWrap.addMapper(mapperType, true);
        }
    }

    private static void loadMapperFiles(Configuration conf, Props itemProps, ClassLoader classLoader) throws Exception {
        ClassPathResourceLoader scanner = new ClassPathResourceLoader(classLoader);
        String mapperLocations = itemProps.get(ConfigKeys.MapperLocations.getConfigKey(), ConfigKeys.MapperLocations.getDefaultValue());
        for (String resourceURL : mapperLocations.split(",")) {
            String resMapper = resourceURL.trim();
            if (StringUtils.startsWithIgnoreCase(resMapper, "classpath:")) {
                resMapper = resMapper.substring("classpath:".length());
            }

            resMapper = MatchUtils.wildToRegex(resMapper);
            List<URI> mapperURIs = scanner.scanResources(MatchType.Regex, ScanEvent::getResource, new String[] { resMapper });
            for (URI uri : mapperURIs) {
                conf.loadMapper(uri.toString());
            }
        }
    }

    private static Options createOptions(ClassLoader classLoader, Props itemProps) {
        String optAutoMapping = itemProps.get(ConfigKeys.OptAutoMapping.getConfigKey(), ConfigKeys.OptAutoMapping.getDefaultValue());
        String optCamelCase = itemProps.get(ConfigKeys.OptCamelCase.getConfigKey(), ConfigKeys.OptCamelCase.getDefaultValue());
        String optCaseInsensitive = itemProps.get(ConfigKeys.OptCaseInsensitive.getConfigKey(), ConfigKeys.OptCaseInsensitive.getDefaultValue());
        String optUseDelimited = itemProps.get(ConfigKeys.OptUseDelimited.getConfigKey(), ConfigKeys.OptUseDelimited.getDefaultValue());
        String optIgnoreNonExistStatement = itemProps.get(ConfigKeys.OptIgnoreNonExistStatement.getConfigKey(), ConfigKeys.OptIgnoreNonExistStatement.getDefaultValue());
        String optSqlDialect = itemProps.get(ConfigKeys.OptSqlDialect.getConfigKey(), ConfigKeys.OptSqlDialect.getDefaultValue());

        Options opt = Options.of();
        if (StringUtils.isNotBlank(optAutoMapping)) {
            opt.autoMapping(Boolean.parseBoolean(optAutoMapping));
        }
        if (StringUtils.isNotBlank(optCamelCase)) {
            opt.mapUnderscoreToCamelCase(Boolean.parseBoolean(optCamelCase));
        }
        if (StringUtils.isNotBlank(optCaseInsensitive)) {
            opt.caseInsensitive(Boolean.parseBoolean(optCaseInsensitive));
        }
        if (StringUtils.isNotBlank(optUseDelimited)) {
            opt.useDelimited(Boolean.parseBoolean(optUseDelimited));
        }
        if (StringUtils.isNotBlank(optIgnoreNonExistStatement)) {
            opt.ignoreNonExistStatement(Boolean.parseBoolean(optIgnoreNonExistStatement));
        }
        if (StringUtils.isNotBlank(optSqlDialect)) {
            opt.dialect(SqlDialectRegister.findOrCreate(optSqlDialect, classLoader));
        }
        return opt;
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
}
