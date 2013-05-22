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

import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notNull;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

/**
 * Resolve resouces from file system.
 *
 * @author edgar.espina
 * @since 0.1.0
 */
public class FileResourceLoader implements ResourceLoader {

  /**
   * The base directory.
   */
  private File baseDir;

  /**
   * Creates a new {@link FileResourceLoader}.
   *
   * @param baseDir The base directory. Required.
   */
  public FileResourceLoader(final File baseDir) {
    notNull(baseDir, "The baseDir is required.");
    isTrue(baseDir.exists() && baseDir.isDirectory(), "Directory not found: %s", baseDir);

    this.baseDir = baseDir;
  }

  /**
   * Creates a new {@link FileResourceLoader} using the <code>user.dir</code> property.
   */
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

  /**
   * Resolve the uri to a file.
   *
   * @param uri The candidate uri.
   * @return A file that represent the uri.
   */
  private File toFile(final ResourceURI uri) {
    return new File(baseDir,  uri.getPath());
  }

}
