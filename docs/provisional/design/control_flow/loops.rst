Loops
=====

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
-  `Details <#details>`__

   -  ```while`` <#while>`__
   -  ```for`` <#for>`__
   -  ```break`` <#break>`__
   -  ```continue`` <#continue>`__

-  `Alternatives considered <#alternatives-considered>`__
-  `References <#references>`__

.. raw:: html

   <!-- tocstop -->

Overview
--------

Carbon provides loops using the ``while`` and ``for`` statements. Within
a loop, the ``break`` and ``continue`` statements can be used for flow
control.

Details
-------

``while``
~~~~~~~~~

``while`` statements loop for as long as the passed expression returns
``True``. Syntax is:

   ``while (`` *boolean expression* ``) {`` *statements* ``}``

For example, this prints ``0``, ``1``, ``2``, then ``Done!``:

.. code:: carbon

   var x: Int = 0;
   while (x < 3) {
     Print(x);
     ++x;
   }
   Print("Done!");

``for``
~~~~~~~

``for`` statements support range-based looping, typically over
containers. Syntax is:

   ``for (`` *var declaration* ``in`` *expression* ``) {`` *statements*
   ``}``

For example, this prints all names in ``names``:

.. code:: carbon

   for (var name: String in names) {
     Print(name);
   }

``PrintNames()`` prints each ``String`` in the ``names`` ``List`` in
iteration order.

``break``
~~~~~~~~~

The ``break`` statement immediately ends a ``while`` or ``for`` loop.
Execution will resume at the end of the loop’s scope. Syntax is:

   ``break;``

For example, this processes steps until a manual step is hit (if no
manual step is hit, all steps are processed):

.. code:: carbon

   for (var step: Step in steps) {
     if (step.IsManual()) {
       Print("Reached manual step!");
       break;
     }
     step.Process();
   }

``continue``
~~~~~~~~~~~~

The ``continue`` statement immediately goes to the next loop of a
``while`` or ``for``. In a ``while``, execution continues with the
``while`` expression. Syntax is:

   ``continue;``

For example, this prints all non-empty lines of a file, using
``continue`` to skip empty lines:

.. code:: carbon

   var f: File = OpenFile(path);
   while (!f.EOF()) {
     var line: String = f.ReadLine();
     if (line.IsEmpty()) {
       continue;
     }
     Print(line);
   }

Alternatives considered
-----------------------

-  `Non-C++ syntax </proposals/p0340.md#non-c-syntax>`__
-  `Initializing variables in the
   ``while`` </proposals/p0340.md#initializing-variables-in-the-while>`__
-  ``for``:

   -  `Include semisemi ``for``
      loops </proposals/p0353.md#include-semisemi-for-loops>`__
   -  `Multi-variable
      bindings </proposals/p0353.md#multi-variable-bindings>`__
   -  ```:`` versus ``in`` </proposals/p0618.md#-versus-in>`__

-  `Optional braces </proposals/p0623.md#optional-braces>`__
-  `Optional parentheses </proposals/p0623.md#optional-parentheses>`__

References
----------

-  Proposal `#340:
   ``while`` <https://github.com/carbon-language/carbon-lang/pull/340>`__
-  Proposal `#353:
   ``for`` <https://github.com/carbon-language/carbon-lang/pull/353>`__
-  Proposal `#618: ``var``
   ordering <https://github.com/carbon-language/carbon-lang/pull/618>`__
-  Proposal `#623: Require
   braces <https://github.com/carbon-language/carbon-lang/pull/623>`__
