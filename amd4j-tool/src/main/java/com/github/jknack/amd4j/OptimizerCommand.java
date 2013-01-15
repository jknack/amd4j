package com.github.jknack.amd4j;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandNames = {"-o", "optimize" }, separators = "=")
public class OptimizerCommand extends BaseCommand {

  @Parameter(names = "-out", description = "output file")
  private File out;

  @Parameter(names = "-findNestedDependencies",
      description = "Finds require() dependencies inside a require() or define call. By default" +
          "this value is false, because those resources should be considered " +
          "dynamic/runtime calls.", arity = 1)
  private Boolean findNestedDependencies;

  @Parameter(names = "-inlineText",
      description = "Inlines the text for any text! dependencies, to avoid the separate " +
          "async XMLHttpRequest calls to load those dependencies.", arity = 1)
  private Boolean inlineText;

  @Parameter(names = "-useStrict", description = "Allow \"use strict\"; be included in the " +
      "JavaScript files. Default is: false", arity = 1)
  private Boolean useStrict;

  @Parameter(names = "-verbose", description="Level of verbosity")
  private boolean verbose = false;

  @Parameter(description = "build's profile")
  private List<String> buildFile = new ArrayList<String>();

  @Override
  public void execute() throws IOException {
    Config config = new Config();
    if (buildFile.size() == 1) {
      config = Config.parse(new File(buildFile.get(0)));
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
    notEmpty(config.getName(), "The following option is required: %s", "name");
    notNull(config.getOut(), "The following option is required: %s", "out");
    notNull(config.getBaseUrl(), "The following option is required: %s", "baseUrl");

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
}
