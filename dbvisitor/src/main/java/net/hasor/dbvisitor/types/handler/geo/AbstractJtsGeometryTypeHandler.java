/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.types.handler.geo;
import java.sql.SQLException;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.types.handler.AbstractTypeHandler;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.*;

/**
 * @author 赵永春 (zyc@hasor.net)
 */
public abstract class AbstractJtsGeometryTypeHandler<T> extends AbstractTypeHandler<T> {

    protected static final GeometryFactory factory = new GeometryFactory();

    protected static String toWKT(byte[] wkb) throws SQLException {
        if (wkb == null) {
            return null;
        }
        try {
            Geometry object = new WKBReader(factory).read(wkb);
            return new WKTWriter().write(object);
        } catch (ParseException e) {
            throw new SQLException(e);
        }
    }

    protected static byte[] toWKB(String wkt) throws SQLException {
        if (StringUtils.isBlank(wkt)) {
            return null;
        }
        try {
            Geometry object = new WKTReader(factory).read(wkt);
            return new WKBWriter().write(object);
        } catch (ParseException e) {
            throw new SQLException(e);
        }
    }
}
