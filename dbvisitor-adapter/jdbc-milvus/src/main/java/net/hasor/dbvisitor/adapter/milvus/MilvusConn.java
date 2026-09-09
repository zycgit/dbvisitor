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
package net.hasor.dbvisitor.adapter.milvus;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CharStreams;

import io.milvus.common.clientenum.ConsistencyLevelEnum;
import io.milvus.v2.client.MilvusClientV2;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.concurrent.future.BasicFuture;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.logging.LoggerFactory;
import net.hasor.dbvisitor.adapter.milvus.parser.*;
import net.hasor.dbvisitor.driver.*;

public class MilvusConn extends AdapterConnection {
    private static final String DEFAULT_CLIENT_NAME = "Milvus-JDBC-Client";
    private static final int    DEFAULT_MAX_RETRY   = 3;

    private static final Logger        logger         = LoggerFactory.getLogger(MilvusConn.class);
    private final Connection           owner;
    private final MilvusCmd            milvusCmd;
    private final ConsistencyLevelEnum consistencyLevel;
    private final int                  maxRetry;
    private final Set<MilvusRequest>   activeRequests = ConcurrentHashMap.newKeySet();

    public MilvusConn(Connection owner, MilvusCmd milvusCmd, String jdbcUrl, Map<String, String> prop) throws SQLException {
        super(jdbcUrl, prop.get(MilvusKeys.USERNAME));
        this.owner = owner;
        this.milvusCmd = milvusCmd;

        String cl = prop.get(MilvusKeys.CONSISTENCY_LEVEL);
        if (StringUtils.isNotBlank(cl)) {
            this.consistencyLevel = ConsistencyLevelEnum.valueOf(cl.toUpperCase());
        } else {
            this.consistencyLevel = null;
        }

        String maxRetryText = prop.get(MilvusKeys.MAX_RETRY);
        try {
            this.maxRetry = StringUtils.isBlank(maxRetryText) ? DEFAULT_MAX_RETRY : Integer.parseInt(maxRetryText);
        } catch (NumberFormatException e) {
            throw new SQLException("Invalid Milvus connection property '" + MilvusKeys.MAX_RETRY + "': " + maxRetryText, e);
        }
        if (this.maxRetry < 0) {
            throw new SQLException("Milvus connection property '" + MilvusKeys.MAX_RETRY + "' must be greater than or equal to 0.");
        }
    }

    @Override
    public int getDefaultGeneratedKeys() {
        return Statement.NO_GENERATED_KEYS;
    }

    public void initConnection() {
        this.getFeatures().addFeature(net.hasor.dbvisitor.driver.AdapterFeatureKey.ReturnGeneratedKeys, false);
        AdapterInfo info = this.getInfo();
        info.getDriverVersion().setName(DEFAULT_CLIENT_NAME);
        info.getDbVersion().setName("Milvus");

        try {
            String ver = this.milvusCmd.getServerVersion();
            info.getDbVersion().setVersion(ver);
            if (ver != null) {
                String[] parts = (ver.startsWith("v") ? ver.substring(1) : ver).split("\\.");
                if (parts.length > 0) {
                    info.getDbVersion().setMajorVersion(Integer.parseInt(parts[0]));
                }
                if (parts.length > 1) {
                    info.getDbVersion().setMinorVersion(Integer.parseInt(parts[1]));
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to get Milvus version: " + e.getMessage());
            info.getDbVersion().setVersion("Unknown");
        }
    }

    @Override
    public String getCatalog() {
        return this.getSchema();
    }

    @Override
    public void setCatalog(String catalog) {
        this.setSchema(catalog);
    }

    @Override
    public String getSchema() {
        return this.milvusCmd.getCatalog();
    }

    @Override
    public void setSchema(String schema) {
        this.milvusCmd.setCatalog(schema);
    }

    @Override
    public AdapterRequest newRequest(String sql) {
        return new MilvusRequest(sql, this.consistencyLevel, this.maxRetry);
    }

    @Override
    protected <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface == MilvusConn.class) {
            return (T) this;
        } else if (iface == MilvusCmd.class) {
            return (T) this.milvusCmd;
        } else if (iface == MilvusClientV2.class) {
            return (T) this.milvusCmd.getClient();
        } else {
            return super.unwrap(iface);
        }
    }

