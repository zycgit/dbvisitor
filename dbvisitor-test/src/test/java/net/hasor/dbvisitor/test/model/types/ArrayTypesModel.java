package net.hasor.dbvisitor.test.model.types;

import java.sql.Types;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

/**
 * 数组类型测试模型 - 用户视角
 * 验证数组类型的映射（主要针对 H2 和 PostgreSQL）
 */
@Table("array_types_test")
public class ArrayTypesModel {
    @Column(name = "id", primary = true)
    private Integer id;

    // 基本类型数组 - 指定 jdbcType = Types.ARRAY 以匹配 ArrayTypeHandler
    @Column(name = "int_array", jdbcType = Types.ARRAY)
    private Integer[] intArray;

    @Column(name = "string_array", jdbcType = Types.ARRAY)
    private String[] stringArray;

    @Column(name = "float_array", jdbcType = Types.ARRAY)
    private Float[] floatArray;

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer[] getIntArray() {
        return intArray;
    }

    public void setIntArray(Integer[] intArray) {
        this.intArray = intArray;
    }

    public String[] getStringArray() {
        return stringArray;
    }

    public void setStringArray(String[] stringArray) {
        this.stringArray = stringArray;
    }

    public Float[] getFloatArray() {
        return floatArray;
    }

    public void setFloatArray(Float[] floatArray) {
        this.floatArray = floatArray;
    }
}
