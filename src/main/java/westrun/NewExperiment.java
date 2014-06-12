package westrun;

import java.io.File;

import org.mvel2.templates.TemplateRuntime;

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
    OptionsUtils.parseOptions(instance, args);
    instance.create();
  }

  private void create()
  {
    String template = BriefIO.resourceToString(templateInitialContentsResource); 
    File draft = new File(DRAFTS_FOLDER_NAME, BriefStrings.generateUniqueId());
    BriefIO.write(draft, template);
    Command.call(openCommand.withArgs(draft.getAbsolutePath()));
  }
  
  public static final Command openCommand = Command.cmd("open");
  
  public static final String DRAFTS_FOLDER_NAME = "run-template-drafts";
  
}
