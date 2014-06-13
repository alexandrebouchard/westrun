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
    
    // if requested, 
    
    // create some folders
    new File(repo.root(), NewExperiment.DRAFTS_FOLDER_NAME).mkdir();
    
    // create remote exec dir
    RemoteUtils.remoteBash(repo.sshRemoteHost, "mkdir " + repo.remoteDirectory);
    
    System.out.println("Created an experiments repository linked with " + repo.getSSHString());
    
    // sync up
    Sync.sync();
  }
  

  

}
