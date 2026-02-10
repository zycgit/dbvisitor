package net.hasor.dbvisitor.test.oneapi.model.naming;

import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

/**
 * Entity for "Case_Test_Upper" table with caseInsensitive=false (strict mode).
 * Column annotations use lowercase names that deliberately mismatch the DB's mixed-case columns.
 * With caseInsensitive=false, ResultSet columns "Id","Name" won't match @Column("id","name").
 */
@Table(value = "Case_Test_Upper", useDelimited = true, caseInsensitive = false)
public class CaseTestUpperCS {
    @Column(value = "id", primary = true)
    private Integer id;
    @Column("name")
    private String  name;
    @Column("age")
    private Integer age;
    @Column("memo")
    private String  memo;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }
}
