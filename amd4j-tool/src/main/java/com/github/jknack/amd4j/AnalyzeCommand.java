package com.github.jknack.amd4j;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.Validate.isTrue;

import java.io.IOException;

import com.beust.jcommander.Parameters;

@Parameters(commandNames = "-a", separators = "=")
public class AnalyzeCommand extends BaseCommand {

  @Override
  public void execute() throws IOException {
    Amd4j amd4j = newAmd4j();
    isTrue(!isEmpty(name), "no input file to process");
    Config config = new Config(name)
        .setBaseUrl(isEmpty(baseUrl) ? "." : baseUrl);
    if (findNestedDependencies != null) {
      config.setFindNestedDependencies(findNestedDependencies);
    }
    registerPaths(config);
    System.out.printf("analyzing %s...\n", name);
    if (verbose) {
      System.out.printf("options:\n%s\n", config);
    }
    long start = System.currentTimeMillis();
    Module module = amd4j.analyze(config);
    long end = System.currentTimeMillis();
    System.out.printf("%s\n", module.toStringTree().trim());
    System.out.printf("analysis of %s took %sms\n\n", module.uri, end - start);
  }
}
