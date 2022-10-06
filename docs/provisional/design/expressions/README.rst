Expressions
===========

.. raw:: html

   <!--
   Part of the Carbon Language project, under the Apache License v2.0 with LLVM
   Exceptions. See /LICENSE for license information.
   SPDX-License-Identifier: Apache-2.0 WITH LLVM-exception
   -->

.. raw:: html

   <!-- toc -->

Table of contents
-----------------

-  `Overview <#overview>`__
-  `Precedence <#precedence>`__
-  `Names <#names>`__

   -  `Unqualified names <#unqualified-names>`__
   -  `Qualified names and member
      access <#qualified-names-and-member-access>`__

-  `Operators <#operators>`__
-  `Conversions and casts <#conversions-and-casts>`__
-  ```if`` expressions <#if-expressions>`__
-  `Alternatives considered <#alternatives-considered>`__
-  `References <#references>`__

.. raw:: html

   <!-- tocstop -->

Overview
--------

Expressions are the portions of Carbon syntax that produce values.
Because types in Carbon are values, this includes anywhere that a type
is specified.

::

   fn Foo(a: i32*) -> i32 {
     return *a;
   }

Here, the parameter type ``i32*``, the return type ``i32``, and the
operand ``*a`` of the ``return`` statement are all expressions.

Precedence
----------

Expressions are interpreted based on a partial `precedence
ordering <https://en.wikipedia.org/wiki/Order_of_operations>`__.
Expression components which lack a relative ordering must be
disambiguated by the developer, for example by adding parentheses;
otherwise, the expression will be invalid due to ambiguity. Precedence
orderings will only be added when it’s reasonable to expect most
developers to understand the precedence without parentheses.

The precedence diagram is defined thusly:

.. code:: mermaid

   %%{init: {'themeVariables': {'fontFamily': 'monospace'}}}%%
   graph BT
       parens["(...)"]

       braces["{...}"]
       click braces "https://github.com/carbon-language/carbon-lang/blob/trunk/docs/design/classes.md#literals"

       unqualifiedName["x"]
       click unqualifiedName "https://github.com/carbon-language/carbon-lang/blob/trunk/docs/design/expressions/README.md#unqualified-names"

       memberAccess>"x.y<br>
                       x.(...)"]
       click memberAccess "https://github.com/carbon-language/carbon-lang/blob/trunk/docs/design/expressions/member_access.md"

       negation["-x"]
       click negation "https://github.com/carbon-language/carbon-lang/blob/trunk/docs/design/expressions/arithmetic.md"

       complement["^x"]
       click complement "https://github.com/carbon-language/carbon-lang/blob/trunk/docs/design/expressions/bitwise.md"

       unary((" "))

       as["x as T"]
       click as "https://github.com/carbon-language/carbon-lang/blob/trunk/docs/design/expressions/implicit_conversions.md"

       multiplication>"x * y<br>
                       x / y"]
       click multiplication "https://github.com/carbon-language/carbon-lang/blob/trunk/docs/design/expressions/arithmetic.md"

       addition>"x + y<br>
                 x - y"]
       click addition "https://github.com/carbon-language/carbon-lang/blob/trunk/docs/design/expressions/arithmetic.md"

       modulo["x % y"]
       click modulo "https://github.com/carbon-language/carbon-lang/blob/trunk/docs/design/expressions/arithmetic.md"

       bitwise_and>"x & y"]
       bitwise_or>"x | y"]
       bitwise_xor>"x ^ y"]
       click bitwise_and "https://github.com/carbon-language/carbon-lang/blob/trunk/docs/design/expressions/bitwise.md"
       click bitwise_or "https://github.com/carbon-language/carbon-lang/blob/trunk/docs/design/expressions/bitwise.md"
       click bitwise_xor "https://github.com/carbon-language/carbon-lang/blob/trunk/docs/design/expressions/bitwise.md"

       shift["x << y<br>
              x >> y"]
       click shift "https://github.com/carbon-language/carbon-lang/blob/trunk/docs/design/expressions/bitwise.md"

       comparison["x == y<br>
                   x != y<br>
                   x < y<br>
                   x <= y<br>
                   x > y<br>
                   x >= y"]
       click comparison "https://github.com/carbon-language/carbon-lang/blob/trunk/docs/design/expressions/comparison_operators.md"

       not["not x"]
       click not "https://github.com/carbon-language/carbon-lang/blob/trunk/docs/design/expressions/logical_operators.md"

       logicalOperand((" "))

       and>"x and y"]
       click and "https://github.com/carbon-language/carbon-lang/blob/trunk/docs/design/expressions/logical_operators.md"

       or>"x or y"]
       click or "https://github.com/carbon-language/carbon-lang/blob/trunk/docs/design/expressions/logical_operators.md"

       logicalExpression((" "))

       if>"if x then y else z"]
       click if "https://github.com/carbon-language/carbon-lang/blob/trunk/docs/design/expressions/if.md"

       expressionEnd["x;"]

       memberAccess --> parens & braces & unqualifiedName
       negation --> memberAccess
       complement --> memberAccess
       unary --> negation & complement
       %% Use a longer arrow here to put `not` next to `and` and `or`.
       not -----> memberAccess
       multiplication & modulo & as & bitwise_and & bitwise_or & bitwise_xor & shift --> unary
       addition --> multiplication
       comparison --> modulo & addition & as & bitwise_and & bitwise_or & bitwise_xor & shift
       logicalOperand --> comparison & not
       and & or --> logicalOperand
       logicalExpression --> and & or
       if & expressionEnd --> logicalExpression

