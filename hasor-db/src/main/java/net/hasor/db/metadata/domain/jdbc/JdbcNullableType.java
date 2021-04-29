package net.hasor.db.metadata.domain.jdbc;
import java.sql.DatabaseMetaData;

/**
 * Jdbc 规定的空值类型
 * @version : 2020-04-25
 * @author 赵永春 (zyc@hasor.net)
 */
public enum JdbcNullableType {
    /** might not allow NULL values */
    ColumnNoNull(DatabaseMetaData.columnNoNulls),
    /** definitely allows NULL values */
    ColumnNullable(DatabaseMetaData.columnNullable),
    /** nullability unknown */
    ColumnNullableUnknown(DatabaseMetaData.columnNullableUnknown);
    private final int typeNumber;

    JdbcNullableType(int typeNumber) {
        this.typeNumber = typeNumber;
    }

    public int getTypeNumber() {
        return this.typeNumber;
    }

    public static JdbcNullableType valueOfCode(int typeNumber) {
        for (JdbcNullableType tableType : JdbcNullableType.values()) {
            if (tableType.typeNumber == typeNumber) {
                return tableType;
            }
        }
        return ColumnNullableUnknown;
    }
}