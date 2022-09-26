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
import net.hasor.dbvisitor.faker.seed.SeedConfig;
import net.hasor.dbvisitor.faker.seed.SeedType;
import net.hasor.dbvisitor.faker.seed.number.Ratio;
import net.hasor.dbvisitor.types.TypeHandler;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

/**
 * 几何图形 SeedConfig
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public class GeometrySeedConfig extends SeedConfig {
    private       GeometryType      geometryType;
    private final Ratio<SpaceRange> range = new Ratio<>(); //图形将在这个矩形范围
    private       int               precision;
    private       int               minPointSize;
    private       int               maxPointSize;

    public final SeedType getSeedType() {
        return SeedType.Geometry;
    }

    @Override
    protected TypeHandler<?> defaultTypeHandler() {
        return TypeHandlerRegistry.DEFAULT.getTypeHandler(String.class);
    }

    public GeometryType getGeometryType() {
        return this.geometryType;
    }

    public void setGeometryType(GeometryType geometryType) {
        this.geometryType = geometryType;
    }

    public void addRange(double axisXofA, double axisYofA, double axisXofB, double axisYofB) {
        this.addRange(50, axisXofA, axisYofA, axisXofB, axisYofB);
    }

    public void addRange(int ratio, double axisXofA, double axisYofA, double axisXofB, double axisYofB) {
        this.range.addRatio(ratio, new SpaceRange(axisXofA, axisYofA, axisXofB, axisYofB));
    }

    public Ratio<SpaceRange> getRange() {
        return this.range;
    }

    public int getPrecision() {
        return precision;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }

    public int getMinPointSize() {
        return minPointSize;
    }

    public void setMinPointSize(int minPointSize) {
        this.minPointSize = minPointSize;
    }

    public int getMaxPointSize() {
        return maxPointSize;
    }

    public void setMaxPointSize(int maxPointSize) {
        this.maxPointSize = maxPointSize;
    }
}
