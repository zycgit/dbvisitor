---
id: vectors
slug: /features/mongo/vectors
sidebar_position: 50
title: Vector Operations
---

## Vector Type Mapping {#vector-mapping}

Vectors are numeric BSON arrays, read and written through array mapping. Use a [BSON collection handler](../../../guides/types/json-serialization.md#bson) for a `List<Float>` entity property rather than storing the vector as a JSON string. For direct commands, see [Array Types](types.md#array-values) for binding.

## Vector Search {#vector-search}

The MongoDB integration does not currently provide builder-based KNN ordering, distance range filters, or combined vector/scalar search. Support for vector type mapping means reading and writing vector values; it does not imply integration with MongoDB native vector search.
