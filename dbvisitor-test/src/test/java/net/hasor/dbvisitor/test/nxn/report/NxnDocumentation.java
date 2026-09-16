/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.nxn.report;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Stream;
import com.google.gson.*;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.capability.SupportStatus;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfileRegistry;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;

/** Joins source-level contracts, executed tests and user-facing compatibility columns. */
public final class NxnDocumentation {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private final Path                    directory;
    private final Map<String, JsonObject> datasources = new LinkedHashMap<>();
    private final Map<String, JsonObject> columns     = new LinkedHashMap<>();
    private final Map<String, List<Case>> cases       = new LinkedHashMap<>();

    public static void main(String[] args) throws Exception {
        if (args.length == 2 && "check".equals(args[0])) {
            new NxnDocumentation(Path.of(args[1]));
            System.out.println("NxN contract-to-documentation mapping checked.");
        } else if (args.length == 4 && "update".equals(args[0])) {
            new NxnDocumentation(Path.of(args[1])).update(args[2], Path.of(args[3]));
            System.out.println("Updated compatibility results for " + args[2] + ".");
        } else {
            throw new IllegalArgumentException("Usage: check <json-directory> | update <json-directory> <env> <run-directory>");
        }
    }

    public NxnDocumentation(Path directory) throws Exception {
        this.directory = directory;
        readTables();
        readDatasources();
        bindContracts();
        checkDatasourceBindings();
    }

    private void readTables() throws Exception {
        try (Stream<Path> files = Files.list(directory.resolve("tables"))) {
            for (Path file : files.filter(path -> path.toString().endsWith(".json")).sorted().toList()) {
                JsonObject document = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
                require(document.get("schemaVersion").getAsInt() == 1, "Unsupported compatibility schema: " + file);
                String page = document.get("id").getAsString();
                require(file.getFileName().toString().equals(page + ".json"), "Table ID does not match filename: " + file);
                for (JsonElement groupValue : document.getAsJsonArray("groups")) {
                    JsonObject group = groupValue.getAsJsonObject();
                    for (JsonElement columnValue : group.getAsJsonArray("columns")) {
                        JsonObject column = columnValue.getAsJsonObject();
                        String key = page + "/" + group.get("id").getAsString() + "/" + column.get("id").getAsString();
                        require(!column.has("results"), "Table definitions must not contain datasource results: " + key);
                        require(!column.has("requiresTransaction") || column.get("requiresTransaction").isJsonPrimitive() && column.getAsJsonPrimitive("requiresTransaction").isBoolean(), "requiresTransaction must be boolean: " + key);
                        require(columns.put(key, column) == null, "Duplicate JSON column: " + key);
                        cases.put(key, new ArrayList<>());
                    }
                }
            }
        }
        require(!columns.isEmpty(), "No compatibility columns in " + directory);
    }

    private void readDatasources() throws Exception {
        Set<String> sources = new HashSet<>();
        for (JsonElement source : JsonParser.parseString(Files.readString(directory.resolve("sources.json"))).getAsJsonArray()) {
            String env = source.getAsJsonObject().get("id").getAsString();
            require(sources.add(env), "Duplicate datasource: " + env);
        }
        Set<String> registered = new HashSet<>();
        for (DataSourceProfile profile : DataSourceProfileRegistry.all()) {
            registered.add(profile.env());
        }
        require(sources.equals(registered), "JSON sources and NxN profiles differ: " + sources + " / " + registered);
        try (Stream<Path> files = Files.list(directory.resolve("datasources"))) {
            for (Path file : files.filter(path -> path.toString().endsWith(".json")).sorted().toList()) {
                JsonObject document = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
                require(document.get("schemaVersion").getAsInt() == 1, "Unsupported datasource schema: " + file);
                String env = document.get("id").getAsString();
                require(file.getFileName().toString().equals(env + ".json"), "Datasource ID does not match filename: " + file);
                require(datasources.put(env, document) == null, "Duplicate datasource: " + env);
                JsonObject capabilities = document.getAsJsonObject("capabilities");
                require(capabilities != null, "Missing datasource capabilities: " + env);
                Set<String> missing = new TreeSet<>(columns.keySet());
                missing.removeAll(capabilities.keySet());
                Set<String> unknown = new TreeSet<>(capabilities.keySet());
                unknown.removeAll(columns.keySet());
                require(missing.isEmpty() && unknown.isEmpty(), "Datasource columns differ for " + env + ": missing=" + missing + ", unknown=" + unknown);
                for (String column : columns.keySet()) {
                    require(capabilities.get(column).isJsonObject(), "Missing capability result object: " + env + " / " + column);
                }
            }
        }
        require(datasources.keySet().equals(sources), "Datasource files and source index differ: " + datasources.keySet() + " / " + sources);
    }

