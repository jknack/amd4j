package com.github.jknack.amd4j;

import java.io.IOException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalysisTest {

  /**
   * The logging system.
   */
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Test
  public void z() throws IOException {
    long start = System.currentTimeMillis();
    Module module = new Amd4j()
        .with(new TextTransformer())
        .analyze("/z");
    long end = System.currentTimeMillis();

    logger.info("{}", module.toStringTree());

    logger.info("amd4j took: {}ms", end - start);

  }

  @Test
  public void complex() throws IOException {
    long start = System.currentTimeMillis();
    Module module = new Amd4j()
        .with(new TextTransformer())
        .analyze(new Config("pages/home/home")
            .setFindNestedDependencies(true)
            .path("sidebar", "widgets/sidebar/sidebar")
            .path("topbar", "widgets/topbar/topbar")
        );

    long end = System.currentTimeMillis();
    logger.info("{}", module.toStringTree());
    logger.info("amd4j took: {}ms", end - start);

  }
}
