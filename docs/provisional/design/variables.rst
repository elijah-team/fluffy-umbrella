Variables
=========

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
-  `Notes <#notes>`__

   -  `Global variables <#global-variables>`__

-  `Alternatives considered <#alternatives-considered>`__
-  `References <#references>`__

.. raw:: html

   <!-- tocstop -->

Overview
--------

Carbon’s local variable syntax is:

-  ``var`` *identifier*\ ``:`` *< expression \|* ``auto`` *> [\_ ``=``
   \_value ]*\ ``;``

Blocks introduce nested scopes and can contain local variable
declarations that work similarly to function parameters.

For example:

::

   fn Foo() {
     var x: i32 = 42;
   }

This introduces a local variable named ``x`` into the block’s scope. It
has the type ``Int`` and is initialized with the value ``42``. These
variable declarations (and function declarations) have a lot more power
than what we’re covering just yet, but this gives you the basic idea.

If ``auto`` is used in place of the type, `type
inference <type_inference.md>`__ is used to automatically determine the
variable’s type.

While there can be global constants, there are no global variables.

Notes
-----

   TODO: Constant syntax is an ongoing discussion.

Global variables
~~~~~~~~~~~~~~~~

We are exploring several different ideas for how to design less
bug-prone patterns to replace the important use cases programmers still
have for global variables. We may be unable to fully address them, at
least for migrated code, and be forced to add some limited form of
global variables back. We may also discover that their convenience
outweighs any improvements afforded.

Alternatives considered
-----------------------

-  `No ``var`` introducer
   keyword </proposals/p0339.md#no-var-introducer-keyword>`__
-  `Name of the ``var`` statement
   introducer </proposals/p0339.md#name-of-the-var-statement-introducer>`__
-  `Colon between type and
   identifier </proposals/p0339.md#colon-between-type-and-identifier>`__
-  `Type elision </proposals/p0339.md#type-elision>`__
-  `Type ordering </proposals/p0618.md#type-ordering>`__
-  `Elide the type instead of using
   ``auto`` </proposals/p0851.md#elide-the-type-instead-of-using-auto>`__

References
----------

-  Proposal `#339: ``var``
   statement <https://github.com/carbon-language/carbon-lang/pull/339>`__
-  Proposal `#618: ``var``
   ordering <https://github.com/carbon-language/carbon-lang/pull/618>`__
-  Proposal `#851: auto keyword for
   vars <https://github.com/carbon-language/carbon-lang/pull/851>`__
