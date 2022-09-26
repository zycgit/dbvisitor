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
package net.hasor.dbvisitor.faker.seed.geometry;
import net.hasor.cobble.StringUtils;

/**
 * 几何信息形态
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public enum GeometryType {
    /** 点 */
    Point,
    /** 线 */
    Line,
    /** 线段 */
    Lseg,
    /** 矩形 */
    Box,
    /** 路径 */
    Path,
    /** 多边形 */
    Polygon,
    /** 多个多边形 */
    MultiPolygon,
    /** 圆形 */
    Circle,
    /** 所有图形中的任意一种 */
    Random;

    public static GeometryType valueOfCode(String name) {
        for (GeometryType scope : GeometryType.values()) {
            if (StringUtils.equalsIgnoreCase(scope.name(), name)) {
                return scope;
            }
        }
        return null;
    }
}