package net.hasor.db.lambda;
import net.hasor.db.lambda.core.InsertExecute;

/** lambda Insert for Entity. */
public interface InsertOperation<T> extends  //
        CommonOperation<InsertOperation<T>>, //
        InsertExecute<InsertOperation<T>, T> {
}