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
 * Append a ';' to a module.
 *
 * @author edgar.espina
 * @since 0.1.0
 */
public class SemicolonAppenderPlugin implements Transformer {

  @Override
  public boolean apply(final ResourceURI uri) {
    return true;
  }

  @Override
  public StringBuilder transform(final Config config, final String name,
      final StringBuilder content) {
    // make sure it has a ';'
    int idx = content.length() - 1;
    while (idx >= 0 && Character.isWhitespace(content.charAt(idx))) {
      idx--;
    }
    if (idx >= 0 && idx < content.length() && content.charAt(idx) != ';') {
      content.append(';');
    }
    return content;
  }

}
