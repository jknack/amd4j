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

/**
 * A module visitor.
 *
 * @author edgar.espina
 * @since 0.1.0
 * @see Module#traverse(ModuleVisitor)
 * @param <Out> A process output.
 */
public abstract class ModuleVisitor<Out> {

  /**
   * Start the walk process.
   *
   * @param module The root's module.
   * @return A process output.
   */
  public abstract Out walk(Module module);

  /**
   * Start visit a module.
   *
   * @param module The candidate module.
   * @return True, if the module's dependencies should be visited too.
   */
  public boolean visit(final Module module) {
    return true;
  }

  /**
   * Finish visiting a module.
   *
   * @param module A candidate module.
   */
  public void endvisit(final Module module) {
  }
}
