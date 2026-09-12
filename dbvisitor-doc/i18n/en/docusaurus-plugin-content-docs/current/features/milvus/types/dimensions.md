---
id: dimensions
sidebar_position: 2
title: Length, Capacity and Dimensions
---

## Length, Capacity and Dimensions

In `VARCHAR(n)`, n is a UTF-8 byte limit, not a Java character count. Account for encoded length when storing Chinese or other multibyte characters. `ARRAY<element_type>(max_capacity)` limits element count, not bytes; arrays do not have to fill that capacity.

Vector `dim` has the following meaning:

| Field type | Dimension and input length |
| --- | --- |
| FloatVector | Number of numeric elements, which must equal dim |
| Int8Vector | One signed byte per dimension; byte count equals dim |
| BinaryVector | dim counts bits; input byte count multiplied by 8 must equal dim. For example, dim=16 requires two bytes |
| Float16Vector | Two bytes per dimension; encoded length is dim × 2 and numeric-list length is dim |
| BFloat16Vector | Two bytes per dimension; encoded length is dim × 2 and numeric-list length is dim |
| SparseFloatVector | An index-to-weight map; map entry count is not a dense dimension |

FloatVector values must be finite. FP16/BF16 also validate encoded values: a finite Float input can overflow during half-precision conversion and be rejected. Server limits on dimensions and vector-field counts still depend on deployment configuration.
