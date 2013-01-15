package com.github.jknack.amd4j;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandNames = {"-a", "analyze" }, separators = "=")
public class AnalyzeCommand extends BaseCommand {

  @Parameter(description = "file names")
  private List<String> files = new ArrayList<String>();

  @Override
  public void execute() throws IOException {
    Amd4j amd4j = newAmd4j();
    if (!isEmpty(name)) {
      files.add(0, name);
    }
    for (String file : files) {
      Config config = new Config(file)
          .setBaseUrl(isEmpty(baseUrl) ? "/" : baseUrl);
      registerPaths(config);
      System.out.printf("analyzing %s...\n", file);
      long start = System.currentTimeMillis();
      Module module = amd4j.analyze(config);
      long end = System.currentTimeMillis();
      System.out.printf("%s\n", module.toStringTree().trim());
      System.out.printf("analysis of %s took %sms\n\n", module.uri, end - start);
    }
  }
}
