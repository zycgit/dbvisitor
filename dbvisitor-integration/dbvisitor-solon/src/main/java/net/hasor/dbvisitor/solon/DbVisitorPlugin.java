package net.hasor.dbvisitor.solon;
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
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import org.noear.solon.Solon;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.BeanWrap;
import org.noear.solon.core.Plugin;
import org.noear.solon.core.Props;

import javax.sql.DataSource;
import java.net.URI;
import java.util.*;

/**
 * @author noear
 * @since 1.8
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
            if (mapperWrapMap.containsKey(bw.name())) {
                loadCoreInject(context, bw, mapperWrapMap, ambiguousMapper);
            }
        });
    }

    private void loadCoreInject(AppContext context, BeanWrap dsBw, Map<String, MapperWrap> wrapMap, Set<Class<?>> ambiguousMapper) {
        DataSource ds = dsBw.get();
        MapperWrap mapperWrap = wrapMap.get(dsBw.name());

        // basic api for this DataSource.
        context.beanInjectorAdd(Db.class, new DbVisitorInjector(wrapMap));

        // mapper for this DataSource.
        try {
            Session session = mapperWrap.getConf().newSession(ds);
            for (Class<?> mapperType : mapperWrap.getMapperType()) {
                if (ambiguousMapper.contains(mapperType)) {
                    continue;
                }

                Object mapper = session.createMapper(mapperType);
                BeanWrap beanWrap = Solon.context().wrap(mapperType.getSimpleName(), mapper);
                beanWrap.singletonSet(mapperWrap.isSingleton(mapperType));
                context.putWrap(mapperType, beanWrap);
            }
        } catch (Exception e) {
            throw ExceptionUtils.toRuntime(e);
        }

        //ambiguousMapper using default.
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
        String mapperScope = itemProps.get(ConfigKeys.MapperScope.getConfigKey(), ConfigKeys.MapperScope.getDefaultValue());

        boolean useSingleton = StringUtils.equalsIgnoreCase(mapperScope, "singleton");
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
            mapperWrap.addMapper(mapperType, useSingleton);
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
            opt.defaultDialect(SqlDialectRegister.findOrCreate(optSqlDialect, classLoader));
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
