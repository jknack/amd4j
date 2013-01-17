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

import java.io.IOException;

/**
 * Analyze an AMD script file.
 *
 * @goal analyze
 * @phase compile
 * @since 0.1.0
 */
public class AnalizeMojo extends Amd4jMojo {

  @Override
  protected void doExecute(final Amd4j amd4j, final Config config) throws IOException {
    printf("analyzing %s...", config.getName());
    long start = System.currentTimeMillis();
    Module module = amd4j.analyze(config);
    long end = System.currentTimeMillis();
    printf("result:\n%s", module.toStringTree().trim());
    printf("analysis of %s took %sms", module.uri, end - start);
  }

  @Override
  protected String header(final String name) {
    return "analyzis of " + name;
  }
}