    private void bindContracts() throws Exception {
        Set<String> methodIds = new HashSet<>();
        for (Class<?> contract : CapabilityMatrixReport.contractClasses()) {
            NxnContract annotation = contract.getDeclaredAnnotation(NxnContract.class);
            for (Method method : contract.getDeclaredMethods()) {
                if (method.getAnnotation(Test.class) == null) {
                    continue;
                }
                Capability capability = method.getAnnotation(Capability.class);
                require(capability != null, "Missing @Capability: " + method);
                if (annotation.scope() != NxnContract.Scope.COMPATIBILITY) {
                    require(capability.column().isEmpty(), "Non-compatibility test must not bind a JSON column: " + method);
                    continue;
                }
                String column = capability.column();
                require(columns.containsKey(column), "Unknown or missing JSON column '" + column + "': " + method);
                require(methodIds.add(contract.getName() + "#" + method.getName()), "Duplicate contract method: " + method);
                List<String> variants = List.of(capability.variants());
                require(new HashSet<>(variants).size() == variants.size(), "Duplicate test variant: " + method);
                cases.get(column).add(new Case(contract, method.getName(), capability.value(), column, variants));
            }
        }
        for (Map.Entry<String, List<Case>> entry : cases.entrySet()) {
            require(!entry.getValue().isEmpty(), "JSON column has no test contract: " + entry.getKey());
            entry.getValue().sort(java.util.Comparator.comparing(Case::id));
            JsonArray declared = columns.get(entry.getKey()).getAsJsonArray("variants");
            Set<String> expected = new LinkedHashSet<>();
            if (declared != null) {
                for (JsonElement variant : declared) {
                    String name = variant.getAsString();
                    require(!name.isBlank() && expected.add(name), "Invalid or duplicate variant: " + entry.getKey());
                }
                require(!expected.isEmpty(), "Empty variants: " + entry.getKey());
            }
            Set<String> covered = new HashSet<>();
            for (Case test : entry.getValue()) {
                require(expected.isEmpty() == test.variants.isEmpty() && expected.containsAll(test.variants), "Missing or unknown variants: " + test.id());
                covered.addAll(test.variants);
            }
            require(covered.equals(expected), "Variants without tests: " + entry.getKey());
            JsonObject definition = columns.get(entry.getKey());
            if (definition.has("availability")) {
                require(!definition.has("variants"), "Availability cannot count variants: " + entry.getKey());
                String capability = definition.get("availability").getAsString();
                require(entry.getValue().stream().filter(test -> test.capability.equals(capability)).count() == 1, "Availability must identify one test in its column: " + entry.getKey());
            }
        }
    }

    private void checkDatasourceBindings() throws Exception {
        for (DataSourceProfile profile : DataSourceProfileRegistry.all()) {
            Map<Class<?>, Class<?>> bindings = CapabilityMatrixReport.realdbBindings(profile);
            for (List<Case> group : cases.values()) {
                for (Case test : group) {
                    Class<?> implementation = bindings.get(test.contract);
                    if (implementation == null) {
                        require(isUnsupported(profile.support(test.capability)), "Missing realdb binding for " + profile.env() + ": " + test.id());
                        continue;
                    }
                    Capability actual = implementation.getMethod(test.method).getAnnotation(Capability.class);
                    require(actual != null && actual.value().equals(test.capability), "Overridden method lost its capability: " + implementation.getName() + "#" + test.method);
                    require(actual.column().equals(test.column), "Datasource override changes or omits its JSON column: " + implementation.getName() + "#" + test.method);
                    require(new HashSet<>(List.of(actual.variants())).equals(new HashSet<>(test.variants)), "Datasource override changes its variants: " + implementation.getName() + "#" + test.method);
                }
            }
        }
    }

