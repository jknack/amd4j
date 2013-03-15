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

import static org.apache.commons.io.FilenameUtils.getName;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.Validate.isTrue;

import java.io.File;
import java.io.IOException;

/**
 * Optimize an AMD script file.
 *
 * @goal optimize
 * @phase prepare-package
 * @since 0.1.0
 */
public class OptimizeMojo extends Amd4jMojo {

  /**
   * The output's file.
   *
   * @parameter
   * @required
   */
  private String out;

  /**
   * Inline text in the final output. Default: true.
   *
   * @parameter
   */
  private Boolean inlineText;

  /**
   * Remove "useStrict"; statement from output.
   *
   * @parameter
   */
  private Boolean useStrict;

  /**
   * An optional build profile.
   *
   * @parameter
   */
  private String buildFile;

  @Override
  public void doExecute(final Amd4j amd4j, final Config config) throws IOException {
    isTrue(config.getOut() != null, "The following option is required: %s", "out");
    isTrue(!isEmpty(config.getBaseUrl()), "The following option is required: %s", "baseUrl");
    printf("optimizing %s...", config.getName());
    long start = System.currentTimeMillis();
    Module module = amd4j.optimize(config);
    long end = System.currentTimeMillis();
    printf("result:\n%s", module.toStringTree().trim());
    printf("optimization of %s took %sms", config.getName(), end - start, out);
  }

  @Override
  protected Config newConfig() throws IOException {
    if (isEmpty(buildFile)) {
      return super.newConfig();
    } else {
      return Config.parse(new File(buildFile));
    }
  }

  @Override
  protected Config merge(final String name, final Config config) throws IOException {
    super.merge(name, config);
    if (!isEmpty(out)) {
      config.setOut(new File(out.replace("${script.name}", getName(name))));
    }
    if (inlineText != null) {
      config.setInlineText(inlineText.booleanValue());
    }
    if (useStrict != null) {
      config.setUseStrict(useStrict.booleanValue());
    }
    return config;
  }

  @Override
  protected String header(final String name) {
    return "optimization of " + name;
  }
}
