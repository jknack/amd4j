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

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Hold information about an AMD.
 *
 * @author edgar.espina
 * @since 0.1.0
 */
public class Module {

  /**
   * The module's name.
   */
  public final String name;

  /**
   * The mutable module's content.
   */
  public final StringBuilder content;

  /**
   * The mutable module's dependencies. This can be alias or absolute paths to a file.
   */
  public final Set<String> dependencies = new LinkedHashSet<String>();

  /**
   * Creates a new {@link Module}.
   *
   * @param name The module's name. Required.
   * @param content The module's content. Required.
   */
  public Module(final String name, final CharSequence content) {
    this.name = notEmpty(name, "The name is required.");
    this.content = new StringBuilder(notEmpty(content, "The content is required."));
  }

  @Override
  public String toString() {
    return name;
  }
}
