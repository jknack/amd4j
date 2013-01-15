package com.github.jknack.amd4j;

import static org.apache.commons.lang3.Validate.notNull;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

public class Optimizer extends OncePerModuleVisitor<File> {

  private Config config;

  private List<Transformer> transformers = new ArrayList<Transformer>();

  private PrintWriter writer;

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
