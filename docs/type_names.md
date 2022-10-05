# Type Names

There a four type of type names in **Elijjah**.

1. Generic

~~~~
(?|"generic") IDENT
~~~~

2. Typeof

~~~~
"typeof" QUALIDENT
~~~~

3. Normal

~~~~
IDENT[IDENT (, IDENT)*]
~~~~

4. Function

~~~~
("func"|"proc") IDENT LPAREN args RPAREN -> returnType
~~~~

Obviously for a `proc`, one doesn't include the return type.
