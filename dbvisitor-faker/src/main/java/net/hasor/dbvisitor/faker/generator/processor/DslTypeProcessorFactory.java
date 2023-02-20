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
package net.hasor.dbvisitor.faker.generator.processor;
import net.hasor.cobble.BeanUtils;
import net.hasor.cobble.CollectionUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.convert.ConverterUtils;
import net.hasor.cobble.io.input.AutoCloseInputStream;
import net.hasor.cobble.loader.ResourceLoader.MatchType;
import net.hasor.cobble.loader.providers.ClassPathResourceLoader;
import net.hasor.cobble.ref.LinkedCaseInsensitiveMap;
import net.hasor.cobble.setting.SettingNode;
import net.hasor.dbvisitor.JdbcUtils;
import net.hasor.dbvisitor.faker.FakerConfig;
import net.hasor.dbvisitor.faker.dsl.TypeProcessConf;
import net.hasor.dbvisitor.faker.dsl.TypeProcessConfSet;
import net.hasor.dbvisitor.faker.dsl.model.ValueModel;
import net.hasor.dbvisitor.faker.generator.TypeProcessor;
import net.hasor.dbvisitor.faker.generator.parameter.ParameterProcessor;
import net.hasor.dbvisitor.faker.generator.parameter.ParameterRegistry;
import net.hasor.dbvisitor.faker.meta.JdbcColumn;
import net.hasor.dbvisitor.faker.seed.SeedConfig;
import net.hasor.dbvisitor.faker.seed.SeedFactory;
import net.hasor.dbvisitor.faker.seed.SeedType;
import net.hasor.dbvisitor.faker.seed.array.ArraySeedConfig;
import net.hasor.dbvisitor.faker.seed.array.ArraySeedFactory;
import net.hasor.dbvisitor.types.TypeHandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.JDBCType;
import java.sql.Types;
import java.util.*;

/**
 * 读取并解析 tpc 配置文件，并根据类型和数据库信息选择对应的 tpc 配置。利用 tpc 的配置信息来创建 TypeProcessor。
 * @version : 2023-02-14
 * @author 赵永春 (zyc@hasor.net)
 */
public class DslTypeProcessorFactory extends DefaultTypeProcessorFactory {
    private final List<String>                                    innerParameter = Arrays.asList(//
            "seedType", "jdbcType", "arrayMinSize", "arrayMaxSize", "arrayTypeHandler", "arrayDimension");
    private final String                                          dbType;
    private final Map<String, Object>                             globalVariables;
    private final Map<String, Map<String, List<TypeProcessConf>>> colTypeConfig;
    private final Map<String, String>                             colTypeThrow;
    private final FakerConfig                                     fakerConfig;

    public DslTypeProcessorFactory(String dbType, Map<String, Object> globalVariables, FakerConfig fakerConfig) throws IOException {
        this.dbType = dbType;
        this.globalVariables = globalVariables;
        this.colTypeConfig = new LinkedCaseInsensitiveMap<>();
        this.colTypeThrow = new LinkedCaseInsensitiveMap<>();
        this.fakerConfig = fakerConfig;

        this.initTypeProcessorPriority(dbType, fakerConfig);
    }

