/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.lambda.dto;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2013-12-10
 */
@Table("point_table")
public class PointTable {
    private Integer id;

    @Column(selectTemplate = "AsText(point)",  // 会生成 select AsText(point) as point
            insertTemplate = "GeomFromText(?)",// 会生成 insert ... values (GeomFromText(?))
            setValueTemplate = "GeomFromText(?)",// 会生成 update ... set point = GeomFromText(?)
            whereColTemplate = "AsText(point)"// 会生成 ... where AsText(point) = ?
    )
    private String point;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPoint() {
        return point;
    }

    public void setPoint(String point) {
        this.point = point;
    }
}
