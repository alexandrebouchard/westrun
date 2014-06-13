package westrun.code;

import java.io.File;

import binc.Command;
import briefj.BriefFiles;
import briefj.BriefIO;

import com.google.gson.Gson;


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
  public static final String SPECIFICATION_FILE_NAME_CONVENTION = ".buildCommands.json";

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
  
  public static void main(String [] args)
  {
    build(BriefFiles.currentDirectory());
  }
}
