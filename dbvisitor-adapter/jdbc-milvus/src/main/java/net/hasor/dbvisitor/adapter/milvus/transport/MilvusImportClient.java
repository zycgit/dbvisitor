/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.milvus.transport;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.milvus.v2.client.ConnectConfig;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.adapter.milvus.MilvusRequest;
import net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandKeys;
import net.hasor.dbvisitor.driver.AdapterRequest;

/** Official /v2/vectordb/jobs/import REST operations, using the JDBC endpoint and credentials. */
public final class MilvusImportClient {
    private final ConnectConfig config;
    private final HttpClient    client;
    private final URI           baseUri;

    public MilvusImportClient(ConnectConfig config, SSLContext tls) {
        this.config = config;
        this.baseUri = URI.create(config.getUri());
        // REST uses HTTP/1.1; gRPC uses HTTP/2. A TLS ingress can route both on one port.
        HttpClient.Builder builder = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1);
        if (tls != null) {
            SSLParameters parameters = tls.getDefaultSSLParameters();
            parameters.setEndpointIdentificationAlgorithm("HTTPS");
            if (StringUtils.isNotBlank(config.getServerName()) && !config.getServerName().equals(config.getHost())) {
                parameters.setServerNames(List.of(new SNIHostName(config.getServerName())));
            }
            builder.sslContext(tls).sslParameters(parameters);
        }
        if (config.getConnectTimeoutMs() > 0) {
            builder.connectTimeout(Duration.ofMillis(config.getConnectTimeoutMs()));
        }
        this.client = builder.build();
    }

    public String start(String database, String collection, String partition, List<List<String>> groups, Map<String, Object> options, AdapterRequest request, long timeoutMs) throws SQLException {
        JsonObject body = new JsonObject();
        body.addProperty(MilvusCommandKeys.REST_DB_NAME, StringUtils.isBlank(database) ? "default" : database);
        body.addProperty(MilvusCommandKeys.REST_COLLECTION_NAME, collection);
        if (StringUtils.isNotBlank(partition)) {
            body.addProperty(MilvusCommandKeys.REST_PARTITION_NAME, partition);
        }
        JsonArray files = new JsonArray();
        for (List<String> paths : groups) {
            JsonArray group = new JsonArray();
            paths.forEach(group::add);
            files.add(group);
        }
        body.add(MilvusCommandKeys.REST_FILES, files);
        if (!options.isEmpty()) {
            JsonObject settings = new JsonObject();
            options.forEach((key, value) -> settings.addProperty(key, String.valueOf(value)));
            body.add(MilvusCommandKeys.REST_OPTIONS, settings);
        }
        JsonObject data = this.post("create", body, request, timeoutMs);
        if (!data.has(MilvusCommandKeys.REST_JOB_ID) || data.get(MilvusCommandKeys.REST_JOB_ID).isJsonNull()) {
            throw new SQLException("Milvus import response did not contain a " + MilvusCommandKeys.REST_JOB_ID + ".");
        }
        return data.get(MilvusCommandKeys.REST_JOB_ID).getAsString();
    }

    public JsonObject progress(String database, String jobId, AdapterRequest request, long timeoutMs) throws SQLException {
        JsonObject body = new JsonObject();
        body.addProperty(MilvusCommandKeys.REST_DB_NAME, StringUtils.isBlank(database) ? "default" : database);
        body.addProperty(MilvusCommandKeys.REST_JOB_ID, jobId);
        return this.post("describe", body, request, timeoutMs);
    }

    public JsonObject list(String database, String collection, Long pageSize, Long currentPage, AdapterRequest request, long timeoutMs) throws SQLException {
        JsonObject body = new JsonObject();
        body.addProperty(MilvusCommandKeys.REST_DB_NAME, StringUtils.isBlank(database) ? "default" : database);
        body.addProperty(MilvusCommandKeys.REST_COLLECTION_NAME, collection);
        if (pageSize != null) {
            body.addProperty(MilvusCommandKeys.REST_PAGE_SIZE, pageSize);
        }
        if (currentPage != null) {
            body.addProperty(MilvusCommandKeys.REST_CURRENT_PAGE, currentPage);
        }
        return this.post("list", body, request, timeoutMs);
    }

    private JsonObject post(String operation, JsonObject body, AdapterRequest request, long timeoutMs) throws SQLException {
        MilvusRequest.checkActive(request);
        URI endpoint = this.baseUri.resolve("/v2/vectordb/jobs/import/" + operation);
        HttpRequest.Builder builder = HttpRequest.newBuilder(endpoint).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body.toString()));
        long deadline = this.config.getRpcDeadlineMs();
        if (deadline <= 0 || (timeoutMs > 0 && timeoutMs < deadline)) {
            deadline = timeoutMs;
        }
        if (deadline > 0) {
            builder.timeout(Duration.ofMillis(deadline));
        }
        String token = this.config.getToken();
        if (StringUtils.isBlank(token) && StringUtils.isNotBlank(this.config.getUsername())) {
            token = this.config.getUsername() + ":" + (this.config.getPassword() == null ? "" : this.config.getPassword());
        }
        if (StringUtils.isNotBlank(token)) {
            builder.header("Authorization", "Bearer " + token);
        }

        CompletableFuture<HttpResponse<String>> response = this.client.sendAsync(builder.build(), HttpResponse.BodyHandlers.ofString());
        try {
            HttpResponse<String> result;
            while (true) {
                MilvusRequest.checkActive(request);
                try {
                    result = response.get(100, TimeUnit.MILLISECONDS);
                    break;
                } catch (TimeoutException e) {
                    // Poll cancellation while the HTTP request is in flight.
                }
            }
            MilvusRequest.checkActive(request);
            if (result.statusCode() < 200 || result.statusCode() >= 300) {
                throw new SQLException("Milvus import HTTP request failed: status=" + result.statusCode());
            }
            JsonObject envelope = JsonParser.parseString(result.body()).getAsJsonObject();
            int code = envelope.has(MilvusCommandKeys.REST_CODE) ? envelope.get(MilvusCommandKeys.REST_CODE).getAsInt() : -1;
            if (code != 0 && code != 200) {
                String message = envelope.has(MilvusCommandKeys.REST_MESSAGE) ? envelope.get(MilvusCommandKeys.REST_MESSAGE).getAsString() : "Unknown import error";
                throw new SQLException("Milvus import failed: " + MilvusCommandKeys.REST_CODE + "=" + code + ", " + MilvusCommandKeys.REST_MESSAGE + "=" + message);
            }
            if (!envelope.has(MilvusCommandKeys.REST_DATA) || !envelope.get(MilvusCommandKeys.REST_DATA).isJsonObject()) {
                throw new SQLException("Milvus import response did not contain " + MilvusCommandKeys.REST_DATA + ".");
            }
            return envelope.getAsJsonObject(MilvusCommandKeys.REST_DATA);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interrupted while executing Milvus import.", e);
        } catch (ExecutionException | RuntimeException e) {
            throw new SQLException("Milvus import request failed: " + e.getMessage(), e);
        } finally {
            response.cancel(true);
        }
    }
}
