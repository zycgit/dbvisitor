package net.hasor.dbvisitor.test.model.naming;

import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

/**
 * Entity for "Case_Test_Upper" table with caseInsensitive=true.
 * Column annotations use mixed-case names matching the quoted DB identifiers.
 * With caseInsensitive=true, ResultSet columns "Id","Name" match @Column("Id","Name") regardless of case.
 */
@Table(value = "Case_Test_Upper", useDelimited = true, caseInsensitive = true)
public class CaseTestUpperCI {
    @Column(value = "Id", primary = true)
    private Integer id;
    @Column("Name")
    private String  name;
    @Column("Age")
    private Integer age;
    @Column("Memo")
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
