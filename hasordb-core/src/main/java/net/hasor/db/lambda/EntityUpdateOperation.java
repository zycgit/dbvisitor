package net.hasor.db.lambda;
import net.hasor.cobble.reflect.SFunction;
import net.hasor.db.lambda.core.QueryCompare;
import net.hasor.db.lambda.core.UpdateExecute;

public interface EntityUpdateOperation<T> extends //
        CommonOperation<EntityUpdateOperation<T>>, //
        UpdateExecute<EntityUpdateOperation<T>, T>, //
        QueryCompare<EntityUpdateOperation<T>, SFunction<T>> {
}
