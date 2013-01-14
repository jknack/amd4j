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

import java.io.File;

/**
 * A resource identifier.
 *
 * @author edgar.espina
 * @since 0.1.0
 */
public final class ResourceURI {

  /**
   * The uri schema or null if there is no schema.
   */
  public final String schema;

  /**
   * The base's url.
   */
  public final String baseUrl;

  /**
   * The resource's path under the base url.
   */
  public final String path;

  /**
   * Creates a new {@link ResourceURI}.
   *
   * @param baseUrl The base url.
   * @param schema The schema. Optional.
   * @param path The uri path.
   */
  private ResourceURI(final String baseUrl, final String schema, final String path) {
    String tmp = baseUrl.endsWith(File.separator) ? baseUrl : baseUrl + File.separator;
    this.baseUrl = tmp.startsWith(".") ? tmp.substring(1) : tmp;
    this.schema = schema;
    this.path = path.startsWith(File.separator) ? path.substring(File.separator.length()) : path;
  }

  /**
   * Creates a {@link ResourceURI}.
   *
   * @param baseUrl The base url.
   * @param path The dependency's path. It might be preffixed with: <code>schema!</code> where
   *        <code>schema</code> is usually a plugin.
   * @return A new {@link ResourceURI}.
   */
  public static ResourceURI parse(final String baseUrl, final String path) {
    notEmpty(baseUrl, "The baseUrl is required.");
    notEmpty(path, "The path is required.");

    String schema = null;
    String realPath = path;
    int idx = realPath.indexOf("!");
    if (idx > 0) {
      schema = realPath.substring(0, idx + 1);
      realPath = realPath.substring(idx + 1);
    }
    return new ResourceURI(baseUrl, schema, realPath);
  }

  @Override
  public String toString() {
    StringBuilder buffer = new StringBuilder();
    if (schema != null) {
      buffer.append(schema);
    }
    buffer.append(getFullPath());
    return buffer.toString();
  }

  /**
   * Get the full path of this uri.
   *
   * @return The full path of this uri.
   */
  public String getFullPath() {
    return baseUrl + path;
  }
}
