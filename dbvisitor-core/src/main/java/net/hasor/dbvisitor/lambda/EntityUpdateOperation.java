package net.hasor.dbvisitor.lambda;
import net.hasor.cobble.reflect.SFunction;
import net.hasor.dbvisitor.lambda.core.QueryCompare;
import net.hasor.dbvisitor.lambda.core.UpdateExecute;
import net.hasor.dbvisitor.lambda.support.entity.EntityQueryCompare;

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