    protected MilvusParser.RootContext parserRequest(AdapterRequest request) throws SQLException {
        if (StringUtils.isBlank(((MilvusRequest) request).getCommandBody())) {
            throw new SQLException("query command is empty.", JdbcErrorCode.SQL_STATE_QUERY_EMPTY);
        }
        try {
            MilvusLexer lexer = new MilvusLexer(CharStreams.fromString(((MilvusRequest) request).getCommandBody()));
            lexer.removeErrorListeners();
            lexer.addErrorListener(ThrowingListener.INSTANCE);

            MilvusParser parser = new MilvusParser(new BufferedTokenStream(lexer));
            parser.removeErrorListeners();
            parser.addErrorListener(ThrowingListener.INSTANCE);
            return parser.root();
        } catch (QueryParseException e) {
            String errorMsg = "command '" + ((MilvusRequest) request).getCommandBody() + "' parserFailed.";
            throw new SQLException(errorMsg, JdbcErrorCode.SQL_STATE_SYNTAX_ERROR);
        }
    }

    @Override
    public void doRequest(AdapterRequest request, AdapterReceive receive) throws SQLException {
        if (logger.isDebugEnabled()) {
            logger.debug("doRequest: " + ((MilvusRequest) request).getCommandBody());
        }
        MilvusRequest milvusRequest = (MilvusRequest) request;
        milvusRequest.begin(() -> this.activeRequests.remove(milvusRequest));
        this.activeRequests.add(milvusRequest);
        try {
            this.executeRequest(request, receive);
        } finally {
            milvusRequest.endExecution();
        }
    }

    private void executeRequest(AdapterRequest request, AdapterReceive receive) throws SQLException {
        MilvusParser.RootContext root = parserRequest(request);
        MilvusArgVisitor commandVisitor = new MilvusArgVisitor();
        root.accept(commandVisitor);
        int argCount = commandVisitor.getArgCount();
        List<MilvusParser.HintCommandContext> commandList = commandVisitor.getCommandList();

        if (commandList.isEmpty()) {
            throw new SQLException("query command is empty.", JdbcErrorCode.SQL_STATE_QUERY_EMPTY);
        }
        if (argCount > 0) {
            if (argCount != request.getArgMap().size()) {
                throw new SQLException("param size not match.", JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
            }
        }

        int startArgIdx = 0;
        for (MilvusParser.HintCommandContext milvusCmd : commandList) {
            ((MilvusRequest) request).checkActive();
            Future<Object> sync = new BasicFuture<>();
            if (argCount > 0) {
                MilvusArgVisitor argVisitor = new MilvusArgVisitor();
                milvusCmd.accept(argVisitor);
                MilvusDistributeCall.execMilvusCmd(sync, this.milvusCmd, milvusCmd, request, receive, startArgIdx, this);
                startArgIdx += argVisitor.getArgCount();
            } else {
                MilvusDistributeCall.execMilvusCmd(sync, this.milvusCmd, milvusCmd, request, receive, startArgIdx, this);
            }

            sync.await();

            if (sync.isDone() && sync.getCause() != null) {
                receive.responseFailed(request, sync.getCause());
                break;
            }
        }

        receive.responseFinish(request);
    }

    @Override
    public void cancelRequest() {
        this.activeRequests.forEach(MilvusRequest::cancel);
    }

    @Override
    public void cancelRequest(AdapterRequest request) {
        if (request instanceof MilvusRequest)
            ((MilvusRequest) request).cancel();
    }

    @Override
    protected void doClose() {
        this.cancelRequest();
        this.milvusCmd.close();
    }
}
