package westrun.code;

import java.io.File;

import binc.Command;
import briefj.opt.Option;



public class CommandSpecification
{
  @Option
  public String commandName = "gradle";
  
  @Option
  public String commandArguments = "--quiet installApp";
  
  public CommandSpecification() {}
  
  public CommandSpecification(String commandName,
      String commandArguments)
  {
    this.commandName = commandName;
    this.commandArguments = commandArguments;
  }

  public Command getCommand(File runLocation)
  {
    return Command.cmd(commandName).withArgs(commandArguments).ranIn(runLocation).throwOnNonZeroReturnCode();
  }
}