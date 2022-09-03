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
    private       NumberType    numberType;
    //随机方式
    private final Ratio<MinMax> minmax = new Ratio<>(); // 最小值/最大值
    private       Integer       precision; //数的长度
    //精度和选项
    private       Integer       scale;
    private       boolean       abs;

    public final SeedType getSeedType() {
        return SeedType.Number;
    }

    @Override
    protected TypeHandler<?> defaultTypeHandler() {
        return TypeHandlerRegistry.DEFAULT.getDefaultTypeHandler();
    }

    public NumberType getNumberType() {
        return numberType;
    }

    public void setNumberType(NumberType numberType) {
        this.numberType = numberType;
        this.setTypeHandler(TypeHandlerRegistry.DEFAULT.getTypeHandler(numberType.getDateType()));
    }

    public void addMinMax(BigDecimal min, BigDecimal max) {
        this.addMinMax(50, min, max);
    }

    public void addMinMax(int ratio, BigDecimal min, BigDecimal max) {
        this.minmax.addRatio(ratio, new MinMax(min, max));
    }

    public Ratio<MinMax> getMinMax() {
        return this.minmax;
    }

    public BigDecimal getMin() {
        if (this.minmax.isEmpty()) {
            return null;
        } else {
            return this.minmax.getLast().getMin();
        }
    }

    public void setMin(BigDecimal min) {
        if (this.minmax.isEmpty()) {
            this.addMinMax(min, null);
        } else {
            this.minmax.getLast().setMin(min);
        }
    }

    public BigDecimal getMax() {
        if (this.minmax.isEmpty()) {
            return null;
        } else {
            return this.minmax.getLast().getMax();
        }
    }

    public void setMax(BigDecimal max) {
        if (this.minmax.isEmpty()) {
            this.addMinMax(null, max);
        } else {
            this.minmax.getLast().setMax(max);
        }
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

    public boolean isAbs() {
        return this.abs;
    }

    public void setAbs(boolean abs) {
        this.abs = abs;
    }
}