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

import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Shim configuration options for 'browser globals scripts'.
 *
 * @author edgar.espina
 * @since 0.1.0
 */
public class Shim {

  /**
   * Module's dependencies.
   */
  private LinkedHashSet<String> deps;

  /**
   * The name of the global variable to exports.
   */
  private String exports;

  /**
   * Alternative, use an init function for export a global variable.
   */
  private String init;

  /**
   * Default constructor.
   */
  protected Shim() {
  }

  /**
   * Set a shim dependencies.
   *
   * @param deps the shim dependencies.
   * @return This shim object.
   */
  Shim setDeps(final Collection<String> deps) {
    this.deps = new LinkedHashSet<String>();
    if (deps != null) {
      this.deps.addAll(deps);
    }
    return this;
  }

  /**
   * Set a shim exports attribute.
   *
   * @param exports the shim exports attribute.
   * @return This shim object.
   */
  Shim setExports(final String exports) {
    this.exports = exports;
    return this;
  }

  /**
   * Set the init attribute.
   *
   * @param init the shim init attribute.
   * @return This shim object.
   */
  Shim setInit(final String init) {
    this.init = init;
    return this;
  }

  /**
   * Creates a new {@link Shim} object.
   *
   * @param exports The exports option.
   * @param dependencies The dependency list.
   * @param init The init function.
   */
  public Shim(final String exports, final Set<String> dependencies, final String init) {
    this.exports = notEmpty(exports, "The exports is required.");
    deps = new LinkedHashSet<String>(notNull(dependencies, "The dependencies is required."));
    this.init = notEmpty(init, "The init is required.");
  }

  /**
   * Creates a new {@link Shim} object.
   *
   * @param exports The exports option.
   * @param dependencies The dependency list.
   */
  public Shim(final String exports, final Set<String> dependencies) {
    this.exports = notEmpty(exports, "The exports is required.");
    deps = new LinkedHashSet<String>(notNull(dependencies, "The dependencies is required."));
  }

  /**
   * Creates a new {@link Shim} object.
   *
   * @param exports The exports option.
   */
  public Shim(final String exports) {
    this.exports = notEmpty(exports, "The exports is required.");
  }

  /**
   * Module's dependencies.
   *
   * @return Module's dependencies.
   */
  public Set<String> dependencies() {
    return deps;
  }

  /**
   * The name of the global variable to export.
   *
   * @return The name of the global variable to export.
   */
  public String exports() {
    return exports;
  }

  /**
   * Alternative, use an init function for export a global variable.
   *
   * @return An init function for export a global variable.
   */
  public String init() {
    return init;
  }

  @Override
  public String toString() {
    StringBuilder buffer = new StringBuilder();
    buffer.append("{\n");
    List<String> properties = new ArrayList<String>();
    properties.add("  \"deps\": " + depsToString("[]"));
    if (exports != null) {
      properties.add("  \"exports\": \"" + exports + "\"");
    }
    if (init != null) {
      properties.add("  \"init\": \"" + init + "\"");
    }
    buffer.append(join(properties, ",\n"));
    buffer.append("\n}");
    return buffer.toString();
  }

  /**
   * Get an string representation of the dependency set.
   *
   * @param empty The empty value to use if no dependencies.
   * @return An string representation of the dependency set.
   */
  private String depsToString(final String empty) {
    return deps == null || deps.isEmpty() ? empty : "[\"" + join(deps, "\", \"") + "\"]";
  }

}
