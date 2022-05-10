package net.hasor.dbvisitor.lambda;
import net.hasor.dbvisitor.lambda.core.QueryCompare;
import net.hasor.dbvisitor.lambda.core.UpdateExecute;

import java.util.Map;

/**
 * lambda Update for Map.
 * @version : 2022-04-02
 * @author 赵永春 (zyc@hasor.net)
 */
public interface MapUpdateOperation extends //
        CommonOperation<MapUpdateOperation>, //
        UpdateExecute<MapUpdateOperation, Map<String, Object>>, //
        QueryCompare<MapUpdateOperation, String> {
}
