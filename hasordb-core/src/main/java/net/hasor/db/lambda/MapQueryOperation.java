package net.hasor.db.lambda;
import net.hasor.db.lambda.core.QueryCompare;
import net.hasor.db.lambda.core.QueryFunc;

import java.util.Map;

/** lambda Query for Map. */
public interface MapQueryOperation extends //
        CommonOperation<MapQueryOperation>, //
        QueryFunc<MapQueryOperation, Map<String, Object>, String>, //
        QueryCompare<MapQueryOperation, String> {
}
