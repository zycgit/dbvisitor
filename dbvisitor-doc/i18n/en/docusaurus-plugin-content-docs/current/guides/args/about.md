---
id: about
sidebar_position: 1
hide_table_of_contents: true
title: Arguments
description: dbVisitor provides multiple ways to pass arguments.
---

dbVisitor provides multiple ways to pass arguments. This section explains how to use them.

## Guide

- [Positional Arguments](./position): use `?` placeholders in SQL and supply values in order. Java arrays are zero-based; JDBC PreparedStatement parameter indexes start at 1.
- [Named Arguments](./named): use `:name`, `&name`, or `#{...}` to name arguments in SQL.
- [SQL Text Substitution](./inject): use `${...}` to fetch named arguments and inject the result into SQL text.
- [Rule-Based Arguments](./rule): use `@{...}` with the [Rule](../rules/about) mechanism to elegantly handle common dynamic SQL scenarios.
- [Interface-based Arguments](./interface): customize argument setting via interfaces for special scenarios.

## Additional Notes

- [Argument Options](./options): when using `#{...}` or `SqlArg` for arguments, you can specify extra options.
