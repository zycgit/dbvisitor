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
package net.hasor.dbvisitor.mapping;
import net.hasor.dbvisitor.dialect.SqlDialect;

/**
 * <resultMap> or <mapper>
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2021-06-21
 */
public class MappingOptions {
    private String     catalog;
    private String     schema;
    private Boolean    autoMapping;
    private Boolean    mapUnderscoreToCamelCase;
    private Boolean    caseInsensitive;
    private Boolean    useDelimited;
    private SqlDialect defaultDialect;

    public MappingOptions() {
    }

    public MappingOptions(MappingOptions options) {
        if (options != null) {
            this.catalog = options.catalog;
            this.schema = options.schema;
            this.autoMapping = options.autoMapping;
            this.mapUnderscoreToCamelCase = options.mapUnderscoreToCamelCase;
            this.caseInsensitive = options.caseInsensitive;
            this.useDelimited = options.useDelimited;
            this.defaultDialect = options.defaultDialect;
        }
    }

    @Override
    public String toString() {
        String dialect = defaultDialect == null ? null : defaultDialect.getClass().getName();
        String key = this.autoMapping + "," + this.mapUnderscoreToCamelCase + "," + this.caseInsensitive + "," + this.useDelimited + "," + dialect;
        return "MappingOptions[" + key + "]";
    }

    public String getCatalog() {
        return this.catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public MappingOptions catalog(String catalog) {
        setCatalog(catalog);
        return this;
    }

    public String getSchema() {
        return this.schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public MappingOptions schema(String schema) {
        setSchema(schema);
        return this;
    }

    public Boolean getAutoMapping() {
        return this.autoMapping;
    }

    public void setAutoMapping(Boolean autoMapping) {
        this.autoMapping = autoMapping;
    }

    public MappingOptions autoMapping(Boolean autoMapping) {
        setAutoMapping(autoMapping);
        return this;
    }

    public Boolean getMapUnderscoreToCamelCase() {
        return this.mapUnderscoreToCamelCase;
    }

    public void setMapUnderscoreToCamelCase(Boolean mapUnderscoreToCamelCase) {
        this.mapUnderscoreToCamelCase = mapUnderscoreToCamelCase;
    }

    public MappingOptions mapUnderscoreToCamelCase(Boolean mapUnderscoreToCamelCase) {
        setMapUnderscoreToCamelCase(mapUnderscoreToCamelCase);
        return this;
    }

    public Boolean getCaseInsensitive() {
        return this.caseInsensitive;
    }

    public void setCaseInsensitive(Boolean caseInsensitive) {
        this.caseInsensitive = caseInsensitive;
    }

    public MappingOptions caseInsensitive(Boolean caseInsensitive) {
        setCaseInsensitive(caseInsensitive);
        return this;
    }

    public SqlDialect getDefaultDialect() {
        return this.defaultDialect;
    }

    public void setDefaultDialect(SqlDialect defaultDialect) {
        this.defaultDialect = defaultDialect;
    }

    public MappingOptions defaultDialect(SqlDialect defaultDialect) {
        setDefaultDialect(defaultDialect);
        return this;
    }

    public Boolean getUseDelimited() {
        return this.useDelimited;
    }

    public void setUseDelimited(Boolean useDelimited) {
        this.useDelimited = useDelimited;
    }

    public MappingOptions defaultDelimited(Boolean useDelimited) {
        setUseDelimited(useDelimited);
        return this;
    }

    public static MappingOptions buildNew() {
        return new MappingOptions();
    }

    public static MappingOptions buildNew(MappingOptions options) {
        return new MappingOptions(options);
    }

    //MappingDefault
    //
    //    private static void fetchPackageInfo(final Map<String, String> confData, Class<?> matchType, final ClassLoader classLoader, final String className) {
    //        if (StringUtils.isBlank(className)) {
    //            return;
    //        }
    //
    //        String packageName = StringUtils.substringBeforeLast(className, ".");
    //
    //        for (; ; ) {
    //            fetchEntityInfo(confData, matchType, classLoader, packageName + ".package-info");
    //            if (!confData.isEmpty()) {
    //                break;
    //            }
    //            if (packageName.indexOf('.') == -1) {
    //                break;
    //            }
    //            packageName = StringUtils.substringBeforeLast(packageName, ".");
    //            if (StringUtils.isBlank(packageName)) {
    //                break;
    //            }
    //        }
    //    }
    //
    //    static boolean fetchEntityInfo(final Map<String, String> confData, Class<?> matchType, final ClassLoader classLoader, final String className) {
    //        if (StringUtils.isBlank(className)) {
    //            return false;
    //        }
    //
    //        String packageName = className.replace(".", "/");
    //        InputStream asStream = classLoader.getResourceAsStream(packageName + ".class");
    //        if (asStream == null) {
    //            return false;
    //        }
    //
    //        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
    //        try {
    //            ClassReader classReader = new ClassReader(asStream);
    //            classReader.accept(new ClassVisitor(Opcodes.ASM9) {
    //                public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
    //                    if (!AsmTools.toAsmType(matchType).equals(desc)) {
    //                        return super.visitAnnotation(desc, visible);
    //                    }
    //                    atomicBoolean.set(true);
    //                    return new TableDefaultVisitor(Opcodes.ASM9, super.visitAnnotation(desc, visible), confData);
    //                }
    //            }, ClassReader.SKIP_CODE);
    //        } catch (Exception e) {
    //            logger.error(e.getMessage(), e);
    //        }
    //        return atomicBoolean.get();
    //    }
}