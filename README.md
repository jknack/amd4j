amd4j [![Build Status](https://travis-ci.org/jknack/amd4j.png?branch=master)](https://travis-ci.org/jknack/amd4j)
======

A command line tool for running JavaScript scripts that use the [Asychronous Module Defintion API (AMD)](https://github.com/amdjs/amdjs-api/wiki/AMD) for declaring and using JavaScript modules and regular JavaScript script files.

This projects aims to be an alternative to the Rhino version of [r.js](http://requirejs.org/docs/optimization.html) created by [@jrburke](https://github.com/jrburke).

why?
======
Beside all the good work and efforts that [@jrburke](https://github.com/jrburke) did in [r.js](http://requirejs.org/docs/optimization.html) for Java.
I found ```r.js``` extremely slow because the use of Rhino.

So, **amd4j need to be extremely fast**.

why not?
======
I love AMD!! So, I created this tool (inspired by [r.js](http://requirejs.org/docs/optimization.html)) for processing AMD scripts in Java where ```node.js``` isn't an option

API Usage
======

**Optimizing an AMD script**:

```java
  new Amd4j()
    .optimize(new Config("module.js", new File("module.out.js")));
```

**Analyzing an AMD script**:

```java
  Module result = new Amd4j().analyze(new Config("module.js")));
 
  // print graph
  System.out.println(result.toStringTree());
 
  // get dependencies
  Iterable<Module> dependencies = result.getDependencies(false);
 
  // get transitive dependencies
  Iterable<Module> dependencies = result.getDependencies(true);
```

Command Line Usage
======

**Optimizing an AMD script**:

```shell
java -jar amd4j-tool.jar -o baseUrl=. name=module.js out=module.opt.js
```

**Analyzing an AMD script**:

```shell
java -jar amd4j-tool.jar -a baseUrl=. name=module.js
```

Maven Usage
======

**Optimizing an AMD script**:

```xml
  <build>
    <plugins>
      <plugin>
        <groupId>com.github.jknack</groupId>
        <artifactId>amd4j-maven-plugin</artifactId>
        <version>${amd4j-version}</version>
        <configuration>
          <baseUrl>src/webapp/js</baseUrl>
          <!-- ${script.name} will be replaced by home and page -->
          <out>${project.build.directory}/${project.build.finalName}/${script.name}.opt.js</out>
          <inlineText>true</inlineText>
          <useStrict>false</useStrict>
          <buildFile></buildFile>
          <!-- One of: none, white (strip comments, spaces and lines), closure (simple optimizations),  closure.advanced, closure.white -->
          <optimize>none</optimize>

          <!--file to be processed-->
          <names>
            <name>home</name>
            <name>page</name>
          </names>

          <!--path configuration-->
          <paths>
            <path>jquery:empty:</path>
            <path>topbar:widgets/topbar/topbar</path>
            <path>sidebar:widgets/sidebar/sidebar</path>
          </paths>
        </configuration>
        <executions>
          <execution>
            <id>optimize</id>
            <goals>
              <goal>optimize</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
```
The plugin will generate two files: ```target/${project.build.finalName}/home.opt.js``` and ```target/${project.build.finalName}/page.opt.js```

**Analyzing an AMD script**:

Just change the goal to: ```analyze```

what is supported so far?
======

* Processing of single AMD script as input
* Naming modules (the optimizer is able to insert module's names)
* Dependency resolution support
* build profile support
* paths support
* ```text!``` plugin support
* ```shim``` support

maven
======
Stable version: **0.1.1**


```xml
  <dependency>
    <groupId>com.github.jknack</groupId>
    <artifactId>amd4j</artifactId>
    <version>${amd4j-version}</version>
  </dependency>
```
 
Development version: **0.1.2-SNAPSHOT**

SNAPSHOT versions are NOT synchronized to Central. If you want to use a snapshot version you need to add the https://oss.sonatype.org/content/repositories/snapshots/ repository to your pom.xml.

dependencies
======

```
+- org.apache.commons:commons-lang3:jar:3.1:compile
+- org.slf4j:slf4j-api:jar:1.6.4:compile
+- commons-io:commons-io:jar:2.4:compile
```

help and support
======
 [Bugs, Issues and Features](https://github.com/jknack/amd4j/issues)

related projects
======
 [r.js](http://requirejs.org/docs/optimization.html)

credits
======
 [@jrburke](https://github.com/jrburke)

author
======
 [@edgarespina](https://twitter.com/edgarespina)

license
======
[Apache License 2](http://www.apache.org/licenses/LICENSE-2.0.html)
