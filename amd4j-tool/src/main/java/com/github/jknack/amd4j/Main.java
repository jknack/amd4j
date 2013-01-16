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

import static org.apache.commons.lang3.Validate.isTrue;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

/**
 * Command line tool for running amd4j commands.
 *
 * @author edgar.espina
 * @since 0.1.0
 */
public class Main {

  /**
   * The logging system.
   */
  private static final Logger logger = LoggerFactory.getLogger(Main.class);

  /**
   * The root jcommander.
   */
  private final JCommander commander;

  /**
   * The current parsed jcommander.
   */
  private JCommander current;

  /**
   * Turn on or off debug mode.
   */
  @Parameter(names = "-X", description = "turn on debug mode")
  private Boolean verbose;

  /**
   * Creates a new main tool.
   */
  public Main() {
    // jcommander init
    commander = new JCommander(this);
    commander.addCommand(new AnalyzeCommand());
    commander.addCommand(new OptimizerCommand());
  }

  /**
   * Parse and execute a command.
   *
   * @param args The command arguments.
   * @throws IOException If something goes wrong.
   */
  public void run(final String[] args) throws IOException {
    // parse arguments
    commander.parse(args);
    // get command
    Map<String, JCommander> commands = commander.getCommands();
    isTrue(commander.getParsedCommand() != null, "missing command, please use one of: %s",
        commands.keySet());
    JCommander parseCommand = commands.get(commander.getParsedCommand());
    isTrue(parseCommand != null, "unknown command: %s", commander.getParsedCommand());
    current = parseCommand;
    List<Object> objects = parseCommand.getObjects();
    // execute command
    Command command = (Command) objects.get(0);
    command.setVerbose(verbose == null ? Boolean.FALSE : verbose);
    command.execute();
  }

  /**
   * Parse and execute a command.
   *
   * @param args The command arguments.
   * @throws IOException If something goes wrong.
   */
  public static void main(final String[] args) throws IOException {
    Main executor = new Main();
    try {
      executor.run(convertArgs(args));
    } catch (IllegalArgumentException ex) {
      executor.usage(ex);
    } catch (ParameterException ex) {
      executor.usage(ex);
    }
  }

  /**
   * Print the usage report.
   *
   * @param ex The problem cause.
   */
  private void usage(final Exception ex) {
    StringBuilder usage = new StringBuilder();
    String header = "error: " + ex.getMessage();
    usage.append(header).append("\n\n");
    JCommander help = current == null ? commander : current;
    help.setProgramName("java -jar amd4j-tool.jar"
        + (help == commander ? "" : " "
            + commander.getParsedCommand()));
    help.usage(usage);
    System.err.println(cleanup(usage.toString()));
    if (verbose == Boolean.TRUE) {
      logger.error("stacktrace:", ex);
    }
    System.exit(1);
  }

  /**
   * Remove "-" prefix required by {@link JCommander}.
   *
   * @param usage The usage report.
   * @return A clean up usage report.
   */
  private String cleanup(final String usage) {
    Field[] fields = Config.class.getDeclaredFields();
    String result = usage;
    for (Field field : fields) {
      String flname = field.getName();
      final String replacement;
      if (flname.equals("paths")) {
        replacement = field.getName() + ".[path]=value";
      } else {
        replacement = field.getName() + "=value";
      }
      result = result.replace("-" + field.getName(), replacement);
    }
    result = result.replace("Syntax: paths.[path]=value.key=value", "Syntax: paths.[path]=value");
    return result;
  }

  /**
   * Prefix options with "-" required by {@link JCommander}.
   *
   * @param args The original arguments.
   * @return A jcommander arguments.
   */
  private static String[] convertArgs(final String[] args) {
    String[] jcommanderArgs = new String[args.length];
    for (int i = 0; i < jcommanderArgs.length; i++) {
      String arg = args[i];
      if (arg.indexOf("=") >= 0) {
        arg = "-" + arg;
      }
      jcommanderArgs[i] = arg;
    }
    return jcommanderArgs;
  }

}
