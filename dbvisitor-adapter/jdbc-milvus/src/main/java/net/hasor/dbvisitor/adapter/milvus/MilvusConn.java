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
import io.milvus.client.MilvusClient;
import io.milvus.grpc.GetVersionResponse;
import io.milvus.param.R;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.concurrent.future.BasicFuture;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.logging.LoggerFactory;
import net.hasor.dbvisitor.adapter.milvus.parser.*;
import net.hasor.dbvisitor.driver.*;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CharStreams;

public class MilvusConn extends AdapterConnection {
    private static final Logger     logger    = LoggerFactory.getLogger(MilvusConn.class);
    private final        Connection owner;
    private final        MilvusCmd  milvusCmd;
    private volatile     boolean    cancelled = false;

    public MilvusConn(Connection owner, MilvusCmd milvusCmd, String jdbcUrl, Map<String, String> prop) {
        super(jdbcUrl, prop.get(MilvusKeys.USERNAME));
        this.owner = owner;
        this.milvusCmd = milvusCmd;
    }

    @Override
    public int getDefaultGeneratedKeys() {
        return Statement.NO_GENERATED_KEYS;
    }

    public void initConnection() {
        AdapterInfo info = this.getInfo();
        info.getDriverVersion().setName(MilvusKeys.DEFAULT_CLIENT_NAME);
        info.getDbVersion().setName("Milvus");

        try {
            R<GetVersionResponse> version = this.milvusCmd.getClient().getVersion();
            if (version.getStatus() == R.Status.Success.getCode()) {
                String ver = version.getData().getVersion();
                info.getDbVersion().setVersion(ver);

                if (ver != null && ver.startsWith("v")) {
                    ver = ver.substring(1);
                    String[] parts = ver.split("\\.");
                    if (parts.length > 0)
                        info.getDbVersion().setMajorVersion(Integer.parseInt(parts[0]));
                    if (parts.length > 1)
                        info.getDbVersion().setMinorVersion(Integer.parseInt(parts[1]));
                }
            } else {
                info.getDbVersion().setVersion("Unknown (" + version.getMessage() + ")");
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
        return new MilvusRequest(sql);
    }

    @Override
    protected <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface == MilvusConn.class) {
            return (T) this;
        } else if (iface == MilvusCmd.class) {
            return (T) this.milvusCmd;
        } else if (iface == MilvusClient.class) {
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
        this.cancelled = false;
        MilvusParser.RootContext root = parserRequest(request);
        MilvusArgVisitor argVisitor = new MilvusArgVisitor();
        root.accept(argVisitor);
        int argCount = argVisitor.getArgCount();
        List<MilvusParser.HintCommandContext> commandList = argVisitor.getCommandList();

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
            if (this.cancelled) {
                throw new SQLException("Operation cancelled.", JdbcErrorCode.SQL_STATE_IS_CANCELLED);
            }
            Future<Object> sync = new BasicFuture<>();
            if (argCount > 0) {
                argVisitor.reset();
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
        this.cancelled = true;
    }

    @Override
    protected void doClose() {
        this.cancelRequest();
        this.milvusCmd.close();
    }
}