The diagram’s attributes are:

-  Each non-empty node represents a precedence group. Empty circles are
   used to simplify the graph, and do not represent a precedence group.

-  When an expression is composed from different precedence groups, the
   interpretation is determined by the precedence edges:

   -  A precedence edge A –> B means that A is lower precedence than B,
      so A can contain B without parentheses. For example,
      ``or --> not`` means that ``not x or y`` is treated as
      ``(not x) or y``.

   -  Precedence edges are transitive. For example, ``or --> == --> as``
      means that ``or`` is lower precedence than ``as``.

-  When an expression is composed from a single precedence group, the
   interpretation is determined by the
   `associativity <https://en.wikipedia.org/wiki/Operator_associativity>`__
   of the precedence group:

   .. code:: mermaid

      graph TD
          non["Non-associative"]
          left>"Left associative"]

   -  For example, ``+`` and ``-`` are left-associative and in the same
      precedence group, so ``a + b + c - d`` is treated as
      ``((a + b) + c) - d``.

Names
-----

Unqualified names
~~~~~~~~~~~~~~~~~

An *unqualified name* is a `word <../lexical_conventions/words.md>`__
that is not a keyword and is not preceded by a period (``.``).

**TODO:** Name lookup rules for unqualified names.

Qualified names and member access
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

A *qualified name* is a word that appears immediately after a period.
Qualified names appear in the following contexts:

-  `Designators </docs/design/classes.md#literals>`__: ``.`` *word*
-  `Simple member access expressions <member_access.md>`__: *expression*
   ``.`` *word*

::

   var x: auto = {.hello = 1, .world = 2};
                   ^^^^^       ^^^^^ qualified name
                  ^^^^^^      ^^^^^^ designator

   x.hello = x.world;
     ^^^^^     ^^^^^ qualified name
   ^^^^^^^   ^^^^^^^ member access expression

Qualified names refer to members of an entity determined by the context
in which the expression appears. For a member access, the entity is
named by the expression preceding the period. In a struct literal, the
entity is the struct type. For example:

::

   package Foo api;
   namespace N;
   fn N.F() {}

   fn G() {
     // Same as `(Foo.N).F()`.
     // `Foo.N` names namespace `N` in package `Foo`.
     // `(Foo.N).F` names function `F` in namespace `N`.
     Foo.N.F();
   }

   // `.n` refers to the member `n` of `{.n: i32}`.
   fn H(a: {.n: i32}) -> i32 {
     // `a.n` is resolved to the member `{.n: i32}.n`,
     // and names the corresponding subobject of `a`.
     return a.n;
   }

   fn J() {
     // `.n` refers to the member `n of `{.n: i32}`.
     H({.n = 5 as i32});
   }

Member access expressions associate left-to-right. If the member name is
more complex than a single *word*, a compound member access expression
can be used, with parentheses around the member name:

-  *expression* ``.`` ``(`` *expression* ``)``

::

   interface I { fn F[me: Self](); }
   class X {}
   external impl X as I { fn F[me: Self]() {} }

   // `x.I.F()` would mean `(x.I).F()`.
   fn Q(x: X) { x.(I.F)(); }

