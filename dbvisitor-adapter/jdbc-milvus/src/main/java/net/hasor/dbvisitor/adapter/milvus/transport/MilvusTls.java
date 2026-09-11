/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.milvus.transport;
import java.io.File;
import java.security.KeyManagementException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Map;
import javax.net.ssl.*;
import io.milvus.param.ServerAddress;
import io.milvus.shaded.io.grpc.netty.shaded.io.netty.handler.ssl.JdkSslContext;
import io.milvus.shaded.io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.milvus.shaded.io.grpc.netty.shaded.io.netty.handler.ssl.SslProvider;
import io.milvus.v2.client.ConnectConfig;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.adapter.milvus.MilvusKeys;

/** JDBC TLS settings shared by the official gRPC client and the Import HTTPS client. */
public final class MilvusTls {
    private MilvusTls() {
    }

    public static void configure(ConnectConfig.ConnectConfigBuilder builder, Map<String, String> properties, ServerAddress address) throws SQLException {
        String ca = property(properties, MilvusKeys.CA_PEM_PATH);
        String server = property(properties, MilvusKeys.SERVER_PEM_PATH);
        String certificate = property(properties, MilvusKeys.CLIENT_PEM_PATH);
        String key = property(properties, MilvusKeys.CLIENT_KEY_PATH);
        String name = property(properties, MilvusKeys.SERVER_NAME);
        boolean configured = ca != null || server != null || certificate != null || key != null || name != null;
        boolean secure = secure(properties, configured);

        if (ca != null && server != null) {
            throw new SQLException("Use either " + MilvusKeys.CA_PEM_PATH + " or " + MilvusKeys.SERVER_PEM_PATH + ", not both.");
        }
        if ((certificate == null) != (key == null)) {
            throw new SQLException(MilvusKeys.CLIENT_PEM_PATH + " and " + MilvusKeys.CLIENT_KEY_PATH + " must be configured together.");
        }
        if (certificate != null && ca == null) {
            throw new SQLException("Mutual TLS requires " + MilvusKeys.CA_PEM_PATH + ", " + MilvusKeys.CLIENT_PEM_PATH + " and " + MilvusKeys.CLIENT_KEY_PATH + ".");
        }
        if (name != null && !name.equalsIgnoreCase(address.getHost())) {
            if (ca == null && server == null) {
                throw new SQLException("A " + MilvusKeys.SERVER_NAME + " override requires " + MilvusKeys.CA_PEM_PATH + " or " + MilvusKeys.SERVER_PEM_PATH + " with SDK 2.6.22.");
            }
            try {
                new SNIHostName(name);
            } catch (IllegalArgumentException e) {
                throw new SQLException(MilvusKeys.SERVER_NAME + " must be a DNS name, without a scheme or port.", e);
            }
        }

        String scheme = secure ? "https://" : "http://";
        builder.uri(scheme + address.getHost() + ":" + address.getPort()).secure(secure);
        if (!secure) {
            return;
        }

        // SDK 2.6.22 uses serverPemPath for one-way trust (a server certificate or CA bundle).
        // Its caPemPath branch is selected only when both client identity files are supplied.
        builder.serverName(name == null ? address.getHost() : name);
        if (certificate == null) {
            builder.serverPemPath(ca == null ? server : ca);
        } else {
            builder.caPemPath(ca).clientPemPath(certificate).clientKeyPath(key);
        }
    }

    private static boolean secure(Map<String, String> properties, boolean configured) throws SQLException {
        String value = property(properties, MilvusKeys.SECURE);
        if (value == null && !properties.containsKey(MilvusKeys.SECURE)) {
            return configured;
        }
        if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
            throw new SQLException("Milvus connection property '" + MilvusKeys.SECURE + "' must be true or false.");
        }
        boolean enabled = Boolean.parseBoolean(value);
        if (!enabled && configured) {
            throw new SQLException("TLS certificate/" + MilvusKeys.SERVER_NAME + " properties cannot be combined with " + MilvusKeys.SECURE + "=false.");
        }
        return enabled;
    }

    private static String property(Map<String, String> properties, String key) {
        String value = properties.get(key);
        return StringUtils.isBlank(value) ? null : value.trim();
    }

    public static SSLContext httpContext(ConnectConfig config) throws SQLException {
        if (!Boolean.TRUE.equals(config.isSecure())) {
            return null;
        }
        try {
            String trust = StringUtils.isNotBlank(config.getServerPemPath()) ? config.getServerPemPath() : config.getCaPemPath();
            SSLContext context;
            if (StringUtils.isBlank(trust) && StringUtils.isBlank(config.getClientPemPath())) {
                context = SSLContext.getDefault();
            } else {
                // Reuse the SDK's PEM parser; validate material before opening any network connection.
                SslContextBuilder builder = SslContextBuilder.forClient().sslProvider(SslProvider.JDK);
                if (StringUtils.isNotBlank(trust)) {
                    builder.trustManager(new File(trust));
                }
                if (StringUtils.isNotBlank(config.getClientPemPath())) {
                    builder.keyManager(new File(config.getClientPemPath()), new File(config.getClientKeyPath()));
                }
                context = ((JdkSslContext) builder.build()).context();
            }
            String name = config.getServerName();
            return StringUtils.isBlank(name) || name.equalsIgnoreCase(config.getHost()) ? context : new NamedContext(context, name);
        } catch (Exception e) {
            throw new SQLException("Invalid Milvus TLS certificate or private key configuration.", e);
        }
    }

    /**
     * HttpClient still opens TCP to the JDBC address; JSSE checks the configured TLS peer name.
     * Setting SNI alone is insufficient: JSSE can otherwise fall back to the URI hostname.
     * Only engine creation changes. Certificate-chain and hostname verification stay with JSSE.
     */
    private static final class NamedContext extends SSLContext {
        NamedContext(SSLContext delegate, String name) {
            super(new NamedContextSpi(delegate, name), delegate.getProvider(), delegate.getProtocol());
        }
    }

    private static final class NamedContextSpi extends SSLContextSpi {
        private final SSLContext delegate;
        private final String     name;

        NamedContextSpi(SSLContext delegate, String name) {
            this.delegate = delegate;
            this.name = name;
        }

        @Override
        protected SSLEngine engineCreateSSLEngine(String host, int port) {
            return this.delegate.createSSLEngine(this.name, port);
        }

        @Override
        protected SSLEngine engineCreateSSLEngine() {
            return this.delegate.createSSLEngine(this.name, -1);
        }

        @Override
        protected void engineInit(KeyManager[] keys, TrustManager[] trust, SecureRandom random) throws KeyManagementException {
            this.delegate.init(keys, trust, random);
        }

        @Override
        protected SSLSocketFactory engineGetSocketFactory() {
            return this.delegate.getSocketFactory();
        }

        @Override
        protected SSLServerSocketFactory engineGetServerSocketFactory() {
            return this.delegate.getServerSocketFactory();
        }

        @Override
        protected SSLSessionContext engineGetClientSessionContext() {
            return this.delegate.getClientSessionContext();
        }

        @Override
        protected SSLSessionContext engineGetServerSessionContext() {
            return this.delegate.getServerSessionContext();
        }

        @Override
        protected SSLParameters engineGetDefaultSSLParameters() {
            return this.delegate.getDefaultSSLParameters();
        }

        @Override
        protected SSLParameters engineGetSupportedSSLParameters() {
            return this.delegate.getSupportedSSLParameters();
        }
    }
}
