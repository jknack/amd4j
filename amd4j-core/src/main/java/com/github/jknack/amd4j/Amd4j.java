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
import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

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
 *       .optimize(new Config("myModule", "output.bundle.js"));
 * </pre>
 *
 * <p>
 * Registering module transformers:
 * </p>
 *
 * <pre>
 *  new Optimizer()
 *       .with(new TextTransformer())
 *       .optimize(new Config("myModule", "output.bundle.js"));
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
 *       .optimize(new Config("myModule", "output.bundle.js"));
 * </pre>
 *
 * @author edgar.espina
 * @since 0.1.0
 */
public class Amd4j {

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
  public Amd4j with(final Transformer transformer) {
    transformers.add(notNull(transformer, "The transformer is required."));
    return this;
  }

  /**
   * Set the resource loader to use.
   *
   * @param loader The resource loader. Required.
   * @return This optimizer.
   */
  public Amd4j with(final ResourceLoader loader) {
    this.loader = notNull(loader, "The loader is required.");
    return this;
  }

  /**
   * Analyze a module by collecting all the dependencies.
   *
   * @param uri The module uri. Required.
   * @return A module and their dependencies.
   */
  public Module analyze(final URI uri) {
    notNull(uri, "The config is required.");

    return analyze(new Config(uri.toString()));
  }

  /**
   * Analyze a module by collecting all the dependencies.
   *
   * @param config The configuration options. Required.
   * @return A module and their dependencies.
   */
  public Module analyze(final Config config) {
    notNull(config, "The config is required.");

    logger.debug("Tracing dependencies for: {}\n", config.getName());
    Module module = walk(config.getName(), config.getName(), config,
        new HashMap<URI, Module>());
    return module;
  }

  /**
   * Merge all the dependencies into one single file, name anonymous modules and make AMD compatible
   * whose script that has a shim entry in the configuration options.
   *
   * @param config The configuration options. Required.
   * @return The module graph.
   */
  public Module optimize(final Config config) {
    Module module = analyze(config);
    new Optimizer(config, transformers).walk(module);
    return module;
  }

  /**
   * Walk through a module and collect dependencies.
   *
   * @param modulePath The module's path.
   * @param moduleName The module's name.
   * @param config The configuration options.
   * @param registry The already processed modules.
   * @return A module or null if the module should be skipped.
   */
  private Module walk(final String modulePath, final String moduleName, final Config config,
      final Map<URI, Module> registry) {
    try {
      String path = config.resolvePath(modulePath);
      if (Config.EMPTY.equals(path)) {
        logger.debug("skipped: {}", modulePath);
        return null;
      }

      URI uri = resolve(loader, newURI(config.getBaseUrl(), path));
      Module existing = registry.get(uri);
      if (existing != null) {
        logger.debug("included already: {}", modulePath);
        return existing;
      }
      String content = loader.load(uri);
      Module module = new Module(moduleName, uri, content);
      registry.put(uri, module);

      // collect dependencies
      Set<String> unresolvedDependencies = DependencyCollector.collect(config, module);
      for (String unresolved : unresolvedDependencies) {
        String dependencyName = unresolved.replace(RELATIVE_EXPRESSION, getPath(moduleName));
        String dependencyPath = unresolved.replace(RELATIVE_EXPRESSION, getPath(path));
        Module resolved = walk(dependencyPath, dependencyName, config, registry);
        if (resolved != null) {
          module.add(resolved);
        }
      }
      logger.debug("{}", uri);
      return module;
    } catch (AmdException ex) {
      LinkedList<String> path = new LinkedList<String>();
      path.add(moduleName);
      path.addAll(ex.getPath());
      AmdException rewriteEx = new AmdException(path, ex.getCause());
      throw rewriteEx;
    } catch (Exception ex) {
      throw new AmdException(moduleName, ex);
    }
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
  private static URI resolve(final ResourceLoader loader, final URI uri)
      throws IOException {
    String path = uri.getPath();
    LinkedList<URI> candidates = new LinkedList<URI>();
    candidates.add(uri);
    URI alternative = URI.create(uri.toString() + ".js");
    if (isEmpty(getExtension(path))) {
      candidates.addFirst(alternative);
    } else {
      candidates.addLast(alternative);
    }
    for (URI candidate : candidates) {
      if (loader.exists(candidate)) {
        return candidate;
      }
    }
    // force a file not found exception
    throw new FileNotFoundException(uri.toString());
  }

  /**
   * Creates a {@link URI}.
   *
   * @param baseUrl The base url.
   * @param path The dependency's path. It might be preffixed with: <code>schema!</code> where
   *        <code>schema</code> is usually a plugin.
   * @return A new {@link ResourceURI}.
   */
  private static URI newURI(final String baseUrl, final String path) {
    notEmpty(baseUrl, "The baseUrl is required.");
    String normBaseUrl = baseUrl;
    if (".".equals(normBaseUrl)) {
      normBaseUrl = File.separator;
    }
    if (!normBaseUrl.startsWith(File.separator)) {
      normBaseUrl = File.separator + normBaseUrl;
    }
    if (!normBaseUrl.endsWith(File.separator)) {
      normBaseUrl += File.separator;
    }
    int idx = Math.max(0, path.indexOf('!') + 1);
    StringBuilder uri = new StringBuilder(path);
    if (uri.charAt(idx) == File.separatorChar) {
      uri.deleteCharAt(idx);
    }
    uri.insert(idx, normBaseUrl);
    return newURI(uri.toString());
  }

  /**
   * Creates a {@link URI}.
   *
   * @param path The dependency's path. It might be prefixed with: <code>schema!</code> where
   *        <code>schema</code> is usually a plugin.
   * @return A new {@link ResourceURI}.
   */
  private static URI newURI(final String path) {
    notEmpty(path, "The path is required.");

    String uri = path.replace("!", ":");
    return URI.create(uri);
  }
}
