package net.hasor.dbvisitor.test.oneapi.model.types;

import java.sql.Types;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

/**
 * 二进制类型显式映射模型 - 数据库视角
 * 验证显式指定 jdbcType 的二进制类型映射
 */
@Table("binary_types_explicit_test")
public class BinaryTypesExplicitModel {
    @Column(name = "id", primary = true, jdbcType = Types.INTEGER)
    private Integer id;

    // 显式指定 JDBC 二进制类型
    @Column(name = "binary_value", jdbcType = Types.BINARY)
    private byte[] binaryValue;

    @Column(name = "varbinary_value", jdbcType = Types.VARBINARY)
    private byte[] varbinaryValue;

    @Column(name = "longvarbinary_value", jdbcType = Types.LONGVARBINARY)
    private byte[] longvarbinaryValue;

    @Column(name = "blob_value", jdbcType = Types.BLOB)
    private byte[] blobValue;

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public byte[] getBinaryValue() {
        return binaryValue;
    }

    public void setBinaryValue(byte[] binaryValue) {
        this.binaryValue = binaryValue;
    }

    public byte[] getVarbinaryValue() {
        return varbinaryValue;
    }

    public void setVarbinaryValue(byte[] varbinaryValue) {
        this.varbinaryValue = varbinaryValue;
    }

    public byte[] getLongvarbinaryValue() {
        return longvarbinaryValue;
    }

    public void setLongvarbinaryValue(byte[] longvarbinaryValue) {
        this.longvarbinaryValue = longvarbinaryValue;
    }

    public byte[] getBlobValue() {
        return blobValue;
    }

    public void setBlobValue(byte[] blobValue) {
        this.blobValue = blobValue;
    }
}
