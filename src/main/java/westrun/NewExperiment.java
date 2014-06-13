package westrun;

import java.io.File;

import westrun.exprepo.ExperimentsRepository;


import binc.Command;
import briefj.BriefIO;
import briefj.BriefStrings;
import briefj.opt.Option;
import briefj.run.OptionsUtils;



public class NewExperiment
{
  @Option
  public String templateInitialContentsResource = "/westrun/startScript.txt";
  
  public static void main(String [] args)
  {
    NewExperiment instance = new NewExperiment();
    OptionsUtils.parseOptions(args, instance);
    instance.create();
  }

  private void create()
  {
    ExperimentsRepository repo = ExperimentsRepository.fromWorkingDirectoryParents();
    String template = BriefIO.resourceToString(templateInitialContentsResource); 
    File draftFolder = new File(repo.root(), DRAFTS_FOLDER_NAME);
    File draft = new File(draftFolder, BriefStrings.generateUniqueId());
    BriefIO.write(draft, template);
    Command.call(openCommand.withArgs(draft.getAbsolutePath()));
  }
  
  public static final Command openCommand = Command.byName("open");
  
  public static final String DRAFTS_FOLDER_NAME = "run-template-drafts";
  
}
