
This is the JSR-223 javax.script engine and factory implementation
for BeanShell.

This package requires BeanShell 2.0b5.

This package currently requires Java 5 to compile, because the javax.script
API uses generics. 



TODO:

Support compile() method (we need to support this directly in Interpreter).

Optimize the default script context for BeanShell... Make a BeanShell specific
context that knows about bsh namespaces and doesn't require bsh to externalize
via the maps.


