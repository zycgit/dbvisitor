package net.hasor.dbvisitor.test.oneapi.model.types;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Types;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

/**
 * 基本类型测试模型 - 数据库类型视角（显式指定 jdbcType）
 * 验证精确的类型控制和跨数据库兼容性
 */
@Table("basic_types_explicit_test")
public class BasicTypesExplicitModel {
    @Column(name = "id", primary = true)
    private Integer id;

    // 显式指定 JDBC 类型
    @Column(name = "byte_value", jdbcType = Types.TINYINT)
    private Byte byteValue;

    @Column(name = "short_value", jdbcType = Types.SMALLINT)
    private Short shortValue;

    @Column(name = "int_value", jdbcType = Types.INTEGER)
    private Integer intValue;

    @Column(name = "long_value", jdbcType = Types.BIGINT)
    private Long longValue;

    @Column(name = "float_value", jdbcType = Types.REAL)
    private Float floatValue;

    @Column(name = "double_value", jdbcType = Types.DOUBLE)
    private Double doubleValue;

    @Column(name = "decimal_value", jdbcType = Types.DECIMAL)
    private BigDecimal decimalValue;

    @Column(name = "big_int_value", jdbcType = Types.NUMERIC)
    private BigInteger bigIntValue;

    // 测试布尔类型在不同数据库的表现
    @Column(name = "bool_bit", jdbcType = Types.BIT)
    private Boolean boolBit;

    @Column(name = "bool_boolean", jdbcType = Types.BOOLEAN)
    private Boolean boolBoolean;

    // 测试字符类型的长度控制
    @Column(name = "char_value", jdbcType = Types.CHAR)
    private Character charValue;

    @Column(name = "varchar_value", jdbcType = Types.VARCHAR)
    private String varcharValue;

    @Column(name = "nvarchar_value", jdbcType = Types.NVARCHAR)
    private String nvarcharValue;

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Byte getByteValue() {
        return byteValue;
    }

    public void setByteValue(Byte byteValue) {
        this.byteValue = byteValue;
    }

    public Short getShortValue() {
        return shortValue;
    }

    public void setShortValue(Short shortValue) {
        this.shortValue = shortValue;
    }

    public Integer getIntValue() {
        return intValue;
    }

    public void setIntValue(Integer intValue) {
        this.intValue = intValue;
    }

    public Long getLongValue() {
        return longValue;
    }

    public void setLongValue(Long longValue) {
        this.longValue = longValue;
    }

    public Float getFloatValue() {
        return floatValue;
    }

    public void setFloatValue(Float floatValue) {
        this.floatValue = floatValue;
    }

    public Double getDoubleValue() {
        return doubleValue;
    }

    public void setDoubleValue(Double doubleValue) {
        this.doubleValue = doubleValue;
    }

    public BigDecimal getDecimalValue() {
        return decimalValue;
    }

    public void setDecimalValue(BigDecimal decimalValue) {
        this.decimalValue = decimalValue;
    }

    public BigInteger getBigIntValue() {
        return bigIntValue;
    }

    public void setBigIntValue(BigInteger bigIntValue) {
        this.bigIntValue = bigIntValue;
    }

    public Boolean getBoolBit() {
        return boolBit;
    }

    public void setBoolBit(Boolean boolBit) {
        this.boolBit = boolBit;
    }

    public Boolean getBoolBoolean() {
        return boolBoolean;
    }

    public void setBoolBoolean(Boolean boolBoolean) {
        this.boolBoolean = boolBoolean;
    }

    public Character getCharValue() {
        return charValue;
    }

    public void setCharValue(Character charValue) {
        this.charValue = charValue;
    }

    public String getVarcharValue() {
        return varcharValue;
    }

    public void setVarcharValue(String varcharValue) {
        this.varcharValue = varcharValue;
    }

    public String getNvarcharValue() {
        return nvarcharValue;
    }

    public void setNvarcharValue(String nvarcharValue) {
        this.nvarcharValue = nvarcharValue;
    }
}
