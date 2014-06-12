package westrun;

import java.io.File;
import java.util.List;

import com.google.gson.Gson;

import binc.Command;
import briefj.BriefIO;


/**
 * Self built repo: a repository that contains a list of commands
 * to be executed to build the repository.
 * 
 * 
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public class SelfBuiltRepository
{
  private static final String SPECIFICATION_FILE_NAME_CONVENTION = ".buildCommands";

  /**
   * Lookup the file .buildCommands and execute them in the repository.
   * 
   * @param repositoryRoot
   */
  public static void build(File repositoryRoot)
  {
    BuildSpecification specs = loadSpecification(repositoryRoot);
    if (specs == null)
      throw new RuntimeException("Could not locate a repository self build specification. " +
      		"This specification should be at " + specificationPath(repositoryRoot).getAbsolutePath());
    
    for (CommandSpecification spec : specs.buildCommands)
      Command.call(spec.getCommand(repositoryRoot).withStandardOutMirroring());
  }
  
  public static BuildSpecification loadSpecification(File repositoryRoot)
  {
    File specificationPath = specificationPath(repositoryRoot);
    if (!specificationPath.exists())
      return null;
    String specString = BriefIO.fileToString(specificationPath);
    return new Gson().fromJson(specString, BuildSpecification.class);
  }

  private static File specificationPath(File repositoryRoot)
  {
    return new File(repositoryRoot, SPECIFICATION_FILE_NAME_CONVENTION);
  }

  public static class BuildSpecification
  {
    public List<CommandSpecification> buildCommands;
  }
  
  public static class CommandSpecification
  {
    public final String commandName;
    
    public final String commandArguments;
    
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
}
