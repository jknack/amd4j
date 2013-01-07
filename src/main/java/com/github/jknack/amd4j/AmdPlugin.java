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

import java.util.ArrayList;
import java.util.List;

import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.ArrayLiteral;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.ExpressionStatement;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.StringLiteral;

import sun.org.mozilla.javascript.Token;

public class AmdPlugin implements OptimizerPlugin {

  public static class AmdVisitor implements NodeVisitor {

    private Module module;

    private int offset = 1;

    private List<Integer> lines = new ArrayList<Integer>();

    private Config config;

    private boolean defineFound;

    public AmdVisitor(final Config config, final Module module) {
      this.config = config;
      this.module = module;
      StringBuilder content = module.content;
      lines.add(0);
      for (int idx = 0; idx < content.length(); idx++) {
        int ch = content.charAt(idx);
        if (ch == '\n') {
          lines.add(idx + 1);
        }
      }
    }

    public void shim() {
      if(!defineFound) {
        Shim shim = config.getShim(module.name);
        if (shim != null) {
          String defineFn = shim.shim(module);
          module.content.append(defineFn);
        }
      }
    }

    @Override
    public boolean visit(final AstNode node) {
      int type = node.getType();
      switch (type) {
        case Token.CALL:
          return visit((FunctionCall) node);
        case Token.STRING:
          return visit((StringLiteral) node);
      }
      return true;
    }

    public boolean visit(final StringLiteral node) {
      String useStrict = "use strict";
      if (useStrict.equals(node.getValue()) && !config.isUseStrict()
          && node.getParent() instanceof ExpressionStatement) {
        int offset = lines.get(node.getLineno() - 1) + this.offset;
        int start = module.content.indexOf(useStrict, offset) - 1;
        int end = module.content.indexOf(";", start) + 1;
        module.content.replace(start, end, "");
        this.offset -= useStrict.length();
      }
      return true;
    }

    public boolean visit(final FunctionCall node) {
      AstNode target = node.getTarget();
      if (target instanceof Name) {
        String name = ((Name) target).getIdentifier();
        if ("define".equals(name)) {
          defineFound = true;
          visitDefine(node);
        } else if ("require".equals(name)) {
          int depth = node.getParent().depth() - 1;
          if (config.isFindNestedDependencies() || depth == 0) {
            visitDependencies(node, 0);
          }
        }
      }
      return true;
    }

    private void visitDefine(final FunctionCall node) {
      List<AstNode> arguments = node.getArguments();
      boolean hasName = true;
      boolean hasDep = true;
      if (arguments.size() == 0) {
        hasName = false;
        hasDep = false;
      } else {
        AstNode arg0 = arguments.get(0);
        hasName = arg0 instanceof StringLiteral;
        hasDep = arg0 instanceof ArrayLiteral;
      }
      // Should we add module's name?
      int offset = lines.get(node.getLineno() - 1) + node.getLp() + this.offset;
      int idx = 1;
      if (!hasName) {
        String chunk = "'" + module.name + "',";
        module.content.insert(offset, chunk);
        offset = chunk.length();
        idx = 0;
      }
      if (!hasDep) {
        int newOffset = module.content.indexOf(",", offset) + 1;
        String chunk = "[],";
        module.content.insert(newOffset, chunk);
        offset = chunk.length();
      }
      // collect dependencies
      visitDependencies(node, idx);
    }

    private void visitDependencies(final FunctionCall node, final int idx) {
      List<AstNode> arguments = node.getArguments();
      if (arguments.size() > idx) {
        AstNode arg0 = arguments.get(idx);
        if (arg0 instanceof ArrayLiteral) {
          ArrayLiteral array = (ArrayLiteral) arg0;
          List<AstNode> dependencyList = array.getElements();
          for (AstNode dependencyNode : dependencyList) {
            StringLiteral stringLiteral = (StringLiteral) dependencyNode;
            module.dependencies.add(stringLiteral.getValue());
          }
        }
      }
    }
  }

  @Override
  public boolean apply(final ResourceURI uri) {
    return true;
  }

  @Override
  public Module transform(final Config config, final Module module) {
    if (module.content.length() == 0) {
      return module;
    }
    Parser parser = new Parser();
    AmdVisitor visitor = new AmdVisitor(config, module);
    AstRoot node = parser.parse(module.content.toString(), module.name, 1);
    node.visit(visitor);
    // make sure it has a ';'
    StringBuilder content = module.content;
    int idx = content.length() - 1;
    while (idx >= 0 && Character.isWhitespace(content.charAt(idx))) {
      idx--;
    }
    if (idx >= 0 && idx < content.length() && content.charAt(idx) != ';') {
      content.append(';');
    }
    visitor.shim();
    return module;
  }

}
