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

import static org.apache.commons.io.FilenameUtils.removeExtension;

import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.AstRoot;

/**
 * Remove comments, spaces and new lines from a JavaScript file.
 *
 * @author edgar.espina
 * @since 0.2.0
 */
public class WhiteMinifier extends Minifier {

  @Override
  public CharSequence minify(final Config config, final CharSequence input) {
    Parser parser = new Parser();
    String fname = removeExtension(config.getName()) + ".js";
    AstRoot tree = parser.parse(input.toString(), fname, 1);

    return strip(tree.toSource());
  }

  /**
   * Remove spaces and lines.
   *
   * @param source JavaScript code without comments.
   * @return The new source code.
   */
  private CharSequence strip(final String source) {
    StringBuilder buffer = new StringBuilder();
    int i = 0;
    while (i < source.length()) {
      char ch = source.charAt(i);
      if (ch == '\'' || ch == '"') {
        String literal = stringLiteral(source, ch, i);
        buffer.append(literal);
        i += literal.length() - 1;
      } else if (ch == '/') {
        String regex = regex(source, i);
        buffer.append(regex);
        i += regex.length() - 1;
      } else if (Character.isWhitespace(ch)) {
        if (i + 1 < source.length() && Character.isJavaIdentifierStart(source.charAt(i + 1))) {
          // keep white between keywords or identifiers.
          buffer.append(' ');
        }
      } else {
        buffer.append(ch);
      }
      i++;
    }
    return buffer;
  }

  /**
   * Extract a string literal from source.
   *
   * @param source The javascript source.
   * @param str The string char.
   * @param start The start position.
   * @return A string literal.
   */
  private String stringLiteral(final String source, final char str, final int start) {
    int i = start + 1;
    while (i < source.length()) {
      char ch = source.charAt(i);
      if (ch == str && source.charAt(i - 1) != '\\') {
        break;
      }
      i++;
    }
    return source.substring(start, i + 1);
  }

  /**
   * Extract a regex literal from source.
   *
   * @param source The javascript source.
   * @param start The start position.
   * @return A regex literal.
   */
  private String regex(final String source, final int start) {
    int i = start + 1;
    while (i < source.length()) {
      char ch = source.charAt(i);
      if (ch == '/' && source.charAt(i - 1) != '\\') {
        break;
      }
      i++;
    }
    return source.substring(start, i + 1);
  }
}
