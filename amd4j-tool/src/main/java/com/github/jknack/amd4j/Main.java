package com.github.jknack.amd4j;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;

public class Main {


  /**
   * The logging system.
   */
  private static final Logger logger = LoggerFactory.getLogger(Main.class);

  static {
    logger.trace("boot {}", new Amd4j());
  }

  public static void main(final String[] args) throws IOException, ClassNotFoundException {
    createCommand(convertArgs(args)).execute();
  }

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

  private static Command createCommand(final String[] args) {
    JCommander commander = new JCommander();
    commander.addCommand(new AnalyzeCommand());
    commander.addCommand(new OptimizerCommand());
    commander.parse(args);
    Map<String, JCommander> commands = commander.getCommands();
    JCommander parseCommand = commands.get(commander.getParsedCommand());
    return (Command) parseCommand.getObjects().get(0);
  }
}
