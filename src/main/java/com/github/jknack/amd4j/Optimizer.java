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

import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.io.FilenameUtils.getPath;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.Validate.notNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Optimize a module by collecting all the dependencies and building a single file.
 * </p>
 * <p>
 * Basic Usage:
 * </p>
 *
 * <pre>
 *  new Optimizer()
 *       .optimize(new Config(".", "myModule", "output.bundle.js"));
 * </pre>
 *
 * <p>
 * Registering module transformers:
 * </p>
 *
 * <pre>
 *  new Optimizer()
 *       .with(new TextTransformer())
 *       .optimize(new Config(".", "myModule", "output.bundle.js"));
 * </pre>
 *
 * <p>
 * Using a {@link ResourceLoader}:
 * </p>
 * By default, resources are loaded from classpath, you can change that using a
 * {@link ResourceLoader}.
 *
 * <pre>
 *  new Optimizer()
 *       .with(new FileResourceLoader("baseDir"))
 *       .optimize(new Config(".", "myModule", "output.bundle.js"));
 * </pre>
 *
 * @author edgar.espina
 * @since 0.1.0
 */
public class Optimizer {

  /**
   * Relative expression.
   */
  private static final String RELATIVE_EXPRESSION = "./";

  /**
   * The logging system.
   */
  private final Logger logger = LoggerFactory.getLogger(getClass());

  /**
   * The list of transformer to apply.
   */
  private LinkedList<Transformer> transformers = new LinkedList<Transformer>();

  /**
   * The resource loader.
   */
  private ResourceLoader loader = new ClasspathResourceLoader();

  /**
   * Append a new {@link Transformer}.
   *
   * @param transformer The transformer to append. Required.
   * @return This optimizer.
   */
  public Optimizer with(final Transformer transformer) {
    transformers.add(notNull(transformer, "The transformer is required."));
    return this;
  }

  /**
   * Set the resource loader to use.
   *
   * @param loader The resource loader. Required.
   * @return This optimizer.
   */
  public Optimizer with(final ResourceLoader loader) {
    this.loader = notNull(loader, "The loader is required.");
    return this;
  }

  /**
   * Takes an AMD file as input and build a single file with all the dependencies embedded.
   *
   * @param config The configuration object. Required.
   * @throws IOException If the bundle cannot be created.
   */
  public void optimize(final Config config) throws IOException {
    notNull(config, "The config is required.");

    long start = System.currentTimeMillis();
    File out = config.getOut();
    DependencyContext context = new DependencyContext(out);
    logger.info("Tracing dependencies for: {}\n", config.getName());
    logger.info("{}", out.getAbsolutePath());
    logger.info("----------------");
    optimize(config.getName(), config.getName(), config, context);
    context.close();
    long end = System.currentTimeMillis();
    logger.info("{} took: {}ms", out.getAbsolutePath(), end - start);
  }

  /**
   * Takes an AMD file as input and build a single file with all the dependencies embedded.
   *
   * @param modulePath The module's path.
   * @param moduleName The module's name.
   * @param config The configuration object.
   * @param context The dependency context.
   * @throws IOException If the bundle cannot be created.
   */
  private void optimize(final String modulePath, final String moduleName, final Config config,
      final DependencyContext context) throws IOException {
    String path = config.resolvePath(modulePath);
    if (Config.EMPTY.equals(path)) {
      logger.debug("skipped: {}", modulePath);
      return;
    }

    ResourceURI uri = resolve(loader, ResourceURI.parse(config.getBaseUrl(), path));
    if (context.hasBeenProcessed(uri)) {
      return;
    }
    String content = loader.load(uri);
    Module module = new Module(moduleName, new StringBuilder(content));

    List<Transformer> plugins = new ArrayList<Transformer>(transformers);
    plugins.add(new SemicolonAppenderPlugin());
    plugins.add(new AmdTransformer());

    for (Transformer plugin : plugins) {
      if (plugin.apply(uri)) {
        module = plugin.transform(config, module);
      }
    }

    // traverse dependencies
    for (String dependency : module.dependencies) {
      String dependencyName = dependency.replace(RELATIVE_EXPRESSION, getPath(moduleName));
      String dependencyPath = dependency.replace(RELATIVE_EXPRESSION, getPath(path));
      optimize(dependencyPath, dependencyName, config, context);
    }
    logger.info("{}", uri);
    context.write(module);
  }

  /**
   * Resolve a candidate uri to an existing uri. We need this bc, dependencies might or mightn't
   * have a file extension, or they might have a '.' in the file's name.
   *
   * @param loader The resource loader.
   * @param uri The candidate uri.
   * @return An existing uri for the candidate uri.
   * @throws IOException If the uri can't be resolved.
   */
  private static ResourceURI resolve(final ResourceLoader loader, final ResourceURI uri)
      throws IOException {
    String path = uri.path;
    LinkedList<ResourceURI> candidates = new LinkedList<ResourceURI>();
    candidates.add(uri);
    ResourceURI alternative = ResourceURI.parse(uri.baseUrl,
        defaultString(uri.schema, "") + path + ".js");
    if (isEmpty(getExtension(path))) {
      candidates.addFirst(alternative);
    } else {
      candidates.addLast(alternative);
    }
    for (ResourceURI candidate : candidates) {
      if (loader.exists(candidate)) {
        return candidate;
      }
    }
    throw new FileNotFoundException(uri.getFullPath());
  }
}
