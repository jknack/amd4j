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

public class Optimizer {

  private static final String RELATIVE_PREFIX = "./";

  public static final String EMPTY = "empty:";

  /**
   * The logging system.
   */
  private final Logger logger = LoggerFactory.getLogger(getClass());

  private LinkedList<OptimizerPlugin> plugins = new LinkedList<OptimizerPlugin>();

  private ResourceLoader loader = new ClasspathResourceLoader();

  public Optimizer with(final OptimizerPlugin plugin) {
    plugins.add(plugin);
    return this;
  }

  public Optimizer with(final ResourceLoader loader) {
    this.loader = notNull(loader, "The loader is required.");
    return this;
  }

  public void optimize(final Config config) throws IOException {
    long start = System.currentTimeMillis();
    File out = new File(config.getOut());
    DependencyContext context = new DependencyContext(out);
    logger.info("Tracing dependencies for: {}\n", config.getName());
    logger.info("{}", out.getAbsolutePath());
    logger.info("----------------");
    optimize(config.getName(), config, context);
    context.close();
    long end = System.currentTimeMillis();
    logger.info("{} took: {}ms", out.getAbsolutePath(), end - start);
  }

  private void optimize(final String moduleName, final Config config,
      final DependencyContext context)
      throws IOException {
    String path = config.resolvePath(config.getName());
    if (EMPTY.equals(path)) {
      logger.debug("skipped: {}", config.getName());
      return;
    }

    ResourceURI uri = resolve(loader, createResourceURI(config.getBaseUrl(), path));
    if (context.hasBeenProcessed(uri)) {
      return;
    }
    String content = loader.load(uri);
    Module module = new Module(moduleName, new StringBuilder(content));

    List<OptimizerPlugin> plugins = new ArrayList<OptimizerPlugin>(this.plugins);
    plugins.add(new SemicolonAppenderPlugin());
    plugins.add(new AmdPlugin());

    for (OptimizerPlugin plugin : plugins) {
      if (plugin.apply(uri)) {
        module = plugin.transform(config, module);
      }
    }

    // traverse dependencies
    for (String dependency : module.dependencies) {
      String dependencyName = dependency.replace(RELATIVE_PREFIX, getPath(moduleName));
      String dependencyPath = dependency.replace(RELATIVE_PREFIX, getPath(path));
      Config depConfig = new Config(config.getBaseUrl(), dependencyPath, config.getOut(),
          config.getPaths())
          .setFindNestedDependencies(config.isFindNestedDependencies())
          .setInlineText(config.isInlineText())
          .setShim(config.getShim())
          .setUseStrict(config.isUseStrict());
      optimize(dependencyName, depConfig, context);
    }
    logger.info("{}", uri);
    context.write(module);
  }

  private static ResourceURI createResourceURI(final String baseUrl, final String dependencyPath) {
    String schema = null;
    String path = dependencyPath;
    int idx = path.indexOf("!");
    if (idx > 0) {
      schema = path.substring(0, idx + 1);
      path = path.substring(idx + 1);
    }
    return new ResourceURI(baseUrl, schema, path);
  }

  private static ResourceURI resolve(final ResourceLoader loader, final ResourceURI uri)
      throws IOException {
    String path = uri.path;
    LinkedList<ResourceURI> candidates = new LinkedList<ResourceURI>();
    candidates.add(uri);
    ResourceURI alternative = new ResourceURI(uri.baseUrl, uri.schema, path + ".js");
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
