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

public class ClasspathResourceLoader implements ResourceLoader {

  private interface StreamHandler<V> {
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

  private <V> V process(final ResourceURI uri, final StreamHandler<V> handler)
      throws IOException {
    String path = uri.baseUrl + uri.path;
    InputStream input = null;
    try {
      input = getClass().getResourceAsStream(path);
      return handler.handle(input);
    } finally {
      IOUtils.closeQuietly(input);
    }
  }
}