Operators
---------

Most expressions are modeled as operators:

+-----+-----------------+----+----------------------------------------+
| Ca  | Operator        | Sy | Function                               |
| teg |                 | nt |                                        |
| ory |                 | ax |                                        |
+=====+=================+====+========================================+
| A   | ```-`` <ar      | `` | The negation of ``x``.                 |
| rit | ithmetic.md>`__ | -x |                                        |
| hme | (unary)         | `` |                                        |
| tic |                 |    |                                        |
+-----+-----------------+----+----------------------------------------+
| B   | ```^``          | `` | The bitwise complement of ``x``.       |
| itw | <bitwise.md>`__ | ^x |                                        |
| ise | (unary)         | `` |                                        |
+-----+-----------------+----+----------------------------------------+
| A   | ```+`` <ar      | `  | The sum of ``x`` and ``y``.            |
| rit | ithmetic.md>`__ | `x |                                        |
| hme |                 |  + |                                        |
| tic |                 |  y |                                        |
|     |                 | `` |                                        |
+-----+-----------------+----+----------------------------------------+
| A   | ```-`` <ar      | `  | The difference of ``x`` and ``y``.     |
| rit | ithmetic.md>`__ | `x |                                        |
| hme | (binary)        |  - |                                        |
| tic |                 |  y |                                        |
|     |                 | `` |                                        |
+-----+-----------------+----+----------------------------------------+
| A   | ```*`` <ar      | `  | The product of ``x`` and ``y``.        |
| rit | ithmetic.md>`__ | `x |                                        |
| hme |                 |  * |                                        |
| tic |                 |  y |                                        |
|     |                 | `` |                                        |
+-----+-----------------+----+----------------------------------------+
| A   | ```/`` <ar      | `  | ``x`` divided by ``y``, or the         |
| rit | ithmetic.md>`__ | `x | quotient thereof.                      |
| hme |                 |  / |                                        |
| tic |                 |  y |                                        |
|     |                 | `` |                                        |
+-----+-----------------+----+----------------------------------------+
| A   | ```%`` <ar      | `  | ``x`` modulo ``y``.                    |
| rit | ithmetic.md>`__ | `x |                                        |
| hme |                 |  % |                                        |
| tic |                 |  y |                                        |
|     |                 | `` |                                        |
+-----+-----------------+----+----------------------------------------+
| B   | ```&``          | `  | The bitwise AND of ``x`` and ``y``.    |
| itw | <bitwise.md>`__ | `x |                                        |
| ise |                 |  & |                                        |
|     |                 |  y |                                        |
|     |                 | `` |                                        |
+-----+-----------------+----+----------------------------------------+
| B   | ```\|``         | `` | The bitwise OR of ``x`` and ``y``.     |
| itw | <bitwise.md>`__ | x  |                                        |
| ise |                 | \| |                                        |
|     |                 |  y |                                        |
|     |                 | `` |                                        |
+-----+-----------------+----+----------------------------------------+
| B   | ```^``          | `  | The bitwise XOR of ``x`` and ``y``.    |
| itw | <bitwise.md>`__ | `x |                                        |
| ise | (binary)        |  ^ |                                        |
|     |                 |  y |                                        |
|     |                 | `` |                                        |
+-----+-----------------+----+----------------------------------------+
| B   | ```<<``         | `` | ``x`` bit-shifted left ``y`` places.   |
| itw | <bitwise.md>`__ | x  |                                        |
| ise |                 | << |                                        |
|     |                 |  y |                                        |
|     |                 | `` |                                        |
+-----+-----------------+----+----------------------------------------+
| B   | ```>>``         | `` | ``x`` bit-shifted right ``y`` places.  |
| itw | <bitwise.md>`__ | x  |                                        |
| ise |                 | >> |                                        |
|     |                 |  y |                                        |
|     |                 | `` |                                        |
+-----+-----------------+----+----------------------------------------+
| C   | ```as`` <as_exp | `` | Converts the value ``x`` to the type   |
| onv | ressions.md>`__ | x  | ``T``.                                 |
| ers |                 | as |                                        |
| ion |                 |  T |                                        |
|     |                 | `` |                                        |
+-----+-----------------+----+----------------------------------------+
| C   | ```==`          | `` | Equality: ``true`` if ``x`` is equal   |
| omp | ` <comparison_o | x  | to ``y``.                              |
| ari | perators.md>`__ | == |                                        |
| son |                 |  y |                                        |
|     |                 | `` |                                        |
+-----+-----------------+----+----------------------------------------+
| C   | ```!=`          | `` | Inequality: ``true`` if ``x`` is not   |
| omp | ` <comparison_o | x  | equal to ``y``.                        |
| ari | perators.md>`__ | != |                                        |
| son |                 |  y |                                        |
|     |                 | `` |                                        |
+-----+-----------------+----+----------------------------------------+
| C   | ```<`           | `  | Less than: ``true`` if ``x`` is less   |
| omp | ` <comparison_o | `x | than ``y``.                            |
| ari | perators.md>`__ |  < |                                        |
| son |                 |  y |                                        |
|     |                 | `` |                                        |
+-----+-----------------+----+----------------------------------------+
| C   | ```<=`          | `` | Less than or equal: ``true`` if ``x``  |
| omp | ` <comparison_o | x  | is less than or equal to ``y``.        |
| ari | perators.md>`__ | <= |                                        |
| son |                 |  y |                                        |
|     |                 | `` |                                        |
+-----+-----------------+----+----------------------------------------+
| C   | ```>`           | `  | Greater than: ``true`` if ``x`` is     |
| omp | ` <comparison_o | `x | greater than to ``y``.                 |
| ari | perators.md>`__ |  > |                                        |
| son |                 |  y |                                        |
|     |                 | `` |                                        |
+-----+-----------------+----+----------------------------------------+
| C   | ```>=`          | `` | Greater than or equal: ``true`` if     |
| omp | ` <comparison_o | x  | ``x`` is greater than or equal to      |
| ari | perators.md>`__ | >= | ``y``.                                 |
| son |                 |  y |                                        |
|     |                 | `` |                                        |
+-----+-----------------+----+----------------------------------------+
| L   | ```a            | `  | A short-circuiting logical AND:        |
| ogi | nd`` <logical_o | `x | ``true`` if both operands are          |
| cal | perators.md>`__ |  a | ``true``.                              |
|     |                 | nd |                                        |
|     |                 |  y |                                        |
|     |                 | `` |                                        |
+-----+-----------------+----+----------------------------------------+
| L   | ```             | `` | A short-circuiting logical OR:         |
| ogi | or`` <logical_o | x  | ``true`` if either operand is          |
| cal | perators.md>`__ | or | ``true``.                              |
|     |                 |  y |                                        |
|     |                 | `` |                                        |
+-----+-----------------+----+----------------------------------------+
| L   | ```n            | `  | Logical NOT: ``true`` if the operand   |
| ogi | ot`` <logical_o | `n | is ``false``.                          |
| cal | perators.md>`__ | ot |                                        |
|     |                 |  x |                                        |
|     |                 | `` |                                        |
+-----+-----------------+----+----------------------------------------+