    public void update(String env, Path runDirectory) throws Exception {
        DataSourceProfile profile = DataSourceProfileRegistry.find(env);
        JsonObject datasource = datasources.get(env).deepCopy();
        JsonObject run = JsonParser.parseString(Files.readString(runDirectory.resolve("run.json"))).getAsJsonObject();
        require(run.get("complete").getAsBoolean() && run.get("failed").getAsLong() == 0 && run.get("tests").getAsLong() > 0, "Test run did not complete successfully; documentation was not changed.");
        require(env.equals(run.get("env").getAsString()), "Wrong datasource in test run");
        require(run.has("filtered") && !run.get("filtered").getAsBoolean(), "Filtered test runs cannot update the compatibility matrix.");
        Map<String, NxnTestResult> observed = readResults(runDirectory, run.get("runId").getAsString(), env);
        Map<Class<?>, Class<?>> bindings = CapabilityMatrixReport.realdbBindings(profile);
        JsonObject evidence = new JsonObject();
        evidence.add("run", run);
        JsonObject evidenceColumns = new JsonObject();
        evidence.add("columns", evidenceColumns);

        // Validate every cell first. A partial or failed run must leave every source file untouched.
        for (Map.Entry<String, List<Case>> entry : cases.entrySet()) {
            int passed = 0;
            JsonArray details = new JsonArray();
            for (Case test : entry.getValue()) {
                Class<?> implementation = bindings.get(test.contract);
                JsonObject detail = new JsonObject();
                detail.addProperty("contract", test.id());
                detail.addProperty("capability", test.capability);
                if (!test.variants.isEmpty()) {
                    detail.add("variants", JSON.toJsonTree(test.variants));
                }
                if (implementation == null) {
                    SupportStatus status = profile.support(test.capability);
                    require(isUnsupported(status), "Missing realdb test: " + test.id());
                    detail.addProperty("outcome", "unsupported");
                    detail.addProperty("reason", "Unbound: " + status);
                } else {
                    String key = implementation.getName() + "#" + test.method;
                    NxnTestResult result = observed.get(key);
                    require(result != null, "Test was not executed: " + key);
                    require(test.capability.equals(result.capability), "Capability mismatch: " + key);
                    require("passed".equals(result.outcome) || "unsupported".equals(result.outcome), "Unresolved result: " + key + " / " + result.outcome + " / " + result.reason);
                    require(!"unsupported".equals(result.outcome) || result.reason != null && !result.reason.isBlank(), "Missing unsupported reason: " + key);
                    if ("passed".equals(result.outcome)) {
                        passed++;
                    }
                    detail.addProperty("test", key);
                    detail.addProperty("outcome", result.outcome);
                    detail.addProperty("reason", result.reason);
                }
                details.add(detail);
            }
            int total = entry.getValue().size();
            String status = passed == total ? "supported" : passed == 0 ? "unsupported" : "partial";
            JsonObject result = datasource.getAsJsonObject("capabilities").getAsJsonObject(entry.getKey());
            result.addProperty("status", status);
            result.addProperty("passed", passed);
            result.addProperty("total", total);
            JsonArray variants = columns.get(entry.getKey()).getAsJsonArray("variants");
            if (variants != null) {
                JsonObject summaries = new JsonObject();
                int supported = 0;
                for (JsonElement variant : variants) {
                    int variantPassed = 0;
                    int variantTotal = 0;
                    for (JsonElement detailValue : details) {
                        JsonObject detail = detailValue.getAsJsonObject();
                        if (detail.getAsJsonArray("variants").contains(variant)) {
                            variantTotal++;
                            if ("passed".equals(detail.get("outcome").getAsString())) {
                                variantPassed++;
                            }
                        }
                    }
                    JsonObject summary = new JsonObject();
                    summary.addProperty("status", variantPassed == variantTotal ? "supported" : variantPassed == 0 ? "unsupported" : "partial");
                    summary.addProperty("passed", variantPassed);
                    summary.addProperty("total", variantTotal);
                    summaries.add(variant.getAsString(), summary);
                    if (variantPassed == variantTotal) {
                        supported++;
                    }
                }
                result.addProperty("passed", supported);
                result.addProperty("total", variants.size());
                String variantStatus = supported == variants.size() ? "supported" : supported > 0 ? "partial" : passed > 0 ? "limited" : "unsupported";
                result.addProperty("status", variantStatus);
                result.add("variants", summaries);
            } else {
                result.remove("variants");
            }
            JsonObject definition = columns.get(entry.getKey());
            if (definition.has("requiresTransaction") && definition.get("requiresTransaction").getAsBoolean() && !profile.supportsFeature(FeatureId.TRANSACTION) && !"supported".equals(result.get("status").getAsString())) {
                // A missing JDBC transaction prerequisite limits these APIs; it does not remove them from dbVisitor.
                result.addProperty("status", "limited");
            }
            if (definition.has("availability")) {
                String capability = definition.get("availability").getAsString();
                for (JsonElement detailValue : details) {
                    JsonObject detail = detailValue.getAsJsonObject();
                    if (capability.equals(detail.get("capability").getAsString())) {
                        // Other tests describe datasource-specific variants, not the availability of the setting API.
                        result.addProperty("status", "passed".equals(detail.get("outcome").getAsString()) ? "supported" : "unsupported");
                    }
                }
                require(hasExplanation(result), "Availability needs a datasource explanation link: " + entry.getKey() + " / " + env);
            }
            require(!"partial".equals(result.get("status").getAsString()) || hasExplanation(result), "Partial support needs a datasource explanation link: " + entry.getKey() + " / " + env);
            require(!"limited".equals(result.get("status").getAsString()) || hasExplanation(result), "Limited support needs a datasource explanation link: " + entry.getKey() + " / " + env);
            evidenceColumns.add(entry.getKey(), details);
        }
        writeJson(runDirectory.resolve("compatibility.json"), evidence);
        writeJson(directory.resolve("datasources").resolve(env + ".json"), datasource);
    }

