package net.hasor.dbvisitor.test.oneapi.model.tabledef;

import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

/** caseInsensitive=false 的实体 */
@Table(value = "user_info", caseInsensitive = false)
public class CaseSensitiveUser {
    @Column(primary = true)
    private Integer id;
    private String  name;

    public Integer getId() { return id; }

    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }
}
