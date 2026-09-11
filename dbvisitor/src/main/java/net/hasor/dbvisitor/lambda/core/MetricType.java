/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
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
