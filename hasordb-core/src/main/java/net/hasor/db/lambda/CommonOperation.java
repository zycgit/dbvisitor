package net.hasor.db.lambda;
/** lambda insert */
public interface CommonOperation<R> {
    /** 参考的样本对象 */
    Class<?> exampleType();

    R useQualifier();
}
