package com.github.jknack.amd4j;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.join;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.mozilla.javascript.tools.shell.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequireOptimizer {

  /**
   * The logging system.
   */
  private static final Logger logger = LoggerFactory.getLogger(RequireOptimizer.class);

  public static void optimize(final String... args) throws IOException {
    List<String> arguments = new ArrayList<String>();
    arguments.add("r.js");
    arguments.addAll(asList(args));
    arguments.add("optimize=none");
    long start = System.currentTimeMillis();
    if (!nodeIfPresent(arguments)) {
      logger.info("\nrhino {}", join(args, " "));
      Main.main(arguments.toArray(new String[arguments.size()]));
    }
    long end = System.currentTimeMillis();
    logger.info("r.js took {}ms\n", end - start);
  }

  private static boolean nodeIfPresent(final List<String> args) throws IOException {
    File node = findNodeJs();
    if (node == null) {
      return false;
    }
    logger.info("\nnode {}", join(args, " "));
    CommandLine command = new CommandLine(node);
    command.addArguments(args.toArray(new String[args.size()]));
    DefaultExecutor executor = new DefaultExecutor();
    LogOutputStream log = new LogOutputStream() {
      @Override
      protected void processLine(final String line, final int level) {
        logger.info(line);
      }
    };
    File worDir = new File(System.getProperty("user.dir"), "src/test/resources");
    executor.setWorkingDirectory(worDir);
    executor.getStreamHandler().setProcessInputStream(log);
    int status = executor.execute(command);
    return status == 0;
  }

  public static boolean isNodeJsPresent() {
    return findNodeJs() != null;
  }

  private static File findNodeJs() {
    String[] paths = StringUtils.split(System.getenv("PATH"), File.pathSeparator);
    for (String path : paths) {
      String[] names = {"node", "nodejs" };
      for (String name : names) {
        File candidate = new File(path, name);
        if (candidate.exists()) {
          return candidate;
        }
      }

    }
    return null;
  }
}
