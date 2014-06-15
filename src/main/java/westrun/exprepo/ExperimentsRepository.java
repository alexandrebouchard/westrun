package westrun.exprepo;

import java.io.File;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;

import briefj.BriefFiles;
import briefj.BriefIO;
import briefj.BriefStrings;
import briefj.opt.Option;
import briefj.run.OptionsUtils;



public class ExperimentsRepository
{
  @Option(required = true, gloss = "SSH host for running remote experiments") 
  public String sshRemoteHost;
  
  @Option(gloss = "Remote directory where the experiments are ran. ")
  public String remoteDirectory = "";
  
  @Option(gloss = "Absolute path to a git code repository.")
  public String codeRepository = "";
  
  public static final String CONFIG_DIR = ".westrun";
  public static final String MAIN_CONFIG_FILE = "config.json";
  
  public File root()
  {
    return BriefFiles.findFileInParents(CONFIG_DIR).getParentFile();
  }
  
  public File configDir()
  {
    return new File(root(), CONFIG_DIR);
  }
  
  public static ExperimentsRepository fromWorkingDirectoryParents()
  {
    File configFile = new File(BriefFiles.findFileInParents(CONFIG_DIR), MAIN_CONFIG_FILE);
    return new Gson().fromJson(BriefIO.fileToString(configFile), ExperimentsRepository.class);
  }
  
  public static ExperimentsRepository fromCommandLineArguments(String [] args)
  {
    ExperimentsRepository result = new ExperimentsRepository();
    OptionsUtils.parseOptions(args, result);
    if (StringUtils.isEmpty(result.remoteDirectory))
      result.remoteDirectory = "~/" + BriefFiles.currentDirectory().getName();
    return result;
  }

  public void save()
  {
    File configDir = new File(CONFIG_DIR);
    configDir.mkdir();
    File destination = new File(configDir, MAIN_CONFIG_FILE);
    BriefIO.write(destination, BriefIO.createGson().toJson(this));
  }

  public String getSSHString()
  {
    return "" + sshRemoteHost + ":" + remoteDirectory;
  }
}
