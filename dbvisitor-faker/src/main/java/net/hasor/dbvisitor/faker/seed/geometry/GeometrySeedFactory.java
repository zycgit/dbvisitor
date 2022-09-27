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
import net.hasor.dbvisitor.faker.seed.SeedConfig;
import net.hasor.dbvisitor.faker.seed.SeedFactory;
import net.hasor.dbvisitor.faker.seed.number.Ratio;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Supplier;

import static java.math.BigDecimal.ROUND_DOWN;
import static net.hasor.dbvisitor.faker.FakerRandomUtils.*;

/**
 * 几何信息 SeedFactory
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public class GeometrySeedFactory implements SeedFactory<GeometrySeedConfig> {
    @Override
    public GeometrySeedConfig newConfig(SeedConfig contextType) {
        return new GeometrySeedConfig();
    }

    @Override
    public Supplier<Serializable> createSeed(GeometrySeedConfig seedConfig) {
        GeometryType geometryType = seedConfig.getGeometryType();
        Ratio<SpaceRange> range = seedConfig.getRange();
        int precision = Math.max(0, seedConfig.getPrecision());
        int minPointSize = Math.max(1, seedConfig.getMinPointSize());
        int maxPointSize = Math.max(1, seedConfig.getMaxPointSize());

        boolean allowNullable = seedConfig.isAllowNullable();
        Float nullableRatio = seedConfig.getNullableRatio();
        if (allowNullable && nullableRatio == null) {
            throw new IllegalStateException("allowNullable is true but, nullableRatio missing.");
        }

        return () -> {
            if (allowNullable && nextFloat(0, 100) < nullableRatio) {
                return null;
            } else {
                BigInteger pointCount = nextLong(BigInteger.valueOf(minPointSize), BigInteger.valueOf(maxPointSize));
                return randomGeometry(geometryType, range.getByRandom(), precision, pointCount);
            }
        };
    }

    private static final GeometryType[] allTypes = GeometryType.values();

    private Serializable randomGeometry(GeometryType type, SpaceRange range, int precision, BigInteger pointCount) {
        while (type == GeometryType.Random) {
            type = allTypes[(int) nextLong(0, allTypes.length)];
        }

        switch (type) {
            case Point: {
                Point point = randomPoint(range.getPointA(), range.getPointB(), precision);
                return "(" + fmtPoint(point) + ")";
            }
            case Line: {
                // ax + by + c = 0
                Point pointA = randomPoint(range.getPointA(), range.getPointB(), precision);
                Point pointB = randomPoint(range.getPointA(), range.getPointB(), precision);
                BigDecimal sum = pointA.getX().add(pointB.getY()).negate();
                return "{" + pointA.getX().toPlainString() + "," + pointB.getY().toPlainString() + "," + sum.toPlainString() + "}";
            }
            case Lseg:
            case Box: {
                Point pointA = randomPoint(range.getPointA(), range.getPointB(), precision);
                Point pointB = randomPoint(range.getPointA(), range.getPointB(), precision);
                return "((" + fmtPoint(pointA) + "),(" + fmtPoint(pointB) + "))";
            }
            case Path: {
                String[] pathStr = new String[pointCount.intValue()];
                for (int i = 0; i < pathStr.length; i++) {
                    pathStr[i] = fmtPoint(randomPoint(range.getPointA(), range.getPointB(), precision));
                }
                return "((" + StringUtils.join(pathStr, "),(") + "))";
            }
            case Polygon: {
                String[] points = nextPolygon(range, precision, pointCount);
                return "((" + StringUtils.join(points, "),(") + "))";
            }
            case MultiPolygon: {
                String[] points = nextPolygon(range, precision, pointCount);
                String[] forMulti = new String[points.length + 1];
                System.arraycopy(points, 0, forMulti, 0, points.length);
                forMulti[points.length] = points[0];

                String multiPolygon = "((" + StringUtils.join(forMulti, "),(") + "))";
                return "MULTIPOLYGON(" + multiPolygon + ")"; // 生成的图形是没有挖洞的
            }
            case Circle: {
                Point point = randomPoint(range.getPointA(), range.getPointB(), precision);
                BigDecimal radius = nextRadius(point, range, precision);
                return "((" + fmtPoint(point) + ")," + radius.toPlainString() + ")";
            }
            default: {
                throw new UnsupportedOperationException("unsupported GeometryType " + type);
            }
        }
    }

    private Point randomPoint(Point formBorder, Point toBorder, int precision) {
        BigDecimal randomX = nextDouble(formBorder.getX(), toBorder.getX(), precision);
        BigDecimal randomY = nextDouble(formBorder.getY(), toBorder.getY(), precision);
        return new Point(randomX, randomY);
    }

    private String fmtPoint(Point point) {
        return point.getX().toPlainString() + "," + point.getX().toPlainString();
    }

    private String[] nextPolygon(SpaceRange range, int precision, BigInteger pointCount) {
        Point centrePoint = randomPoint(range.getPointA(), range.getPointB(), precision);
        String[] pathStr = new String[pointCount.intValue()];
        double unitAngle = 360d / (double) pathStr.length;
        for (int i = 0; i < pathStr.length; i++) {
            // 角度和半径
            double curAngle = i * unitAngle;
            BigDecimal curRadius = nextRadius(centrePoint, curAngle, range, precision);
            // 多边形上的点
            BigDecimal x = centrePoint.getX().add(curRadius.multiply(BigDecimal.valueOf(Math.sin(curAngle)))).setScale(precision, ROUND_DOWN);
            BigDecimal y = centrePoint.getY().add(curRadius.multiply(BigDecimal.valueOf(Math.cos(curAngle)))).setScale(precision, ROUND_DOWN);
            pathStr[i] = fmtPoint(new Point(x, y));
        }
        return pathStr;
    }

    private BigDecimal nextRadius(Point point, double dipAngle, SpaceRange range, int precision) {
        // TODO point 是矩形 range 中的一个点；dipAngle 是 point 在一个方向上的倾角；point 和 dipAngle 共同决定了一个射线；该射线最终会与 range 的一个边相交。
        //    - 求 point 到 range 相交 点之间的长度，如果有小数则最终精度按照 precision 来限定
        //    - 该结果作为 maxRadius 值，minRadius 是 precision 允许范围内最接近 0 的一个数

        BigDecimal minRadius = BigDecimal.ONE.divide(new BigDecimal("1" + StringUtils.repeat("0", precision)));
        BigDecimal maxRadius = BigDecimal.valueOf(100);
        return nextDouble(minRadius, maxRadius, precision);
    }

    private BigDecimal nextRadius(Point point, SpaceRange range, int precision) {
        // TODO point 是矩形 range 中的一个点；、
        //    - 求 point 到 range 所有相交点之间的最短的长度，如果有小数则最终精度按照 precision 来限定
        //    - 该结果作为 maxRadius 值，minRadius 是 precision 允许范围内最接近 0 的一个数

        BigDecimal minRadius = BigDecimal.ONE.divide(new BigDecimal("1" + StringUtils.repeat("0", precision)));
        BigDecimal maxRadius = BigDecimal.valueOf(100);
        return nextDouble(minRadius, maxRadius, precision);
    }
}