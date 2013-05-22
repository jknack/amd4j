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

import static org.apache.commons.lang3.StringUtils.leftPad;
import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Hold information about an AMD.
 *
 * @author edgar.espina
 * @since 0.1.0
 */
public class Module implements Iterable<Module> {

  /**
   * The module's name.
   */
  public final String name;

  /**
   * The mutable module's content.
   */
  public final String content;

  /**
   * The dependency set.
   */
  private final Set<Module> dependencies = new LinkedHashSet<Module>();

  /**
   * The module's uri.
   */
  public final ResourceURI uri;

  /**
   * Creates a new {@link Module}.
   *
   * @param name The module's name. Required.
   * @param uri The module's uri. Required.
   * @param content The module's content. Required.
   */
  public Module(final String name, final ResourceURI uri, final String content) {
    this.name = notEmpty(name, "The name is required.");
    this.uri = notNull(uri, "The uri is required.");
    this.content = notEmpty(content, "The content is required.");
  }

  /**
   * Walk through all the module's dependencies.
   *
   * @param visitor A module's visitor. Required.
   */
  public void traverse(final ModuleVisitor<?> visitor) {
    notNull(visitor, "The visitor is required.");
    if (visitor.visit(this)) {
      for (Module dependency : dependencies) {
        dependency.traverse(visitor);
      }
      visitor.endvisit(this);
    }
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof Module) {
      Module that = (Module) obj;
      return uri.equals(that.uri);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return uri.hashCode();
  }

  /**
   * Add a new dependency.
   *
   * @param dependency A new dependency. Required.
   */
  public void add(final Module dependency) {
    notNull(dependency, "The dependency is required.");
    dependencies.add(dependency);
  }

  /**
   * Remove an existing dependency.
   *
   * @param dependency A dependency. Required.
   */
  public void remove(final Module dependency) {
    notNull(dependency, "The dependency is required.");
    dependencies.remove(dependency);
  }

  @Override
  public Iterator<Module> iterator() {
    return dependencies.iterator();
  }

  /**
   * Return all the module's dependencies.
   *
   * @param includeTransitive True, if transitive dependencies should be returned.
   * @return All the module's dependencies.
   */
  public List<Module> getDependencies(final boolean includeTransitive) {
    if (includeTransitive) {
      return new OncePerModuleVisitor<List<Module>>() {
        List<Module> dependencies = new ArrayList<Module>();

        @Override
        public List<Module> walk(final Module module) {
          module.traverse(this);
          return dependencies;
        }

        @Override
        public boolean doVisit(final Module module) {
          dependencies.add(module);
          return true;
        }
      } .walk(this);
    }
    return new ArrayList<Module>(dependencies);
  }

  /**
   * Print a tree of the module. Useful for debugging.
   *
   * @return Print a tree of the module. Useful for debugging.
   */
  public String toStringTree() {
    return new ModuleVisitor<String>() {
      /**
       * The buffer.
       */
      StringBuilder buffer = new StringBuilder();

      /**
       * The node's level.
       */
      int level = 0;

      /**
       * The tab's size.
       */
      int tabSize = 3;

      @Override
      public String walk(final Module module) {
        module.traverse(this);
        return buffer.toString();
      }

      @Override
      public boolean visit(final Module module) {
        buffer.append(leftPad("", level));
        buffer.append(module.uri).append("\n");
        level += tabSize;
        return true;
      }

      @Override
      public void endvisit(final Module module) {
        level -= tabSize;
      }
    } .walk(this);
  }

  @Override
  public String toString() {
    return name;
  }
}
