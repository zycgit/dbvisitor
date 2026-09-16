---
id: vectors
sidebar_position: 50
title: Vector Operations
---

## Vector Type Mapping {#vector-mapping}

Store vectors as `Array(Float32)` and map them to `List<Float>` with `ChVectorTypeHandler`. See [Vector Type Handlers](../../guides/types/vector-handler.md). Vectors cannot be NULL, and stored and query vectors must have matching dimensions.

## KNN Ordering {#knn-ordering}

The builder API generates distance-function ordering and uses `initPage(k, 0)` to limit the results. It does not create vector indexes automatically.

L2, cosine, and inner-product ordering are integrated; Hamming, Jaccard, and BM25 are not.

| Method | Ordering expression |
| --- | --- |
| `orderByL2` | `L2Distance(embedding, ?)` |
| `orderByCosine` | `cosineDistance(embedding, ?)` |
| `orderByIP` | `-dotProduct(embedding, ?)` |

Ascending order returns the smallest distances first. Negating the inner product puts the largest products first.

## Distance Range Filtering {#range-filters}

`vectorByL2`, `vectorByCosine`, and `vectorByIP` compare the expressions above with `< threshold`. Cosine similarity above `0.8` uses a distance threshold of `0.2`; inner product above `0.8` uses a negative-product threshold of `-0.8`.

Hamming, Jaccard, and BM25 range filters are not available through the builder API.

## Combined Queries {#vector-scalar}

Scalar predicates become ordinary WHERE conditions. Combine them with vector range filters, then order by distance. Add conditions before ordering methods; see [Combined Conditions](../../guides/core/vector_query/combined.md).
