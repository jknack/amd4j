/**
 * Copyright (c) 2013 Edgar Espina
 *
 * This file is part of amd4j (https://github.com/jknack/amd4j)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jknack.amd4j;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.Validate.isTrue;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;

/**
 * Base class for commands.
 *
 * @author edgar.espina
 * @since 0.1.0
 */
public abstract class BaseCommand implements Command {

  /**
   * The file to process.
   */
  @Parameter(names = "-name", description = "File to process")
  protected String name;

  /**
   * The baseUrl to use.
   */
  @Parameter(names = "-baseUrl", description = "All modules are located relative to this path")
  protected String baseUrl;

  /**
   * Set paths for modules.
   */
  @DynamicParameter(names = "-paths.", description = "Set paths for modules. If relative paths, "
      + "set relative to baseUrl above. If a special value of \"empty:\" is used for the path "
      + "value, then that acts like mapping the path to an empty file")
  protected Map<String, String> paths = new HashMap<String, String>();

  /**
   * Turn on discovering of nested dependencies.
   */
  @Parameter(names = "-findNestedDependencies",
      description = "Finds require() dependencies inside a require() or define call. By default "
          + "this value is false, because those resources should be considered "
          + "dynamic/runtime calls. Default: false", arity = 1)
  protected Boolean findNestedDependencies;

  /**
   * Turn on/off debug mode.
   */
  @Parameter(names = "-X", description = "turn on debug mode")
  private boolean verbose;

  @Override
  public void execute() throws IOException {
    String userDir = System.getProperty("user.dir");

    Amd4j amd4j = new Amd4j()
        .with(new TextTransformer())
        .with(new FileResourceLoader(new File(userDir)));

    Config config = merge(newConfig());
    if (isEmpty(config.getBaseUrl())) {
      config.setBaseUrl(".");
    } else if (!config.getBaseUrl().equals(".")) {
      // remove the user.dir prefix
      config.setBaseUrl(config.getBaseUrl().replace(userDir, ""));
    }
    if (verbose) {
      System.out.printf("options:\n%s\n", config);
    }
    isTrue(!isEmpty(config.getName()), "The following option is required: %s", "name");
    doExecute(amd4j, config);
  }

  /**
   * Execute the command.
   *
   * @param amd4j An {@link Amd4j} instance.
   * @param config The configuration options.
   * @throws IOException If something goes wrong.
   */
  protected abstract void doExecute(Amd4j amd4j, Config config) throws IOException;

  /**
   * Creates a new {@link Config}.
   *
   * @return A new {@link Config}.
   * @throws IOException If something goes wrong.
   */
  protected Config newConfig() throws IOException {
    return new Config();
  }

  /**
   * Override any option that migh be specify from the command line.
   *
   * @param config The configuration options.
   * @return The same configuration options.
   * @throws IOException If file handler can't be obtained.
   */
  protected Config merge(final Config config) throws IOException {
    if (!isEmpty(name)) {
      config.setName(name);
    }
    if (!isEmpty(baseUrl)) {
      config.setBaseUrl(baseUrl);
    }
    if (findNestedDependencies != null) {
      config.setFindNestedDependencies(findNestedDependencies.booleanValue());
    }
    // merge paths
    for (Entry<String, String> path : paths.entrySet()) {
      config.path(path.getKey(), path.getValue());
    }
    return config;
  }

  @Override
  public void setVerbose(final boolean verbose) {
    if (!this.verbose) {
      this.verbose = verbose;
    }
  }
}
