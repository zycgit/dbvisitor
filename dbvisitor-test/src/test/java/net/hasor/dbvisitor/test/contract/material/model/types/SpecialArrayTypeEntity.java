package net.hasor.dbvisitor.test.contract.material.model.types;

import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

@Table(value = "test_special_types")
public class SpecialArrayTypeEntity {
    @Column(primary = true)
    private int       id;
    @Column(value = "int_array")
    private Integer[] intArray;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer[] getIntArray() {
        return intArray;
    }

    public void setIntArray(Integer[] intArray) {
        this.intArray = intArray;
    }
}
