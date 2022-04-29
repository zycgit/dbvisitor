package net.hasor.db.lambda;
import net.hasor.cobble.reflect.SFunction;
import net.hasor.db.lambda.core.QueryCompare;
import net.hasor.db.lambda.core.UpdateExecute;
import net.hasor.db.lambda.support.entity.EntityQueryCompare;

/**
 * lambda Update for Entity.
 * @version : 2022-04-02
 * @author 赵永春 (zyc@hasor.net)
 */
public interface EntityUpdateOperation<T> extends //
        CommonOperation<EntityUpdateOperation<T>>, //
        UpdateExecute<EntityUpdateOperation<T>, T>, //
        QueryCompare<EntityUpdateOperation<T>, SFunction<T>>,//
        EntityQueryCompare<EntityUpdateOperation<T>> {
}
