package com.github.jknack.amd4j;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class ConfigTest {

  @Test
  public void requirejs() throws IOException {
    Config config = Config.parse(new File("src/test/resources/config/requirejs.js"));
    assertNotNull(config);
    assertEquals("requirejs", config.getName());
  }

  @Test
  public void require() throws IOException {
    Config config = Config.parse(new File("src/test/resources/config/require.js"));
    assertNotNull(config);
    assertEquals("require", config.getName());
  }

  @Test
  public void requirejsConfig() throws IOException {
    Config config = Config.parse(new File("src/test/resources/config/requirejs.config.js"));
    assertNotNull(config);
    assertEquals("requirejs.config", config.getName());
  }

  @Test
  public void requireConfig() throws IOException {
    Config config = Config.parse(new File("src/test/resources/config/require.config.js"));
    assertNotNull(config);
    assertEquals("require.config", config.getName());
  }

  @Test
  public void requireLiteral() throws IOException {
    Config config = Config.parse(new File("src/test/resources/config/require.literal.js"));
    assertNotNull(config);
    assertEquals("require.literal", config.getName());
  }

  @Test
  public void requireJson() throws IOException {
    Config config = Config.parse(new File("src/test/resources/config/require.json.js"));
    assertNotNull(config);
    assertEquals("require.json", config.getName());
  }

  @Test
  public void pathFallbacks() throws IOException {
    Config config = Config.parse(new File("src/test/resources/config/config2.js"));
    assertNotNull(config);
    List<String> fallback = config.paths("jquery");
    assertEquals(Arrays.asList("https://ajax.googleapis.com/ajax/libs/jquery/1.8.3/jquery.min",
        "js/components/external/jquery"), fallback);
  }
}
