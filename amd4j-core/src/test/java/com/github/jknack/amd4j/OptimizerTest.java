package com.github.jknack.amd4j;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Assume;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OptimizerTest {

  /**
   * The logging system.
   */
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Test
  public void z() throws IOException {
    long start = System.currentTimeMillis();
    File foutput = new File("target/z.bundle.js");
    foutput.delete();
    new Amd4j()
        .with(new TextTransformer())
        .optimize(new Config(".", "z", foutput));
    long end = System.currentTimeMillis();
    logger.info("amd4j took: {}ms", end - start);

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
    long start = System.currentTimeMillis();
    File foutput = new File("target/complex.bundle.js");
    foutput.delete();
    new Amd4j()
        .with(new TextTransformer())
        .optimize(new Config(".", "pages/home/home", foutput)
            .setFindNestedDependencies(true)
            .path("sidebar", "widgets/sidebar/sidebar")
            .path("topbar", "widgets/topbar/topbar")
        );

    long end = System.currentTimeMillis();
    logger.info("amd4j took: {}ms", end - start);
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
