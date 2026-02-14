package net.hasor.dbvisitor.test.model.types;

import java.util.*;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;
import net.hasor.dbvisitor.types.handler.json.JsonTypeHandler;

@Table(value = "test_special_types")
public class SpecialTypeEntity {
    @Column(primary = true)
    private int id;

    // Map -> JSON, 指定具体实现为 LinkedHashMap
    @Column(value = "json_map", typeHandler = JsonTypeHandler.class, specialJavaType = LinkedHashMap.class)
    private Map<String, Object> jsonMap;

    // List -> JSON, 指定具体实现为 LinkedList
    @Column(value = "json_list", typeHandler = JsonTypeHandler.class, specialJavaType = LinkedList.class)
    private List<String> jsonList;

    // Set -> JSON, 指定具体实现为 HashSet
    @Column(value = "json_set", typeHandler = JsonTypeHandler.class, specialJavaType = HashSet.class)
    private Set<String> jsonSet;

    // List -> Native Array (INTEGER[])
    // 使用 Integer[] 对应 DB Array
    @Column(value = "int_array")
    private Integer[] intArray;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Map<String, Object> getJsonMap() {
        return jsonMap;
    }

    public void setJsonMap(Map<String, Object> jsonMap) {
        this.jsonMap = jsonMap;
    }

    public List<String> getJsonList() {
        return jsonList;
    }

    public void setJsonList(List<String> jsonList) {
        this.jsonList = jsonList;
    }

    public Set<String> getJsonSet() {
        return jsonSet;
    }

    public void setJsonSet(Set<String> jsonSet) {
        this.jsonSet = jsonSet;
    }

    public Integer[] getIntArray() {
        return intArray;
    }

    public void setIntArray(Integer[] intArray) {
        this.intArray = intArray;
    }
}
