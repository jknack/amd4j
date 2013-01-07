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
        .with(new TextPlugin())
        .optimize(new Config(".", "z", foutput.getPath()));

    assertTrue(foutput.exists());

    Assume.assumeTrue(RequireOptimizer.isNodeJsPresent());

    // node is in the system, validate the output using node.js
    File fexpected = new File("target/z.expected.js");
    fexpected.delete();
    RequireOptimizer.optimize("-o", "name=z", "out=" + fexpected.getPath(),
        "baseUrl=src/test/resources");

    String output = FileUtils.readFileToString(foutput);
    String expected = FileUtils.readFileToString(fexpected);
    assertEquals(expected, output);
  }

}
