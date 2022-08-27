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
import net.hasor.dbvisitor.faker.seed.SeedFactory;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Supplier;

import static net.hasor.dbvisitor.faker.FakerRandomUtils.*;

/**
 * 数值类型的 SeedFactory
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public class NumberSeedFactory implements SeedFactory<NumberSeedConfig> {
    @Override
    public NumberSeedConfig newConfig() {
        return new NumberSeedConfig();
    }

    @Override
    public Supplier<Serializable> createSeed(NumberSeedConfig seedConfig) {
        NumberType numberType = seedConfig.getNumberType();

        Integer precision = seedConfig.getPrecision();
        Number min = fixNumber(seedConfig.getMin(), (numberType == NumberType.Decimal || numberType == NumberType.BigInt) ? null : 0);
        Number max = fixNumber(seedConfig.getMax(), (numberType == NumberType.Decimal || numberType == NumberType.BigInt) ? null : 100);

        Integer scale = seedConfig.getScale();
        boolean abs = seedConfig.isAbs();

        boolean allowNullable = seedConfig.isAllowNullable();
        Float nullableRatio = seedConfig.getNullableRatio();
        if (allowNullable && nullableRatio == null) {
            throw new IllegalStateException("allowNullable is true but, nullableRatio missing.");
        }

        return () -> {
            if (allowNullable && nextFloat(0, 100) < nullableRatio) {
                return null;
            } else if (precision != null) {
                return toNumber(randomNumber(precision, scale, numberType), numberType, abs);
            } else {
                return toNumber(randomNumber(min, max, scale, numberType), numberType, abs);
            }
        };
    }

    private Number fixNumber(BigDecimal decimal, Number defaultValue) {
        if (decimal == null) {
            return defaultValue;
        } else {
            return decimal;
        }
    }

    private Number randomNumber(Number min, Number max, Integer scale, NumberType numberType) {
        switch (numberType) {
            case Bool:
            case Byte:
            case Short:
            case Integer:
            case Long:
            case BigInt:
                return nextLong(min, max);
            case Float:
            case Double:
            case Decimal:
                return nextDouble(min, max, scale);
            default:
                throw new UnsupportedOperationException(numberType + " randomNumber Unsupported.");
        }
    }

    private Number randomNumber(Integer precision, Integer scale, NumberType numberType) {
        switch (numberType) {
            case Bool:
            case Byte:
            case Short:
            case Integer:
            case Long:
            case BigInt:
            case Float:
            case Double:
            case Decimal:
                return nextDecimal(precision, scale);
            default:
                throw new UnsupportedOperationException(numberType + " randomNumber Unsupported.");
        }
    }

    private Number toNumber(Number number, NumberType classType, boolean abs) {
        switch (classType) {
            case Bool:
                return (byte) (number.intValue() > 0 ? 1 : 0);
            case Byte:
                return abs ? (byte) Math.abs(number.byteValue()) : number.byteValue();
            case Short:
                return abs ? (short) Math.abs(number.shortValue()) : number.shortValue();
            case Integer:
                return abs ? (int) Math.abs(number.intValue()) : number.intValue();
            case Long:
                return abs ? (long) Math.abs(number.longValue()) : number.longValue();
            case Float:
                return abs ? (float) Math.abs(number.floatValue()) : number.floatValue();
            case Double:
                return abs ? (double) Math.abs(number.doubleValue()) : number.doubleValue();
            case BigInt: {
                BigInteger result = null;
                if (number instanceof BigInteger) {
                    result = (BigInteger) number;
                } else if (number instanceof BigDecimal) {
                    result = ((BigDecimal) number).toBigInteger();
                } else {
                    result = BigInteger.valueOf(number.longValue());
                }
                return abs ? result.abs() : result;
            }
            case Decimal: {
                BigDecimal result = null;
                if (number instanceof BigDecimal) {
                    result = (BigDecimal) number;
                } else if (number instanceof BigInteger) {
                    result = new BigDecimal((BigInteger) number);
                } else {
                    result = BigDecimal.valueOf(number.doubleValue());
                }
                return abs ? result.abs() : result;
            }
            default:
                throw new UnsupportedOperationException(classType + " toNumber Unsupported.");
        }
    }
}