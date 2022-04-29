package net.hasor.db.lambda;
import net.hasor.db.lambda.core.DeleteExecute;
import net.hasor.db.lambda.core.QueryCompare;

/**
 * lambda Delete for Map.
 * @version : 2022-04-02
 * @author 赵永春 (zyc@hasor.net)
 */
public interface MapDeleteOperation extends //
        CommonOperation<MapDeleteOperation>, //
        DeleteExecute<MapDeleteOperation>, //
        QueryCompare<MapDeleteOperation, String> {
}
