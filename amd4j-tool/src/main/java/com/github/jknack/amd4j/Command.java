package com.github.jknack.amd4j;

import java.io.IOException;

public interface Command {
  void execute() throws IOException;
}
