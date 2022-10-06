Names
=====

.. raw:: html

   <!--
   Part of the Carbon Language project, under the Apache License v2.0 with LLVM
   Exceptions. See /LICENSE for license information.
   SPDX-License-Identifier: Apache-2.0 WITH LLVM-exception
   -->

TODO

.. _names-1:

Names
-----

1. A *name* is an `identifier <lex.md>`__. Two names are the same if
   they comprise the same sequence of Unicode code points.

   TODO: Normalization?

Scopes
------

1. A *scope* is one of:

   -  The top level in a source file.
   -  A pattern scope.
   -  A block scope.
   -  A type definition.

2. Every construct that declares a name *binds* the name to the declared
   entity within the innermost enclosing scope.

Unqualified name lookup
-----------------------

1. Unqualified name lookup associates a name with an entity. The
   associated entity is the entity to which the name is bound in the
   innermost enclosing scope in which the name is bound.
