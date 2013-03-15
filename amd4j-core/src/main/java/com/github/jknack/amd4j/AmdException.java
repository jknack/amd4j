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

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * Amd exceptions.
 *
 * @author edgar.espina
 * @since 0.1.0
 */
@SuppressWarnings("serial")
public class AmdException extends RuntimeException {

  /**
   * The execution path.
   */
  private final LinkedList<String> path = new LinkedList<String>();

  /**
   * Creates a new {@link AmdException}.
   *
   * @param module The failing module.
   * @param cause The cause.
   */
  public AmdException(final String module, final Throwable cause) {
    super("", cause);
    path.add(module);
  }

  /**
   * Creates a new {@link AmdException}.
   *
   * @param path The failing module.
   * @param cause The cause.
   */
  public AmdException(final List<String> path, final Throwable cause) {
    super("", cause);
    this.path.addAll(path);
  }

  /**
   * The execution path.
   *
   * @return The execution path.
   */
  public LinkedList<String> getPath() {
    return path;
  }

  @Override
  public String getMessage() {
    StringBuilder message = new StringBuilder("Execution of: ").append(path.getFirst()).append(
        " resulted in exception:\n");
    int indent = 0;
    for (String segment : path) {
      message.append(StringUtils.leftPad(" ", indent)).append(segment).append("\n");
      indent += 2;
    }
    return message.toString();
  }

  @Override
  public String toString() {
    return getMessage() + getCause().toString();
  }
}
