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
 * A transformer can modify the content of a {@link Module}.
 * Also, a transformer can report dependencies for modules.
 * A depedency will be resolve later by the {@link Amd4j}.
 *
 * @author edgar.espina
 * @since 0.1.0
 */
public interface Transformer {

  /**
   * True, if the transformer apply for the given uri.
   *
   * @param uri The resource uri. Not null.
   * @return True, if the transformer apply for the given uri.
   */
  boolean apply(ResourceURI uri);

  /**
   * Transform module content and return a new or modified {@link StringBuilder}.
   *
   * @param config The configuration options.
   * @param name The module's name.
   * @param content The module's content.
   * @return A new or modified module's content.
   */
  StringBuilder transform(Config config, String name, StringBuilder content);
}
