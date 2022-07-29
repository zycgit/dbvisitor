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

import net.hasor.cobble.RandomUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.faker.seed.SeedConfig;
import net.hasor.dbvisitor.faker.seed.SeedFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Supplier;

/**
 * 数值类型的 SeedFactory
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public class NumberSeedFactory implements SeedFactory<NumberSeedConfig, Number> {
    @Override
    public SeedConfig newConfig() {
        return new NumberSeedConfig();
    }

    @Override
    public Supplier<Number> createSeed(NumberSeedConfig seedConfig) {
        NumberType numberType = seedConfig.getNumberType();

        boolean useDecimal = numberType == NumberType.Decimal;
        Integer precision = seedConfig.getPrecision();
        Integer scale = seedConfig.getScale();
        Number min;
        Number max;

        if (numberType != NumberType.Decimal) {
            min = fixMin(seedConfig.getMin(), numberType);
            max = fixMax(seedConfig.getMax(), numberType);
        } else {
            min = 0;
            max = 100;
            if (precision == null || precision < 0) {
                throw new IllegalStateException("useDecimal but precision missing or lt 0.");
            }
            if (scale == null) {
                scale = precision / 2;
            }
            if (scale < 0 || scale > precision) {
                throw new IllegalStateException("the S must be '0 <= S <= P'");
            }
        }

        boolean allowNullable = seedConfig.isAllowNullable();
        Float nullableRatio = seedConfig.getNullableRatio();
        if (allowNullable && nullableRatio == null) {
            throw new IllegalStateException("allowNullable is true but, nullableRatio missing.");
        }

        Integer finalScale = scale;
        return () -> {
            if (nullableRatio != null && RandomUtils.nextFloat(0, 100) < nullableRatio) {
                return null;
            } else {
                if (useDecimal) {
                    return randomDecimal(precision, finalScale);
                } else {
                    return randomNumber(min, max, numberType);
                }
            }
        };
    }

    private Number fixMin(BigDecimal decimal, NumberType classType) {
        switch (classType) {
            case Bool:
                return 0;
            case Byte:
                if (decimal.compareTo(BigDecimal.valueOf(Byte.MIN_VALUE)) <= 0) {
                    return Byte.MIN_VALUE;
                } else {
                    return decimal.byteValue();
                }
            case Short:
                if (decimal.compareTo(BigDecimal.valueOf(Short.MIN_VALUE)) <= 0) {
                    return Short.MIN_VALUE;
                } else {
                    return decimal.shortValue();
                }
            case Integer:
                if (decimal.compareTo(BigDecimal.valueOf(Integer.MIN_VALUE)) <= 0) {
                    return Integer.MIN_VALUE;
                } else {
                    return decimal.intValue();
                }
            case Long:
                if (decimal.compareTo(BigDecimal.valueOf(Long.MIN_VALUE)) <= 0) {
                    return Long.MIN_VALUE;
                } else {
                    return decimal.longValue();
                }
            case Float:
                if (decimal.compareTo(BigDecimal.valueOf(Float.MIN_VALUE)) <= 0) {
                    return Float.MIN_VALUE;
                } else {
                    return decimal.floatValue();
                }
            case Double:
                if (decimal.compareTo(BigDecimal.valueOf(Double.MIN_VALUE)) <= 0) {
                    return Double.MIN_VALUE;
                } else {
                    return decimal.doubleValue();
                }
            default:
                throw new UnsupportedOperationException(classType + " fixMin Unsupported.");
        }
    }

    private Number fixMax(BigDecimal decimal, NumberType classType) {
        switch (classType) {
            case Bool:
                return 1;
            case Byte:
                if (decimal.compareTo(BigDecimal.valueOf(Byte.MAX_VALUE)) >= 0) {
                    return Byte.MAX_VALUE;
                } else {
                    return decimal.byteValue();
                }
            case Short:
                if (decimal.compareTo(BigDecimal.valueOf(Short.MAX_VALUE)) >= 0) {
                    return Short.MAX_VALUE;
                } else {
                    return decimal.shortValue();
                }
            case Integer:
                if (decimal.compareTo(BigDecimal.valueOf(Integer.MAX_VALUE)) >= 0) {
                    return Integer.MAX_VALUE;
                } else {
                    return decimal.intValue();
                }
            case Long:
                if (decimal.compareTo(BigDecimal.valueOf(Long.MAX_VALUE)) >= 0) {
                    return Long.MAX_VALUE;
                } else {
                    return decimal.longValue();
                }
            case Float:
                if (decimal.compareTo(BigDecimal.valueOf(Float.MAX_VALUE)) >= 0) {
                    return Float.MAX_VALUE;
                } else {
                    return decimal.floatValue();
                }
            case Double:
                if (decimal.compareTo(BigDecimal.valueOf(Double.MAX_VALUE)) >= 0) {
                    return Double.MAX_VALUE;
                } else {
                    return decimal.doubleValue();
                }
            default:
                throw new UnsupportedOperationException(classType + " fixMax Unsupported.");
        }
    }

    private Number toNumber(Number number, NumberType classType) {
        switch (classType) {
            case Bool:
                return (byte) (number.intValue() > 0 ? 1 : 0);
            case Byte:
                return number.byteValue();
            case Short:
                return number.shortValue();
            case Integer:
                return number.intValue();
            case Long:
                return number.longValue();
            case Float:
                return number.floatValue();
            case Double:
                return number.doubleValue();
            default:
                throw new UnsupportedOperationException(classType + " toNumber Unsupported.");
        }
    }

    private Number randomNumber(Number minNum, Number maxNum, NumberType classType) {
        switch (classType) {
            case Bool:
            case Byte:
            case Short:
            case Integer:
            case Long:
                long nextLong = 0;
                if (RandomUtils.nextBoolean()) {
                    nextLong = -RandomUtils.nextLong(0, Math.abs(minNum.longValue()));
                } else {
                    nextLong = RandomUtils.nextLong(0, Math.abs(maxNum.longValue() + 1));
                }
                return toNumber(nextLong, classType);
            case Float:
            case Double:
                double nextDouble = 0;
                if (RandomUtils.nextBoolean()) {
                    nextDouble = -RandomUtils.nextDouble(0, Math.abs(minNum.doubleValue()));
                } else {
                    nextDouble = RandomUtils.nextDouble(0, Math.abs(maxNum.doubleValue() + 1));
                }
                return toNumber(nextDouble, classType);
            default:
                throw new UnsupportedOperationException(classType + " randomNumber Unsupported.");
        }
    }

    private Number randomDecimal(int precision, int scale) {
        StringBuilder builder = new StringBuilder();
        if (scale <= 0) {
            if (precision > 0) {
                double nextDouble = RandomUtils.nextDouble();
                int mulriple = Integer.parseInt("1" + StringUtils.repeat("0", precision));
                builder.append((int) (nextDouble * mulriple));
                return new BigInteger(builder.toString());
            } else {
                return BigInteger.ZERO;
            }
        } else {
            precision = precision - scale;
            if (precision > 0) {
                double nextDouble = RandomUtils.nextDouble();
                int mulriple = Integer.parseInt("1" + StringUtils.repeat("0", precision));
                builder.append((int) (nextDouble * mulriple));
            }

            builder.append(".");

            double nextDouble = RandomUtils.nextDouble();
            int mulriple = Integer.parseInt("1" + StringUtils.repeat("0", scale));
            builder.append((int) (nextDouble * mulriple));

            if (builder.length() == 0) {
                return BigDecimal.ZERO;
            } else {
                BigDecimal decimal = new BigDecimal(builder.toString());
                return RandomUtils.nextBoolean() ? decimal : decimal.negate();
            }
        }
    }
}