    private Map<String, NxnTestResult> readResults(Path directory, String runId, String env) throws Exception {
        String fingerprint = NxnTestResult.fingerprint();
        Map<String, NxnTestResult> results = new LinkedHashMap<>();
        try (Stream<Path> files = Files.list(directory.resolve("cases"))) {
            for (Path file : files.filter(path -> path.toString().endsWith(".json")).sorted().toList()) {
                NxnTestResult result = JSON.fromJson(Files.readString(file), NxnTestResult.class);
                require(runId.equals(result.runId) && env.equals(result.env) && fingerprint.equals(result.fingerprint), "Stale or mixed test results: " + file);
                require(!"failed".equals(result.outcome), "Failed test: " + result.testClass + "#" + result.method);
                String key = result.testClass + "#" + result.method;
                require(results.put(key, result) == null, "Duplicate test execution: " + key);
            }
        }
        return results;
    }

    private static boolean hasExplanation(JsonObject result) {
        JsonElement href = result.get("href");
        if (href == null || href.isJsonNull()) {
            return false;
        }
        if (href.isJsonPrimitive()) {
            return href.getAsString().startsWith("/docs/");
        }
        JsonObject translated = href.getAsJsonObject();
        return translated.has("zh-cn") && translated.has("en") && translated.get("zh-cn").getAsString().startsWith("/docs/") && translated.get("en").getAsString().startsWith("/docs/");
    }

    private static boolean isUnsupported(SupportStatus status) {
        return status == SupportStatus.UNSUPPORTED_BY_DATABASE || status == SupportStatus.UNSUPPORTED_BY_DRIVER || status == SupportStatus.UNSUPPORTED_BY_DBVISITOR;
    }

    private static void writeJson(Path path, JsonObject value) throws Exception {
        String content = JSON.toJson(value) + "\n";
        if (Files.exists(path) && Files.readString(path).equals(content)) {
            return;
        }
        Path temporary = Files.createTempFile(path.getParent(), ".nxn-", ".json");
        try {
            Files.writeString(temporary, content, StandardCharsets.UTF_8);
            Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private record Case(Class<?> contract, String method, String capability, String column, List<String> variants) {
        String id() {
            return contract.getName() + "#" + method;
        }
    }
}
