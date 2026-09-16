/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.unit.nxn;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import net.hasor.dbvisitor.test.nxn.report.NxnDocumentation;
import net.hasor.dbvisitor.test.nxn.report.NxnTestResult;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public class NxnDocumentationTest {
    private static final Gson            JSON         = new Gson();
    @Rule
    public               TemporaryFolder temporary    = new TemporaryFolder();
    private              Path            documents;
    private              Path            runDirectory;
    private final        List<Path>      observations = new ArrayList<>();

    @Before
    public void prepareSyntheticRun() throws Exception {
        documents = temporary.newFolder("capabilities").toPath();
        Path original = Path.of("../dbvisitor-doc/src/data/capabilities");
        try (Stream<Path> files = Files.walk(original)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".json")).toList()) {
                Path target = documents.resolve(original.relativize(file));
                Files.createDirectories(target.getParent());
                Files.copy(file, target);
            }
        }
        runDirectory = temporary.newFolder("run").toPath();
        Files.createDirectory(runDirectory.resolve("cases"));
        JsonObject summary = new JsonObject();
        summary.addProperty("runId", "synthetic-unit-run");
        summary.addProperty("env", "h2");
        summary.addProperty("tests", 1000);
        summary.addProperty("failed", 0);
        summary.addProperty("filtered", false);
        summary.addProperty("complete", true);
        write(runDirectory.resolve("run.json"), summary);

        // Synthetic observations test aggregation only; no database results are published here.
        String fingerprint = NxnTestResult.fingerprint();
        Path sourceRoot = Path.of("src/test/java");
        try (Stream<Path> files = Files.walk(sourceRoot.resolve("net/hasor/dbvisitor/test/realdb/h2"))) {
            for (Path file : files.filter(path -> path.toString().endsWith("Test.java")).sorted().toList()) {
                String name = sourceRoot.relativize(file).toString().replace('/', '.').replace('\\', '.').replaceFirst("\\.java$", "");
                Class<?> implementation = Class.forName(name);
                Class<?> contract = implementation.getSuperclass();
                while (contract != null && contract.getDeclaredAnnotation(NxnContract.class) == null) {
                    contract = contract.getSuperclass();
                }
                if (contract == null || contract.getDeclaredAnnotation(NxnContract.class).scope() != NxnContract.Scope.COMPATIBILITY) {
                    continue;
                }
                for (Method method : contract.getDeclaredMethods()) {
                    if (method.getAnnotation(Test.class) == null) {
                        continue;
                    }
                    NxnTestResult result = new NxnTestResult();
                    result.runId = "synthetic-unit-run";
                    result.env = "h2";
                    result.fingerprint = fingerprint;
                    result.testClass = name;
                    result.method = method.getName();
                    result.capability = method.getAnnotation(Capability.class).value();
                    // H2 has no vector backend; keep its bound and unbound vector contracts consistent.
                    result.outcome = result.capability.startsWith("vector.") ? "unsupported" : "passed";
                    if ("unsupported".equals(result.outcome)) {
                        result.reason = "Synthetic H2 vector limitation";
                    }
                    Path output = runDirectory.resolve("cases/" + observations.size() + ".json");
                    write(output, result);
                    observations.add(output);
                }
            }
        }
    }

    @Test
    public void checksAllSourceBindingsAndCountsMethodsOnce() throws Exception {
        Map<Path, String> before = documentContents();
        new NxnDocumentation(documents).update("h2", runDirectory);
        JsonObject h2 = read(documents.resolve("datasources/h2.json")).getAsJsonObject("capabilities");
        JsonObject query = h2.getAsJsonObject("jdbc/queries/queries");
        assertEquals(16, query.get("total").getAsInt());
        assertEquals("supported", query.get("status").getAsString());
        assertEquals(10, h2.getAsJsonObject("types/array-handlers/arrays").get("total").getAsInt());
        assertFalse(h2.has("types/array-handlers/null-round-trips"));
        for (Map.Entry<Path, String> original : before.entrySet()) {
            if (!original.getKey().equals(documents.resolve("datasources/h2.json"))) {
                assertEquals("Changed table, index or another datasource: " + original.getKey(), original.getValue(), Files.readString(original.getKey()));
            }
        }
        assertTrue(Files.isRegularFile(runDirectory.resolve("compatibility.json")));
    }

    @Test
    public void mergedCapabilitiesRetainAllTheirScenarios() throws Exception {
        new NxnDocumentation(documents).update("h2", runDirectory);
        JsonObject results = read(documents.resolve("datasources/h2.json")).getAsJsonObject("capabilities");
        assertEquals(9, results.getAsJsonObject("jdbc/batch-operations/batch").get("total").getAsInt());
        assertEquals(7, results.getAsJsonObject("mapper-files/pagination/file-pagination").get("total").getAsInt());
        assertEquals(11, results.getAsJsonObject("types/json-serialization-handlers/conversion").get("total").getAsInt());
        assertEquals(11, results.getAsJsonObject("types/basic-types/values").get("total").getAsInt());
        assertEquals(18, results.getAsJsonObject("types/dates-and-times/values").get("total").getAsInt());
        assertEquals(7, results.getAsJsonObject("types/enum-handlers/enums").get("total").getAsInt());
        assertFalse(results.has("types/custom-type-handlers/named-columns"));
        int typeColumns = 0;
        int typeScenarios = 0;
        for (var entry : results.entrySet()) {
            if (entry.getKey().startsWith("types/")) {
                typeColumns++;
                typeScenarios += entry.getValue().getAsJsonObject().get("total").getAsInt();
            }
        }
        assertEquals(5, results.getAsJsonObject("vectors/vectors/vector-mapping").get("total").getAsInt());
        assertFalse(results.has("types/vector-handlers/mapping"));
        assertEquals(4, results.keySet().stream().filter(key -> key.startsWith("vectors/")).count());
        assertEquals(7, typeColumns);
        assertEquals(68, typeScenarios);
        assertEquals(12, results.getAsJsonObject("mapping-keys/table-mapping/fields").get("total").getAsInt());
        assertEquals(10, results.getAsJsonObject("mapping-keys/naming-and-case-sensitivity/identifiers").get("total").getAsInt());
        assertEquals(7, results.getAsJsonObject("mapping-keys/write-policies/fields").get("total").getAsInt());
        assertEquals(19, results.getAsJsonObject("mapping-keys/key-generators/strategies").get("total").getAsInt());
        assertEquals(8, results.keySet().stream().filter(key -> key.startsWith("mapping-keys/")).count());
        assertEquals(4, results.getAsJsonObject("results/rowmapper/mapping").get("total").getAsInt());
        assertEquals(4, results.getAsJsonObject("results/resultsetextractor/extraction").get("total").getAsInt());

        JsonObject columns = read(runDirectory.resolve("compatibility.json")).getAsJsonObject("columns");
        java.util.Set<String> methods = new java.util.HashSet<>();
        for (var entry : columns.entrySet()) {
            JsonObject result = results.getAsJsonObject(entry.getKey());
            int expectedTotal = result.has("variants") ? result.getAsJsonObject("variants").size() : entry.getValue().getAsJsonArray().size();
            assertEquals(expectedTotal, result.get("total").getAsInt());
            for (var test : entry.getValue().getAsJsonArray()) {
                assertTrue("Method counted more than once", methods.add(test.getAsJsonObject().get("contract").getAsString()));
            }
        }
    }

    @Test
    public void mapperFilesGroupByCapabilityWithoutLosingScenarios() throws Exception {
        new NxnDocumentation(documents).update("h2", runDirectory);
        JsonObject results = read(documents.resolve("datasources/h2.json")).getAsJsonObject("capabilities");
        Map<String, Integer> expected = Map.of("statements/execution", 11, "statements/options", 6, "statements/key-strategies", 7, "statements/callable", 6, "statements/fragments", 4, "dynamic-sql-and-result-mapping/dynamic-commands", 8, "dynamic-sql-and-result-mapping/resultmap", 13, "pagination/file-pagination", 7, "external-mapper-references/calls", 6);
        assertEquals(expected.size(), results.keySet().stream().filter(key -> key.startsWith("mapper-files/")).count());
        for (var entry : expected.entrySet()) {
            assertEquals(entry.getKey(), entry.getValue().intValue(), results.getAsJsonObject("mapper-files/" + entry.getKey()).get("total").getAsInt());
        }
        assertEquals(68, expected.values().stream().mapToInt(Integer::intValue).sum());
        assertFalse(results.has("mapper-files/session-calls/query-results"));
        assertFalse(results.has("mapper-files/statement-tags-queries-and-writes/query-results"));

        JsonObject columns = read(runDirectory.resolve("compatibility.json")).getAsJsonObject("columns");
        String execution = columns.getAsJsonArray("mapper-files/statements/execution").toString();
        String mapping = columns.getAsJsonArray("mapper-files/dynamic-sql-and-result-mapping/resultmap").toString();
        assertTrue(execution.contains("xmlMapperLoad_shouldExposeMappedStatement"));
        assertTrue(execution.contains("xmlMapperNamespace_shouldResolveStatementsAcrossMultipleLoadedMappers"));
        assertFalse(mapping.contains("xmlMapperLoad_shouldExposeMappedStatement"));
        assertFalse(mapping.contains("xmlMapperNamespace_shouldResolveStatementsAcrossMultipleLoadedMappers"));
    }

    @Test
    public void builderGroupsRetainAllScenariosAndQueryOwnership() throws Exception {
        new NxnDocumentation(documents).update("h2", runDirectory);
        JsonObject results = read(documents.resolve("datasources/h2.json")).getAsJsonObject("capabilities");
        Map<String, Integer> expected = Map.of("inserts-updates-and-deletes/writes", 23, "inserts-updates-and-deletes/insert-conflicts", 7, "queries/query", 16, "map-query/modes", 16, "pagination-and-iteration/pagination", 22, "condition-builders/predicates", 37, "condition-values/parameter-values", 19, "grouping-and-ordering/grouping", 6, "grouping-and-ordering/ordering", 9);
        assertEquals(expected.size(), results.keySet().stream().filter(key -> key.startsWith("builder/")).count());
        for (var entry : expected.entrySet()) {
            assertEquals(entry.getKey(), entry.getValue().intValue(), results.getAsJsonObject("builder/" + entry.getKey()).get("total").getAsInt());
        }
        assertEquals(155, expected.values().stream().mapToInt(Integer::intValue).sum());

        JsonObject columns = read(runDirectory.resolve("compatibility.json")).getAsJsonObject("columns");
        String writes = columns.getAsJsonArray("builder/inserts-updates-and-deletes/writes").toString();
        String query = columns.getAsJsonArray("builder/queries/query").toString();
        assertFalse(writes.contains("lambdaEntityQuery_shouldReadUserById"));
        assertTrue(query.contains("lambdaEntityQuery_shouldReadUserById"));
        assertTrue(writes.contains("lambdaInsert_shouldAllowJdbcStringReadback"));
        assertTrue(writes.contains("lambdaUpdate_shouldReturnZeroWhenNoRowMatches"));
        assertTrue(query.contains("lambdaSelect_shouldCountDistinctValues"));
        assertTrue(columns.getAsJsonArray("builder/pagination-and-iteration/pagination").toString().contains("lambdaIteratorByBatch_shouldIterateAllRowsUsingBatchSize"));
        assertTrue(columns.getAsJsonArray("builder/condition-builders/predicates").toString().contains("lambdaQuery_shouldDistinguishEmptyStringAndNullValues"));
    }

    @Test
    public void parameterCapabilitiesAreSharedAcrossApis() throws Exception {
        new NxnDocumentation(documents).update("h2", runDirectory);
        JsonObject results = read(documents.resolve("datasources/h2.json")).getAsJsonObject("capabilities");
        assertEquals(5, results.getAsJsonObject("parameters/positional-and-named-parameters/positional").get("total").getAsInt());
        assertEquals(17, results.getAsJsonObject("parameters/positional-and-named-parameters/named").get("total").getAsInt());
        assertEquals(9, results.getAsJsonObject("parameters/command-rules/sql-fragments").get("total").getAsInt());
        assertEquals(2, results.getAsJsonObject("parameters/command-rules/conditional-command").get("total").getAsInt());
        assertFalse(results.has("mapper/method-annotations-parameters/positional-values"));
        assertFalse(results.has("mapper/method-annotations-parameters/named-values"));
        assertFalse(results.has("mapper/method-annotations-parameters/collection-expansion"));
        assertFalse(results.has("mapper-files/session-calls/parameters"));
        assertFalse(results.has("rules/statement-generation-rules/rules"));
    }

    @Test
    public void resultHandlingCountsApiEntrancesRatherThanTestMethods() throws Exception {
        for (Path file : observations) {
            JsonObject observation = read(file);
            String capability = observation.get("capability").getAsString();
            if (capability.startsWith("lambda.result.row-") || capability.startsWith("lambda.result.extractor.")) {
                observation.addProperty("outcome", "unsupported");
                observation.addProperty("reason", "Synthetic datasource without builder queries");
                write(file, observation);
            }
        }
        Path target = documents.resolve("datasources/h2.json");
        JsonObject datasource = read(target);
        for (String column : List.of("results/rowmapper/mapping", "results/rowcallbackhandler/callback", "results/resultsetextractor/extraction")) {
            datasource.getAsJsonObject("capabilities").getAsJsonObject(column).addProperty("href", "/docs/features/redis/result-handling");
        }
        write(target, datasource);
        new NxnDocumentation(documents).update("h2", runDirectory);
        JsonObject results = read(target).getAsJsonObject("capabilities");
        for (String column : List.of("results/rowmapper/mapping", "results/rowcallbackhandler/callback", "results/resultsetextractor/extraction")) {
            JsonObject result = results.getAsJsonObject(column);
            assertEquals("partial", result.get("status").getAsString());
            assertEquals(3, result.get("passed").getAsInt());
            assertEquals(4, result.get("total").getAsInt());
            assertEquals("unsupported", result.getAsJsonObject("variants").getAsJsonObject("builder").get("status").getAsString());
        }
    }

    @Test
    public void propagationCountsSevenModesAndPreservesAllScenarios() throws Exception {
        new NxnDocumentation(documents).update("h2", runDirectory);
        String column = "transactions/transaction-propagation/propagation";
        JsonObject result = read(documents.resolve("datasources/h2.json")).getAsJsonObject("capabilities").getAsJsonObject(column);
        assertEquals(7, result.get("passed").getAsInt());
        assertEquals(7, result.get("total").getAsInt());
        assertEquals(7, result.getAsJsonObject("variants").getAsJsonObject("NESTED").get("total").getAsInt());
        assertEquals(25, read(runDirectory.resolve("compatibility.json")).getAsJsonObject("columns").getAsJsonArray(column).size());

        JsonObject datasource = read(documents.resolve("datasources/h2.json"));
        datasource.getAsJsonObject("capabilities").getAsJsonObject(column).addProperty("href", "/docs/guides/transaction/propagation#nested");
        write(documents.resolve("datasources/h2.json"), datasource);
        for (Path file : observations) {
            JsonObject observation = read(file);
            if (CapabilityId.TRANSACTION_NESTED_SAVEPOINT.equals(observation.get("capability").getAsString())) {
                observation.addProperty("outcome", "unsupported");
                observation.addProperty("reason", "Synthetic savepoint limit");
                write(file, observation);
            }
        }
        new NxnDocumentation(documents).update("h2", runDirectory);
        result = read(documents.resolve("datasources/h2.json")).getAsJsonObject("capabilities").getAsJsonObject(column);
        assertEquals("partial", result.get("status").getAsString());
        assertEquals(6, result.get("passed").getAsInt());
        assertEquals(7, result.get("total").getAsInt());
        assertEquals("partial", result.getAsJsonObject("variants").getAsJsonObject("NESTED").get("status").getAsString());
    }

    @Test
    public void nontransactionalCallsDoNotCountAsFullPropagationSupport() throws Exception {
        new NxnDocumentation(documents).update("h2", runDirectory);
        String column = "transactions/transaction-propagation/propagation";
        java.util.Set<String> propagationCases = new java.util.HashSet<>();
        for (var detail : read(runDirectory.resolve("compatibility.json")).getAsJsonObject("columns").getAsJsonArray(column)) {
            propagationCases.add(detail.getAsJsonObject().get("capability").getAsString());
        }
        for (Path file : observations) {
            JsonObject observation = read(file);
            String capability = observation.get("capability").getAsString();
            if (propagationCases.contains(capability) && !CapabilityId.TRANSACTION_SUPPORTS_NO_TX.equals(capability) && !CapabilityId.TRANSACTION_NEVER_NO_TX.equals(capability) && !CapabilityId.TRANSACTION_NOT_SUPPORTED_NO_TX.equals(capability)) {
                observation.addProperty("outcome", "unsupported");
                observation.addProperty("reason", "Synthetic datasource without JDBC transactions");
                write(file, observation);
            }
        }
        assertUpdateRejected("Limited support needs a datasource explanation link");
        JsonObject datasource = read(documents.resolve("datasources/h2.json"));
        datasource.getAsJsonObject("capabilities").getAsJsonObject(column).addProperty("href", "/docs/guides/transaction/propagation");
        write(documents.resolve("datasources/h2.json"), datasource);
        new NxnDocumentation(documents).update("h2", runDirectory);
        JsonObject result = read(documents.resolve("datasources/h2.json")).getAsJsonObject("capabilities").getAsJsonObject(column);
        assertEquals("limited", result.get("status").getAsString());
        assertEquals(0, result.get("passed").getAsInt());
        assertEquals(7, result.get("total").getAsInt());
        assertEquals("partial", result.getAsJsonObject("variants").getAsJsonObject("SUPPORTS").get("status").getAsString());
        assertEquals("partial", result.getAsJsonObject("variants").getAsJsonObject("NEVER").get("status").getAsString());
    }

    @Test
    public void isolationAvailabilityDoesNotCountDatabaseSpecificLevels() throws Exception {
        String column = "transactions/isolation-levels/isolation";
        for (Path file : observations) {
            JsonObject observation = read(file);
            if (CapabilityId.TRANSACTION_ISOLATION_REPEATABLE_READ.equals(observation.get("capability").getAsString())) {
                observation.addProperty("outcome", "unsupported");
                observation.addProperty("reason", "Synthetic database without this isolation level");
                write(file, observation);
            }
        }
        new NxnDocumentation(documents).update("h2", runDirectory);
        JsonObject result = read(documents.resolve("datasources/h2.json")).getAsJsonObject("capabilities").getAsJsonObject(column);
        assertEquals("supported", result.get("status").getAsString());
        assertEquals(5, result.get("passed").getAsInt());
        assertEquals(6, result.get("total").getAsInt());
        for (Path file : observations) {
            JsonObject observation = read(file);
            if (CapabilityId.TRANSACTION_TEMPLATE_ISOLATION.equals(observation.get("capability").getAsString())) {
                observation.addProperty("outcome", "unsupported");
                observation.addProperty("reason", "Synthetic driver cannot apply isolation settings");
                write(file, observation);
            }
        }
        new NxnDocumentation(documents).update("h2", runDirectory);
        result = read(documents.resolve("datasources/h2.json")).getAsJsonObject("capabilities").getAsJsonObject(column);
        assertEquals("unsupported", result.get("status").getAsString());
        assertEquals(4, result.get("passed").getAsInt());
    }

    @Test
    public void transactionPrerequisiteLimitsApisButNotIsolationAvailability() throws Exception {
        Path run = temporary.newFolder("clickhouse-run").toPath();
        Files.createDirectory(run.resolve("cases"));
        JsonObject summary = read(runDirectory.resolve("run.json"));
        summary.addProperty("env", "clickhouse");
        write(run.resolve("run.json"), summary);
        String fingerprint = NxnTestResult.fingerprint();
        Path sourceRoot = Path.of("src/test/java");
        int index = 0;
        try (Stream<Path> files = Files.walk(sourceRoot.resolve("net/hasor/dbvisitor/test/realdb/clickhouse"))) {
            for (Path file : files.filter(path -> path.toString().endsWith("Test.java")).sorted().toList()) {
                String name = sourceRoot.relativize(file).toString().replace('/', '.').replace('\\', '.').replaceFirst("\\.java$", "");
                Class<?> contract = Class.forName(name).getSuperclass();
                while (contract != null && contract.getDeclaredAnnotation(NxnContract.class) == null) {
                    contract = contract.getSuperclass();
                }
                if (contract == null || contract.getDeclaredAnnotation(NxnContract.class).scope() != NxnContract.Scope.COMPATIBILITY) {
                    continue;
                }
                for (Method method : contract.getDeclaredMethods()) {
                    if (method.getAnnotation(Test.class) == null) {
                        continue;
                    }
                    Capability capability = method.getAnnotation(Capability.class);
                    NxnTestResult result = new NxnTestResult();
                    result.runId = "synthetic-unit-run";
                    result.env = "clickhouse";
                    result.fingerprint = fingerprint;
                    result.testClass = name;
                    result.method = method.getName();
                    result.capability = capability.value();
                    result.outcome = capability.column().startsWith("transactions/") ? "unsupported" : "passed";
                    result.reason = "Synthetic JDBC transaction prerequisite limit";
                    write(run.resolve("cases/" + index++ + ".json"), result);
                }
            }
        }
        new NxnDocumentation(documents).update("clickhouse", run);
        JsonObject results = read(documents.resolve("datasources/clickhouse.json")).getAsJsonObject("capabilities");
        for (var entry : results.entrySet()) {
            if (entry.getKey().startsWith("transactions/")) {
                JsonObject value = entry.getValue().getAsJsonObject();
                assertEquals(entry.getKey().endsWith("/isolation") ? "unsupported" : "limited", value.get("status").getAsString());
                assertEquals(0, value.get("passed").getAsInt());
                assertTrue(value.has("href"));
            }
        }
    }

    @Test
    public void variantsWithoutTestsAreRejected() throws Exception {
        Path tableFile = documents.resolve("tables/transactions.json");
        JsonObject table = read(tableFile);
        for (var group : table.getAsJsonArray("groups")) {
            for (var column : group.getAsJsonObject().getAsJsonArray("columns")) {
                if (column.getAsJsonObject().has("variants")) {
                    column.getAsJsonObject().getAsJsonArray("variants").add("UNTESTED_MODE");
                }
            }
        }
        write(tableFile, table);
        assertUpdateRejected("Variants without tests");
    }

    @Test
    public void mixedScenariosAreAssignedToTheirActualOperations() throws Exception {
        new NxnDocumentation(documents).update("h2", runDirectory);
        JsonObject columns = read(runDirectory.resolve("compatibility.json")).getAsJsonObject("columns");
        Map<String, String> expected = Map.of("XmlMapperStatementAccessCase#xmlMapperExecute_shouldRunDmlStatementAndReturnAffectedRows", "mapper-files/statements/execution", "AnnotationMapperExecutionCase#annotationAttributes_shouldJoinMultilineSqlValueArray", "mapper/method-annotations/execution", "XmlMapperResultHandlerCase#resultHandler_shouldSupportEntityMapAndScalarResultTypes", "mapper-files/dynamic-sql-and-result-mapping/resultmap", "XmlMapperResultHandlerCase#resultHandler_shouldSupportNamedResultMap", "mapper-files/dynamic-sql-and-result-mapping/resultmap",
                "SessionCoreCase#sessionLambda_shouldExposeUsableLambdaTemplate", "mapper/builder/calls", "SessionCoreCase#sessionBaseMapper_shouldRunCrudThroughCreatedMapper", "mapper/base-mapper/operations", "SessionCoreCase#sessionBaseMapper_shouldSupportCompositeKeyEntity", "mapper/key-strategies/strategies", "SessionMapperSharingCase#sessionCreateMapper_shouldAllowMapperTypesAndBaseMapperToShareOneSession", "mapper/base-mapper/operations", "BaseMapperCombinedCase#baseMapperMixedOperations_shouldSupportCrudAndUpsertInOneMapper", "mapper/builder/calls");
        for (Map.Entry<String, String> method : expected.entrySet()) {
            boolean found = false;
            for (var test : columns.getAsJsonArray(method.getValue())) {
                if (test.getAsJsonObject().get("contract").getAsString().endsWith("." + method.getKey())) {
                    found = true;
                }
            }
            assertTrue(method.getKey(), found);
        }
    }

    @Test
    public void mapperColumnsRetainAllScenariosAfterMerging() throws Exception {
        new NxnDocumentation(documents).update("h2", runDirectory);
        JsonObject results = read(documents.resolve("datasources/h2.json")).getAsJsonObject("capabilities");
        Map<String, Integer> expected = Map.of("mapper/method-annotations/execution", 22, "mapper/base-mapper/operations", 45, "mapper/key-strategies/strategies", 18, "mapper/pagination/pagination", 4, "mapper/execution-options/options", 8, "mapper/builder/calls", 2, "mapper/file-mapper/calls", 5, "mapper/session/management", 8);
        int total = 0;
        int columns = 0;
        for (String column : results.keySet()) {
            if (column.startsWith("mapper/")) {
                assertTrue(column, expected.containsKey(column));
                int count = results.getAsJsonObject(column).get("total").getAsInt();
                assertEquals(column, expected.get(column).intValue(), count);
                total += count;
                columns++;
            }
        }
        assertEquals(8, columns);
        assertEquals(112, total);
    }

    @Test
    public void jdbcColumnsRetainAllScenariosAfterMerging() throws Exception {
        new NxnDocumentation(documents).update("h2", runDirectory);
        JsonObject results = read(documents.resolve("datasources/h2.json")).getAsJsonObject("capabilities");
        Map<String, Integer> expected = Map.of("jdbc/updates/updates", 6, "jdbc/queries/queries", 16, "jdbc/queries/pairs-queries", 3, "jdbc/batch-operations/batch", 9, "jdbc/multiple-results/multiple-results", 5, "jdbc/stored-routines/routines", 14);
        int total = 0;
        int columns = 0;
        for (String column : results.keySet()) {
            if (column.startsWith("jdbc/")) {
                assertTrue(column, expected.containsKey(column));
                int count = results.getAsJsonObject(column).get("total").getAsInt();
                assertEquals(column, expected.get(column).intValue(), count);
                total += count;
                columns++;
            }
        }
        assertEquals(6, columns);
        assertEquals(53, total);
        JsonObject evidence = read(runDirectory.resolve("compatibility.json")).getAsJsonObject("columns");
        assertTrue(evidence.getAsJsonArray("jdbc/queries/queries").toString().contains("JdbcCrudCase#jdbcQuery_shouldReadUserById"));
        assertTrue(evidence.getAsJsonArray("jdbc/multiple-results/multiple-results").toString().contains("JdbcCallResultCase#callShouldCollectReturnedResultSet"));
    }

    @Test
    public void partialSupportRetainsTranslatedExplanationAndEvidence() throws Exception {
        Path observation = mapQueryObservation();
        JsonObject result = read(observation);
        result.addProperty("outcome", "unsupported");
        result.addProperty("reason", "Synthetic explicit driver limit");
        write(observation, result);
        JsonObject h2 = read(documents.resolve("datasources/h2.json"));
        JsonObject href = new JsonObject();
        href.addProperty("zh-cn", "/docs/features/h2/about#限制");
        href.addProperty("en", "/docs/features/h2/about#limits");
        h2.getAsJsonObject("capabilities").getAsJsonObject("jdbc/queries/queries").add("href", href);
        write(documents.resolve("datasources/h2.json"), h2);

        new NxnDocumentation(documents).update("h2", runDirectory);
        JsonObject actual = read(documents.resolve("datasources/h2.json")).getAsJsonObject("capabilities").getAsJsonObject("jdbc/queries/queries");
        assertEquals("partial", actual.get("status").getAsString());
        assertEquals(15, actual.get("passed").getAsInt());
        assertEquals(16, actual.get("total").getAsInt());
        assertEquals(href, actual.get("href"));
        assertTrue(Files.readString(runDirectory.resolve("compatibility.json")).contains("Synthetic explicit driver limit"));
    }

    @Test
    public void missingExecutionDoesNotBecomeUnsupported() throws Exception {
        Files.delete(observations.get(0));
        assertUpdateRejected("Test was not executed");
    }

    @Test
    public void unknownSkipDoesNotBecomeUnsupported() throws Exception {
        changeObservation("outcome", "unverified");
        assertUpdateRejected("Unresolved result");
    }

    @Test
    public void failedExecutionNeverUpdatesAnyDocument() throws Exception {
        changeObservation("outcome", "failed");
        assertUpdateRejected("Failed test");
    }

    @Test
    public void interruptedRunDoesNotUpdateAnyDocument() throws Exception {
        JsonObject run = read(runDirectory.resolve("run.json"));
        run.addProperty("complete", false);
        write(runDirectory.resolve("run.json"), run);
        assertUpdateRejected("did not complete");
    }

    @Test
    public void filteredRunDoesNotUpdateAnyDocument() throws Exception {
        JsonObject run = read(runDirectory.resolve("run.json"));
        run.addProperty("filtered", true);
        write(runDirectory.resolve("run.json"), run);
        assertUpdateRejected("Filtered test runs");
    }

    @Test
    public void mixedRunsAreRejected() throws Exception {
        changeObservation("runId", "old-run");
        assertUpdateRejected("Stale or mixed");
    }

    @Test
    public void changedTestClassesRequireAnotherRun() throws Exception {
        changeObservation("fingerprint", "old-classes");
        assertUpdateRejected("Stale or mixed");
    }

    @Test
    public void duplicateExecutionsDoNotInflateCounts() throws Exception {
        Files.copy(observations.get(0), runDirectory.resolve("cases/duplicate.json"));
        assertUpdateRejected("Duplicate test execution");
    }

    @Test
    public void partialSupportRequiresAnExplanationLink() throws Exception {
        JsonObject result = read(mapQueryObservation());
        result.addProperty("outcome", "unsupported");
        result.addProperty("reason", "Synthetic driver limit");
        write(mapQueryObservation(), result);
        JsonObject h2 = read(documents.resolve("datasources/h2.json"));
        h2.getAsJsonObject("capabilities").getAsJsonObject("jdbc/queries/queries").remove("href");
        write(documents.resolve("datasources/h2.json"), h2);
        assertUpdateRejected("needs a datasource explanation");
    }

    @Test
    public void renamedColumnsCannotSilentlyLoseTheirTests() throws Exception {
        JsonObject jdbc = read(documents.resolve("tables/jdbc.json"));
        column(jdbc, "queries", "queries").addProperty("id", "renamed-without-updating-test");
        write(documents.resolve("tables/jdbc.json"), jdbc);
        assertUpdateRejected("Datasource columns differ");
    }

    @Test
    public void renamedJsonColumnsStillRequireUpdatedMethodAnnotations() throws Exception {
        JsonObject jdbc = read(documents.resolve("tables/jdbc.json"));
        column(jdbc, "queries", "queries").addProperty("id", "renamed");
        write(documents.resolve("tables/jdbc.json"), jdbc);
        try (Stream<Path> files = Files.list(documents.resolve("datasources"))) {
            for (Path file : files.toList()) {
                JsonObject datasource = read(file);
                JsonObject capabilities = datasource.getAsJsonObject("capabilities");
                capabilities.add("jdbc/queries/renamed", capabilities.remove("jdbc/queries/queries"));
                write(file, datasource);
            }
        }
        assertUpdateRejected("Unknown or missing JSON column 'jdbc/queries/queries'");
    }

    @Test
    public void missingDatasourceFileIsRejected() throws Exception {
        Files.delete(documents.resolve("datasources/mysql.json"));
        assertUpdateRejected("Datasource files and source index differ");
    }

    @Test
    public void datasourceIdMustMatchFilename() throws Exception {
        JsonObject h2 = read(documents.resolve("datasources/h2.json"));
        h2.addProperty("id", "mysql");
        write(documents.resolve("datasources/h2.json"), h2);
        assertUpdateRejected("Datasource ID does not match filename");
    }

    @Test
    public void missingDatasourceColumnIsRejected() throws Exception {
        JsonObject h2 = read(documents.resolve("datasources/h2.json"));
        h2.getAsJsonObject("capabilities").remove("jdbc/queries/queries");
        write(documents.resolve("datasources/h2.json"), h2);
        assertUpdateRejected("missing=[jdbc/queries/queries]");
    }

    @Test
    public void unknownDatasourceColumnIsRejected() throws Exception {
        JsonObject h2 = read(documents.resolve("datasources/h2.json"));
        h2.getAsJsonObject("capabilities").add("jdbc/queries/unknown", new JsonObject());
        write(documents.resolve("datasources/h2.json"), h2);
        assertUpdateRejected("unknown=[jdbc/queries/unknown]");
    }

    @Test
    public void tableDefinitionsCannotContainDatasourceResults() throws Exception {
        JsonObject jdbc = read(documents.resolve("tables/jdbc.json"));
        column(jdbc, "queries", "queries").add("results", new JsonObject());
        write(documents.resolve("tables/jdbc.json"), jdbc);
        assertUpdateRejected("Table definitions must not contain datasource results");
    }

    private Map<Path, String> documentContents() throws Exception {
        Map<Path, String> before = new LinkedHashMap<>();
        try (Stream<Path> files = Files.walk(documents)) {
            for (Path file : files.filter(Files::isRegularFile).toList()) {
                before.put(file, Files.readString(file));
            }
        }
        return before;
    }

    private void assertUpdateRejected(String expected) throws Exception {
        Map<Path, String> before = documentContents();
        IllegalStateException error = assertThrows(IllegalStateException.class, () -> new NxnDocumentation(documents).update("h2", runDirectory));
        assertTrue(error.getMessage(), error.getMessage().contains(expected));
        for (Map.Entry<Path, String> original : before.entrySet()) {
            assertEquals("Rejected update changed " + original.getKey(), original.getValue(), Files.readString(original.getKey()));
        }
    }

    private void changeObservation(String field, String value) throws Exception {
        JsonObject observation = read(observations.get(0));
        observation.addProperty(field, value);
        write(observations.get(0), observation);
    }

    private Path mapQueryObservation() throws Exception {
        for (Path observation : observations) {
            if (read(observation).get("testClass").getAsString().endsWith("H2JdbcMapQueryTest")) {
                return observation;
            }
        }
        throw new IllegalStateException("Map query fixture not found");
    }

    private JsonObject column(JsonObject document, String group, String column) {
        for (var groupValue : document.getAsJsonArray("groups")) {
            if (groupValue.getAsJsonObject().get("id").getAsString().equals(group)) {
                for (var columnValue : groupValue.getAsJsonObject().getAsJsonArray("columns")) {
                    if (columnValue.getAsJsonObject().get("id").getAsString().equals(column)) {
                        return columnValue.getAsJsonObject();
                    }
                }
            }
        }
        throw new IllegalArgumentException("Missing column " + group + "/" + column);
    }

    private JsonObject read(Path file) throws Exception {
        return JsonParser.parseString(Files.readString(file)).getAsJsonObject();
    }

    private void write(Path file, Object value) throws Exception {
        Files.writeString(file, JSON.toJson(value));
    }
}
