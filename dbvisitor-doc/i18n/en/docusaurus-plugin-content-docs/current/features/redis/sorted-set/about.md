---
id: about
slug: /features/redis/commands/sorted-set
sidebar_position: 0
hide_table_of_contents: true
title: Sorted Set
---

Choose a command for its syntax, arguments, results and example. See [Reading and Writing Data](../dbvisitor/usage.mdx) for execution and [Command Format and Parameters](../basics/commands.md) for placeholders.

| Command | Purpose |
| --- | --- |
| [ZMPOP](zmpop.md) | Pop low- or high-score members from the first nonempty sorted set. |
| [BZMPOP](bzmpop.md) | Wait for members and pop from a sorted set. |
| [ZPOPMAX](zpopmax.md) | Pop the highest-scoring members. |
| [BZPOPMAX](bzpopmax.md) | Wait and pop the highest-scoring member from the first nonempty sorted set. |
| [ZPOPMIN](zpopmin.md) | Pop the lowest-scoring members. |
| [BZPOPMIN](bzpopmin.md) | Wait and pop the lowest-scoring member from the first nonempty sorted set. |
| [ZADD](zadd.md) | Add members or update their scores. |
| [ZCARD](zcard.md) | Read the sorted-set cardinality. |
| [ZCOUNT](zcount.md) | Count members within a score range. |
| [ZDIFF](zdiff.md) | Read the difference between the first sorted set and the others. |
| [ZDIFFSTORE](zdiffstore.md) | Store a sorted-set difference. |
| [ZINCRBY](zincrby.md) | Increase a member's score. |
| [ZINTER](zinter.md) | Read an intersection and aggregate member scores. |
| [ZINTERCARD](zintercard.md) | Count intersection members, optionally up to a limit. |
| [ZINTERSTORE](zinterstore.md) | Store an intersection with aggregated scores. |
| [ZLEXCOUNT](zlexcount.md) | Count members within a lexicographical range. |
| [ZSCORE](zscore.md) | Read one member's score. |
| [ZMSCORE](zmscore.md) | Read several member scores in order. |
| [ZRANDMEMBER](zrandmember.md) | Read random members, optionally with scores. |
| [ZRANGE](zrange.md) | Read a range by rank, score or lexicographical order. |
| [ZRANGEBYLEX](zrangebylex.md) | Read a lexicographical range in ascending order. |
| [ZRANGEBYSCORE](zrangebyscore.md) | Read a score range in ascending order. |
| [ZRANGESTORE](zrangestore.md) | Store members and scores from a range at the destination. |
| [ZRANK](zrank.md) | Read a member's ascending rank. |
| [ZREVRANK](zrevrank.md) | Read a member's descending rank. |
| [ZREM](zrem.md) | Remove specified members. |
| [ZREMRANGEBYLEX](zremrangebylex.md) | Remove members in a lexicographical range. |
| [ZREMRANGEBYRANK](zremrangebyrank.md) | Remove members in a rank range. |
| [ZREMRANGEBYSCORE](zremrangebyscore.md) | Remove members in a score range. |
| [ZREVRANGE](zrevrange.md) | Read a range by descending rank. |
| [ZREVRANGEBYLEX](zrevrangebylex.md) | Read a lexicographical range in descending order. |
| [ZREVRANGEBYSCORE](zrevrangebyscore.md) | Read a score range in descending order. |
| [ZSCAN](zscan.md) | Read one cursor batch of members and scores. |
| [ZUNION](zunion.md) | Read a union and aggregate scores of shared members. |
| [ZUNIONSTORE](zunionstore.md) | Store a union with aggregated scores. |
