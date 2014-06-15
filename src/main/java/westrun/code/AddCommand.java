package westrun.code;

import java.io.File;

import briefj.BriefFiles;
import briefj.run.OptionsUtils;



public class AddCommand
{
  public static void main(String [] args)
  {
    File currentDir = BriefFiles.currentDirectory();
    if (!new File(currentDir, ".git").isDirectory())
    {
      System.err.println("This command should be ran from the root of git repository.");
      System.exit(1);
    }
    BuildSpecification spec = SelfBuiltRepository.loadSpecification(currentDir);
    if (spec == null)
    {
      System.out.println("Creating empty build specification.");
      spec = new BuildSpecification();
    }
    CommandSpecification newCmd = new CommandSpecification();
    OptionsUtils.parseOptions(args, newCmd);
    spec.buildCommands.add(newCmd);
    File result = new File(currentDir, SelfBuiltRepository.SPECIFICATION_FILE_NAME_CONVENTION);
    spec.save(result);
    System.out.println("Added build command in " + result);
  }
}
