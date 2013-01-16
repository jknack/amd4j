package com.github.jknack.amd4j;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;

public abstract class BaseCommand implements Command {

  @Parameter(names = "-name", description = "File to process")
  protected String name;

  @Parameter(names = "-baseUrl", description = "All modules are located relative to this path")
  protected String baseUrl;

  @DynamicParameter(names = "-paths.", description = "Set paths for modules. If relative paths, " +
      "set relative to baseUrl above. If a special value of \"empty:\" is used for the path " +
      "value, then that acts like mapping the path to an empty file")
  protected Map<String, String> paths = new HashMap<String, String>();

  @Parameter(names = "-findNestedDependencies",
      description = "Finds require() dependencies inside a require() or define call. By default " +
          "this value is false, because those resources should be considered " +
          "dynamic/runtime calls. Default: false", arity = 1)
  protected Boolean findNestedDependencies;

  protected boolean verbose;

  @Override
  public abstract void execute() throws IOException;

  protected Amd4j newAmd4j(final String baseDir) {
    return new Amd4j()
        .with(new TextTransformer())
        .with(new FileResourceLoader(new File(baseDir)));
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

  @Override
  public void setVerbose(final boolean verbose) {
    this.verbose = verbose;
  }
}
