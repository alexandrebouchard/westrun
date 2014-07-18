package westrun.exprepo;

import java.io.File;

import org.apache.commons.lang3.StringUtils;

import briefj.BriefFiles;
import briefj.run.OptionsUtils;
import briefj.unix.RemoteUtils;


/**
 * A repository of experiments. Contains the information read
 * from a config file. The main purpose of this class is to 
 * keep centralized info on the subdirectory structure used by
 * westrun.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public class ExperimentsRepository
{
  /*
   * e.g. name@host.com
   */
  public final String sshRemoteHost;
  
  /*
   * These are assumed to be absolute paths.
   */
  public final File localExpRepoRoot;
  public final File remoteExpRepoRoot;
  public final File localCodeRepoRoot;
  
  public final ExperimentsRepoConfig configuration;
  
  private ExperimentsRepository(ExperimentsRepoConfig config, File localExpRepoRoot)
  {
    if (StringUtils.isEmpty(config.remoteDirectory))
      throw new RuntimeException("Remote should not be empty in the JSON file.");
    this.localExpRepoRoot = localExpRepoRoot;
    this.sshRemoteHost = config.sshRemoteHost;
    this.remoteExpRepoRoot = new File(config.remoteDirectory);
    this.localCodeRepoRoot = StringUtils.isEmpty(config.codeRepository) ? null : new File(config.codeRepository);
    this.configuration = config;
  }

  public static ExperimentsRepository fromWorkingDirParents()
  {
    File mainConf = BriefFiles.findFileInParents(ExpRepoPath.CONFIG.getName());
    if (mainConf == null)
      throw new RuntimeException("Make sure you run the command in an experiment repo (or a descendent directory of)");
    File configFile = new File(mainConf, ExpRepoPath.MAIN_CONFIG_FILE.getName()); ///CONFIG_DIR), MAIN_CONFIG_FILE);
    ExperimentsRepoConfig config = ExperimentsRepoConfig.fromJSON(configFile);
    File localExpRepoRoot = configFile.getParentFile().getParentFile();
    return new ExperimentsRepository(config, localExpRepoRoot);
  }
  
  public static ExperimentsRepository fromCommandLineArguments(String [] args)
  {
    // gather info
    ExperimentsRepoConfig config = new ExperimentsRepoConfig();
    OptionsUtils.parseOptions(args, config);
    File localExpRepoRoot = BriefFiles.currentDirectory();
    
    // resolve remote home
    resolveRemoteHome(config, localExpRepoRoot);
    
    return new ExperimentsRepository(config, localExpRepoRoot);
  }
  
//  public static final String CONFIG_DIR = ".westrun";
//  public static final String MAIN_CONFIG_FILE = "config.json";
  
  public String getSSHString()
  {
    return "" + sshRemoteHost + ":" + remoteExpRepoRoot;
  }
  
  public boolean hasCodeRepository()
  {
    return localCodeRepoRoot != null;
  }
  
  public File resolveLocal(ExpRepoPath path)
  {
    return path.buildFile(localExpRepoRoot);
  }
  
  public File resolveRemote(ExpRepoPath path)
  {
    return path.buildFile(remoteExpRepoRoot);
  }
  
  private static void resolveRemoteHome(ExperimentsRepoConfig config, File localExpRepoRoot)
  {
    if (StringUtils.isEmpty(config.remoteDirectory))
    {
      // find remote home
      String home = RemoteUtils.remoteBash(config.sshRemoteHost, "echo ~").replace("\n", "");
      config.remoteDirectory = home + "/" + localExpRepoRoot.getName();
    }
  }
}
