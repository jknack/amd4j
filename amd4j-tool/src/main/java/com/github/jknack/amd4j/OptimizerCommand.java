package com.github.jknack.amd4j;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.Validate.isTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandNames = "-o", separators = "=")
public class OptimizerCommand extends BaseCommand {

  @Parameter(names = "-out", description = "Output file")
  private File out;

  @Parameter(names = "-inlineText",
      description = "Inlines the text for any text! dependencies, to avoid the separate " +
          "async XMLHttpRequest calls to load those dependencies. Default: true", arity = 1)
  private Boolean inlineText;

  @Parameter(names = "-useStrict", description = "Allow \"use strict\"; be included in the " +
      "JavaScript files. Default: false", arity = 1)
  private Boolean useStrict;

  @Parameter(description = "[build.js]")
  private List<String> buildFile = new ArrayList<String>();

  @Override
  public void execute() throws IOException {
    Config config = newConfig();
    isTrue(!isEmpty(config.getName()), "The following option is required: %s", "name");
    isTrue(config.getOut() != null, "The following option is required: %s", "out");
    isTrue(!isEmpty(config.getBaseUrl()), "The following option is required: %s", "baseUrl");

    System.out.printf("optimizing %s...\n", name);
    if (verbose) {
      System.out.printf("options:\n%s\n", config);
    }
    Amd4j amd4j = newAmd4j(config.getBaseUrl());
    config.setBaseUrl("/");
    long start = System.currentTimeMillis();
    Module module = amd4j.optimize(config);
    long end = System.currentTimeMillis();
    System.out.printf("%s\n", module.toStringTree().trim());
    System.out.printf("optimization of %s took %sms\n\n", out.getPath(), end - start,
        out.getAbsolutePath());
  }

  private Config newConfig() throws IOException {
    final Config config;
    if (buildFile.size() == 1) {
      config = Config.parse(new File(buildFile.get(0)));
    } else {
      config = new Config();
    }
    if (!isEmpty(name)) {
      config.setName(name);
    }
    if (!isEmpty(baseUrl)) {
      config.setBaseUrl(baseUrl);
    }
    if (out != null) {
      config.setOut(out);
    }
    if (findNestedDependencies != null) {
      config.setFindNestedDependencies(findNestedDependencies.booleanValue());
    }
    if (inlineText != null) {
      config.setInlineText(inlineText.booleanValue());
    }
    if (useStrict != null) {
      config.setUseStrict(useStrict.booleanValue());
    }
    // add paths
    registerPaths(config);
    return config;
  }
}
