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
import static org.apache.commons.lang3.Validate.notNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jknack.amd4j.ResourceResolver.ResolvedResource;

public class Optimizer {

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
    optimize(config, context, 0);
    context.close();
    long end = System.currentTimeMillis();
    logger.info("{} took: {}ms", out.getAbsolutePath(), end - start);
  }

  private void optimize(final Config config, final DependencyContext context, final int indent)
      throws IOException {
    String path = config.resolvePath(config.getName());
    if (EMPTY.equals(path)) {
      logger.debug("skipped: {}", config.getName());
      return;
    }

    ResolvedResource resource = new ResourceResolver(loader)
        .resolve(createResourceURI(config.getBaseUrl(), path));
    ResourceURI uri = resource.uri;
    if (context.hasBeenProcessed(uri)) {
      return;
    }
    Module module = new Module(config.getName(), new StringBuilder(resource.content));

    List<OptimizerPlugin> plugins = new ArrayList<OptimizerPlugin>(this.plugins);
    plugins.add(new AmdPlugin());

    boolean processed = false;
    for (OptimizerPlugin plugin : plugins) {
      if (plugin.apply(uri)) {
        module = plugin.transform(config, module);
        processed = true;
      }
    }
    if (!processed) {
      throw new IllegalArgumentException("There is no plugin for: " + config.getName());
    }
    // traverse dependencies
    Set<String> dependencies = module.dependencies;
    for (String dependency : dependencies) {
      String dependencyPath = dependency;
      String relativePathPrefix = "./";
      ResourceURI dependencyURI = createResourceURI(config.getBaseUrl(), dependencyPath);
      if (dependencyURI.path.startsWith(relativePathPrefix)) {
        // resolve a relative path
        String currentPath = FilenameUtils.getPath(uri.path);
        dependencyPath = dependencyURI.path.substring(relativePathPrefix.length());
        if (!isEmpty(currentPath)) {
          dependencyPath = currentPath + dependencyPath;
        }
        if (dependencyURI.schema != null) {
          dependencyPath = dependencyURI.schema + dependencyPath;
        }
      }
      Config depConfig = new Config(config.getBaseUrl(), dependencyPath, config.getOut(),
          config.getPaths())
          .setFindNestedDependencies(config.isFindNestedDependencies())
          .setInlineText(config.isInlineText())
          .setShim(config.getShim())
          .setUseStrict(config.isUseStrict());
      optimize(depConfig, context, indent + 2);
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

}
