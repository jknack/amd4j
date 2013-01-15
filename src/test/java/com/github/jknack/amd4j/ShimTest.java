package com.github.jknack.amd4j;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;

public class ShimTest {

  @Test
  public void emptyShim() {
    Shim shim = new Shim();
    assertEquals("\n" +
        "define(\"empty\", function(){});\n"
        , shim.shim("empty"));
  }

  @Test
  public void exportsNoDeps() {
    Shim shim = new Shim("backbone");
    assertEquals("\n" +
        "define(\"backbone\", [], (function (global) {\n" +
        "    return function () {\n" +
        "        var ret, fn;\n" +
        "        return ret || global.backbone;\n" +
        "    };\n" +
        "}(this)));\n"
        , shim.shim("backbone"));
  }

  @Test
  public void exportsWithDeps() {
    Shim shim = new Shim("backbone", new HashSet<String>(Arrays.asList("underscore")));
    assertEquals("\n" +
        "define(\"backbone\", [\"underscore\"], (function (global) {\n" +
        "    return function () {\n" +
        "        var ret, fn;\n" +
        "        return ret || global.backbone;\n" +
        "    };\n" +
        "}(this)));\n"
        , shim.shim("backbone"));
  }

  @Test
  public void initNoDeps() {
    Shim shim = new Shim("jQuery", new HashSet<String>(), "function(){return jQuery.noConflict();}");
    assertEquals("\n" +
        "define(\"jquery\", [], (function (global) {\n" +
        "    return function () {\n" +
        "        var ret, fn;\n" +
        "fn = function(){return jQuery.noConflict();};\n" +
        "        ret = fn.apply(global, arguments);\n" +
        "        return ret || global.jQuery;\n" +
        "    };\n" +
        "}(this)));\n"
        , shim.shim("jquery"));
  }

  @Test
  public void initWithDeps() {
    Shim shim = new Shim("jQuery", new HashSet<String>(Arrays.asList("underscore")),
        "function(_){return jQuery.noConflict();}");
    assertEquals("\n" +
        "define(\"jquery\", [\"underscore\"], (function (global) {\n" +
        "    return function () {\n" +
        "        var ret, fn;\n" +
        "fn = function(_){return jQuery.noConflict();};\n" +
        "        ret = fn.apply(global, arguments);\n" +
        "        return ret || global.jQuery;\n" +
        "    };\n" +
        "}(this)));\n"
        , shim.shim("jquery"));
  }
}
