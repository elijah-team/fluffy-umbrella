Naming conventions
==================

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

   -  `Constants <#constants>`__
   -  `Carbon-provided item naming <#carbon-provided-item-naming>`__

-  `Alternatives considered <#alternatives-considered>`__
-  `References <#references>`__

.. raw:: html

   <!-- tocstop -->

Overview
--------

Our naming conventions are:

-  For idiomatic Carbon code:

   -  ``UpperCamelCase`` will be used when the named entity cannot have
      a dynamically varying value. For example, functions, namespaces,
      or compile-time constant values.
   -  ``lower_snake_case`` will be used when the named entity’s value
      won’t be known until runtime, such as for variables.

-  For Carbon-provided features:

   -  Keywords and type literals will use ``lower_snake_case``.
   -  Other code will use the guidelines for idiomatic Carbon code.

In other words:

+------------+--------+-----------------------------------------------+
| Item       | Conv   | Explanation                                   |
|            | ention |                                               |
+============+========+===============================================+
| Packages   | ``Uppe | Used for compile-time lookup.                 |
|            | rCamel |                                               |
|            | Case`` |                                               |
+------------+--------+-----------------------------------------------+
| Types      | ``Uppe | Resolved at compile-time.                     |
|            | rCamel |                                               |
|            | Case`` |                                               |
+------------+--------+-----------------------------------------------+
| Functions  | ``Uppe | Resolved at compile-time.                     |
|            | rCamel |                                               |
|            | Case`` |                                               |
+------------+--------+-----------------------------------------------+
| Methods    | ``Uppe | Methods, including virtual methods, are       |
|            | rCamel | equivalent to functions.                      |
|            | Case`` |                                               |
+------------+--------+-----------------------------------------------+
| Generic    | ``Uppe | May vary based on inputs, but are ultimately  |
| parameters | rCamel | resolved at compile-time.                     |
|            | Case`` |                                               |
+------------+--------+-----------------------------------------------+
| Co         | ``Uppe | Resolved at compile-time. See                 |
| mpile-time | rCamel | `constants <#constants>`__ for more remarks.  |
| constants  | Case`` |                                               |
+------------+--------+-----------------------------------------------+
| Variables  | ``     | May be reassigned and thus require runtime    |
|            | lower_ | information.                                  |
|            | snake_ |                                               |
|            | case`` |                                               |
+------------+--------+-----------------------------------------------+
| Member     | ``     | Behave like variables.                        |
| variables  | lower_ |                                               |
|            | snake_ |                                               |
|            | case`` |                                               |
+------------+--------+-----------------------------------------------+
| Keywords   | ``     | Special, and developers can be expected to be |
|            | lower_ | comfortable with this casing cross-language.  |
|            | snake_ |                                               |
|            | case`` |                                               |
+------------+--------+-----------------------------------------------+
| Type       | ``     | Equivalent to keywords.                       |
| literals   | lower_ |                                               |
|            | snake_ |                                               |
|            | case`` |                                               |
+------------+--------+-----------------------------------------------+
| Boolean    | ``     | Equivalent to keywords.                       |
| type and   | lower_ |                                               |
| literals   | snake_ |                                               |
|            | case`` |                                               |
+------------+--------+-----------------------------------------------+
| Other      | ``Uppe | Behave like normal types.                     |
| Carbon     | rCamel |                                               |
| types      | Case`` |                                               |
+------------+--------+-----------------------------------------------+
| ``Self``   | ``Uppe | These are similar to type members on a class. |
| and        | rCamel |                                               |
| ``Base``   | Case`` |                                               |
+------------+--------+-----------------------------------------------+

We only use ``UpperCamelCase`` and ``lower_snake_case`` in naming
conventions in order to minimize the variation in rules.

Details
-------

Constants
~~~~~~~~~

Consider the following code:

.. code:: carbon

   package Example;

   let CompileTimeConstant: i32 = 7;

   fn RuntimeFunction(runtime_constant: i32);

In this example, ``CompileTimeConstant`` has a singular value (``7``)
which is known at compile-time. As such, it uses ``UpperCamelCase``.

On the other hand, ``runtime_constant`` may be constant within the
function body, but it is assigned at runtime when ``RuntimeFunction`` is
called. Its value is only known in a given runtime invocation of
``RuntimeFunction``. As such, it uses ``lower_snake_case``.

Carbon-provided item naming
~~~~~~~~~~~~~~~~~~~~~~~~~~~

Carbon-provided items are split into a few categories:

-  Keywords; for example, ``for``, ``fn``, and ``var``.
-  Type literals; for example, ``i<digits>``, ``u<digits>``, and
   ``f<digits>``.
-  Boolean type and literals; for example, ``bool``, ``true``, and
   ``false``.

   -  The separate categorization of booleans should not be taken as a
      rule that only booleans would use lowercase; it’s just the only
      example right now.

-  ``Self`` and ``Base``.
-  Other Carbon types; for example, ``Int``, ``UInt``, and ``String``.

Note that while other Carbon types currently use ``UpperCamelCase``,
that should not be inferred to mean that future Carbon types will do the
same. The leads will make decisions on future naming.

Alternatives considered
-----------------------

-  `Other naming
   conventions </proposals/p0861.md#other-naming-conventions>`__
-  `Other conventions for naming Carbon
   types </proposals/p0861.md#other-conventions-for-naming-carbon-types>`__

References
----------

-  Proposal `#861: Naming
   conventions <https://github.com/carbon-language/carbon-lang/pull/861>`__
