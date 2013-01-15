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

import java.io.IOException;
import java.net.URI;

/**
 * Resolve a uri to a resource content.
 *
 * @author edgar.espina
 * @since 0.1.0
 */
public interface ResourceLoader {

  /**
   * True, if the {@link ResourceURI} exists.
   *
   * @param uri The resource uri.
   * @return True, if the {@link ResourceURI} exists.
   * @throws IOException If something goes wrong.
   */
  boolean exists(URI uri) throws IOException;

  /**
   * Try to load a resource content under the given uri.
   *
   * @param uri The resource uri.
   * @return The resource content.
   * @throws IOException If the file isn't found or can't be read.
   */
  String load(URI uri) throws IOException;
}
