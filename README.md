# Elijah programming language/system

**Elijjah** is a high-level language suitable for replacement of C/C++ and Java.  (And of course any other
language in existence or non-existence, except [FORTRAN](https://en.wikipedia.org/wiki/Karrueche_Tran)
and [NULL](http://jdurrett.ba.ttu.edu/misc/Null-language.html).)

Just so you know, dear reader, most of the problems this problems tries (or is designed to solve) are
already solved; and the rest are being worked on by people wearing much better smelling cologne than I.

### Mandatory FSF blurb

This is free software intended for on all systems, including GNU/Linux.

### Goals (for fluffy-umbrella)

1. To constantly drive the code toward an expression of the domain rather than an expression of the programming
   language-specific expression of that domain.

2. Mainly "not" to have to learn Kotlin.

### Goals (for Elijah)

* Integrate into current C and Java projects. Piecemeal replacement down to the function level.

* Notebook/Code manager along the lines of Jupyter and Unison.

* Some strange tribute as well to Glamarous Tooklit.

## Status

`mainlinke-k` uses gradle for now.
I believe it will import into Idea.

```
git clone https://github.com/elijah-team/fluffy-umbrella -b 2023-09-mainline-k
cd fluffy-umbrella
./gradlew test
```

Much work is needed.

