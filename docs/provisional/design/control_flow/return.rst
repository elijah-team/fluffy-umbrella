``return``
==========

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

   -  `Returning empty tuples <#returning-empty-tuples>`__
   -  ```returned var`` <#returned-var>`__
   -  ```return`` and initialization <#return-and-initialization>`__

-  `Alternatives considered <#alternatives-considered>`__
-  `References <#references>`__

.. raw:: html

   <!-- tocstop -->

Overview
--------

The ``return`` statement ends the flow of execution within a
`function <../functions.md>`__, returning execution to the caller. Its
syntax is:

   ``return`` *[ expression ]* ``;``

If the function returns a value to the caller, that value is provided by
an expression in the return statement. For example:

.. code:: carbon

   fn Sum(a: i32, b: i32) -> i32 {
     return a + b;
   }

When a return type is specified, a function must *always* ``return``
before control flow can reach the end of the function body. In other
words, ``fn DoNothing() -> i32 {}`` would be invalid because execution
will reach the end of the function body without returning a value.

Returning empty tuples
~~~~~~~~~~~~~~~~~~~~~~

Returning an empty tuple ``()`` is special, and similar to C++’s
``void`` returns. When a function has no specified return type, its
return type is implicitly ``()``. ``return`` must not have an expression
argument in this case. It also has an implicit ``return;`` at the end of
the function body. For example:

.. code:: carbon

   // No return type is specified, so this returns `()` implicitly.
   fn MaybeDraw(should_draw: bool) {
     if (!should_draw) {
       // No expression is passed to `return`.
       return;
     }
     ActuallyDraw();
     // There is an implicit `return;` here.
   }

When ``-> ()`` is specified in the function signature, the return
expression is required. Omitting ``-> ()`` is encouraged, but specifying
it is supported for generalized code structures, including
`templates <../templates.md>`__. In order to be consistent with other
explicitly specified return types, ``return;`` is invalid in this case.
For example:

.. code:: carbon

   // `-> ()` defines an explicit return value.
   fn MaybeDraw(should_draw: bool) -> () {
     if (!should_draw) {
       // As a consequence, a return value must be passed.
       return ();
     }
     ActuallyDraw();
     // The return value must again be explicit.
     return ();
   }

``returned var``
~~~~~~~~~~~~~~~~

`Variables <../variables.md>`__ may be declared with a ``returned``
statement. Its syntax is:

   ``returned`` *var statement*

When a variable is marked as ``returned``, it must be the only
``returned`` value in-scope.

If a ``returned var`` is returned, the specific syntax ``return var``
must be used. Returning expressions is not allowed while a
``returned var`` is in scope. For example:

.. code:: carbon

   fn MakeCircle(radius: i32) -> Circle {
     returned var c: Circle;
     c.radius = radius;
     // `return c` would be invalid because `returned` is in use.
     return var;
   }

If control flow exits the scope of a ``returned`` variable in any way
other than ``return var``, the ``returned var``\ ’s lifetime ends as
normal. When this occurs, ``return`` may again be used with expressions.
For example:

.. code:: carbon

   fn MakePointInArea(area: Area, preferred_x: i32, preferred_y: i32) -> Point {
     if (preferred_x >= 0 && preferred_y >= 0) {
       returned var p: Point = { .x = preferred_x, .y = preferred_y };
       if (area.Contains(p)) {
         return var;
       }
       // p's lifetime ends here when `return var` is not reached.
     }

     return area.RandomPoint();
   }

``return`` and initialization
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Consider the following common initialization code:

.. code:: carbon

   fn CreateMyObject() -> MyType {
     return <expression>;
   }

   var x: MyType = CreateMyObject();

The ``<expression>`` in the return statement of ``CreateMyObject``
initializes the variable ``x`` here. There is no copy or similar. It is
equivalent to:

.. code:: carbon

   var x: MyType = <expression>;

This applies recursively, similar to C++’s guaranteed copy elision.

In the case where additional statements should be run between
constructing the return value and returning, the use of ``returned var``
allows for improved efficiency because the ``returned var`` can directly
use the address of ``var`` declared by the caller. For example, here the
``returned var vector`` in ``CreateVector`` uses the storage of
``my_vector`` for initialization, avoiding a copy:

.. code:: carbon

   fn CreateVector(x: i32, y: i32) -> Vector {
     returned var vector: Vector;
     vector.x = x;
     vector.y = y;
     return var;
   }

   var my_vector: Vector = CreateVector(1, 2);

As a consequence, ``returned var`` is encouraged because it makes it
easier to avoid copies.

   **TODO:** Have some discussion of RVO and NRVO as they are found in
   C++ here, and the fact that Carbon provides the essential part of
   these as first-class features and therefore they are never
   “optimizations” or done implicitly or optionally.

Alternatives considered
-----------------------

-  `Implicit or expression
   returns </proposals/p0415.md#implicit-or-expression-returns>`__
-  `Named return variable in place of a return
   type </proposals/p0257.md#named-return-variable-in-place-of-a-return-type>`__
-  `Retain the C++ rule </proposals/p0538.md#retain-the-c-rule>`__
-  `Fully divorce functions and
   procedures </proposals/p0538.md#fully-divorce-functions-and-procedures>`__

References
----------

-  Proposal `#257: Initialization of memory and
   variables <https://github.com/carbon-language/carbon-lang/pull/257>`__
-  Proposal `#415: Syntax:
   ``return`` <https://github.com/carbon-language/carbon-lang/pull/415>`__
-  Proposal `#538: ``return`` with no
   argument <https://github.com/carbon-language/carbon-lang/pull/538>`__
