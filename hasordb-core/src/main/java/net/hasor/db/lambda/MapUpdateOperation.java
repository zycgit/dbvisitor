package net.hasor.db.lambda;
import net.hasor.db.lambda.core.QueryCompare;
import net.hasor.db.lambda.core.UpdateExecute;

import java.util.Map;

public interface MapUpdateOperation extends //
        CommonOperation<MapUpdateOperation>, //
        UpdateExecute<MapUpdateOperation, Map<String, Object>>, //
        QueryCompare<MapUpdateOperation, String> {
}
