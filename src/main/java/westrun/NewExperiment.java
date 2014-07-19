package westrun;

import java.io.File;

import org.apache.commons.lang3.StringUtils;

import westrun.exprepo.ExpRepoPath;
import westrun.exprepo.ExperimentsRepository;


import binc.Command;
import briefj.BriefIO;
import briefj.BriefStrings;
import briefj.opt.Option;
import briefj.run.OptionsUtils;



public class NewExperiment
{
  @Option(gloss = "The resource to use as the initial contents of the " +
  		"draft, relative to westrun.examples in the resources.")
  public String templateInitialContentsResource = "java";
  
  @Option(gloss = "Name for the new template. Generated if left empty.")
  public String name = "";
  
  public static void main(String [] args)
  {
    NewExperiment instance = new NewExperiment();
    OptionsUtils.parseOptions(args, instance);
    instance.create();
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
    File draftFolder = repo.resolveLocal(ExpRepoPath.TEMPLATE_DRAFTS); //  new File(repo.root(), DRAFTS_FOLDER_NAME);
    String finalName = (StringUtils.isEmpty(name) ? BriefStrings.generateUniqueId() : name);
    File draft = new File(draftFolder,finalName);
    BriefIO.write(draft, template);
    Command.call(openCommand.appendArgs(draft.getAbsolutePath()));
  }
  
  public static final Command openCommand = Command.byName("open").withArgs("-t");
  
  public static final String DRAFTS_FOLDER_NAME = "templates";
}
