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
package net.hasor.dbvisitor.lambda.segment;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dialect.SqlDialect;

/**
 * 提供多个 Segment 汇聚成为一个的工具。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-11-02
 */
public final class MergeSqlSegment implements Segment {
    private final String        separator;
    private final List<Segment> segments = new LinkedList<>();

    public MergeSqlSegment(List<Segment> segments) {
        this.separator = " ";
        this.segments.addAll(segments);
    }

    public MergeSqlSegment() {
        this.separator = " ";
    }

    public MergeSqlSegment(String separator) {
        this.separator = separator;
    }

    public void addSegment(Segment... segmentArrays) {
        if (segmentArrays != null) {
            this.segments.addAll(Arrays.asList(segmentArrays));
        }
    }

    @Override
    public String getSqlSegment(boolean delimited, SqlDialect dialect) throws SQLException {
        return this.getSqlSegment(delimited, dialect, this.segments);
    }

    public MergeSqlSegment sub(int form) {
        return new MergeSqlSegment(this.segments.subList(form, this.segments.size()));
    }

    private String getSqlSegment(boolean delimited, SqlDialect dialect, List<Segment> dataList) throws SQLException {
        StringBuilder strBuilder = new StringBuilder("");
        boolean first = true;
        for (Segment segment : dataList) {
            String str = segment.getSqlSegment(delimited, dialect);
            if (StringUtils.isBlank(str)) {
                continue;
            }
            if (!first) {
                strBuilder.append(this.separator);
            }
            strBuilder.append(str);
            first = false;
        }
        return strBuilder.toString();
    }

    public boolean isEmpty() {
        return this.segments.isEmpty();
    }

    public Segment firstSqlSegment() {
        if (this.segments.isEmpty()) {
            return null;
        } else {
            Segment segment = segments.get(0);
            if (segment instanceof MergeSqlSegment) {
                return ((MergeSqlSegment) segment).firstSqlSegment();
            } else {
                return segment;
            }
        }
    }

    public Segment lastSegment() {
        return this.segments.get(this.segments.size() - 1);
    }

    public void cleanSegment() {
        this.segments.clear();
    }

    public int size() {
        return this.segments.size();
    }

    public Segment getSqlSegment(int i) {
        return this.segments.get(i);
    }
}
