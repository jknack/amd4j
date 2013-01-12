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

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class FileResourceLoader implements ResourceLoader {

  private File baseDir;

  public FileResourceLoader(final File baseDir) {
    this.baseDir = baseDir;
  }

  public FileResourceLoader() {
    this(new File(System.getProperty("user.dir")));
  }

  @Override
  public boolean exists(final ResourceURI uri) throws IOException {
    return toFile(uri).exists();
  }

  @Override
  public String load(final ResourceURI uri) throws IOException {
    return FileUtils.readFileToString(toFile(uri), "UTF-8");
  }

  private File toFile(final ResourceURI uri) {
    return new File(new File(baseDir, uri.baseUrl), uri.path);
  }

}
