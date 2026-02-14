package net.hasor.dbvisitor.test.model.naming;

import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

/**
 * Entity for case_test_lower table — all identifiers lowercase.
 * Used by CaseSensitiveTest for real DB cross-case verification.
 */
@Table(value = "case_test_lower", caseInsensitive = true)
public class CaseTestLower {
    @Column(primary = true)
    private Integer id;
    private String  name;
    private Integer age;
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
