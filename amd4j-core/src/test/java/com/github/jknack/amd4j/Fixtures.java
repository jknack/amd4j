package com.github.jknack.amd4j;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

public class Fixtures {

  public static String load(final String path) {
    InputStream stream = null;
    try {
      stream = Fixtures.class.getResourceAsStream(path);
      return IOUtils.toString(stream);
    } catch (IOException ex) {
      throw new IllegalArgumentException("Cannot read: " + path, ex);
    } finally {
      IOUtils.closeQuietly(stream);
    }
  }
}
