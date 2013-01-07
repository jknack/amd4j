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

  @Override
  public String load(final ResourceURI uri) throws IOException {
    String path = uri.baseUrl + uri.path;
    InputStream input = null;
    try {
      input = getClass().getResourceAsStream(path);
      if (input == null) {
        throw new FileNotFoundException("classpath:" + path);
      }
      return IOUtils.toString(input, "UTF-8");
    } finally {
      IOUtils.closeQuietly(input);
    }
  }

}
