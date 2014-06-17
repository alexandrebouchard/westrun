package westrun.code;

import java.io.File;
import java.util.ArrayList;

import com.beust.jcommander.internal.Lists;

import binc.Command;
import briefj.opt.Option;



public class CommandSpecification
{
  @Option
  public String commandName = "gradle";
  
  @Option
  public ArrayList<String> commandArguments = (ArrayList<String>) Lists.newArrayList("--quiet", "installApp", "-x", "test");
  
  public CommandSpecification() {}

  public Command getCommand(File runLocation)
  {
    return Command.cmd(commandName).withSegmentedArguments(commandArguments).ranIn(runLocation).throwOnNonZeroReturnCode();
  }
}