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

public class Main {

  /**
   * The logging system.
   */
  private static final Logger logger = LoggerFactory.getLogger(Main.class);

  private final JCommander commander;

  private JCommander current;

  @Parameter(names = "-X", description = "turn on debug mode")
  private Boolean verbose;

  static {
    logger.trace("boot {}", new Amd4j());
  }

  public Main() {
    // jcommander init
    commander = new JCommander(this);
    commander.addCommand(new AnalyzeCommand());
    commander.addCommand(new OptimizerCommand());
  }

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

  private void usage(final Exception ex) {
    StringBuilder usage = new StringBuilder();
    String header = "error: " + ex.getMessage();
    usage.append(header).append("\n\n");
    JCommander help = current == null ? commander : current;
    help.setProgramName("java -jar amd4j-tool.jar" +
        (help == commander ? "" : " " + commander.getParsedCommand()));
    help.usage(usage);
    System.err.println(cleanup(usage.toString()));
    if (verbose == Boolean.TRUE) {
      logger.error("stacktrace:", ex);
    }
    System.exit(1);
  }

  private String cleanup(final String input) {
    Field[] fields = Config.class.getDeclaredFields();
    String result = input;
    for (Field field : fields) {
      result = result.replace("-" + field.getName(), field.getName());
    }
    return result;
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

}
