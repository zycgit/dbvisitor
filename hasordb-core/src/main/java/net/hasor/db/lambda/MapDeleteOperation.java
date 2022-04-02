package net.hasor.db.lambda;
import net.hasor.db.lambda.core.DeleteExecute;
import net.hasor.db.lambda.core.QueryCompare;

/** lambda Update for Entity. */
public interface MapDeleteOperation extends //
        CommonOperation<MapDeleteOperation>, //
        DeleteExecute<MapDeleteOperation>, //
        QueryCompare<MapDeleteOperation, String> {
}
