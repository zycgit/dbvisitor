package net.hasor.dbvisitor.test.oneapi.model.types;

import java.sql.Types;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

/**
 * 数组类型显式映射模型 - 数据库视角
 * 验证显式指定 jdbcType 的数组类型映射
 */
@Table("array_types_explicit_test")
public class ArrayTypesExplicitModel {
    @Column(name = "id", primary = true, jdbcType = Types.INTEGER)
    private Integer id;

    // 显式指定 ARRAY 类型
    @Column(name = "int_array", jdbcType = Types.ARRAY)
    private Integer[] intArray;

    @Column(name = "varchar_array", jdbcType = Types.ARRAY)
    private String[] varcharArray;

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

    public String[] getVarcharArray() {
        return varcharArray;
    }

    public void setVarcharArray(String[] varcharArray) {
        this.varcharArray = varcharArray;
    }
}
