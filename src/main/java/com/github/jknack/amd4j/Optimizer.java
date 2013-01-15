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

import static org.apache.commons.lang3.Validate.notNull;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

/**
 * Merge a module and module's dependencie into a single file.
 *
 * @author edgar.espina
 * @since 0.1.0
 */
public class Optimizer extends OncePerModuleVisitor<File> {

  /**
   * The configuration options.
   */
  private Config config;

  /**
   * The transformer set.
   */
  private List<Transformer> transformers = new ArrayList<Transformer>();

  /**
   * The writer.
   */
  private PrintWriter writer;

  /**
   * Creates a new {@link Optimizer}.
   *
   * @param config The configuration options. Required.
   * @param transformers The list of additional transformer to use. Required.
   */
  public Optimizer(final Config config, final List<Transformer> transformers) {
    this.config = notNull(config, "The config is required.");
    notNull(transformers, "The transformers is required.");
    this.transformers.addAll(transformers);
    this.transformers.add(new SemicolonAppenderPlugin());
    this.transformers.add(new AmdTransformer());
  }

  @Override
  public File walk(final Module module) {
    boolean success = false;
    File out = config.getOut();
    try {
      writer = new PrintWriter(out);
      module.traverse(this);
      success = true;
      return config.getOut();
    } catch (IOException ex) {
      throw new IllegalStateException("Unable to write on " + out, ex);
    } finally {
      IOUtils.closeQuietly(writer);
      if (!success) {
        out.delete();
      }
    }
  }

  @Override
  public void endvisit(final Module module) {
    StringBuilder content = new StringBuilder(module.content);
    for (Transformer transformer : transformers) {
      if (transformer.apply(module.uri)) {
        content = transformer.transform(config, module.name, content);
      }
    }
    writer.append("\n").append(content);
  }
}
