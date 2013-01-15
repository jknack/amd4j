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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.mozilla.javascript.Parser;
import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.ArrayLiteral;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.ExpressionStatement;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.StringLiteral;

/**
 * Augment a module by inserting the module names into <code>anonymous define</code> functions.
 * The insertation is done by parsing the JavaScript code using the Rhino {@link Parser}.
 * Finally, if a <code>define</code> statement isn't found and there is a
 * {@link Config#getShim(String)} shim option for the module, the module will be converted to AMD.
 *
 * @author edgar.espina
 * @since 0.1.0
 */
public class AmdTransformer implements Transformer {

  /**
   * The JavaScript visitor responsible of inserting modules names and/or convert modules to AMD.
   *
   * @author edgar.espina
   *
   */
  public static class AmdVisitor implements NodeVisitor {

    /**
     * Keep track of inserted chunks.
     */
    private int offset = 1;

    /**
     * The start offset per each line.
     */
    private List<Integer> lines = new ArrayList<Integer>();

    /**
     * The configuration options.
     */
    private Config config;

    /**
     * True, if there isn't a define function.
     */
    private boolean defineFound;

    private String moduleName;

    private StringBuilder content;

    /**
     * Creates a new {@link AmdVisitor}.
     *
     * @param config The configuration options.
     * @param moduleName The module's name.
     */
    public AmdVisitor(final Config config, final String moduleName, final StringBuilder content) {
      this.config = config;
      this.moduleName = moduleName;
      this.content = content;
      lines.add(0);
    }

    /**
     * Get the start text offset for the given line.
     *
     * @param line The line number.
     * @return The start text offset for the requested line.
     */
    public int lineAt(final int line) {
      int idx = lines.get(lines.size() - 1);
      while (lines.size() <= line && idx < content.length()) {
        int ch = content.charAt(idx);
        if (ch == '\n') {
          lines.add(idx + 1);
        }
        idx++;
      }
      return lines.get(line);
    }

    @Override
    public boolean visit(final AstNode node) {
      int type = node.getType();
      switch (type) {
        case Token.CALL:
          return visit((FunctionCall) node);
        case Token.STRING:
          return visit((StringLiteral) node);
        default:
          return true;
      }
    }

    /**
     * Remove "use strict" statement if the configuration doesn't allow it.
     *
     * @param node The string literal node.
     * @return True, to keep walking.
     */
    public boolean visit(final StringLiteral node) {
      String useStrict = "use strict";
      if (useStrict.equals(node.getValue()) && !config.isUseStrict()
          && node.getParent() instanceof ExpressionStatement) {
        int offset = lineAt(node.getLineno() - 1) + this.offset;
        int start = content.indexOf(useStrict, offset) - 1;
        int end = content.indexOf(";", start) + 1;
        content.replace(start, end, "");
        this.offset -= useStrict.length();
      }
      return true;
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
          defineFound = true;
          visitDefine(node);
        }
      }
      return true;
    }

    /**
     * <ul>
     * <li>Insert module's name if necessary.</li>
     * <li>Insert module's dependencies if necessary.</li>
     * <li>Report dependencies if any.</li>
     * </ul>
     *
     * @param node The function call.
     */
    private void visitDefine(final FunctionCall node) {
      List<AstNode> arguments = node.getArguments();
      final boolean hasName;
      final boolean hasDep;
      if (arguments.size() == 0) {
        hasName = false;
        hasDep = false;
      } else if (arguments.size() == 1) {
        hasName = arguments.get(0) instanceof StringLiteral;
        hasDep = arguments.get(0) instanceof ArrayLiteral;
      } else {
        hasName = arguments.get(0) instanceof StringLiteral;
        if (!hasName) {
          hasDep = arguments.get(0) instanceof ArrayLiteral;
        } else {
          hasDep = arguments.get(1) instanceof ArrayLiteral;
        }
      }
      // Should we add module's name?
      int offset = lineAt(node.getLineno() - 1) + node.getLp() + this.offset;
      if (!hasName) {
        String chunk = "'" + moduleName + "',";
        content.insert(offset, chunk);
        offset = chunk.length();
      }
      if (!hasDep) {
        int newOffset = content.indexOf(",", offset) + 1;
        String chunk = "[],";
        content.insert(newOffset, chunk);
        offset = chunk.length();
      }
    }

  }

  @Override
  public boolean apply(final URI uri) {
    return true;
  }

  @Override
  public StringBuilder transform(final Config config, final String name, final StringBuilder content) {
    if (content.length() == 0) {
      return content;
    }
    Parser parser = new Parser();
    AmdVisitor visitor = new AmdVisitor(config, name, content);
    AstRoot node = parser.parse(content.toString(), name, 1);
    node.visit(visitor);
    // shim
    if (!visitor.defineFound) {
      Shim shim = config.getShim(name);
      if (shim != null) {
        String defineFn = shim.shim(name);
        content.append(defineFn);
      }
    }
    return content;
  }

}
