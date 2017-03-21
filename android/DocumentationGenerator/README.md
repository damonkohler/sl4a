## DocumentationGenerator not work now.
In android studio and gradle environment,
I have not successed to build this DocumentationGenerator project.

So, API document(sl4adoc.zip) can't update anymore and
can't remove it from source repository.


## Old instruction for Eclipse environment
This application generates API documentation.

* To compile RpcDoclet in Eclipse, go to:
```
  Windows-->Preferences-->Java-->Build Path-->Classpath Variables
```

* Add "JDK" and point it to a valid jdk environment
  (ie, C:\program files\java\jdk1.6.0_11)

* RpcDoclet needs to be able to find "lib/tools.jar" to compile.

* TextDocumentationGenerator creates a wiki version of the current API Reference
  (suitable for the code.google.com wiki)
  - run it, then cut and paste the console output into your wiki page.

* RpcDoclet is a javadoc add on that combines javadoc and
  Rpc annotations into a more detailled API reference.

* To build the onboard api help, run: javadoc.xml as an ant build.
  You may need to modify the ant environment variables to tell it where
  to find javadoc.exe if it isn't in the path.
  ie:
```
 javadoc.xml-->Run as-->Ant Build...-->Environment Variables
  -->New-->PATH C:\Program Files\Java\jdk1.6.0_11\bin;${env_var:Path}
```

* This will create a doc folder with the api help html files,
  and zip these into ../ScriptingLayerForAndroid/assets/sl4adoc.zip