    protected void initTypeProcessorPriority(String dbType, FakerConfig config) throws IOException {
        // all tpcConf
        ClassLoader classLoader = config.getClassLoader();
        List<URI> uriList = new ClassPathResourceLoader(classLoader).scanResources(MatchType.Prefix, event -> {
            URI resource = event.getResource();
            if (StringUtils.endsWithIgnoreCase(resource.toString(), ".tpc")) {
                return resource;
            } else {
                return null;
            }
        }, new String[] { "META-INF/faker-dbtpc/" });

        // read tpcConf
        List<TypeProcessConfSet> allTpcConf = new ArrayList<>();
        for (URI uri : uriList) {
            InputStream tpcStream = new AutoCloseInputStream(uri.toURL().openStream());
            TypeProcessConfSet dslConf = TypeProcessConfSet.parserTypeProcessConf(tpcStream, StandardCharsets.UTF_8);
            dslConf.setSource(uri);
            if (StringUtils.equalsIgnoreCase(dbType, dslConf.getDatabaseType())) {
                allTpcConf.add(dslConf);
            }
        }

        // sort tpcConf by priority
        allTpcConf.sort((o1, o2) -> {
            ValueModel o1Priority = (ValueModel) o1.getDefConfig("priority");
            ValueModel o2Priority = (ValueModel) o2.getDefConfig("priority");
            int o1Int = o1Priority == null ? 0 : (int) ConverterUtils.convert(Integer.TYPE, o1Priority.recover(globalVariables));
            int o2Int = o2Priority == null ? 0 : (int) ConverterUtils.convert(Integer.TYPE, o2Priority.recover(globalVariables));
            return -Integer.compare(o1Int, o2Int);
        });

        // found final TypeProcessConfSet
        TypeProcessConfSet useConfSet = null;
        for (TypeProcessConfSet confSet : allTpcConf) {
            ValueModel policyValue = (ValueModel) confSet.getDefConfig("policy");
            Object policyName = policyValue == null ? null : policyValue.recover(globalVariables);
            if (policyName == null) {
                continue;
            }

            if (StringUtils.equalsIgnoreCase(policyName.toString(), config.getPolicy())) {
                useConfSet = confSet;
                break;
            }
        }
        if (useConfSet == null) {
            for (TypeProcessConfSet confSet : allTpcConf) {
                ValueModel defaultValue = (ValueModel) confSet.getDefConfig("default");
                Object isDefault = defaultValue == null ? null : defaultValue.recover(globalVariables);
                if (isDefault == null) {
                    continue;
                }

                if ((boolean) ConverterUtils.convert(Boolean.TYPE, isDefault)) {
                    useConfSet = confSet;
                    break;
                }
            }
        }

        // init columnConf
        if (useConfSet != null) {
            logger.info("DSL TypeProcessor use '" + useConfSet.getSource() + "'");
            for (String colType : useConfSet.getConfigKeys()) {
                List<TypeProcessConf> colConfList = useConfSet.getConfig(colType);
                if (colConfList == null || colConfList.isEmpty()) {
                    continue;
                }

                Map<String, List<TypeProcessConf>> typeConfMap = this.colTypeConfig.computeIfAbsent(colType, s -> new LinkedHashMap<>());
                for (TypeProcessConf confItem : colConfList) {
                    typeConfMap.computeIfAbsent(confItem.getConfName(), s -> new ArrayList<>()).add(confItem);
                }

            }

            for (String colType : useConfSet.getThrowKeys()) {
                String throwMessage = useConfSet.getThrow(colType);
                if (throwMessage != null) {
                    this.colTypeThrow.put(colType, throwMessage);
                }
            }
        }
    }

    public TypeProcessor createSeedFactory(JdbcColumn jdbcColumn, SettingNode columnConfig) throws ReflectiveOperationException {
        String colType = jdbcColumn.getColumnType();

        if (JdbcUtils.ORACLE.equalsIgnoreCase(this.dbType)) {
            colType = colType.replaceAll("\\(\\d+\\)", "");
        }
        if (JdbcUtils.POSTGRESQL.equalsIgnoreCase(this.dbType)) {
            while (colType.length() > 0 && colType.charAt(0) == '_') {
                colType = colType.substring(1);
            }
        }

        // need throw
        if (this.colTypeThrow.containsKey(colType)) {
            String throwMessage = this.colTypeThrow.get(colType);
            throw new UnsupportedOperationException("unsupported columnName " + jdbcColumn.getColumnName()//
                    + ", columnType '" + colType + "', msg is " + throwMessage);
        }

        // use default
        if (!this.colTypeConfig.containsKey(colType)) {
            return super.defaultSeedFactory(jdbcColumn);
        }

        Map<String, Object> variables = new HashMap<>(this.globalVariables);
        BeanUtils.copyProperties(variables, jdbcColumn);

        // create and config
        Map<String, List<TypeProcessConf>> typeConfMap = this.colTypeConfig.get(colType);
        SeedFactory<? extends SeedConfig> seedFactory = this.createSeedFactory(colType, typeConfMap, variables);
        SeedConfig seedConfig = seedFactory.newConfig();
        TypeProcessor typeProcessor = createTypeProcessor(colType, jdbcColumn, columnConfig, typeConfMap, seedFactory, seedConfig, variables);

        this.applyConfigSet(jdbcColumn, columnConfig, seedConfig, typeProcessor, typeConfMap, variables);
        return typeProcessor;
    }

