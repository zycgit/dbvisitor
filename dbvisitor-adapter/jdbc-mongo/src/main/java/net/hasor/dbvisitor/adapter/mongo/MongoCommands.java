package net.hasor.dbvisitor.adapter.mongo;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.driver.AdapterType;
import net.hasor.dbvisitor.driver.JdbcColumn;

abstract class MongoCommands {
    // value (value / element / the value hash)
    protected static final JdbcColumn COL_VALUE_STRING   = new JdbcColumn("VALUE", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_VALUE_LONG     = new JdbcColumn("VALUE", AdapterType.Long, "", "", "");
    protected static final JdbcColumn COL_VALUE_BYTES    = new JdbcColumn("VALUE", AdapterType.Bytes, "", "", "");
    protected static final JdbcColumn COL_ELEMENT_STRING = new JdbcColumn("ELEMENT", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_RANK_LONG      = new JdbcColumn("RANK", AdapterType.Long, "", "", "");
    protected static final JdbcColumn COL_SCORE_DOUBLE   = new JdbcColumn("SCORE", AdapterType.Double, "", "", "");
    // result (not value)
    protected static final JdbcColumn COL_RESULT_BOOLEAN = new JdbcColumn("RESULT", AdapterType.Boolean, "", "", "");
    protected static final JdbcColumn COL_RESULT_STRING  = new JdbcColumn("RESULT", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_RESULT_LONG    = new JdbcColumn("RESULT", AdapterType.Long, "", "", "");
    protected static final JdbcColumn COL_RESULT_DOUBLE  = new JdbcColumn("RESULT", AdapterType.Double, "", "", "");
    // other
    protected static final JdbcColumn COL_KEY_STRING     = new JdbcColumn("KEY", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_FIELD_STRING   = new JdbcColumn("FIELD", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_CURSOR_STRING  = new JdbcColumn("CURSOR", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_LOCAL_LONG     = new JdbcColumn("LOCAL", AdapterType.Long, "", "", "");
    protected static final JdbcColumn COL_REPLICAS_LONG  = new JdbcColumn("REPLICAS", AdapterType.Long, "", "", "");
    protected static final JdbcColumn COL_GROUP_STRING   = new JdbcColumn("GROUP", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_NAME_STRING    = new JdbcColumn("NAME", AdapterType.String, "", "", "");

    protected static Future<?> completed(Future<Object> sync) {
        sync.completed(true);
        return sync;
    }

    public static Future<?> failed(Future<Object> sync, Exception e) {
        sync.failed(e);
        return sync;
    }

}