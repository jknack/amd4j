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

import com.beust.jcommander.Parameters;

/**
 * Analyze an AMD script file.
 *
 * @author edgar.espina
 * @since 01.10
 */
@Parameters(commandNames = "-a", separators = "=")
public class AnalyzeCommand extends BaseCommand {

  @Override
  public void doExecute(final Amd4j amd4j, final Config config) throws IOException {
    System.out.printf("analyzing %s...\n", config.getName());
    long start = System.currentTimeMillis();
    Module module = amd4j.analyze(config);
    long end = System.currentTimeMillis();
    System.out.printf("%s\n", module.toStringTree().trim());
    System.out.printf("analysis of %s took %sms\n\n", module.uri, end - start);
  }
}
