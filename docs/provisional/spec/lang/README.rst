Carbon language specification
=============================

.. raw:: html

   <!--
   Part of the Carbon Language project, under the Apache License v2.0 with LLVM
   Exceptions. See /LICENSE for license information.
   SPDX-License-Identifier: Apache-2.0 WITH LLVM-exception
   -->

Program structure
-----------------

1. A *program* is a collection of one or more linkage units that are
   `linked <#linkage>`__ together.

2. A *Carbon linkage unit* is the result of
   `translating <#translation>`__ a source file. A *foreign linkage
   unit* is an artifact produced by a translation process for some other
   programming language. A linkage unit is either a Carbon linkage unit
   or a foreign linkage unit.

3. A *source file* is a sequence of Unicode code points.

      Note: Source files are typically stored on disk in files with a
      ``.carbon`` file extension, encoded in UTF-8.

Conformance
-----------

1. A program is *valid* if it contains no constructs that violate
   “shall” constraints in this specification. Otherwise, the program is
   *invalid*.

2. An implementation is *conforming* if it accepts all valid programs,
   it rejects all invalid programs for which a diagnostic is required,
   and the `execution <execution.md>`__ semantics of all accepted
   programs is as specified in this specification.

Translation
-----------

1. Translation of a source file into a Carbon linkage unit proceeds as
   follows:

   -  `Lexical analysis <lex.md>`__ decomposes the sequence of code
      points into a sequence of lexical elements.
   -  Whitespace and text comments are discarded, leaving a sequence of
      `tokens <lex.md>`__.
   -  The tokens are `parsed <parsing.md>`__ into an abstract syntax
      tree.
   -  `Unqualified names are bound <names.md>`__ to declarations in the
      abstract syntax tree.
   -  A translated form of each imported `library <libs.md>`__ is
      located and loaded.
   -  `Semantic analysis <semantics.md>`__ is performed: types are
      determined and semantic checks are performed for all
      non-template-dependent constructs in the abstract syntax tree,
      constant expressions are evaluated, and templates are instantiated
      and semantically analyzed.

2. Note: After semantic analysis, an implementation may optionally
      monomorphize generics by a process similar to template
      instantiation.

3. The resulting linkage unit comprises all entities in the translated
   source file that are either `external <#linkage>`__ or are reachable
   from an external entity.

      Note: A linkage unit can include non-monomorphized generics, but
      never includes templates. Constant evaluation can eliminate
      references to entities.

Linkage
-------

1. Two declarations declare the same entity if both declarations are in
   the same library and the same `scope <names.md#scopes>`__ and declare
   the same `name <names.md>`__.

   TODO: Linkage rules for foreign entities. TODO: Ability to declare
   file-local entities.

2. All declarations of an entity shall use the same type.

3. Every entity that is reachable from a linkage unit in a program shall
   be defined by a linkage unit in the program; no diagnostic is
   required unless an entity that can be referenced during the
   `execution <execution.md>`__ of the program is not defined.

4. There shall not be more than one definition of an entity in a
   program.
