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
import java.io.PrintWriter;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;

/**
 * Utility class for tracking module dependencies and building the output.
 *
 * @author edgar.espina
 * @since 0.1.0
 */
class DependencyContext {

  /**
   * Contains the already process dependencies.
   */
  private final Set<String> dependencies = new LinkedHashSet<String>();

  /**
   * The optimizer output.
   */
  private PrintWriter writer;

  /**
   * Creates a new {@link DependencyContext}.
   *
   * @param output The optimizer output.
   * @throws IOException If the output file cannot be created.
   */
  public DependencyContext(final File output) throws IOException {
    writer = new PrintWriter(output, "UTF-8");
  }

  /**
   * Returns true, if the file has been processed.
   *
   * @param uri The file uri.
   * @return True, if the file has been processed.
   */
  public boolean hasBeenProcessed(final ResourceURI uri) {
    return !dependencies.add(uri.getFullPath());
  }

  /**
   * Write the module to the final output.
   *
   * @param module The module to write.
   */
  public void write(final Module module) {
    if (module.content.length() > 0) {
      writer.append("\n").append(module.content);
    }
  }

  /**
   * Close the context.
   */
  public void close() {
    IOUtils.closeQuietly(writer);
  }
}
