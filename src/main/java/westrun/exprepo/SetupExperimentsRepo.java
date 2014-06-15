package westrun.exprepo;


import java.io.File;

import westrun.NewExperiment;
import westrun.Sync;
import briefj.unix.RemoteUtils;



public class SetupExperimentsRepo
{
  
  public static void main(String [] args)
  {
    // create config files
    ExperimentsRepository repo = ExperimentsRepository.fromCommandLineArguments(args);
    repo.save();
    
    // create ignore file for rsync
    Sync.createExcludeList();
    
    // create some folders
    new File(repo.root(), NewExperiment.DRAFTS_FOLDER_NAME).mkdir();
    
    // create remote exec dir
    try { RemoteUtils.remoteBash(repo.sshRemoteHost, "mkdir " + repo.remoteDirectory); }
    catch (Exception e)
    {
      System.err.println("Could not create " + repo.getSSHString());
      System.err.println("Check that you have password-less access to the server and " +
      		" that the file does not exists already.");
      System.exit(1);
    }
    
    System.out.println("Created an experiments repository linked with " + repo.getSSHString());
    
    // sync up
    Sync.sync();
  }
  

  

}
