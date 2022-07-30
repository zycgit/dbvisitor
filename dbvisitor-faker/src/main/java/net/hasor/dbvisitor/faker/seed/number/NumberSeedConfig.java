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
package net.hasor.dbvisitor.faker.seed.number;

import net.hasor.dbvisitor.faker.seed.SeedConfig;
import net.hasor.dbvisitor.faker.seed.SeedType;
import net.hasor.dbvisitor.types.TypeHandler;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

import java.math.BigDecimal;

/**
 * 数值类型的 SeedConfig
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public class NumberSeedConfig extends SeedConfig {
    private TypeHandler<?> typeHandler = TypeHandlerRegistry.DEFAULT.getDefaultTypeHandler();
    private NumberType     numberType;

    private BigDecimal min;
    private BigDecimal max;

    private Integer precision;
    private Integer scale;

    public final SeedType getSeedType() {
        return SeedType.Number;
    }

    @Override
    public TypeHandler<?> getTypeHandler() {
        return this.typeHandler;
    }

    public NumberType getNumberType() {
        return numberType;
    }

    public void setNumberType(NumberType numberType) {
        this.numberType = numberType;
        this.typeHandler = TypeHandlerRegistry.DEFAULT.getTypeHandler(numberType.getDateType());
    }

    public BigDecimal getMin() {
        return min;
    }

    public void setMin(BigDecimal min) {
        this.min = min;
    }

    public BigDecimal getMax() {
        return max;
    }

    public void setMax(BigDecimal max) {
        this.max = max;
    }

    public Integer getPrecision() {
        return precision;
    }

    public void setPrecision(Integer precision) {
        this.precision = precision;
    }

    public Integer getScale() {
        return scale;
    }

    public void setScale(Integer scale) {
        this.scale = scale;
    }
}
