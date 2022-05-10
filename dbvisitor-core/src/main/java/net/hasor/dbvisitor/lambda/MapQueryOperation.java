package net.hasor.dbvisitor.lambda;
import net.hasor.dbvisitor.lambda.core.QueryCompare;
import net.hasor.dbvisitor.lambda.core.QueryFunc;

import java.util.Map;

/**
 * lambda Query for Map.
 * @version : 2022-04-02
 * @author 赵永春 (zyc@hasor.net)
 */
public interface MapQueryOperation extends //
        CommonOperation<MapQueryOperation>, //
        QueryFunc<MapQueryOperation, Map<String, Object>, String>, //
        QueryCompare<MapQueryOperation, String> {
}
