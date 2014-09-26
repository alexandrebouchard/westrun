package westrun;

import java.io.File;

import org.apache.commons.lang3.StringUtils;

import westrun.exprepo.ExpRepoPath;
import westrun.exprepo.ExperimentsRepository;
import westrun.exprepo.ExperimentsRepository.NotInExpRepoException;


import binc.Command;
import briefj.BriefIO;
import briefj.BriefStrings;
import briefj.opt.Option;
import briefj.run.OptionsUtils;
import briefj.run.OptionsUtils.InvalidOptionsException;



public class NewExperiment
{
  @Option(gloss = "The resource to use as the initial contents of the " +
  		"draft, relative to westrun.examples in the resources.")
  public String templateInitialContentsResource = "java";
  
  @Option(required = true, gloss = "Name for the new template.")
  public String name = "";
  
  public static void main(String [] args)
  {
    try
    {
      NewExperiment instance = new NewExperiment();
      OptionsUtils.parseOptions(args, instance);
      instance.create();
    }
    catch (InvalidOptionsException ioe)
    {
    }
    catch (NotInExpRepoException niere)
    {
      System.err.println(niere.getMessage());
    }
  }

  private void create()
  {
    ExperimentsRepository repo = ExperimentsRepository.fromWorkingDirParents();
    
    String template = null;
    String resource = "/westrun/examples/" + templateInitialContentsResource;
    try { template = BriefIO.resourceToString(resource); }
    catch (Exception e)
    {
      System.err.println("Initial contents resource not found: " + resource);
      System.exit(1);
    }
    File plansFolder = repo.resolveLocal(ExpRepoPath.PLANS); //  new File(repo.root(), DRAFTS_FOLDER_NAME);
    String finalName = (StringUtils.isEmpty(name) ? BriefStrings.generateUniqueId() : name);
    File draft = new File(plansFolder, finalName);
    BriefIO.write(draft, template);
    Command.call(openCommand.appendArgs(draft.getAbsolutePath()));
  }
  
  public static final Command openCommand = Command.byName("open").withArgs("-t");
  
}
