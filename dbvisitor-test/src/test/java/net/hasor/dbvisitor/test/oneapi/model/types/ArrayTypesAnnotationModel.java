package net.hasor.dbvisitor.test.oneapi.model.types;

import java.sql.Types;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;
import net.hasor.dbvisitor.types.handler.array.ArrayTypeHandler;

/**
 * 数组类型注解测试模型
 * 测试不同 @Column 注解配置对数组类型映射的影响
 */
@Table("array_types_annotation_test")
public class ArrayTypesAnnotationModel {
    @Column(name = "id", primary = true)
    private Integer id;

    // 场景1: Column 注解不指定任何属性，依赖 TypeHandlerRegistry 的默认映射
    @Column(name = "array_no_annotation")
    private Integer[] arrayNoAnnotation;

    // 场景2: 只指定 jdbcType 属性
    @Column(name = "array_jdbc_type", jdbcType = Types.ARRAY)
    private Integer[] arrayJdbcType;

    // 场景3: 只指定 typeHandler 属性
    @Column(name = "array_type_handler", typeHandler = ArrayTypeHandler.class)
    private Integer[] arrayTypeHandler;

    // 场景4: Number[] 类型通过 specialJavaType 映射到 Integer[]
    @Column(name = "array_number_special", specialJavaType = Integer[].class)
    private Number[] arrayNumberSpecial;

    // 场景5: 同时指定 jdbcType 和 specialJavaType（完整配置）
    @Column(name = "array_full_annotated", jdbcType = Types.ARRAY, specialJavaType = Integer[].class)
    private Number[] arrayFullAnnotated;

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer[] getArrayNoAnnotation() {
        return arrayNoAnnotation;
    }

    public void setArrayNoAnnotation(Integer[] arrayNoAnnotation) {
        this.arrayNoAnnotation = arrayNoAnnotation;
    }

    public Integer[] getArrayJdbcType() {
        return arrayJdbcType;
    }

    public void setArrayJdbcType(Integer[] arrayJdbcType) {
        this.arrayJdbcType = arrayJdbcType;
    }

    public Integer[] getArrayTypeHandler() {
        return arrayTypeHandler;
    }

    public void setArrayTypeHandler(Integer[] arrayTypeHandler) {
        this.arrayTypeHandler = arrayTypeHandler;
    }

    public Number[] getArrayNumberSpecial() {
        return arrayNumberSpecial;
    }

    public void setArrayNumberSpecial(Number[] arrayNumberSpecial) {
        this.arrayNumberSpecial = arrayNumberSpecial;
    }

    public Number[] getArrayFullAnnotated() {
        return arrayFullAnnotated;
    }

    public void setArrayFullAnnotated(Number[] arrayFullAnnotated) {
        this.arrayFullAnnotated = arrayFullAnnotated;
    }
}