    protected SeedFactory<? extends SeedConfig> createSeedFactory(String colType, Map<String, List<TypeProcessConf>> typeConfMap, //
            Map<String, Object> variables) throws ReflectiveOperationException {
        TypeProcessConf seedTypeConf = findOne("seedType", typeConfMap);
        if (seedTypeConf == null) {
            throw new IllegalArgumentException("columnType '" + colType + "' missing parameter seedType.");
        }

        Object seedType = seedTypeConf.recover(variables, "recover colType [" + colType + "]");
        if (seedType == null || StringUtils.isBlank(seedType.toString())) {
            throw new IllegalArgumentException("columnType '" + colType + "', the seedType parameter is incorrect.");
        }

        String seedTypeStr = seedType.toString();
        SeedType seedTypeEnum = SeedType.valueOfCode(seedTypeStr);
        if (seedTypeEnum != null) {
            return seedTypeEnum.newFactory();
        }

        Class<?> seedFactoryType = this.fakerConfig.getClassLoader().loadClass(seedTypeStr);
        return (SeedFactory) seedFactoryType.newInstance();
    }

    protected TypeProcessor createTypeProcessor(String colType, JdbcColumn colMeta, SettingNode colSetting, Map<String, List<TypeProcessConf>> typeConfMap,//
            SeedFactory<? extends SeedConfig> seedFactory, SeedConfig seedConfig, Map<String, Object> variables) throws ReflectiveOperationException {
        TypeProcessConf jdbcTypeConf = findOne("jdbcType", typeConfMap);
        if (jdbcTypeConf == null) {
            throw new IllegalArgumentException("columnType '" + colType + "' missing parameter jdbcType.");
        }

        Object jdbcType = jdbcTypeConf.recover(variables, "recover colType [" + colType + "]");
        if (jdbcType == null || StringUtils.isBlank(jdbcType.toString())) {
            throw new IllegalArgumentException("columnType '" + colType + "', the jdbcType parameter is incorrect.");
        }

        int jdbcTypeInt = Types.OTHER;
        for (JDBCType jdbcTypeEnum : JDBCType.values()) {
            if (StringUtils.equalsIgnoreCase(jdbcTypeEnum.name(), jdbcType.toString())) {
                jdbcTypeInt = jdbcTypeEnum.getVendorTypeNumber();
                break;
            }
        }

        //
        TypeProcessConf arrayDimensionConf = findOne("arrayDimension", typeConfMap);
        Object arrayDimension = arrayDimensionConf == null ? 0 : arrayDimensionConf.recover(variables, "recover colType [" + colType + "]");
        int arrayDimensionInt = (int) ConverterUtils.convert(Integer.TYPE, arrayDimension);
        if (arrayDimensionInt == 0) {
            return new TypeProcessor(seedFactory, seedConfig, jdbcTypeInt);
        } else {
            return this.createArrayTypeProcessor(arrayDimensionInt, colType, colMeta, colSetting, typeConfMap, seedFactory, seedConfig, variables);
        }
    }

