/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.nxn.report;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.SupportStatus;
import net.hasor.dbvisitor.test.nxn.env.DataSourceId;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfileRegistry;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

public final class CapabilityMatrixReport {
    private CapabilityMatrixReport() {
    }

    public static Path writeCurrentProfileReport(DataSourceProfile currentProfile) throws Exception {
        Path output = Paths.get("target", "nxn-capability-matrix-" + currentProfile.env() + ".md");
        Files.createDirectories(output.getParent());
        Files.write(output, render(currentProfile).getBytes(StandardCharsets.UTF_8));
        return output;
    }

    public static String render(DataSourceProfile currentProfile) throws Exception {
        List<CapabilityRow> rows = collectRows();
        List<DataSourceProfile> profiles = DataSourceProfileRegistry.all();
        Map<DataSourceId, Map<Class<?>, Class<?>>> realdbBindings = new LinkedHashMap<>();
        for (DataSourceProfile profile : profiles) {
            realdbBindings.put(profile.id(), realdbBindings(profile));
        }

        StringBuilder out = new StringBuilder();
        out.append("# NXN Capability Matrix\n\n");
        out.append("Declared capability bindings and native limits; this report is not a test execution result.\n\n");
        out.append("- Current env: `").append(currentProfile.env()).append("`\n");
        out.append("- Capability rows: `").append(rows.size()).append("`\n");
        out.append("- Datasources: ");
        for (int i = 0; i < profiles.size(); i++) {
            if (i > 0) {
                out.append(", ");
            }
            out.append("`").append(profiles.get(i).env()).append("`");
        }
        out.append("\n\n");
        out.append("| Capability | Contract ");
        for (DataSourceProfile profile : profiles) {
            out.append("| ").append(profile.env()).append(" ");
        }
        out.append("|\n");
        out.append("| --- | --- ");
        for (int i = 0; i < profiles.size(); i++) {
            out.append("| --- ");
        }
        out.append("|\n");
        for (CapabilityRow row : rows) {
            out.append("| `").append(row.capabilityId).append("` ");
            out.append("| `").append(row.contractMethod).append("` ");
            for (DataSourceProfile profile : profiles) {
                out.append("| ").append(renderStatus(profile, row, realdbBindings.get(profile.id()))).append(" ");
            }
            out.append("|\n");
        }
        return out.toString();
    }

    static List<CapabilityRow> collectRows() throws Exception {
        List<CapabilityRow> rows = new ArrayList<>();
        for (Class<?> contractClass : contractClasses()) {
            for (Method method : contractClass.getDeclaredMethods()) {
                if (method.getAnnotation(Test.class) == null) {
                    continue;
                }
                Capability capability = method.getAnnotation(Capability.class);
                if (capability == null) {
                    continue;
                }
                rows.add(new CapabilityRow(capability.value(), contractClass, contractClass.getSimpleName() + "#" + method.getName()));
            }
        }
        for (DataSourceProfile profile : DataSourceProfileRegistry.all()) {
            for (Class<?> realdbClass : realdbClasses(profile)) {
                for (Method method : realdbClass.getDeclaredMethods()) {
                    if (method.getAnnotation(Test.class) == null) {
                        continue;
                    }
                    Capability capability = method.getAnnotation(Capability.class);
                    if (capability == null) {
                        continue;
                    }
                    if (inheritsSameCapability(realdbClass, method, capability)) {
                        continue;
                    }
                    rows.add(new CapabilityRow(capability.value(), realdbClass, realdbClass.getSimpleName() + "#" + method.getName(), profile.id()));
                }
            }
        }
        rows.sort(Comparator.comparing((CapabilityRow row) -> row.capabilityId).thenComparing(row -> row.contractMethod));
        return rows;
    }

    private static boolean inheritsSameCapability(Class<?> realdbClass, Method method, Capability capability) {
        Class<?> contract = nearestContractSuperclass(realdbClass);
        if (contract == null) {
            return false;
        }
        try {
            Capability inherited = contract.getMethod(method.getName(), method.getParameterTypes()).getAnnotation(Capability.class);
            return inherited != null && inherited.value().equals(capability.value());
        } catch (NoSuchMethodException ignored) {
            return false;
        }
    }

    private static String renderStatus(DataSourceProfile profile, CapabilityRow row, Map<Class<?>, Class<?>> realdbBindings) {
        if (row.ownerDataSourceId != null) {
            if (!profile.id().equals(row.ownerDataSourceId)) {
                return "`" + SupportStatus.NOT_IMPLEMENTED + "`";
            }
            return "`" + profile.support(row.capabilityId) + "`<br>`" + row.contractClass.getSimpleName() + "`";
        }
        Class<?> realdbClass = realdbBindings.get(row.contractClass);
        if (realdbClass == null) {
            return "`" + SupportStatus.NOT_IMPLEMENTED + "`";
        }
        return "`" + profile.support(row.capabilityId) + "`<br>`" + realdbClass.getSimpleName() + "`";
    }

    private static Map<Class<?>, Class<?>> realdbBindings(DataSourceProfile profile) throws Exception {
        Map<Class<?>, Class<?>> bindings = new LinkedHashMap<>();
        for (Class<?> realdbClass : realdbClasses(profile)) {
            Class<?> contractClass = nearestContractSuperclass(realdbClass);
            if (contractClass != null) {
                bindings.put(contractClass, realdbClass);
            }
        }
        return bindings;
    }

