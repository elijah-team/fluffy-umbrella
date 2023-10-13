# Elijah programming language/system
## `mainline-k`

**Elijah** is a high-level language suitable for replacement of C/C++ and Java.  (And of course any other
language in existence or non-existence, except [FORTRAN](https://en.wikipedia.org/wiki/Karrueche_Tran)
and [NULL](http://jdurrett.ba.ttu.edu/misc/Null-language.html).)

<details>
<summary>Just so you know, dear reader, most of the problems this project tries (or is intended to solve) are
already solved; and the rest are being worked on by people wearing much better smelling cologne than I.</summary>

&lt;tmi&gt;
I don't recall any specific compliments about my cologne, but I don't do it for other people. *I* like it.
&lt;/tmi&gt;

</details>

### Mandatory FSF blurb

This is free software intended for on all systems, including GNU/Linux.

### Goals (for `mainline-k`)

1. Provide a safe space to test `UT_Controller` and friends. (This is the 'k' I think.  Don't ask.)

### Goals (for fluffy-umbrella)

1. To constantly drive the code toward an expression of the domain rather than an expression of the programming
   language-specific expression of that domain.

### Goals (for Elijah Language)

1. Integrate into current C and Java projects. Piecemeal replacement down to the function level.

2. Don't interrupt current workflows too much. (It's like it's not even there, but things are better...)

3. Mainly "not" to have to learn Kotlin.

### Goals (for Elijah Programming System)

1. Sharable, live, interactive notebooks along the lines of Jupyter.

<!--
2. Code manager along the lines of Unison. (Not too sure about this one yet)
-->

2. Cool incremental/instant compilation.

3. A pretty development environment a la [Glamorous Toolkit](https://gtoolkit.com/), especially the custom views.

## Status

<details>
<summary>`mainline-k` uses gradle for now.</summary>

Maven fails sometimes.

Gradle fails sometimes.

Kotlin fails sometimes.

Eclipse fails sometimes.

Idea was great when it didn't try to be so pretty.  It does look nice though...

Annotation processing fails all the time.

Personal projects keep going.

</details>

Idea should "always" give you a good experience.  `dev` branches will contain work in progress, things that don't work
because the gods are angry, etc.

```
git clone https://github.com/elijah-team/fluffy-umbrella -b mainline-k
cd fluffy-umbrella
./gradlew test
# or:
nix-shell -p gradle jdk17 --pure
gradle test
```

Much work is needed, same as always.
