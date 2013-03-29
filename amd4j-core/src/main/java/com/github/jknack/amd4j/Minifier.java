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

import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Minify/Optimize JavaScript code.
 *
 * @author edgar.espina
 * @since 0.2.0
 */
public abstract class Minifier {

  /**
   * The default minifier.
   */
  public static final Minifier NONE = new Minifier() {
    @Override
    public CharSequence minify(final CharSequence input) {
      return input;
    }
  };

  /**
   * The minifiers registry.
   */
  private static Map<String, Minifier> registry = new HashMap<String, Minifier>();

  static {
    register("none", NONE);

    register("white", new JSMinifier());
  }

  /**
   * Minify/Optimize JavaScript code.
   *
   * @param input The JavaScript code.
   * @return A minified output.
   */
  public abstract CharSequence minify(CharSequence input);

  /**
   * Get a minifier by name.
   *
   * @param name The minifier name.
   * @return A minifier.
   */
  public static Minifier get(final String name) {
    notEmpty(name, "The name is required.");
    Minifier minifier = registry.get(name.toLowerCase());
    if (minifier == null) {
      throw new IllegalArgumentException("No minifier/optimizer found for: " + name);
    }
    return minifier;
  }

  /**
   * Register a new optimizer/minifier.
   *
   * @param name The optimizer/minifier name. Required.
   * @param minifier The optimizer/minifier.
   */
  public static void register(final String name, final Minifier minifier) {
    notEmpty(name, "The name is required.");
    notNull(minifier, "The minifier is required.");
    registry.put(name.toLowerCase(), minifier);
  }
}
