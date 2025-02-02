package net.hasor.dbvisitor.session;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.io.IOUtils;
import net.hasor.dbvisitor.JdbcHelper;
import net.hasor.dbvisitor.dialect.*;
import net.hasor.dbvisitor.types.SqlArg;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;

class SessionHelper {
    public static PageSqlDialect findDialect(Connection conn) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();
        String tmpDbType = JdbcHelper.getDbType(metaData.getURL(), metaData.getDriverName());
        SqlDialect tempDialect = SqlDialectRegister.findOrCreate(tmpDbType);
        return (!(tempDialect instanceof PageSqlDialect)) ? DefaultSqlDialect.DEFAULT : (PageSqlDialect) tempDialect;
    }

    public static boolean usingPage(Page pageInfo) {
        return pageInfo != null && pageInfo.getPageSize() > 0;
    }

    public static StringBuilder fmtBoundSql(BoundSql sqlBuilder) {
        StringBuilder builder = new StringBuilder("querySQL: ");

        try {
            if (sqlBuilder == null) {
                builder.append("(Empty)");
                return builder;
            } else {
                List<String> lines = IOUtils.readLines(new StringReader(sqlBuilder.getSqlString()));
                for (String line : lines) {
                    if (StringUtils.isNotBlank(line)) {
                        builder.append(line.trim()).append(" ");
                    }
                }
            }
        } catch (Exception e) {
            builder.append(sqlBuilder.getSqlString().replace("\n", ""));
        }

        builder.append(", parameter: [");
        int i = 0;
        for (Object arg : sqlBuilder.getArgs()) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(fmtValue(arg));
            i++;
        }
        builder.append("] ");

        return builder;
    }

    private static String fmtValue(Object value) {
        Object object = value instanceof SqlArg ? ((SqlArg) value).getValue() : value;
        if (object == null) {
            return "null";
        } else if (object instanceof String) {
            if (((String) object).length() > 2048) {
                return "'" + ((String) object).substring(0, 2048) + "...'";
            } else {
                return "'" + ((String) object).replace("'", "\\'") + "'";
            }
        } else if (object instanceof Page) {
            return "page[pageSize=" + ((Page) object).getPageSize()//
                    + ", currentPage=" + ((Page) object).getCurrentPage()//
                    + ", pageNumberOffset=" + ((Page) object).getPageNumberOffset() + "]";
        }
        return object.toString();
    }
}
