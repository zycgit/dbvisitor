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
import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.logging.LoggerFactory;
import net.hasor.dbvisitor.adapter.mongo.parser.*;
import net.hasor.dbvisitor.driver.*;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CharStreams;
import org.bson.Document;

public class MongoConn extends AdapterConnection {
    private static final Logger       logger    = LoggerFactory.getLogger(MongoConn.class);
    private final        Connection   owner;
    private final        MongoCmd     mongoCmd;
    private final        boolean      preRead;
    private final        long         preReadThreshold;
    private final        long         preReadMaxFileSize;
    private final        java.io.File preReadCacheDir;
    private volatile     boolean      cancelled = false;

    public MongoConn(Connection owner, MongoCmd mongoCmd, String jdbcUrl, Map<String, String> prop) {
        super(jdbcUrl, prop.get(MongoKeys.USERNAME));
        this.owner = owner;
        this.mongoCmd = mongoCmd;

        this.preRead = "true".equalsIgnoreCase(prop.getOrDefault(MongoKeys.PREREAD_ENABLED, "true"));
        this.preReadThreshold = parseSize(prop.get(MongoKeys.PREREAD_THRESHOLD), 5 * 1024 * 1024); // Default 5MB
        this.preReadMaxFileSize = parseSize(prop.get(MongoKeys.PREREAD_MAX_FILE_SIZE), 20 * 1024 * 1024); // Default 20MB
        String cacheDirStr = prop.get(MongoKeys.PREREAD_CACHE_DIR);
        this.preReadCacheDir = StringUtils.isBlank(cacheDirStr) ? new java.io.File(System.getProperty("java.io.tmpdir")) : new java.io.File(cacheDirStr);
    }

    private long parseSize(String sizeStr, long defaultValue) {
        if (StringUtils.isBlank(sizeStr)) {
            return defaultValue;
        }
        sizeStr = sizeStr.toUpperCase().trim();
        long multiplier = 1;
        if (sizeStr.endsWith("KB")) {
            multiplier = 1024;
            sizeStr = sizeStr.substring(0, sizeStr.length() - 2);
        } else if (sizeStr.endsWith("MB")) {
            multiplier = 1024 * 1024;
            sizeStr = sizeStr.substring(0, sizeStr.length() - 2);
        } else if (sizeStr.endsWith("GB")) {
            multiplier = 1024 * 1024 * 1024;
            sizeStr = sizeStr.substring(0, sizeStr.length() - 2);
        } else if (sizeStr.endsWith("B")) {
            sizeStr = sizeStr.substring(0, sizeStr.length() - 1);
        }
        try {
            return Long.parseLong(sizeStr.trim()) * multiplier;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public boolean isPreRead() {
        return this.preRead;
    }

    public long getPreReadThreshold() {
        return this.preReadThreshold;
    }

    public long getPreReadMaxFileSize() {
        return this.preReadMaxFileSize;
    }

    public java.io.File getPreReadCacheDir() {
        return this.preReadCacheDir;
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
            if (buildInfo == null || !buildInfo.containsKey("version")) {
                logger.warn("initConnection failed: buildInfo is null or version not found");
                return;
            }

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
        } catch (Throwable e) {
            logger.warn("initConnection failed: " + e.getMessage(), e);
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
        MongoRequest request = new MongoRequest(sql, this.preRead);
        return request;
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
        if (logger.isDebugEnabled()) {
            logger.debug("doRequest: " + ((MongoRequest) request).getCommandBody());
        }
        this.cancelled = false;
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
            if (this.cancelled) {
                throw new SQLException("Operation cancelled.", JdbcErrorCode.SQL_STATE_IS_CANCELLED);
            }
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
        this.cancelled = true;
    }

    @Override
    protected void doClose() throws IOException {
        this.cancelRequest();
        this.mongoCmd.close();
    }
}