Conversions and casts
---------------------

When an expression appears in a context in which an expression of a
specific type is expected, `implicit
conversions <implicit_conversions.md>`__ are applied to convert the
expression to the target type.

Expressions can also be converted to a specific type using an ```as``
expression <as_expressions.md>`__.

::

   fn Bar(n: i32);
   fn Baz(n: i64) {
     // OK, same as Bar(n as i32)
     Bar(n);
   }

``if`` expressions
------------------

An ```if`` expression <if.md>`__ chooses between two expressions.

::

   fn Run(args: Span(StringView)) {
     var file: StringView = if args.size() > 1 then args[1] else "/dev/stdin";
   }

``if`` expressions are analogous to ``?:`` ternary expressions in C and
C++.

Alternatives considered
-----------------------

Other expression documents will list more alternatives; this lists
alternatives not noted elsewhere.

-  `Total order </proposals/p0555.md#total-order>`__
-  `Different precedence for different
   operands </proposals/p0555.md#different-precedence-for-different-operands>`__
-  `Require less than a partial
   order </proposals/p0555.md#require-less-than-a-partial-order>`__

References
----------

Other expression documents will list more references; this lists
references not noted elsewhere.

-  Proposal `#555: Operator
   precedence <https://github.com/carbon-language/carbon-lang/pull/555>`__.
