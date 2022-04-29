package net.hasor.db.lambda;
import net.hasor.db.lambda.core.QueryCompare;
import net.hasor.db.lambda.core.QueryFunc;

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
