package net.hasor.dbvisitor.test.oneapi.model.types;

import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

/**
 * 二进制类型测试模型 - 用户视角
 * 验证 BLOB/BINARY 类型的映射
 */
@Table("binary_types_test")
public class BinaryTypesModel {
    @Column(name = "id", primary = true)
    private Integer id;

    // 字节数组 - 映射到 VARBINARY/BLOB
    @Column(name = "binary_data")
    private byte[] binaryData;

    @Column(name = "small_blob")
    private byte[] smallBlob;

    @Column(name = "medium_blob")
    private byte[] mediumBlob;

    @Column(name = "large_blob")
    private byte[] largeBlob;

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public byte[] getBinaryData() {
        return binaryData;
    }

    public void setBinaryData(byte[] binaryData) {
        this.binaryData = binaryData;
    }

    public byte[] getSmallBlob() {
        return smallBlob;
    }

    public void setSmallBlob(byte[] smallBlob) {
        this.smallBlob = smallBlob;
    }

    public byte[] getMediumBlob() {
        return mediumBlob;
    }

    public void setMediumBlob(byte[] mediumBlob) {
        this.mediumBlob = mediumBlob;
    }

    public byte[] getLargeBlob() {
        return largeBlob;
    }

    public void setLargeBlob(byte[] largeBlob) {
        this.largeBlob = largeBlob;
    }
}
