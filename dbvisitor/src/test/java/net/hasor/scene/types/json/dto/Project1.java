/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.scene.types.json.dto;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.KeyType;
import net.hasor.dbvisitor.mapping.Table;
import net.hasor.dbvisitor.types.handler.json.JsonTypeHandler;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2013-12-10
 */
@Table(table = "project")
public class Project1 {
    @Column(keyType = KeyType.Auto)
    private Integer        id;
    @Column
    private String         name;
    @Column(typeHandler = JsonTypeHandler.class)
    private ProjectFeature feature;

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

    public ProjectFeature getFeature() {
        return feature;
    }

    public void setFeature(ProjectFeature feature) {
        this.feature = feature;
    }
}
