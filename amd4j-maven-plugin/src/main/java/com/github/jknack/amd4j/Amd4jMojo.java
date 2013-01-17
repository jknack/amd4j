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

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.Validate.isTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashSet;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

public abstract class Amd4jMojo extends AbstractMojo {
  /**
   * The file to process.
   *
   * @parameter
   * @required
   */
  protected String[] names;

  /**
   * The baseUrl to use.
   *
   * @parameter expression="${basedir}/src/main/webapp/js"
   */
  protected String baseUrl;

  /**
   * Set paths for modules.
   *
   * @parameter
   */
  protected String[] paths = new String[0];

  /**
   * Turn on discovering of nested dependencies.
   *
   * @parameter
   */
  protected Boolean findNestedDependencies;

  @Override
  public final void execute() throws MojoExecutionException, MojoFailureException {
    String basedir = System.getProperty("user.dir");

    Amd4j amd4j = new Amd4j()
        .with(new TextTransformer())
        .with(new FileResourceLoader(new File(basedir)));

    for (String name : new LinkedHashSet<String>(asList(names))) {
      execute(amd4j, basedir, name);
    }
  }

  /**
   * Execute the command.
   *
   * @param amd4j An {@link Amd4j} instance.
   * @param basedir The working directory.
   * @param name The script's name to execute.
   * @throws IOException If something goes wrong.
   */
  protected final void execute(final Amd4j amd4j, final String basedir, final String name)
      throws MojoExecutionException,
      MojoFailureException {
    try {
      Config config = merge(name, newConfig());
      if (isEmpty(config.getBaseUrl())) {
        config.setBaseUrl(".");
      } else if (!config.getBaseUrl().equals(".")) {
        // remove the user.dir prefix
        config.setBaseUrl(config.getBaseUrl().replace(basedir, ""));
      }
      getLog().debug("options:\n" + config + "\n");
      isTrue(!isEmpty(config.getName()), "The following option is required: %s", "name");
      doExecute(amd4j, config);
    } catch (FileNotFoundException ex) {
      processError(name, "File not found: " + ex.getMessage(), ex);
    } catch (IOException ex) {
      processError(name, "I/O error: " + ex.getMessage(), ex);
    } catch (IllegalArgumentException ex) {
      processError(name, ex.getMessage(), ex);
    } catch (Exception ex) {
      processError(name, "Unexpected error: " + ex.getMessage(), ex);
    }
  }

  protected void processError(final String name, final String message, final Exception cause)
      throws MojoFailureException {
    getLog().error(header(name) + " failed. Reason: " + message);
    throw new MojoFailureException(message, cause);
  }

  protected String header(final String name) {
    return name;
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
   */
  protected Config merge(final String name, final Config config) {
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
    for (String path : paths) {
      int idx = path.indexOf(":");
      isTrue(idx > 0, "wrong path format: %s, expected: path:value");
      String alias = path.substring(0, idx).trim();
      String absolutePath = path.substring(idx + 1).trim();
      config.path(alias, absolutePath);
    }
    return config;
  }

  protected void printf(final String message, final Object... args) {
    getLog().info(String.format(message, args));
  }
}
