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


public class ResourceURI {

  public final String schema;

  public final String baseUrl;

  public final String path;

  public ResourceURI(final String baseUrl, final String schema, final String path) {
    String tmp = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
    this.baseUrl = tmp.startsWith(".")? tmp.substring(1): tmp;
    this.schema = schema;
    this.path = path.startsWith("/") ? path.substring(1) : path;
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

  public String getFullPath() {
    return baseUrl + path;
  }
}