    // maybe is array
    protected TypeProcessor createArrayTypeProcessor(int arrayDimension, String colType, JdbcColumn colMeta, SettingNode colSetting, Map<String, List<TypeProcessConf>> typeConfMap,//
            SeedFactory<? extends SeedConfig> seedFactory, SeedConfig seedConfig, Map<String, Object> variables) throws ReflectiveOperationException {
        if (arrayDimension > 1) {
            throw new UnsupportedOperationException("colType is " + colType + ", multi-dimensional arrays are not supported.");
        }

        // create array
        TypeProcessConf arrayMinSizeConf = findOne("arrayMinSize", typeConfMap);
        TypeProcessConf arrayMaxSizeConf = findOne("arrayMaxSize", typeConfMap);
        TypeProcessConf arrayTypeHandlerConf = findOne("arrayTypeHandler", typeConfMap);

        Object arrayMinSize = arrayMinSizeConf == null ? 0 : arrayMinSizeConf.recover(variables, "recover colType [" + colType + "]");
        Object arrayMaxSize = arrayMaxSizeConf == null ? 10 : arrayMaxSizeConf.recover(variables, "recover colType [" + colType + "]");
        Object arrayTypeHandler = arrayTypeHandlerConf == null ? null : arrayTypeHandlerConf.recover(variables, "recover colType [" + colType + "]");

        int arrayMinSizeInt = (int) ConverterUtils.convert(Integer.TYPE, arrayMinSize);
        int arrayMaxSizeInt = (int) ConverterUtils.convert(Integer.TYPE, arrayMaxSize);

        ArraySeedFactory arrayFactory = new ArraySeedFactory(seedFactory);
        ArraySeedConfig arrayConfig = new ArraySeedConfig(seedConfig);
        arrayConfig.setMinSize(arrayMinSizeInt);
        arrayConfig.setMaxSize(arrayMaxSizeInt);

        if (arrayTypeHandler != null && StringUtils.isNotBlank(arrayTypeHandler.toString())) {
            ClassLoader classLoader = this.fakerConfig.getClassLoader();
            Class<?> typeHandlerType = classLoader.loadClass(arrayTypeHandler.toString());
            if (TypeHandler.class.isAssignableFrom(typeHandlerType)) {
                TypeHandler<?> instance = (TypeHandler<?>) typeHandlerType.newInstance();
                arrayConfig.setTypeHandler(instance);
            } else if (TypeHandlerFactory.class.isAssignableFrom(typeHandlerType)) {
                TypeHandlerFactory instance = (TypeHandlerFactory) typeHandlerType.newInstance();
                arrayConfig.setTypeHandler(instance.createTypeHandler(variables));
            } else {
                throw new UnsupportedOperationException("type '" + arrayTypeHandler + "' Unsupported.");
            }
        }

        return new TypeProcessor(arrayFactory, arrayConfig, Types.ARRAY);
    }

    protected static TypeProcessConf findOne(String name, Map<String, List<TypeProcessConf>> typeConfMap) {
        List<TypeProcessConf> tpcList = typeConfMap.get(name);
        if (CollectionUtils.isEmpty(tpcList)) {
            return null;
        } else {
            return tpcList.get(0);
        }
    }

    private void applyConfigSet(JdbcColumn jdbcColumn, SettingNode columnConfig, SeedConfig seedConfig, //
            TypeProcessor typeProcessor, Map<String, List<TypeProcessConf>> typeConfMap, Map<String, Object> variables) throws ReflectiveOperationException {

        List<TypeProcessConf> allConf = new ArrayList<>();
        for (Map.Entry<String, List<TypeProcessConf>> entry : typeConfMap.entrySet()) {
            if (!innerParameter.contains(entry.getKey())) {
                allConf.addAll(entry.getValue());
            }
        }

        for (TypeProcessConf conf : allConf) {
            applyConfig(jdbcColumn, columnConfig, seedConfig, typeProcessor, conf, variables);
        }
    }

    private void applyConfig(JdbcColumn colMeta, SettingNode colSetting, SeedConfig seedConfig, //
            TypeProcessor typeProcessor, TypeProcessConf confItem, Map<String, Object> variables) throws ReflectiveOperationException {
        String columnName = colMeta.getColumnName();
        String confName = confItem.getConfName();
        Object parameter = confItem.recover(variables, "recover column [" + columnName + "]");

        variables.put("@@" + confName, parameter);

        // customize
        ParameterProcessor processor = ParameterRegistry.DEFAULT.findByName(confName, seedConfig.getClass());
        if (processor != null) {
            processor.processor(this.fakerConfig, colMeta, colSetting, seedConfig, typeProcessor, confItem.isUseAppend(), parameter);
            return;
        }

        boolean write = BeanUtils.writeProperty(seedConfig, confName, parameter);
        if (!write) {
            logger.warn("column '" + columnName + "' applyConfig '" + confName + "' failed.");
        }
    }
}
