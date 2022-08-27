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
package net.hasor.dbvisitor.faker;

import net.hasor.cobble.RandomUtils;
import net.hasor.cobble.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;

import static java.math.BigDecimal.ROUND_DOWN;

/**
 * 基础随机工具
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public class FakerRandomUtils extends RandomUtils {

    public static BigInteger nextLong(Number min, Number max) {
        BigInteger minBig = (min instanceof BigInteger) ? (BigInteger) min : (min instanceof BigDecimal) ? ((BigDecimal) min).toBigInteger() : (BigInteger.valueOf(min.longValue()));
        BigInteger maxBig = (max instanceof BigInteger) ? (BigInteger) max : (max instanceof BigDecimal) ? ((BigDecimal) max).toBigInteger() : (BigInteger.valueOf(max.longValue()));

        int signum = minBig.signum() == maxBig.signum() ? minBig.signum() : 1;
        BigInteger offset = minBig.abs();
        BigInteger bigRange = minBig.abs().add(maxBig.abs());

        StringBuilder result = new StringBuilder();
        boolean inFree = false;
        char[] bitChars = bigRange.toString(2).toCharArray();
        for (char rangeChar : bitChars) {
            if (inFree) {
                result.append(nextBoolean() ? 1 : 0);
            } else {
                boolean oriBit = rangeChar == '1';
                boolean newBit = nextBoolean();
                inFree = oriBit != newBit && oriBit;

                if (inFree) {
                    result.append(0);
                } else {
                    result.append(oriBit ? 1 : 0);
                }
            }
        }

        BigInteger after = new BigInteger(result.toString(), 2);
        if (signum == -1) {
            return after.negate().subtract(offset);
        } else {
            return after.subtract(offset);
        }
    }

    public static BigDecimal nextDouble(Number min, Number max, Integer scale) {
        BigDecimal minBig = (min instanceof BigDecimal) ? (BigDecimal) min : (min instanceof BigInteger) ? new BigDecimal((BigInteger) min) : (BigDecimal.valueOf(min.doubleValue()));
        BigDecimal maxBig = (max instanceof BigDecimal) ? (BigDecimal) max : (max instanceof BigInteger) ? new BigDecimal((BigInteger) max) : (BigDecimal.valueOf(max.doubleValue()));

        if (scale == null) {
            BigDecimal[] minParts = minBig.divideAndRemainder(BigDecimal.ONE);
            BigDecimal[] maxParts = maxBig.divideAndRemainder(BigDecimal.ONE);
            scale = Math.max(minParts[1].scale(), maxParts[1].scale());
        }

        BigDecimal result;
        if (scale == 0) {
            result = new BigDecimal(nextLong(minBig, maxBig));
        } else {
            BigDecimal scaleMul = new BigDecimal("1" + StringUtils.repeat("0", scale));
            BigDecimal scaleMin = minBig.multiply(scaleMul);
            BigDecimal scaleMax = maxBig.multiply(scaleMul);
            result = new BigDecimal(nextLong(scaleMin, scaleMax)).divide(scaleMul);
        }

        return result.setScale(scale, ROUND_DOWN);
    }

    public static BigDecimal nextDecimal(Integer precision, Integer scale) {
        if (precision == null && scale == null) {
            return BigDecimal.valueOf(nextDouble());
        }
        if (precision == null) {
            precision = scale;
        }
        if (scale == null) {
            scale = 0;
        }

        BigDecimal randomDecimal = new BigDecimal(nextLong(0, new BigInteger(StringUtils.repeat("9", precision))));
        BigDecimal divNum = new BigDecimal("1" + StringUtils.repeat("0", scale));
        randomDecimal = randomDecimal.divide(divNum, scale, ROUND_DOWN).stripTrailingZeros();

        return nextBoolean() ? randomDecimal : randomDecimal.negate();
    }
}
