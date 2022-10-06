Conditionals
============

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
-  `Alternatives considered <#alternatives-considered>`__
-  `References <#references>`__

.. raw:: html

   <!-- tocstop -->

Overview
--------

``if`` and ``else`` provide conditional execution of statements. Syntax
is:

   ``if (``\ *boolean expression* ``) {`` *statements* ``}``

   [ ``else if (`` *boolean expression* ``) {`` *statements* ``}`` ] …

   [ ``else {`` *statements* ``}`` ]

Only one group of statements will execute:

-  When the first ``if``\ ’s boolean expression evaluates to true, its
   associated statements will execute.
-  When earlier boolean expressions evaluate to false and an
   ``else if``\ ’s boolean expression evaluates to true, its associated
   statements will execute.

   -  ``... else if ...`` is equivalent to ``... else { if ... }``, but
      without visible nesting of braces.

-  When all boolean expressions evaluate to false, the ``else``\ ’s
   associated statements will execute.

When a boolean expression evaluates to true, no later boolean
expressions will evaluate.

Note that ``else if`` may be repeated.

For example:

.. code:: carbon

   if (fruit.IsYellow()) {
     Print("Banana!");
   } else if (fruit.IsOrange()) {
     Print("Orange!");
   } else if (fruit.IsGreen()) {
     Print("Apple!");
   } else {
     Print("Vegetable!");
   }
   fruit.Eat();

This code will:

-  Evaluate ``fruit.IsYellow()``:

   -  When ``True``, print ``Banana!`` and resume execution at
      ``fruit.Eat()``.
   -  When ``False``, evaluate ``fruit.IsOrange()``:

      -  When ``True``, print ``Orange!`` and resume execution at
         ``fruit.Eat()``.
      -  When ``False``, evaluate ``fruit.IsGreen()``:

         -  When ``True``, print ``Orange!`` and resume execution at
            ``fruit.Eat()``.
         -  When ``False``, print ``Vegetable!`` and resume execution at
            ``fruit.Eat()``.

Alternatives considered
-----------------------

-  `Optional braces </proposals/p0623.md#optional-braces>`__
-  `Optional parentheses </proposals/p0623.md#optional-parentheses>`__
-  ```elif`` </proposals/p0623.md#elif>`__

References
----------

-  Proposal `#285: ``if`` and
   ``else`` <https://github.com/carbon-language/carbon-lang/pull/285>`__
-  Proposal `#623: Require
   braces <https://github.com/carbon-language/carbon-lang/pull/623>`__
