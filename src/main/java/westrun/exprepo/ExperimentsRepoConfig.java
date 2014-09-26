package westrun.exprepo;

import java.io.File;



import briefj.BriefIO;
import briefj.opt.Option;
import briefj.run.ExecutionInfoFiles;


/**
 * All paths should be absolute.
 * 
 * Note: This is maintained separate from ExperimentsRepository since 
 * some piece of info can be inferred, such as the root of the local
 * experiments repository, a behavior moved to ExperimentsRepository.java.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public class ExperimentsRepoConfig
{
  @Option(required = true, gloss = "SSH host for running remote experiments") 
  public String sshRemoteHost;
  
  /**
   * Development note: can be left empty when called from the command line, but not in the
   * JSON file or programatically. 
   * 
   * Rationale: The code that create repo resolves the remote home and writes in the 
   * config file. We do this because we do not want to have to do this resolution
   * each time an ExperimentsRepository object is created
   */
  @Option(gloss = "Absolute path to remote directory where the experiments are ran. " +
  		"If empty, [remote home]/[local dir name].")
  public String remoteDirectory = "";
  
  @Option(gloss = "Absolute path to a git code repository.")
  public String codeRepository = "";
  
  @Option(gloss = "Path relative to each .exec where tab-separated options (one per line), are stored (used to index runs).")
  public String optionsLocation = "";
  
  public static ExperimentsRepoConfig fromJSON(File configFile)
  {
    return BriefIO.createGson().fromJson(BriefIO.fileToString(configFile), ExperimentsRepoConfig.class);
  }
  
  public void toJSON(File configFile)
  {
    BriefIO.write(configFile, BriefIO.createGson().toJson(this));
  }
  
  public String getOptionsLocation()
  {
    if (optionsLocation == null || optionsLocation.isEmpty())
      return ExecutionInfoFiles.infoFileDirectoryName + "/" + ExecutionInfoFiles.OPTIONS_MAP;
    else
      return optionsLocation;
  }
}
