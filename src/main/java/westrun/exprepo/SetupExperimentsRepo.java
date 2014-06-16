package westrun.exprepo;


import westrun.Sync;
import briefj.unix.RemoteUtils;



public class SetupExperimentsRepo
{
  public static void main(String [] args)
  {
    ExperimentsRepository repo = ExperimentsRepository.fromCommandLineArguments(args);
    createAllLocalDirs(repo);
    repo.configuration.toJSON(repo.resolveLocal(ExpRepoPath.MAIN_CONFIG_FILE));
    Sync.createExcludeList(repo);
    createRemoteExpRepoRoot(repo);
    Sync.sync(repo);
  }
  
  private static void createAllLocalDirs(ExperimentsRepository repo)
  {
    for (ExpRepoPath item : ExpRepoPath.values())
      if (item.isDirectory)
        repo.resolveLocal(item).mkdir();
  }

  private static void createRemoteExpRepoRoot(ExperimentsRepository repo)
  {
    System.out.println("Creating remote folder via ssh");
    try { RemoteUtils.remoteBash(repo.sshRemoteHost, "mkdir -p " + repo.remoteExpRepoRoot.getAbsolutePath()); }
    catch (Exception e)
    {
      System.err.println("Could not create " + repo.getSSHString());
      System.err.println("Check that you have password-less access to the server and " +
          " that the file does not exists already.");
      System.exit(1);
    }
    
    System.out.println("Created an experiments repository linked with " + repo.getSSHString());
  }
}
