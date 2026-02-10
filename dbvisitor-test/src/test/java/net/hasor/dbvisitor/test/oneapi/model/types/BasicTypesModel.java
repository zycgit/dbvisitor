package net.hasor.dbvisitor.test.oneapi.model.types;

import java.math.BigDecimal;
import java.math.BigInteger;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

/**
 * 基本类型测试模型 - 用户视角（无 jdbcType 注解，依赖框架自动推断）
 * 验证 dbVisitor 的类型自动推断能力
 */
@Table("basic_types_test")
public class BasicTypesModel {
    @Column(name = "id", primary = true)
    private Integer id;

    // 数值类型 - 依赖框架自动推断
    @Column(name = "byte_value")
    private Byte       byteValue;
    @Column(name = "short_value")
    private Short      shortValue;
    @Column(name = "int_value")
    private Integer    intValue;
    @Column(name = "long_value")
    private Long       longValue;
    @Column(name = "float_value")
    private Float      floatValue;
    @Column(name = "double_value")
    private Double     doubleValue;
    @Column(name = "decimal_value")
    private BigDecimal decimalValue;
    @Column(name = "big_int_value")
    private BigInteger bigIntValue;

    // 布尔类型
    @Column(name = "bool_value")
    private Boolean boolValue;

    // 字符类型
    @Column(name = "string_value")
    private String    stringValue;
    @Column(name = "char_value")
    private Character charValue;

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

    public Boolean getBoolValue() {
        return boolValue;
    }

    public void setBoolValue(Boolean boolValue) {
        this.boolValue = boolValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public Character getCharValue() {
        return charValue;
    }

    public void setCharValue(Character charValue) {
        this.charValue = charValue;
    }
}
