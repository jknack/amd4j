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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

/**
 * Resolve resource from classpath.
 *
 * @author edgar.espina
 * @since 0.1.0
 */
public class ClasspathResourceLoader implements ResourceLoader {

  /**
   * Handle {@link InputStream}.
   *
   * @author edgar.espina
   *
   * @param <V> The resulting value.
   */
  private interface StreamHandler<V> {

    /**
     * Handle an {@link InputStream}.
     *
     * @param in The {@link InputStream}.
     * @return A value.
     * @throws IOException If the stream cannot be handled.
     */
    V handle(InputStream in) throws IOException;
  }

  @Override
  public boolean exists(final ResourceURI uri) throws IOException {
    return process(uri, new StreamHandler<Boolean>() {
      @Override
      public Boolean handle(final InputStream in) throws IOException {
        return in != null;
      }
    });
  }

  @Override
  public String load(final ResourceURI uri) throws IOException {
    return process(uri, new StreamHandler<String>() {
      @Override
      public String handle(final InputStream in) throws IOException {
        if (in == null) {
          throw new FileNotFoundException("classpath:" + uri);
        }
        return IOUtils.toString(in, "UTF-8");
      }
    });
  }

  /**
   * Resolve the uri to an {@link InputStream} and ask the handler to handle it.
   *
   * @param uri The resource uri.
   * @param handler The stream handler.
   * @param <V> The resulting value.
   * @return The resulting value.
   * @throws IOException If the file isn't found.
   */
  private <V> V process(final ResourceURI uri, final StreamHandler<V> handler)
      throws IOException {
    InputStream input = null;
    try {
      input = getClass().getResourceAsStream(uri.getPath());
      return handler.handle(input);
    } finally {
      IOUtils.closeQuietly(input);
    }
  }
}
