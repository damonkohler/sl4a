
These files are part of the org.objectweb.asm distribution.
(http://asm.objectweb.org) and are included under the terms of the LGPL license.

ASM is a very light weight, fast, visitor-pattern style Java byte code 
reader / generator.  

We have repackaged these classes under a "bsh." prefix for two reasons: 
1) BeanShell uses the subset of ASM only for writing classes and 
2) Since BeanShell is widely distributed we don't want to break the ability
of script writers to use updated versions of ASM in their scripts.

