package com.github.jknack.amd4j;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Assume;
import org.junit.Test;

public class OptimizerTest {

  @Test
  public void z() throws IOException {
    File foutput = new File("target/z.bundle.js");
    foutput.delete();
    new Optimizer()
        .with(new TextTransformer())
        .optimize(new Config(".", "z", foutput));

    assertTrue(foutput.exists());

    Assume.assumeTrue(RequireOptimizer.isNodeJsPresent());

    // node is in the system, validate the output using node.js
    File fexpected = new File(System.getProperty("user.dir"), "target/z.expected.js");
    fexpected.delete();
    RequireOptimizer.optimize("-o", "name=z", "out=" + fexpected.getPath(),
        "baseUrl=.");

    String output = FileUtils.readFileToString(foutput);
    String expected = FileUtils.readFileToString(fexpected);
    assertEquals(expected, output);
  }

  @Test
  public void complex() throws IOException {
    File foutput = new File("target/complex.bundle.js");
    foutput.delete();
    new Optimizer()
        .with(new TextTransformer())
        .optimize(new Config(".", "pages/home/home", foutput)
            .setFindNestedDependencies(true)
            .path("sidebar", "widgets/sidebar/sidebar")
            .path("topbar", "widgets/topbar/topbar")
        );

    assertTrue(foutput.exists());

    Assume.assumeTrue(RequireOptimizer.isNodeJsPresent());

    // node is in the system, validate the output using node.js
    File fexpected = new File(System.getProperty("user.dir"), "target/complex.expected.js");
    fexpected.delete();
    RequireOptimizer.optimize("-o", "name=pages/home/home", "out=" + fexpected.getPath(),
        "baseUrl=.",
        "findNestedDependencies=true",
        "paths.text=text",
        "paths.sidebar=widgets/sidebar/sidebar",
        "paths.topbar=widgets/topbar/topbar"
        );

    String output = FileUtils.readFileToString(foutput);
    String expected = FileUtils.readFileToString(fexpected);
    assertEquals(expected, output);
  }
}
