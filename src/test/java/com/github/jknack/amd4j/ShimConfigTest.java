package com.github.jknack.amd4j;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import org.junit.Test;

public class ShimConfigTest {

  @Test
  public void config1() throws IOException {
    Config config = Config.parse(new File("src/test/resources/config/config1.js"));
    assertNotNull(config);
    // backbone
    Shim backbone = config.getShim("backbone");
    assertNotNull(backbone);
    assertEquals(new HashSet<String>(asList("underscore", "jquery")), backbone.dependencies());
    assertEquals("Backbone", backbone.exports());

    // foo
    Shim foo = config.getShim("foo");
    assertNotNull(foo);
    assertEquals(new HashSet<String>(asList("bar")), foo.dependencies());
    assertEquals("Foo", foo.exports());
    assertEquals("function(bar) {\n  return this.Foo.noConflict();\n}", foo.init());
  }

  @Test
  public void config2() throws IOException {
    Config config = Config.parse(new File("src/test/resources/config/config2.js"));
    assertNotNull(config);

    // jquery.colorize
    Shim jqueryColorize = config.getShim("jquery.colorize");
    assertNotNull(jqueryColorize);
    assertEquals(new HashSet<String>(asList("jquery")), jqueryColorize.dependencies());
    assertNull(jqueryColorize.exports());
    assertNull(jqueryColorize.init());

    // jquery.scroll
    Shim jqueryScroll = config.getShim("jquery.scroll");
    assertNotNull(jqueryScroll);
    assertEquals(new HashSet<String>(asList("jquery")), jqueryScroll.dependencies());
    assertNull(jqueryScroll.exports());
    assertNull(jqueryScroll.init());

    // backbone.layoutmanager
    Shim backboneLayoutmanager = config.getShim("backbone.layoutmanager");
    assertNotNull(backboneLayoutmanager);
    assertEquals(new HashSet<String>(asList("backbone")), backboneLayoutmanager.dependencies());
    assertNull(backboneLayoutmanager.exports());
    assertNull(backboneLayoutmanager.init());
  }

}
