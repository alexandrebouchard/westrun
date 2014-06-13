package westrun;

import java.io.File;

import westrun.exprepo.ExperimentsRepository;

import binc.Command;

import static binc.Command.call;



public class Sync
{
  public static void main(String [] args)
  {
    sync();
  }
  
  public static void sync()
  {
    ExperimentsRepository repo = ExperimentsRepository.fromWorkingDirectoryParents();
    System.out.println("Starting sync. Note: if files were deleted, this may recreate them.");
    
    call(rsync
      .ranIn(repo.root())
      .withArgs("-u -r " + repo.root().getAbsolutePath() + " " +  repo.getSSHString())
      .saveOutputTo(new File(repo.configDir(), "synclog1")));
    
    call(rsync
        .ranIn(repo.root())
        .withArgs("-u -r " + repo.getSSHString() + " " +  repo.root().getAbsolutePath())
        .saveOutputTo(new File(repo.configDir(), "synclog2")));
    
    System.out.println("Sync complete. See .westrun/synclog{1,2} for details");
  }
  
  private static final Command rsync = Command.byName("rsync").throwOnNonZeroReturnCode();
}
