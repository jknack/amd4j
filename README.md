amd4j
======

A command line tool for running JavaScript scripts that use the [Asychronous Module Defintion API (AMD)](https://github.com/amdjs/amdjs-api/wiki/AMD) for declaring and using JavaScript modules and regular JavaScript script files.

This projects aims to be an alternative to the Rhino version of [r.js](http://requirejs.org/docs/optimization.html) created by [@jrburke](https://github.com/jrburke).

why?
======
Beside all the good work and efforts that [@jrburke](https://github.com/jrburke) did in [r.js](http://requirejs.org/docs/optimization.html) for Java.

I found r.js extremely slow because the use of Rhino.

Please note that r.js for Node is exactly the opposite: *extremely fast*

So, **amd4j need to be extremely fast.**

why not?
======
I love AMD!! So, I created this tool (inspired by [r.js](http://requirejs.org/docs/optimization.html)) for processing AMD scripts in Java where ```node.js``` isn't an option

Usage
======

**Optimizing an AMD script**:

```java
  new Optimizer()
    .optimize(new Config("module.js", new File(out.js)));
```

**Analyzing an AMD script**:

```java
  Module result = new Optimizer().analyze(new Config("module.js")));
 
  // print graph
  System.out.println(result.toStringTree());
 
  // get dependencies
  Iterable<Module> dependencies = result.getDependencies(false);
 
  // get transitive dependencies
  Iterable<Module> dependencies = result.getDependencies(true);
```

what is supported so far?
======

* Processing of single AMD script as input
* Naming modules (the optimizer is able to insert module's names)
* Dependency resolution support
* ```text!``` plugin support
* ```shim``` support

dependencies
======

```
+- org.apache.commons:commons-lang3:jar:3.1:compile
+- org.slf4j:slf4j-api:jar:1.6.4:compile
+- commons-io:commons-io:jar:2.4:compile
+- org.mozilla:rhino:jar:1.7R3:compile
+- com.fasterxml.jackson.core:jackson-databind:jar:2.1.0:compile
|  +- com.fasterxml.jackson.core:jackson-annotations:jar:2.1.0:compile
|  \- com.fasterxml.jackson.core:jackson-core:jar:2.1.0:compile
```

help and support
======
 [Bugs, Issues and Features](https://github.com/jknack/amd4j/issues)

related projects
======
 * [r.js](http://requirejs.org/docs/optimization.html)

credits
======
 * [@jrburke](https://github.com/jrburke)

author
======
 * [@edgarespina](https://twitter.com/edgarespina)

license
======
[Apache License 2](http://www.apache.org/licenses/LICENSE-2.0.html)
