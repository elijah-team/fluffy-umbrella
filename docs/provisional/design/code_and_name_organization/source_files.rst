Source files
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
-  `Encoding <#encoding>`__
-  `Alternatives considered <#alternatives-considered>`__
-  `References <#references>`__

.. raw:: html

   <!-- tocstop -->

Overview
--------

A Carbon *source file* is a sequence of Unicode code points in Unicode
Normalization Form C (“NFC”), and represents a portion of the complete
text of a program.

Program text can come from a variety of sources, such as an interactive
programming environment (a so-called “Read-Evaluate-Print-Loop” or
REPL), a database, a memory buffer of an IDE, or a command-line
argument.

The canonical representation for Carbon programs is in files stored as a
sequence of bytes in a file system on disk. Such files have a
``.carbon`` extension.

Encoding
--------

The on-disk representation of a Carbon source file is encoded in UTF-8.
Such files may begin with an optional UTF-8 BOM, that is, the byte
sequence EF16,BB16,BF16. This prefix, if present, is ignored.

No Unicode normalization is performed when reading an on-disk
representation of a Carbon source file, so the byte representation is
required to be normalized in Normalization Form C. The Carbon source
formatting tool will convert source files to NFC as necessary.

Alternatives considered
-----------------------

-  `Character encoding </proposals/p0142.md#character-encoding-1>`__
-  `Byte order marks </proposals/p0142.md#byte-order-marks>`__
-  `Normalization forms </proposals/p0142.md#normalization-forms>`__

References
----------

-  Proposal `#142: Unicode source
   files <https://github.com/carbon-language/carbon-lang/pull/142>`__

-  `Unicode <https://www.unicode.org/versions/latest/>`__ is a universal
   character encoding, maintained by the `Unicode
   Consortium <https://home.unicode.org/basic-info/overview/>`__. It is
   the canonical encoding used for textual information interchange
   across all modern technology.

   Carbon is based on Unicode 13.0, which is currently the latest
   version of the Unicode standard. Newer versions will be considered
   for adoption as they are released.

-  `Unicode Standard Annex #15: Unicode Normalization
   Forms <https://www.unicode.org/reports/tr15/tr15-50.html>`__

-  `Wikipedia Unicode equivalence page: Normal
   forms <https://en.wikipedia.org/wiki/Unicode_equivalence#Normal_forms>`__
