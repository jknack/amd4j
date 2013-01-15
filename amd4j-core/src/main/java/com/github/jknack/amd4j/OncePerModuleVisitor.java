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

import java.util.HashSet;
import java.util.Set;

/**
 * Keep track of already visited nodes and don't visit twice.
 *
 * @author edgar.espina
 * @since 0.1.0
 * @param <Out> A process output.
 */
public abstract class OncePerModuleVisitor<Out> extends ModuleVisitor<Out> {

  /**
   * Keep track of visited modules.
   */
  private Set<Module> visited = new HashSet<Module>();

  @Override
  public final boolean visit(final Module module) {
    boolean visit = visited.add(module);
    if (visit) {
      return doVisit(module);
    }
    return visit;
  }

  /**
   * Guarded visit call.
   * @param module The candidate module.
   * @return True, if module dependencies should be visited too.
   */
  protected boolean doVisit(final Module module) {
    return true;
  }

}
