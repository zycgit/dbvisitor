package net.hasor.dbvisitor.lambda.core;

public enum MetricType {
    /** Euclidean Distance */
    L2,
    /** Cosine Distance */
    COSINE,
    /** Inner Product */
    IP,
    /** Hamming Distance */
    HAMMING,
    /** Jaccard Distance */
    JACCARD,
    /** BM25 Score */
    BM25,
}
