# Access Notation

The `access` notation is used for to purposes:

 * Controlling which classes have access to member functions and member variables
 * Putting members into sections/categories (a la Smalltalk)
 
 
## Syntax

~~~~
access QUOTED_STRING:section IDENT:shortname = LCURLY typeNameList RCURLY
~~~~


For example

```
access Q = {A, B, C}
```

```
access Q
```

will now allow the same access as above. See `Q` is a shortname
