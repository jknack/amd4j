package com.github.jknack.amd4j;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;

public abstract class BaseCommand implements Command {

  @Parameter(names = "-name", description = "file's name")
  protected String name;

  @Parameter(names = "-baseUrl", description="All modules are located relative to this path")
  protected String baseUrl;

  @DynamicParameter(names = "-paths.")
  protected Map<String, String> paths = new HashMap<String, String>();

  @Override
  public abstract void execute() throws IOException;

  protected Amd4j newAmd4j(final String baseDir) {
    return new Amd4j().with(new FileResourceLoader(new File(baseDir)));
  }

  protected Amd4j newAmd4j() {
    return newAmd4j(System.getProperty("user.dir"));
  }

  protected Config registerPaths(final Config config) {
    for (Entry<String, String> path : paths.entrySet()) {
      config.path(path.getKey(), path.getValue());
    }
    return config;
  }
}
