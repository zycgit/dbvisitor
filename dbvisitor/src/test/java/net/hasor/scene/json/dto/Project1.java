/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.scene.json.dto;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.KeyTypeEnum;
import net.hasor.dbvisitor.mapping.Table;
import net.hasor.dbvisitor.types.handler.JsonTypeHandler;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2013-12-10
 */
@Table(table = "project")
public class Project1 {
    @Column(keyType = KeyTypeEnum.Auto)
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