    private static List<Class<?>> contractClasses() throws Exception {
        List<Class<?>> contracts = classNamesUnder("net/hasor/dbvisitor/test/contract", "", ".java").stream()//
                .map(CapabilityMatrixReport::loadClass)//
                .filter(CapabilityMatrixReport::isNxnContractClass)//
                .collect(Collectors.toList());
        contracts.add(NxnMetadataContractTest.class);
        return contracts;
    }

    private static List<Class<?>> realdbClasses(DataSourceProfile profile) throws Exception {
        List<Class<?>> realdbClasses = classNamesUnder("net/hasor/dbvisitor/test/realdb/" + realdbPackage(profile), "", "Test.java").stream()//
                .map(CapabilityMatrixReport::loadClass)//
                .filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()))//
                .collect(Collectors.toList());
        Class<?> metadataClass = metadataClass(profile);
        if (metadataClass != null) {
            realdbClasses.add(metadataClass);
        }
        return realdbClasses;
    }

    private static List<String> classNamesUnder(String packagePath, String filePrefix, String fileSuffix) throws IOException {
        Path root = sourceRoot();
        Path packageRoot = root.resolve(packagePath);
        try (Stream<Path> stream = Files.walk(packageRoot)) {
            return stream//
                    .filter(Files::isRegularFile)//
                    .filter(path -> path.getFileName().toString().startsWith(filePrefix))//
                    .filter(path -> path.getFileName().toString().endsWith(fileSuffix))//
                    .map(root::relativize)//
                    .map(CapabilityMatrixReport::toClassName)//
                    .sorted()//
                    .collect(Collectors.toList());
        }
    }

    private static Path sourceRoot() {
        for (String candidate : new String[] { "src/test/java", "dbvisitor-test/src/test/java", "dbvisitor/dbvisitor-test/src/test/java" }) {
            Path path = Paths.get(candidate);
            if (Files.isDirectory(path)) {
                return path;
            }
        }
        throw new IllegalStateException("Cannot find dbvisitor-test source root from " + Paths.get("").toAbsolutePath());
    }

    private static String toClassName(Path path) {
        String value = path.toString().replace('\\', '.').replace('/', '.');
        return value.substring(0, value.length() - ".java".length());
    }

    private static Class<?> loadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Cannot load class " + className, e);
        }
    }

    private static Class<?> nearestContractSuperclass(Class<?> realdbClass) {
        Class<?> cursor = realdbClass.getSuperclass();
        while (cursor != null && cursor != Object.class) {
            if (isNxnContractClass(cursor)) {
                return cursor;
            }
            cursor = cursor.getSuperclass();
        }
        return null;
    }

    private static boolean isNxnContractClass(Class<?> clazz) {
        return clazz.getDeclaredAnnotation(NxnContract.class) != null;
    }

    private static String realdbPackage(DataSourceProfile profile) {
        if (DataSourceId.ELASTIC6.equals(profile.id())) {
            return "elastic6";
        }
        if (DataSourceId.ELASTIC7.equals(profile.id())) {
            return "elastic7";
        }
        return profile.env();
    }

    private static Class<?> metadataClass(DataSourceProfile profile) {
        String className;
        if (DataSourceId.H2.equals(profile.id())) {
            className = "H2NxnMetadataContractTest";
        } else if (DataSourceId.MYSQL.equals(profile.id())) {
            className = "MySqlNxnMetadataContractTest";
        } else if (DataSourceId.PG.equals(profile.id())) {
            className = "PostgreSqlNxnMetadataContractTest";
        } else if (DataSourceId.MSSQL.equals(profile.id())) {
            className = "MsSqlNxnMetadataContractTest";
        } else if (DataSourceId.ORACLE.equals(profile.id())) {
            className = "OracleNxnMetadataContractTest";
        } else if (DataSourceId.DB2.equals(profile.id())) {
            className = "Db2NxnMetadataContractTest";
        } else if (DataSourceId.CLICKHOUSE.equals(profile.id())) {
            className = "ClickHouseNxnMetadataContractTest";
        } else if (DataSourceId.REDIS.equals(profile.id())) {
            className = "RedisNxnMetadataContractTest";
        } else if (DataSourceId.MONGO.equals(profile.id())) {
            className = "MongoNxnMetadataContractTest";
        } else if (DataSourceId.ELASTIC6.equals(profile.id())) {
            className = "Elastic6NxnMetadataContractTest";
        } else if (DataSourceId.ELASTIC7.equals(profile.id())) {
            className = "Elastic7NxnMetadataContractTest";
        } else if (DataSourceId.MILVUS.equals(profile.id())) {
            className = "MilvusNxnMetadataContractTest";
        } else {
            return null;
        }
        return loadClass("net.hasor.dbvisitor.test.nxn.report.metadata." + className);
    }

    static final class CapabilityRow {
        final String   capabilityId;
        final Class<?> contractClass;
        final String   contractMethod;
        final DataSourceId ownerDataSourceId;

        CapabilityRow(String capabilityId, Class<?> contractClass, String contractMethod) {
            this(capabilityId, contractClass, contractMethod, null);
        }

        CapabilityRow(String capabilityId, Class<?> contractClass, String contractMethod, DataSourceId ownerDataSourceId) {
            this.capabilityId = capabilityId;
            this.contractClass = contractClass;
            this.contractMethod = contractMethod;
            this.ownerDataSourceId = ownerDataSourceId;
        }
    }
}
