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
package net.hasor.dbvisitor.mapping.resolve;
import net.hasor.cobble.asm.AnnotationVisitor;

import java.util.Map;

/**
 * 注解属性挖掘（注解的注解不挖取）
 * @version : 2021-06-21
 * @author 赵永春 (zyc@hasor.net)
 */
class TableDefaultVisitor extends AnnotationVisitor {
    private final Map<String, String> allAnnoInfo;

    public TableDefaultVisitor(int api, AnnotationVisitor av, Map<String, String> allAnnoInfo) {
        super(api, av);
        this.allAnnoInfo = allAnnoInfo;
    }

    public void visit(String name, Object value) {
        allAnnoInfo.put(name, value.toString());
    }
}