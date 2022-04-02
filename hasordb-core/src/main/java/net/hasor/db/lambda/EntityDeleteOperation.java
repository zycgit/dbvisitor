package net.hasor.db.lambda;
import net.hasor.cobble.reflect.SFunction;
import net.hasor.db.lambda.core.DeleteExecute;
import net.hasor.db.lambda.core.QueryCompare;

public interface EntityDeleteOperation<T> extends //
        CommonOperation<EntityDeleteOperation<T>>, //
        DeleteExecute<EntityDeleteOperation<T>>, //
        QueryCompare<EntityDeleteOperation<T>, SFunction<T>> {
}
