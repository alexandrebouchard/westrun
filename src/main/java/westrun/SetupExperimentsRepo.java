package westrun;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.mvel2.templates.TemplateRuntime;

import briefj.BriefFiles;
import briefj.BriefIO;
import briefj.BriefStrings;
import briefj.opt.Option;
import briefj.run.OptionsUtils;
import briefj.unix.RemoteUtils;



public class SetupExperimentsRepo
{
  @Option(required = true, gloss = "SSH host for running remote experiments") 
  public String sshRemoteHost;
  
  @Option(gloss = "Remote directory where the experiments are ran. " +
  		"Remote creation taken care by this script. " +
  		"If left empty, a name will be generated for you.")
  public String remoteDirectory = "";
  
  public String getLocalDirectory()
  {
    return BriefFiles.currentDirectory().getAbsolutePath();
  }
  
  public String getRemoteDirectory()
  {
    if (StringUtils.isEmpty(remoteDirectory))
      remoteDirectory = "~/" + BriefFiles.currentDirectory().getName() + "-" + BriefStrings.generateUniqueId();
    return remoteDirectory;
  }
  
  
  public static void main(String [] args)
  {
    SetupExperimentsRepo instance = new SetupExperimentsRepo();
    OptionsUtils.parseOptions(instance, args);
    instance.setup();
  }

  
  private void setup()
  {
    // create the remote folder
    RemoteUtils.remoteBash(sshRemoteHost, "mkdir " + getRemoteDirectory());
    
    // setup a bookmarked rsync script
    String template = BriefIO.resourceToString("/westrun/syncScript.txt"); 
    String generatedScript = (String) TemplateRuntime.eval(template, this);
    File bookmarksFolder = new File(".", RunBookmarkedCommand.BOOKMARK_FOLDER_NAME);
    bookmarksFolder.mkdir();
    if (!bookmarksFolder.isDirectory())
      throw new RuntimeException("Could not create the bookmark folder.");
    BriefIO.write(new File(bookmarksFolder, Launch.SYNC_BOOKMARK_NAME), generatedScript);
    
    System.out.println("Created an experiments repository linked with " + sshRemoteHost + ":" + remoteDirectory);
  }
}
