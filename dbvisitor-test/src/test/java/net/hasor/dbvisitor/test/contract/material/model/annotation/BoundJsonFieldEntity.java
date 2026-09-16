/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.material.model.annotation;

import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;
import net.hasor.dbvisitor.test.contract.material.model.types.JsonAnnotatedBean;

/** The field mapping relies on the business type's @BindTypeHandler, not a field-level handler. */
@Table("test_special_types")
public class BoundJsonFieldEntity {
    @Column(primary = true)
    private int               id;
    @Column("json_map")
    private JsonAnnotatedBean details;

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public JsonAnnotatedBean getDetails() {
        return this.details;
    }

    public void setDetails(JsonAnnotatedBean details) {
        this.details = details;
    }
}
