/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.nxn.report;

import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.env.DataSourceId;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfileRegistry;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class NxnMetaTest extends AbstractNxnContractTest {
    @Test
    @Capability(CapabilityId.NXN_METADATA_CAPABILITY_ANNOTATIONS)
    public void nxnMetaDeclareCapabilityOnEveryContractTestMethod() throws Exception {
        List<String> missing = new ArrayList<>();
        for (Class<?> contractClass : contractClasses()) {
            for (Method method : contractClass.getDeclaredMethods()) {
                if (method.getAnnotation(Test.class) != null && method.getAnnotation(Capability.class) == null) {
                    missing.add(contractClass.getName() + "#" + method.getName());
                }
            }
        }
        assertTrue("Every contract @Test method must declare @Capability: " + missing, missing.isEmpty());
    }

    @Test
    @Capability(CapabilityId.NXN_METADATA_REALDB_CONTRACT_BINDINGS)
    public void nxnMetaCurDatasourceRealdbClassesToContracts() throws Exception {
        List<Class<?>> contracts = contractClasses();
        List<Class<?>> realdbClasses = realdbClassesForCurrentProfile();

        assertFalse("Expected contract classes", contracts.isEmpty());
        assertFalse("Expected realdb contract classes for " + profile().env(), realdbClasses.isEmpty());

        Set<Class<?>> contractSet = new HashSet<>(contracts);
        Set<Class<?>> boundContracts = new HashSet<>();
        List<String> invalid = new ArrayList<>();
        List<String> duplicate = new ArrayList<>();
        for (Class<?> realdbClass : realdbClasses) {
            Class<?> contract = nearestContractSuperclass(realdbClass);
            if ((contract == null || !contractSet.contains(contract)) && !hasValidNativeCapabilities(realdbClass)) {
                invalid.add(realdbClass.getName());
                continue;
            }
            if (contract == null || !contractSet.contains(contract)) {
                continue;
            }
            if (!boundContracts.add(contract)) {
                duplicate.add(contract.getName());
            }
        }
        assertTrue("Matrix tests must inherit a known case or declare valid datasource capabilities: " + invalid, invalid.isEmpty());
        assertTrue("A datasource must not bind the same contract more than once: " + duplicate, duplicate.isEmpty());
    }

    @Test
    @Capability(CapabilityId.NXN_METADATA_PROFILE_REGISTRY)
    public void nxnMetaRelationalProfiles() throws SQLException {
        assertSame(DataSourceId.H2, DataSourceProfileRegistry.find(DataSourceId.H2.env()).id());
        assertSame(DataSourceId.MYSQL, DataSourceProfileRegistry.find(DataSourceId.MYSQL.env()).id());
        assertSame(DataSourceId.PG, DataSourceProfileRegistry.find(DataSourceId.PG.env()).id());
        assertSame(DataSourceId.MSSQL, DataSourceProfileRegistry.find(DataSourceId.MSSQL.env()).id());
        assertSame(DataSourceId.ORACLE, DataSourceProfileRegistry.find(DataSourceId.ORACLE.env()).id());
        assertSame(DataSourceId.DB2, DataSourceProfileRegistry.find(DataSourceId.DB2.env()).id());
        assertSame(DataSourceId.CLICKHOUSE, DataSourceProfileRegistry.find(DataSourceId.CLICKHOUSE.env()).id());
        assertSame(DataSourceId.REDIS, DataSourceProfileRegistry.find(DataSourceId.REDIS.env()).id());
        assertSame(DataSourceId.MONGO, DataSourceProfileRegistry.find(DataSourceId.MONGO.env()).id());
        assertSame(DataSourceId.ELASTIC6, DataSourceProfileRegistry.find(DataSourceId.ELASTIC6.env()).id());
        assertSame(DataSourceId.ELASTIC7, DataSourceProfileRegistry.find(DataSourceId.ELASTIC7.env()).id());
        assertSame(DataSourceId.MILVUS, DataSourceProfileRegistry.find(DataSourceId.MILVUS.env()).id());

        DataSourceProfile current = DataSourceProfileRegistry.find(profile().env());
        assertEquals(profile().env(), current.env());
    }

    @Test
    @Capability(CapabilityId.NXN_METADATA_REPORT_GENERATION)
    public void nxnMetaCapabilityMatrixReportForCurDatasource() throws Exception {
        Path report = CapabilityMatrixReport.writeCurrentProfileReport(profile());

        assertTrue("NXN report should be generated: " + report, Files.isRegularFile(report));
        String text = new String(Files.readAllBytes(report), StandardCharsets.UTF_8);
        assertTrue(text.contains("- Current env: `" + profile().env() + "`"));
        assertTrue(text.contains("- Datasources: `h2`, `mysql`, `pg`, `mssql`, `oracle`, `db2`, `clickhouse`, `redis`, `mongo`, `es6`, `es7`, `milvus`"));
        assertTrue(text.contains("| Capability | Contract | h2 | mysql | pg | mssql | oracle | db2 | clickhouse | redis | mongo | es6 | es7 | milvus |"));
        assertTrue(text.contains("`" + CapabilityId.SCHEMA_STANDARD_TABLES + "`"));
        assertTrue(text.contains("`" + CapabilityId.ADAPTER_MONGO_BSON_TYPES + "`"));
        assertTrue(text.contains("`" + CapabilityId.TYPE_ARRAY_JDBC_ARRAY + "`"));
        assertTrue(text.contains("`SUPPORTED`"));
        assertTrue(text.contains("`UNSUPPORTED_BY_DATABASE`"));
        assertTrue(text.contains("`NOT_IMPLEMENTED`"));
    }

    private List<Class<?>> contractClasses() throws Exception {
        List<Class<?>> contracts = classNamesUnder("net/hasor/dbvisitor/test/contract", "", ".java").stream()//
                .map(this::loadClass)//
                .filter(this::isNxnContractClass)//
                .collect(Collectors.toList());
        contracts.add(NxnMetaTest.class);
        return contracts;
    }

    private List<Class<?>> realdbClassesForCurrentProfile() throws Exception {
        List<Class<?>> realdbClasses = classNamesUnder("net/hasor/dbvisitor/test/realdb/" + realdbPackage(), "", "Test.java").stream()//
                .map(this::loadClass)//
                .filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()))//
                .filter(clazz -> nearestContractSuperclass(clazz) != null || hasCapabilityMethods(clazz))//
                .collect(Collectors.toList());
        Class<?> metadataClass = metadataClassForCurrentProfile();
        if (metadataClass != null) {
            realdbClasses.add(metadataClass);
        }
        return realdbClasses;
    }

    private List<String> classNamesUnder(String packagePath, String filePrefix, String fileSuffix) throws Exception {
        Path root = sourceRoot();
        Path packageRoot = root.resolve(packagePath);
        try (Stream<Path> stream = Files.walk(packageRoot)) {
            return stream//
                    .filter(Files::isRegularFile)//
                    .filter(path -> path.getFileName().toString().startsWith(filePrefix))//
                    .filter(path -> path.getFileName().toString().endsWith(fileSuffix))//
                    .map(root::relativize)//
                    .map(this::toClassName)//
                    .sorted()//
                    .collect(Collectors.toList());
        }
    }

    private Path sourceRoot() {
        for (String candidate : new String[] { "src/test/java", "dbvisitor-test/src/test/java", "dbvisitor/dbvisitor-test/src/test/java" }) {
            Path path = Paths.get(candidate);
            if (Files.isDirectory(path)) {
                return path;
            }
        }
        throw new IllegalStateException("Cannot find dbvisitor-test source root from " + Paths.get("").toAbsolutePath());
    }

    private String toClassName(Path path) {
        String value = path.toString().replace('\\', '.').replace('/', '.');
        return value.substring(0, value.length() - ".java".length());
    }

    private Class<?> loadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Cannot load class " + className, e);
        }
    }

    private Class<?> nearestContractSuperclass(Class<?> realdbClass) {
        Class<?> cursor = realdbClass.getSuperclass();
        while (cursor != null && cursor != Object.class) {
            if (isNxnContractClass(cursor)) {
                return cursor;
            }
            cursor = cursor.getSuperclass();
        }
        return null;
    }

    private boolean isNxnContractClass(Class<?> clazz) {
        return clazz.getDeclaredAnnotation(NxnContract.class) != null;
    }

    private boolean hasCapabilityMethods(Class<?> realdbClass) {
        for (Method method : realdbClass.getMethods()) {
            if (method.getAnnotation(Test.class) != null && method.getAnnotation(Capability.class) != null) {
                return true;
            }
        }
        return false;
    }

    private boolean hasValidNativeCapabilities(Class<?> realdbClass) throws Exception {
        Set<String> knownCapabilities = new HashSet<>();
        for (Field field : CapabilityId.class.getFields()) {
            if (field.getType() == String.class) {
                knownCapabilities.add((String) field.get(null));
            }
        }
        String adapterName = profile().env();
        if (profile().id() == DataSourceId.ELASTIC6 || profile().id() == DataSourceId.ELASTIC7) {
            adapterName = "elastic";
        }
        String ownerPrefix = "adapter." + adapterName + ".";
        boolean hasTest = false;
        for (Method method : realdbClass.getMethods()) {
            if (method.getAnnotation(Test.class) == null) {
                continue;
            }
            hasTest = true;
            Capability capability = method.getAnnotation(Capability.class);
            if (capability == null || !knownCapabilities.contains(capability.value())) {
                return false;
            }
            if (capability.value().startsWith("adapter.") && !capability.value().startsWith(ownerPrefix)
                    && !isSharedCapability(capability.value())) {
                return false;
            }
        }
        return hasTest;
    }

    private boolean isSharedCapability(String capabilityId) throws Exception {
        for (Class<?> contract : contractClasses()) {
            for (Method method : contract.getDeclaredMethods()) {
                Capability capability = method.getAnnotation(Capability.class);
                if (method.getAnnotation(Test.class) != null && capability != null && capabilityId.equals(capability.value())) {
                    return true;
                }
            }
        }
        return false;
    }

    private String realdbPackage() {
        if (DataSourceId.H2.env().equals(profile().env())) {
            return "h2";
        }
        if (DataSourceId.MYSQL.env().equals(profile().env())) {
            return "mysql";
        }
        if (DataSourceId.PG.env().equals(profile().env())) {
            return "pg";
        }
        if (DataSourceId.ELASTIC6.env().equals(profile().env())) {
            return "elastic6";
        }
        if (DataSourceId.ELASTIC7.env().equals(profile().env())) {
            return "elastic7";
        }
        return profile().env();
    }

    private Class<?> metadataClassForCurrentProfile() {
        String className;
        if (DataSourceId.H2.env().equals(profile().env())) {
            className = "H2NxnMetadataContractTest";
        } else if (DataSourceId.MYSQL.env().equals(profile().env())) {
            className = "MySqlNxnMetadataContractTest";
        } else if (DataSourceId.PG.env().equals(profile().env())) {
            className = "PostgreSqlNxnMetadataContractTest";
        } else if (DataSourceId.MSSQL.env().equals(profile().env())) {
            className = "MsSqlNxnMetadataContractTest";
        } else if (DataSourceId.ORACLE.env().equals(profile().env())) {
            className = "OracleNxnMetadataContractTest";
        } else if (DataSourceId.DB2.env().equals(profile().env())) {
            className = "Db2NxnMetadataContractTest";
        } else if (DataSourceId.CLICKHOUSE.env().equals(profile().env())) {
            className = "ClickHouseNxnMetadataContractTest";
        } else if (DataSourceId.REDIS.env().equals(profile().env())) {
            className = "RedisNxnMetadataContractTest";
        } else {
            return null;
        }
        return loadClass("net.hasor.dbvisitor.test.nxn.report.metadata." + className);
    }
}
