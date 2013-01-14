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

import org.apache.commons.lang3.text.translate.AggregateTranslator;
import org.apache.commons.lang3.text.translate.CharSequenceTranslator;
import org.apache.commons.lang3.text.translate.EntityArrays;
import org.apache.commons.lang3.text.translate.LookupTranslator;
import org.apache.commons.lang3.text.translate.UnicodeEscaper;

/**
 * Embedded text dependencies as an AMD module.
 *
 * @author edgar.espina
 * @since 0.1.0
 */
public class TextTransformer implements Transformer {

  /**
   * Translator object for escaping EcmaScript/JavaScript.
   *
   * While {@link #escapeEcmaScript(String)} is the expected method of use, this
   * object allows the EcmaScript escaping functionality to be used
   * as the foundation for a custom translator.
   *
   * @since 3.0
   */
  public static final CharSequenceTranslator ESCAPE_ECMASCRIPT =
      new AggregateTranslator(
          new LookupTranslator(
              new String[][]{
                  {"'", "\\'" },
                  {"\\", "\\\\" }
              }),
          new LookupTranslator(EntityArrays.JAVA_CTRL_CHARS_ESCAPE()),
          UnicodeEscaper.outsideOf(32, 0x7f)
      );

  @Override
  public boolean apply(final ResourceURI uri) {
    return "text!".equals(uri.schema);
  }

  @Override
  public Module transform(final Config config, final Module module) {
    if (config.isInlineText()) {
      StringBuilder define = new StringBuilder("define([],function () {");
      define.append(" return '")
          .append(ESCAPE_ECMASCRIPT.translate(module.content.toString())).append("';");
      define.append("});\n");
      Module output = new Module(module.name, define);
      output.dependencies.add("text");
      output.dependencies.addAll(module.dependencies);
      return output;
    } else {
      module.content.setLength(0);
      module.dependencies.add("text");
      return module;
    }
  }

}
