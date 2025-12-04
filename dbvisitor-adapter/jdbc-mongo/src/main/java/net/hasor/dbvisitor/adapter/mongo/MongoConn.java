package net.hasor.dbvisitor.adapter.mongo;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.concurrent.future.BasicFuture;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.mongo.parser.*;
import net.hasor.dbvisitor.driver.*;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CharStreams;
import org.bson.Document;

public class MongoConn extends AdapterConnection {
    private final Connection owner;
    private final MongoCmd   mongoCmd;

    public MongoConn(Connection owner, MongoCmd mongoCmd, String jdbcUrl, Map<String, String> prop) {
        super(jdbcUrl, prop.get(MongoKeys.USERNAME));
        this.owner = owner;
        this.mongoCmd = mongoCmd;
    }

    protected Connection getOwner() {
        return this.owner;
    }

    public void initConnection() {
        AdapterInfo info = this.getInfo();
        info.getDriverVersion().setName(MongoKeys.DEFAULT_CLIENT_NAME);
        info.getDbVersion().setName("Mongo");
        info.getDbVersion().setVersion("Unknown");
        info.getDbVersion().setMajorVersion(0);
        info.getDbVersion().setMinorVersion(0);

        try {
            Document buildInfo = mongoCmd.getClient().getDatabase("admin").runCommand(new Document("buildInfo", 1));
            String version = buildInfo.getString("version");
            info.getDbVersion().setVersion(version);

            List<Integer> versionArray = (List<Integer>) buildInfo.get("versionArray");
            if (versionArray != null) {
                if (versionArray.size() >= 1) {
                    info.getDbVersion().setMajorVersion(versionArray.get(0));
                }
                if (versionArray.size() >= 2) {
                    info.getDbVersion().setMinorVersion(versionArray.get(1));
                }
            }
        } catch (Throwable ignored) {
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
        return this.mongoCmd.getCatalog();
    }

    @Override
    public void setSchema(String catalog) {
        this.mongoCmd.setCatalog(catalog);
    }

    @Override
    public AdapterRequest newRequest(String sql) {
        return new MongoRequest(sql);
    }

    @Override
    protected <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface == MongoConn.class) {
            return (T) this;
        } else if (iface == MongoCmd.class) {
            return (T) this.mongoCmd;
        } else if (iface == MongoClient.class) {
            return (T) this.mongoCmd.getClient();
        } else if (iface == MongoDatabase.class) {
            String catalog = this.mongoCmd.getCatalog();
            return StringUtils.isBlank(catalog) ? null : (T) this.mongoCmd.getMongoDB(catalog);
        } else {
            return super.unwrap(iface);
        }
    }

    protected MongoParser.MongoCommandsContext parserRequest(AdapterRequest request) throws SQLException {
        if (StringUtils.isBlank(((MongoRequest) request).getCommandBody())) {
            throw new SQLException("query command is empty.", JdbcErrorCode.SQL_STATE_QUERY_EMPTY);
        }
        try {
            MongoLexer lexer = new MongoLexer(CharStreams.fromString(((MongoRequest) request).getCommandBody()));
            lexer.removeErrorListeners();
            lexer.addErrorListener(ThrowingListener.INSTANCE);

            MongoParser parser = new MongoParser(new BufferedTokenStream(lexer));
            parser.removeErrorListeners();
            parser.addErrorListener(ThrowingListener.INSTANCE);
            return parser.mongoCommands();
        } catch (QueryParseException e) {
            String errorMsg = "command '" + ((MongoRequest) request).getCommandBody() + "' parserFailed.";
            throw new SQLException(errorMsg, JdbcErrorCode.SQL_STATE_SYNTAX_ERROR);
        }
    }

    @Override
    public synchronized void doRequest(AdapterRequest request, AdapterReceive receive) throws SQLException {
        MongoParser.MongoCommandsContext root = parserRequest(request);
        MongoArgVisitor argVisitor = new MongoArgVisitor();
        root.accept(argVisitor);
        int argCount = argVisitor.getArgCount();
        List<MongoParser.CommandContext> commandList = argVisitor.getCommandList();

        if (commandList.isEmpty()) {
            throw new SQLException("query command is empty.", JdbcErrorCode.SQL_STATE_QUERY_EMPTY);
        }
        if (argCount > 0) {
            if (argCount != request.getArgMap().size()) {
                throw new SQLException("param size not match.", JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
            }
        }

        int startArgIdx = 0;
        for (MongoParser.CommandContext mongoCmd : commandList) {
            Future<Object> sync = new BasicFuture<>();
            if (argCount > 0) {
                argVisitor.reset();
                mongoCmd.accept(argVisitor);
                MongoDistributeCall.execMongoCmd(sync, this.mongoCmd, mongoCmd, request, receive, startArgIdx, this);
                startArgIdx += argVisitor.getArgCount();
            } else {
                MongoDistributeCall.execMongoCmd(sync, this.mongoCmd, mongoCmd, request, receive, startArgIdx, this);
            }
            sync.await();
        }

        receive.responseFinish(request);
    }

    @Override
    public void cancelRequest() {
        throw new UnsupportedOperationException("cancelRequest not support."); // TODO
    }

    @Override
    protected void doClose() throws IOException {
        // this.cancelRequest();
        this.mongoCmd.close();
    }
}
