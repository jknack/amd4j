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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.mozilla.javascript.Parser;
import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.ArrayLiteral;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.StringLiteral;

public final class DependencyCollector {

  /**
   * The JavaScript visitor responsible of inserting modules names and/or convert modules to AMD.
   *
   * @author edgar.espina
   *
   */
  public static class DependencyAnalizer implements NodeVisitor {

    /**
     * The configuration options.
     */
    private Config config;

    private Set<String> dependencies = new LinkedHashSet<String>();

    /**
     * Creates a new {@link DependencyAnalizer}.
     *
     * @param config The configuration options.
     * @param module The candidate module.
     */
    public DependencyAnalizer(final Config config) {
      this.config = config;
    }

    @Override
    public boolean visit(final AstNode node) {
      int type = node.getType();
      switch (type) {
        case Token.CALL:
          return visit((FunctionCall) node);
        default:
          return true;
      }
    }

    /**
     * Find out "define" and "require" function calls.
     *
     * @param node The function call node.
     * @return True, to keep walking.
     */
    public boolean visit(final FunctionCall node) {
      AstNode target = node.getTarget();
      if (target instanceof Name) {
        String name = ((Name) target).getIdentifier();
        if ("define".equals(name)) {
          visitDependencies(node);
        } else if ("require".equals(name)) {
          int depth = node.getParent().depth() - 1;
          if (config.isFindNestedDependencies() || depth == 0) {
            visitDependencies(node);
          }
        }
      }
      return true;
    }

    /**
     * Report module's dependencies.
     *
     * @param node The function's call.
     */
    private void visitDependencies(final FunctionCall node) {
      List<AstNode> arguments = node.getArguments();
      for (AstNode arg : arguments) {
        if (arg instanceof ArrayLiteral) {
          // found!
          ArrayLiteral array = (ArrayLiteral) arg;
          List<AstNode> dependencyList = array.getElements();
          for (AstNode dependencyNode : dependencyList) {
            StringLiteral stringLiteral = (StringLiteral) dependencyNode;
            dependencies.add(stringLiteral.getValue());
          }
          break;
        }
      }
    }
  }

  private DependencyCollector() {
  }

  public static Set<String> collect(final Config config, final Module module) {
    if (module.content.length() == 0) {
      return Collections.emptySet();
    }

    return new NodeVisitor() {
      private Set<String> dependencies = new LinkedHashSet<String>();

      public Set<String> collect() {
        Parser parser = new Parser();
        AstRoot node = parser.parse(module.content.toString(), module.name, 1);
        if (!isEmpty(module.uri.getScheme())) {
          // report a plugin as a dependency
          dependencies.add(module.uri.getScheme().replace("!", ""));
        }
        node.visit(this);
        // check shim configuration
        Shim shim = config.getShim(module.name);
        if (shim != null) {
          // add dependencies
          if (shim.dependencies() != null) {
            dependencies.addAll(shim.dependencies());
          }
        }
        return dependencies;
      }

      @Override
      public boolean visit(final AstNode node) {
        int type = node.getType();
        switch (type) {
          case Token.CALL:
            return visit((FunctionCall) node);
          default:
            return true;
        }
      }

      /**
       * Find out "define" and "require" function calls.
       *
       * @param node The function call node.
       * @return True, to keep walking.
       */
      public boolean visit(final FunctionCall node) {
        AstNode target = node.getTarget();
        if (target instanceof Name) {
          String name = ((Name) target).getIdentifier();
          if ("define".equals(name)) {
            visitDependencies(node);
          } else if ("require".equals(name)) {
            int depth = node.getParent().depth() - 1;
            if (config.isFindNestedDependencies() || depth == 0) {
              visitDependencies(node);
            }
          }
        }
        return true;
      }

      /**
       * Report module's dependencies.
       *
       * @param node The function's call.
       */
      private void visitDependencies(final FunctionCall node) {
        List<AstNode> arguments = node.getArguments();
        for (AstNode arg : arguments) {
          if (arg instanceof ArrayLiteral) {
            // found!
            ArrayLiteral array = (ArrayLiteral) arg;
            List<AstNode> dependencyList = array.getElements();
            for (AstNode dependencyNode : dependencyList) {
              StringLiteral stringLiteral = (StringLiteral) dependencyNode;
              dependencies.add(stringLiteral.getValue());
            }
            break;
          }
        }
      }
    }.collect();
  }

}
